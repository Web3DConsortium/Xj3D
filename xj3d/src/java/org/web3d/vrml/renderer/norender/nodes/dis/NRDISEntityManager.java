/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.dis;

// External imports
import edu.nps.moves.dis7.pdus.EntityID;
import edu.nps.moves.dis7.pdus.EntityStatePdu;
import java.util.ArrayList;
import java.util.List;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLDISNodeType;
import org.web3d.vrml.renderer.common.nodes.dis.BaseDISEntityManager;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.DefaultNodeFactory;

/**
 * NR renderer implementation of a DISEntityManager node.
 * <p>
 *
 * This node is purely informational within the scene graph. It does not have
 * a renderable representation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class NRDISEntityManager extends BaseDISEntityManager
    implements NRVRMLNode {

    /** New entities, will become addedEntities at end of frame */
    private List<VRMLNodeType> addedEntities;

    /** Removed entities, will become removedEntities at end of frame */
    private List<VRMLNodeType> removedEntities;

    /** Are there new added entities */
    private boolean newAddedEntities;

    /** Are there new removed entities */
    private boolean newRemovedEntities;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public NRDISEntityManager() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRDISEntityManager(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods from NRVRMLNode class.
    //----------------------------------------------------------

    //----------------------------------------------------------
    // Methods overriding BaseGroup class.
    //----------------------------------------------------------

    @Override
    public void setupFinished() {
        if(!inSetup) {
            return;
        }

        super.setupFinished();
    }

    @Override
    public void allEventsComplete() {
        if (newAddedEntities) {
            vfAddedEntities.clear();

            synchronized(addedEntities) {
                vfAddedEntities.addAll(addedEntities);
                addedEntities.clear();
                newAddedEntities = false;
            }

            hasChanged[FIELD_ADDED_ENTITIES] = true;
            fireFieldChanged(FIELD_ADDED_ENTITIES);
        }

        if (newRemovedEntities) {
            vfRemovedEntities.clear();

            synchronized(removedEntities) {
                vfRemovedEntities.addAll(removedEntities);
                removedEntities.clear();
                newRemovedEntities = false;
            }

            hasChanged[FIELD_REMOVED_ENTITIES] = true;
            fireFieldChanged(FIELD_REMOVED_ENTITIES);
        }
    }

    @Override
    public void entityRemoved(VRMLDISNodeType node) {
        synchronized(removedEntities) {
            removedEntities.add(node);
            newRemovedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }

    @Override
    public void entityArrived(EntityStatePdu espdu) {
        if (nodeFactory == null) {
            nodeFactory = DefaultNodeFactory.createFactory(
                DefaultNodeFactory.OPENGL_RENDERER );
        }

        try {
            // Is this available from AbstractNode?
            nodeFactory.setSpecVersion(vrmlMajorVersion, vrmlMinorVersion);
            nodeFactory.setProfile("Interchange");
            nodeFactory.addComponent("DIS",1);
        } catch(UnsupportedProfileException upe) {
            upe.printStackTrace(System.err);
        }

        NREspduTransform node =
            (NREspduTransform)nodeFactory.createVRMLNode("EspduTransform",
                                                         false);

        int idx = node.getFieldIndex("entityID");
        EntityID entityID = espdu.getEntityID();
        node.setValue(idx, entityID.getEntityID());
        idx = node.getFieldIndex("applicationID");
        node.setValue(idx, entityID.getApplicationID());
        idx = node.getFieldIndex("siteID");
        node.setValue(idx, entityID.getSiteID());
        idx = node.getFieldIndex("networkMode");
        node.setValue(idx, "networkReader");
        idx = node.getFieldIndex("address");
        node.setValue(idx, vfAddress);
        idx = node.getFieldIndex("port");
        node.setValue(idx, vfPort);
        node.setFrameStateManager(stateManager);
        node.setupFinished();

        synchronized(addedEntities) {
            addedEntities.add(node);
            newAddedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to initialise the OpenGL data structures.
     */
    private void init() {
        addedEntities = new ArrayList<>();
        removedEntities = new ArrayList<>();
        newAddedEntities = false;
        newRemovedEntities = false;
    }
}
