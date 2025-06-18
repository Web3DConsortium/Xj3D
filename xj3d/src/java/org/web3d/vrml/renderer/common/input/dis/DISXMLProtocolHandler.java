/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// External imports
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.*;

// Local imports
import edu.nps.moves.dis7.pdus.DeadReckoningParameters;
import edu.nps.moves.dis7.pdus.EntityStatePdu;
import edu.nps.moves.dis7.pdus.EulerAngles;
import edu.nps.moves.dis7.pdus.Pdu;
import edu.nps.moves.dis7.pdus.Vector3Double;
import edu.nps.moves.dis7.pdus.Vector3Float;

import edu.nps.moves.dis7.utilities.DisTime;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import org.web3d.util.PropertyTools;

import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.NetworkProtocolHandler;

/**
 * TODO: Consolidate DISProtocolHandler and this PH
 *
 * The handler for DISXML protocol network traffic.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public class DISXMLProtocolHandler implements NetworkProtocolHandler, NetworkRoleListener {

    private static final String PROTOCOL = "DIS";

    /** The amount of time between inactive checks */
    private static final int INACTIVE_CHECK_TIME = 1_000;

    /** The amount of time before we mark a ESPDU as inactive */
    private static final int INACTIVE_TIME = 5_000;

    /** The amount of time between heartbeats.   */
    private static final int HEARTBEAT_CHECK_TIME = 4_500;

    /**
     * The default order.
     */
    private static final int DEFAULT_ORDER = 2;

    /**
     * The default convergence interval.
     */
    private static final int DEFAULT_CONVERGENCE_INTERVAL = 200;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** A map of open connections.  Only open one per address/port */
    private final Map<DISConnectionId, DISXMLConnectionHandler> connections;

    /** A map of DIS nodes wrappers and their unique ID's  */
    private final Map<DISId, NodeMapEntry> nodeMap;

    /** A map of DIS nodes wrappers and their unique ID's  */
    private final Map<DISId, WriterMapEntryDX> writerMap;

    /** Live list variables */
    private final LinkedList liveList;

    /** Nodes which want to write to the network */
    private final LinkedList writerList;

    /** The last time we checked for inactive pdus */
    private long lastCheck;

    /** The list of managers */
    private final List<VRMLDISNodeType> managerList;

    /** The Entities we've placed on the addedEntities */
    private final Set<DISId> notifiedSet;

    // Scratch vars to avoid gc.  Do not store DISId as a Map id, clone it
    private final DISId disId;

    float[] tempPositionArray;

    private final float[] rotation;

    /** Whether we should smooth the DIS traffic */
//    private final boolean smooth = true;

    /** The default dead reckon position value */
    protected static final boolean DEFAULT_DEADRECKON_POSITION = true;

    /** Property describing the rescaling method to use */
    protected static final String DEADRECKON_POSITION_PROP =
            "org.web3d.vrml.renderer.common.dis.input.deadreckonPosition";

    /** The value read from the system property for DEADRECKON_POSITION */
    protected static final boolean DEAD_RECKON_POSITION;

    /** The default dead reckon position value */
    protected static final boolean DEFAULT_DEADRECKON_ROTATION = true;

    /** Property describing the dead reckon */
    protected static final String DEADRECKON_ROTATION_PROP =
            "org.web3d.vrml.renderer.common.dis.input.deadreckonRotation";

    /** The value read from the system property for DEADRECKON_ROTATION_PROP */
    protected static final boolean DEAD_RECKON_ROTATION;

    // Scratch matrixes for smoothing
    Matrix3d rotationMatrix;

    Matrix3d psiMat;

    Matrix3d thetaMat;

    Matrix3d phiMat;

    Quat4d rotationQuat;

    Vector3d translationVec;

    Vector3d[] translationDerivatives;

    Vector3d[] rotationDerivatives;

    RungeKuttaSolver solver;

    AxisAngle4d axisTemp;

    static {
        DEAD_RECKON_POSITION = PropertyTools.fetchSystemProperty(DEADRECKON_POSITION_PROP,
                DEFAULT_DEADRECKON_POSITION);

        DEAD_RECKON_ROTATION = PropertyTools.fetchSystemProperty(DEADRECKON_ROTATION_PROP,
                DEFAULT_DEADRECKON_ROTATION);
    }

    /**
     * Create a new instance of the execution space manager to run all the
     * routing.
     */
    public DISXMLProtocolHandler() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        connections = new HashMap<>();
        nodeMap = Collections.synchronizedMap(new HashMap<>());
        writerMap = Collections.synchronizedMap(new HashMap<>());

        liveList = new LinkedList();
        writerList = new LinkedList();
        managerList = Collections.synchronizedList(new ArrayList<>());
        notifiedSet = Collections.synchronizedSet(new java.util.HashSet<>());
        disId = new DISId(0, 0, 0);
        tempPositionArray = new float[3];

        rotation = new float[4];

        rotationMatrix = new Matrix3d();
        psiMat = new Matrix3d();
        thetaMat = new Matrix3d();
        phiMat = new Matrix3d();
        rotationQuat = new Quat4d();
        translationVec = new Vector3d();
        translationDerivatives = new Vector3d[]{
                    new Vector3d(),
                    new Vector3d()
                };

        rotationDerivatives = new Vector3d[]{
                    new Vector3d()
                };

        axisTemp = new AxisAngle4d();
    }

    //----------------------------------------------------------
    // Methods required by NetworkProtocolHandler
    //----------------------------------------------------------
    /** @return the protocol this handler supports */
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;
    }

    static int numProcessed = 0;

    /**
     * Process network traffic now.
     */
    @Override
    public void processNetworkTraffic() {
        long currTime = System.currentTimeMillis();
        boolean checkInactive = (currTime - lastCheck >= INACTIVE_CHECK_TIME);

        LiveListEntry node = (LiveListEntry) liveList.head;
        LiveListEntry last = (LiveListEntry) liveList.head;
        VRMLDISNodeType di;
        float dt;
        EntityStatePdu espdu;
        EulerAngles orient;
        Vector3Double location;
        Vector3d translation;
        DeadReckoningParameters drp;
        Vector3Float linearVelocity, angularVelocity, linearAcceleration;
        int siteID , appID , entityID, len;
        NodeMapEntry nmEntry;
        VRMLDISManagerNodeType manager;

        while (node != null) {
            di = node.node;

            if (node.newPackets) {
                if (node.currFire != null) {
                    di.packetArrived(node.currFire);
                    node.currFire = null;
                } else if (node.currDetonate != null) {
                    di.packetArrived(node.currDetonate);

                    // Stop dead reckon on detonate. If still alive it will send more updates.
                    node.closeEnough = true;
                    node.currDetonate = null;
                } else if (node.currEspdu != null) {
                    di.packetArrived(node.currEspdu);
//                    System.out.println("ESPDU: " + numProcessed + " processed");
                    espdu = node.currEspdu;

//System.out.println("***Real Pos: " + espdu.getEntityLocationX() + " " + espdu.getEntityLocationY() + " " + espdu.getEntityLocationZ());

                    rotationMatrix.setIdentity();

                    orient = espdu.getEntityOrientation();

                    eulersToMatrix(orient.getPhi(),
                            orient.getTheta(),
                            orient.getPsi());

                    rotationQuat.set(rotationMatrix);

                    // Convert to normal coordinates
                    location = espdu.getEntityLocation();

                    translation = new Vector3d(location.getX(),
                            -location.getZ(),
                            location.getY());

                    linearVelocity = espdu.getEntityLinearVelocity();

                    translationDerivatives[0].set(linearVelocity.getX(),
                            -linearVelocity.getZ(),
                            linearVelocity.getY());

                    drp = espdu.getDeadReckoningParameters();
                    angularVelocity = drp.getEntityAngularVelocity();

                    rotationDerivatives[0].set(angularVelocity.getX(),
                            -angularVelocity.getZ(),
                            angularVelocity.getY());

                    linearAcceleration = drp.getEntityLinearAcceleration();

                    translationDerivatives[1].set(linearAcceleration.getX(),
                            -linearAcceleration.getZ(),
                            linearAcceleration.getY());

                    node.translationConverger.convergeTo(
                            translation,
                            translationDerivatives,
                            currTime,
                            currTime);

                    node.rotationConverger.convergeTo(
                            rotationQuat,
                            rotationDerivatives,
                            currTime,
                            currTime);
                }

                node.newPackets = false;
                numProcessed++;
            // TODO:  Handle notification of other packet types
            }

            if (checkInactive && (currTime - node.lastTime >= INACTIVE_TIME)) {
                node.node.setIsActive(false);

                liveList.remove(node, last);

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();

                disId.setValue(siteID, appID, entityID);

                nmEntry = nodeMap.get(disId);

                if (nmEntry == null) {
                    System.out.println("DIS Entry null on timeout");
                } else {
                    nmEntry.listEntry = null;
                    // TODO: I'm not sure we want to remove entries from the nodeMap
                    // Removing from the map means restored entities with this ID do not update
//                    nodeMap.remove(disId);

                    len = managerList.size();

                    for (int i = 0; i < len; i++) {
                        manager = (VRMLDISManagerNodeType) managerList.get(i);

                        manager.entityRemoved(di);
                        notifiedSet.remove(disId);
                    }
                }
            }

            if ((DEAD_RECKON_POSITION || DEAD_RECKON_ROTATION) && !node.closeEnough) {
                // Handle dead reckoning
                dt = (currTime - node.lastTime) * 0.001f;
//                dt = (currTime - node.lastTime) * 0.01f;

                drp = node.currEspdu.getDeadReckoningParameters();
                linearVelocity = node.currEspdu.getEntityLinearVelocity();
                linearAcceleration = drp.getEntityLinearAcceleration();

                // TODO: What about a rotating only entity?

                if (Math.abs(linearVelocity.getX()) <= 0.0001 &&
                        Math.abs(linearVelocity.getY()) <= 0.0001 &&
                        Math.abs(linearVelocity.getZ()) <= 0.0001 &&
                        Math.abs(linearAcceleration.getX()) <= 0.0001 &&
                        Math.abs(linearAcceleration.getY()) <= 0.0001 &&
                        Math.abs(linearAcceleration.getZ()) <= 0.0001) {

                    node.closeEnough = true;
                }

                int idx;

                if (DEAD_RECKON_POSITION) {
                    node.translationConverger.getValue(currTime, translationVec);

                    tempPositionArray[0] = (float) translationVec.x;
                    tempPositionArray[1] = (float) translationVec.y;
                    tempPositionArray[2] = (float) translationVec.z;

                    idx = di.getFieldIndex("translation");
                    di.setValue(idx, tempPositionArray, 3);
                }

                if (DEAD_RECKON_ROTATION) {
                    node.rotationConverger.getValue(currTime, rotationQuat);
                    rotationQuat.normalize();

                    axisTemp.set(rotationQuat);
                    rotation[0] = (float) axisTemp.x;
                    rotation[1] = (float) axisTemp.y;
                    rotation[2] = (float) axisTemp.z;
                    rotation[3] = (float) axisTemp.angle;

                    idx = di.getFieldIndex("rotation");
                    di.setValue(idx, rotation, 4);
                }

                /*
                DRPosition(node.currEspdu, dt, tempPositionArray);

                idx = di.getFieldIndex("translation");
                di.setValue(idx, tempPositionArray, 3);


                DROrientation(node.currEspdu, dt, dRorientation);
                axisTemp.set(dRorientation);
                rotation[0] = (float) axisTemp.x;
                rotation[1] = (float) axisTemp.y;
                rotation[2] = (float) axisTemp.z;
                rotation[3] = (float) axisTemp.angle;

                idx = di.getFieldIndex("rotation");
                di.setValue(idx, rotation, 4);
                 */
                node.prevDt = dt;
            }

            last = node;
            node = (LiveListEntry) node.next;
        }

        if (checkInactive) {
            lastCheck = currTime;
        }

        WriterListEntry writer = (WriterListEntry) writerList.head;
        Pdu pdu;
        WriterMapEntryDX entry;

        while (writer != null) {
            di = writer.node;

            if (di.valuesToWrite()) {
                writer.lastTime = currTime;
                pdu = di.getState();

                disId.setValue(di.getSiteID(), di.getAppID(), di.getEntityID());
                entry = writerMap.get(disId);

                pdu.setTimestamp(DisTime.getCurrentDisTimestamp());
                try {
                    entry.writer.write(pdu.marshal());
                } catch (Exception ex) {
                    Logger.getLogger(DISXMLProtocolHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                if (currTime - writer.lastTime >= HEARTBEAT_CHECK_TIME) {
                    // Definately write a value
                    writer.lastTime = currTime;
                    pdu = di.getState();

                    disId.setValue(di.getSiteID(), di.getAppID(), di.getEntityID());
                    entry = writerMap.get(disId);

                    pdu.setTimestamp(DisTime.getCurrentDisTimestamp());
                    try {
                        entry.writer.write(pdu.marshal());
                    } catch (Exception ex) {
                        Logger.getLogger(DISXMLProtocolHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // TODO: Check Dead Reckon error
                }
            }
            writer = (WriterListEntry) writer.next;
        }
    }

    @Override
    public void addNode(VRMLNetworkInterfaceNodeType node) {
        int port;
        String address;
        String xmppUsername;
        String xmppPassword;
        String[] xmppAuthServer;
        String xmppMucServer;
        String xmppMucRoom;

        VRMLDISNodeType di = (VRMLDISNodeType) node;
        DISConnectionId id;
        DISXMLConnectionHandler conn;
        int siteID;
        int appID;
        int entityID;
        DISId did;
        WriterListEntry newwle;
        WriterMapEntryDX entry;

        di.addNetworkRoleListener(this);

        address = di.getAddress();
        port = di.getPort();
        xmppUsername = di.getUsername();
        xmppPassword = di.getPassword();
        xmppAuthServer = di.getAuthServer();
        xmppMucServer = di.getMucServer();
        xmppMucRoom = di.getMucRoom();

        switch (di.getRole()) {
            case VRMLNetworkInterfaceNodeType.ROLE_MANAGER:
                id = new DISConnectionId(address, port);
                conn = connections.get(id);

                if (conn == null) {
                    conn = new DISXMLConnectionHandler(nodeMap, liveList,
                            managerList, notifiedSet, address, port,
                            xmppUsername, xmppPassword, xmppAuthServer,
                            xmppMucServer, xmppMucRoom);

                    connections.put(id, conn);
                }

                managerList.add(di);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_READER:
                id = new DISConnectionId(address, port);
                conn = connections.get(id);

                if (conn == null) {
                    // TODO: When do we get rid of these?
                    conn = new DISXMLConnectionHandler(nodeMap, liveList, managerList, notifiedSet,
                            address, port,
                            xmppUsername, xmppPassword, xmppAuthServer, xmppMucServer, xmppMucRoom);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();
                int idx = node.getFieldIndex("marking");
                VRMLFieldData field = node.getFieldValue(idx);
                System.out.println("New DIS node: siteID: " + siteID + " appID: " + appID + " " + " entityID: " + entityID + " marking: " + field.stringValue);

                did = new DISId(siteID, appID, entityID);

                nodeMap.put(did, new NodeMapEntry((VRMLDISNodeType) node, null));

                // Add all nodes to writer map in case they change status
                newwle = new WriterListEntry(di);

                entry = new WriterMapEntryDX((VRMLDISNodeType) node, newwle, conn);
                writerMap.put(did, entry);

                break;
            case VRMLNetworkInterfaceNodeType.ROLE_WRITER:
                id = new DISConnectionId(address, port);
                conn = connections.get(id);

                if (conn == null) {
                    // TODO: When do we get rid of these?
                    conn = new DISXMLConnectionHandler(nodeMap, liveList, managerList, notifiedSet,
                            address, port,
                            xmppUsername, xmppPassword, xmppAuthServer, xmppMucServer, xmppMucRoom);

                    connections.put(id, conn);
                }

                siteID = di.getSiteID();
                appID = di.getAppID();
                entityID = di.getEntityID();
                //System.out.println("New DIS node: " + siteID + " " + appID + " " + entityID);
                did = new DISId(siteID, appID, entityID);

                // Add all nodes to writer map in case they change status
                newwle = new WriterListEntry(di);

                entry = new WriterMapEntryDX((VRMLDISNodeType) node, newwle, conn);
                writerMap.put(did, entry);

                writerList.add(newwle);
                break;
            case VRMLNetworkInterfaceNodeType.ROLE_INACTIVE:
                System.out.println("Logic to change Inactive to Writer not implemented");
                break;
        }
    }

    @Override
    public void removeNode(VRMLNetworkInterfaceNodeType node) {
        System.out.println("DISProtocolHandler: removeNode not implemented");
    }

    @Override
    public void clear() {
        if (!connections.isEmpty()) {
            System.out.println("DISProtocolHandler: clear not implemented");
        }
    }

    @Override
    public void shutdown() {
        if (!connections.isEmpty()) {
            System.out.println("DISProtocolHandler: shutdown not implemented");
        }
    }

    //----------------------------------------------------------
    // Methods required for NetworkRoleListener
    //----------------------------------------------------------

    @Override
    public void roleChanged(int newRole, Object node) {
        VRMLDISNodeType dis_node = (VRMLDISNodeType) node;

        long timestamp = System.currentTimeMillis();

        switch (newRole) {
            case VRMLNetworkInterfaceNodeType.ROLE_INACTIVE:
                break;

            case VRMLNetworkInterfaceNodeType.ROLE_READER:
                System.out.println("*** Now READER: " + this);
                // remove from writer list
                WriterListEntry wlist_node = (WriterListEntry) writerList.head;
                WriterListEntry wlast = (WriterListEntry) writerList.head;

                while (wlist_node != null) {
                    if (wlist_node.node == dis_node) {
                        writerList.remove(wlist_node, wlast);
                        break;
                    }

                    wlast = wlist_node;
                    wlist_node = (WriterListEntry) wlist_node.next;
                }

                // add to livelist
                NodeMapEntry entry = new NodeMapEntry(dis_node, null);

                try {
                    nodeMap.put((DISId) disId.clone(), entry);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(DISXMLProtocolHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                LiveListEntry newlle = new LiveListEntry(dis_node, timestamp);
                entry.listEntry = newlle;

                EntityStatePdu espdu = (EntityStatePdu) dis_node.getState();

                newlle.lastEspdu = espdu;
                newlle.currEspdu = espdu;
                newlle.rotationConverger = new OrderNQuat4dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                newlle.translationConverger = new OrderNVector3dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                newlle.espduTimestamp = espdu.getTimestamp();
                newlle.closeEnough = false;
                newlle.avgTime = 0.01f;
                newlle.newPackets = true;

                liveList.add(newlle);
                break;

            case VRMLNetworkInterfaceNodeType.ROLE_WRITER:
                System.out.println("*** Now WRITER: " + this);
                // remove from liveList

                LiveListEntry list_node = (LiveListEntry) liveList.head;
                LiveListEntry last = (LiveListEntry) liveList.head;

                while (list_node != null) {
                    if (list_node.node == dis_node) {
                        liveList.remove(list_node, last);
                        break;
                    }

                    last = list_node;
                    list_node = (LiveListEntry) list_node.next;
                }

                // add to writer list
                WriterListEntry newwle = new WriterListEntry(dis_node);
                writerList.add(newwle);
                break;

            default:
                break;
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    // See ProtocolHandlerUtils

    /**
     * Converts a set of Euler angles (phi, theta, psi) to a rotation matrix.
     *
     * @param x phi
     * @param y theta
     * @param z psi
     */
    private void eulersToMatrix(double x, double y, double z) {
        psiMat.setIdentity();
        psiMat.rotY(-z);

        thetaMat.rotZ(y);

        phiMat.rotX(x);

        rotationMatrix.mul(phiMat, thetaMat);
        rotationMatrix.mul(psiMat);
    }
}