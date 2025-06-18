/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.eventmodel;

// External imports
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.odejava.Body;
import org.odejava.PlaceableGeom;
import org.odejava.Odejava;
import org.odejava.ode.Ode;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.HashSet;
import org.j3d.util.IntHashMap;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.util.NodeArray;

import org.xj3d.core.eventmodel.NodeManager;

/**
 * Manager for the rigid body physics model nodes.
 * <p>
 *
 * Keeps track of both the collections of bodies and the individual joints.
 * The collection nodes are evaluated at the end of the frame so as to modify
 * the final object locations for this frame. The joints are managed so that
 * those that need to produce output will be evaluated at the start of the
 * next frame. This requires the manager to register as both pre and post
 * event model node manager, as it manages both sets of nodes, though
 * independently.
 * <p>
 *
 * The physics model is run at a somewhat fixed frame rate. Physics models
 * don't like to have variable frame rate as input, so we smooth these out
 * over a fixed number of frames. Every set of frame resets the calculation
 * interval based on recent history.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class DefaultRigidBodyPhysicsManager implements NodeManager, Runnable {

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.RigidBodyCollectionNodeType,
        TypeConstants.RigidJointNodeType,
        TypeConstants.RigidBodyNodeType,
        TypeConstants.nBodyCollidableNodeType,
        TypeConstants.nBodyCollisionCollectionNodeType,
        TypeConstants.nBodyCollisionSensorNodeType
    };

    /** Average out the timesteps every so often */
    private static final int RECALC_INTERVAL = 10;

    /** If no ODE, don't do anything */
    private static boolean odeInitialized;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Manager for all the RigidBodyCollection nodes here */
    private NodeArray collections;

    /** The updated set of collections */
    private HashSet<VRMLNodeType> collectionSet;

    /** Manager for all the joint nodes here */
    private NodeArray joints;

    /** The updated set of joints */
    private HashSet<VRMLNodeType> jointSet;

    /** Manager for all the body nodes here */
    private NodeArray bodies;

    /** The updated set of collections */
    private HashSet<VRMLNodeType> bodySet;

    /** Manager for the nbody collidable nodes */
    private NodeArray collidables;

    /** The updated set of collidables */
    private HashSet<VRMLNodeType> collidableSet;

    /** Manager for the nbody collision space nodes */
    private NodeArray collisionSpaces;

    /** The updated set of collision spaces */
    private HashSet<VRMLNodeType> collisionCollectionSet;

    /** Manager for the nbody sensor nodes */
    private NodeArray sensors;

    /** The updated set of sensors */
    private HashSet<VRMLNodeType> sensorSet;

    /** Time in seconds this was last called. The dT passed to ODE. */
    private long lastTime;

    /** Elapsed time since the last recalc interval */
    private long elapsedTime;

    /** The current counter in the recalc time */
    private int countTick;

    /** The current deltaT calculated to feed to the physics model */
    private float deltaT;

    /** The clock used to reinitialise time with */
    private VRMLClock clock;

    /** Map of the native address of a body to the VRML node */
    private IntHashMap<Body> bodyIdMap;

    /** Map of the native address of a geometry to the VRML node */
    private IntHashMap<VRMLNBodyCollidableNodeType> geomIdMap;
    
    static {
        try {
            Boolean val = AccessController.doPrivileged(
                    (PrivilegedExceptionAction<Boolean>) () -> {
                        
                Boolean ret_val;
                
                try {
                    ret_val = Odejava.init();
                } catch (NoClassDefFoundError ncdfe) {
                    System.err.println("Unable to initialise ODE due "
                            + "to missing class definitions: " + ncdfe);
                    ret_val = Boolean.FALSE;
                } catch (java.lang.UnsatisfiedLinkError ule) {
                    System.err.println("Unable to initialise ODE due "
                            + "to UnsatisfiedLinkError: " + ule);
                    ret_val = Boolean.FALSE;
                }
                
                return ret_val;
            });

            odeInitialized = val;
        } catch (PrivilegedActionException pae) {
            System.err.println("Failed to partake priviledged action "
                    + "to load odejava libs: " + pae);
            odeInitialized = false;
        }
    }

    /**
     * Create a new, empty instance of the physics manager.
     */
    public DefaultRigidBodyPhysicsManager() {
        collections = new NodeArray();
        joints = new NodeArray();
        bodies = new NodeArray();
        collidables = new NodeArray();
        collisionSpaces = new NodeArray();
        sensors = new NodeArray();

        collectionSet = new HashSet<>();
        jointSet = new HashSet<>();
        bodySet = new HashSet<>();
        collidableSet = new HashSet<>();
        collisionCollectionSet = new HashSet<>();
        sensorSet = new HashSet<>();

        bodyIdMap = new IntHashMap<>();
        geomIdMap = new IntHashMap<>();

        countTick = 0;
        elapsedTime = 0;
        deltaT = 0.02f;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        
        Thread t = new Thread("ODE shutdown hook");
        t.setDaemon(true);
        Runtime r = Runtime.getRuntime();
        r.addShutdownHook(t);
    }
    
    @Override
    public void run() {
        Ode.dCloseODE();
        odeInitialized = false;
    }

    //-------------------------------------------------------------
    // Methods defined by NodeManager
    //-------------------------------------------------------------

    @Override
    public boolean initialize() {
        return odeInitialized;
    }

    @Override
    public void shutdown() {

        // So it's see better in the output
        System.err.println(Ode.class.getName() + " will remain open until JVM shutdown");
    }

    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    @Override
    public ComponentInfo[] getSupportedComponents() {
        return new ComponentInfo[] {
            new ComponentInfo("xj3d_RigidBodyPhysics", 1)
        };
    }

    @Override
    public void setVRMLClock(VRMLClock clk) {
        clock = clk;
        lastTime = clk.getWallTime();
    }

    @Override
    public void resetTimeZero() {
        lastTime = clock.getWallTime();
        countTick = 0;
        elapsedTime = 0;
        deltaT = 0.02f;
    }

    @Override
    public int[] getManagedNodeTypes() {
        return MANAGED_NODE_TYPES;
    }

    @Override
    public boolean evaluatePreEventModel() {
        return true;
    }

    @Override
    public boolean evaluatePostEventModel() {
        return true;
    }

    @Override
    public void addManagedNode(VRMLNodeType node) {
        int type = node.getPrimaryType();

        switch(type) {
            case TypeConstants.RigidBodyCollectionNodeType:
                if(!collectionSet.contains(node)) {
                    collections.add(node);
                    collectionSet.add(node);
                    ((VRMLRigidBodyGroupNodeType)node).setTimestep(deltaT);
                }
                break;

            case TypeConstants.RigidJointNodeType:
                if(!jointSet.contains(node)) {
                    joints.add(node);
                    jointSet.add(node);
                }
                break;

            case TypeConstants.RigidBodyNodeType:
                if(!bodySet.contains(node)) {
                    bodies.add(node);
                    bodySet.add(node);

                    // Fetch the ID of the node and register it in the map.
                    VRMLRigidBodyNodeType r_body =
                        (VRMLRigidBodyNodeType)node;
                    Body body = r_body.getODEBody();
//                    int addr = body.getNativeAddr();

                    // Assuming the last geom added to this body
                    int addr = (int) body.getGeom().getNativeAddr();

                    bodyIdMap.put(addr, body);
                }
                break;

            case TypeConstants.nBodyCollidableNodeType:
                if(!collidableSet.contains(node)) {
                    collidables.add(node);
                    collidableSet.add(node);

                    // Fetch the ID of the node and register it in the map.
                    VRMLNBodyCollidableNodeType coll =
                        (VRMLNBodyCollidableNodeType)node;
                    PlaceableGeom geom = coll.getODEGeometry();
                    int addr = (int) geom.getNativeAddr();
                    geomIdMap.put(addr, coll);
                }
                break;

            case TypeConstants.nBodyCollisionSensorNodeType:
                if(!sensorSet.contains(node)) {
                    sensors.add(node);
                    sensorSet.add(node);
                }
                break;

            case TypeConstants.nBodyCollisionCollectionNodeType:
                if(!collisionCollectionSet.contains(node)) {
                    collisionSpaces.add(node);
                    collisionCollectionSet.add(node);
                }
                break;

            default:
                errorReporter.warningReport("Non-Physics node added to the manager",
                                            null);
        }
    }

    @Override
    public void removeManagedNode(VRMLNodeType node) {
        int type = node.getPrimaryType();

        switch(type) {
            case TypeConstants.RigidBodyCollectionNodeType:
                collections.remove(node);
                collectionSet.remove(node);
                break;

            case TypeConstants.RigidBodyNodeType:
                bodies.remove(node);
                bodySet.remove(node);

                // Fetch the ID of the node and register it in the map.
                VRMLRigidBodyNodeType r_body =
                    (VRMLRigidBodyNodeType)node;
                Body body = r_body.getODEBody();
//                int addr = body.getNativeAddr();

                // Assuming the last geom added to this body
                int addr = (int) body.getGeom().getNativeAddr();

                bodyIdMap.remove(addr);
                break;

            case TypeConstants.RigidJointNodeType:
                joints.remove(node);
                jointSet.remove(node);

                VRMLNBodyCollidableNodeType coll =
                    (VRMLNBodyCollidableNodeType)node;
                PlaceableGeom geom = coll.getODEGeometry();
                addr = (int) geom.getNativeAddr();
                geomIdMap.remove(addr);
                break;

            case TypeConstants.nBodyCollidableNodeType:
                collidables.remove(node);
                collidableSet.remove(node);
                break;

            case TypeConstants.nBodyCollisionSensorNodeType:
                sensors.remove(node);
                sensorSet.remove(node);
                break;

            case TypeConstants.nBodyCollisionCollectionNodeType:
                collisionSpaces.remove(node);
                collisionCollectionSet.remove(node);
                break;

            default:
                errorReporter.warningReport("Non-physics node removed from the manager",
                                            null);
        }
    }

    @Override
    public void executePreEventModel(long time) {

        double dt = (time - lastTime);

        // Avoid div-zero issues in the physics model.
        if(dt == 0)
            return;

        // First evaluate collision detection. Use the values from here to
        // feed values into the physics model.
        int size = collisionSpaces.size();

        for(int i = 0; i < size; i++) {
            VRMLNBodyGroupNodeType group =
                (VRMLNBodyGroupNodeType)collisionSpaces.get(i);

            if(group.isEnabled())
                group.evaluateCollisions();
        }


        // Update the joint outputs
        size = joints.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidJointNodeType joint =
                (VRMLRigidJointNodeType)joints.get(i);

            if(joint.numOutputs() != 0)
                joint.updateRequestedOutputs();
        }

        // Finally have the sensors dump their stuff.
        size = sensors.size();
        for(int i = 0; i < size; i++) {
            VRMLNBodySensorNodeType sensor =
                (VRMLNBodySensorNodeType)sensors.get(i);

            sensor.updateContacts(bodyIdMap, geomIdMap);
        }

        size = collections.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidBodyGroupNodeType group =
                (VRMLRigidBodyGroupNodeType)collections.get(i);

            if(group.isEnabled())
                group.updatePostSimulation();
        }
    }

    @Override
    public void executePostEventModel(long time) {

        int size = collections.size();

        if(++countTick == RECALC_INTERVAL) {
            deltaT = (elapsedTime / (float)countTick) * 0.001f;
            countTick = 0;
            elapsedTime = 0;

            for(int i = 0; i < size; i++) {
                VRMLRigidBodyGroupNodeType group =
                    (VRMLRigidBodyGroupNodeType)collections.get(i);

                group.setTimestep(deltaT);
            }
        } else {
            elapsedTime += time - lastTime;
        }

        lastTime = time;

        // Do stuff here to push the collision stuff over to the physics
        // model.

        for(int i = 0; i < size; i++) {
            VRMLRigidBodyGroupNodeType group =
                (VRMLRigidBodyGroupNodeType)collections.get(i);

            if(group.isEnabled()) {
                group.processInputContacts();
                group.evaluateModel();
            }
        }

        size = collidables.size();

        for(int i = 0; i < size; i++) {
            VRMLNBodyCollidableNodeType geom =
                (VRMLNBodyCollidableNodeType)collidables.get(i);

            geom.updateFromODE();
        }
    }

    @Override
    public void clear() {
        int size = joints.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidJointNodeType joint =
                (VRMLRigidJointNodeType)joints.get(i);

            joint.delete();
        }

        size = collisionSpaces.size();

        for(int i = 0; i < size; i++) {
            VRMLNBodyGroupNodeType group =
                (VRMLNBodyGroupNodeType)collisionSpaces.get(i);

            group.delete();
        }

        size = collections.size();

        for(int i = 0; i < size; i++) {
            VRMLRigidBodyGroupNodeType group =
                (VRMLRigidBodyGroupNodeType)collections.get(i);

            group.delete();
        }

        collections.clear();
        joints.clear();
        bodies.clear();
        collidables.clear();
        collisionSpaces.clear();
        sensors.clear();

        collectionSet.clear();
        jointSet.clear();
        bodySet.clear();
        collidableSet.clear();
        collisionCollectionSet.clear();
        sensorSet.clear();

        bodyIdMap.clear();
        geomIdMap.clear();
    }
}
