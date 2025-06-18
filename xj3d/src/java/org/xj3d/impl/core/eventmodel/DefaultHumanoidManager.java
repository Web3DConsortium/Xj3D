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

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLHAnimHumanoidNodeType;
import org.web3d.vrml.util.NodeArray;

import org.xj3d.core.eventmodel.NodeManager;

/**
 * Manager for HAnimHumanoid nodes and their contained sets of children.
 * <p>
 *
 * Keeps track of all humanoids and makes sure that they update every frame.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DefaultHumanoidManager implements NodeManager {

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.HumanoidNodeType
    };

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Manager for all the humanoids here */
    private NodeArray humanoids;

    /**
     * Create a new, empty instance of the humanoid manager.
     */
    public DefaultHumanoidManager() {
        humanoids = new NodeArray();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-------------------------------------------------------------
    // Methods defined by NodeManager
    //-------------------------------------------------------------

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public void shutdown() {
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
            new ComponentInfo("H-Anim", 1), // TODO HAnim in X3Dv4
            new ComponentInfo("HAnim", 1)   // HAnim in X3Dv4, backwards compatible
        };
    }

    @Override
    public void setVRMLClock(VRMLClock clk) {
    }

    @Override
    public void resetTimeZero() {
    }

    @Override
    public int[] getManagedNodeTypes() {
        return MANAGED_NODE_TYPES;
    }

    @Override
    public boolean evaluatePreEventModel() {
        return false;
    }

    @Override
    public boolean evaluatePostEventModel() {
        return true;
    }

    @Override
    public void addManagedNode(VRMLNodeType node) {
        if(!(node instanceof VRMLHAnimHumanoidNodeType)) {
            errorReporter.warningReport("Non-HAnim node added to the manager",
                                        null);
            return;
        }

        humanoids.add(node);
    }

    @Override
    public void removeManagedNode(VRMLNodeType node) {
        if(!(node instanceof VRMLHAnimHumanoidNodeType)) {
            errorReporter.warningReport("Non-HAnim node removed from the manager",
                                        null);
            return;
        }

        humanoids.remove(node);
    }

    @Override
    public void executePreEventModel(long time) {
        // do nothing
    }

    @Override
    public void executePostEventModel(long time) {
        int size = humanoids.size();

        for(int i = 0; i < size; i++) {
            VRMLHAnimHumanoidNodeType human =
                (VRMLHAnimHumanoidNodeType)humanoids.get(i);
            human.updateMesh();
        }
    }

    @Override
    public void clear() {
        humanoids.clear();
    }
}
