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
package org.web3d.vrml.renderer.ogl.nodes.dis;

// External imports
import edu.nps.moves.dis7.enumerations.Country;
import edu.nps.moves.dis7.enumerations.EntityKind;
import edu.nps.moves.dis7.pdus.Domain;
import edu.nps.moves.dis7.pdus.EntityID;
import edu.nps.moves.dis7.pdus.EntityStatePdu;

import java.util.ArrayList;
import java.util.List;

import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.dis.BaseDISEntityManager;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.DefaultNodeFactory;;

/**
 * OGL renderer implementation of a DISEntityManager node.
 * <p>
 *
 * This node is purely informational within the scene graph. It does not have
 * a renderable representation.
 *
 * @author Alan Hudson, Vivian Gottesman
 * @version $Revision: 1.3 $
 */
public class OGLDISEntityManager extends BaseDISEntityManager
    implements OGLVRMLNode, VRMLSingleExternalNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SingleExternalNodeType };

    /** New entities, will become addedEntities at end of frame */
    private List<VRMLNodeType> addedEntities;

    /** Removed entities, will become removedEntities at end of frame */
    private List<VRMLNodeType> removedEntities;

    /** Are there new added entities */
    private boolean newAddedEntities;

    /** Are there new removed entities */
    private boolean newRemovedEntities;

    /** The world URL */
    private String worldURL;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public OGLDISEntityManager() {
        super();
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
    public OGLDISEntityManager(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    @Override
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    @Override
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

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
            nodeFactory.setProfile("Immersive");
            nodeFactory.addComponent("DIS", 1);
        } catch(UnsupportedProfileException upe) {
            upe.printStackTrace(System.err);
        }

        OGLEspduTransform node =
            (OGLEspduTransform)nodeFactory.createVRMLNode("EspduTransform",
                                                          false);
        int idx, category, extra, specific;
        Country country = espdu.getEntityType().getCountry();
        Domain domain = espdu.getEntityType().getDomain();
        category = espdu.getEntityType().getCategory();
        byte subcategory = espdu.getEntityType().getSubCategory();
        EntityKind kind = espdu.getEntityType().getEntityKind();
        specific = espdu.getEntityType().getSpecific();
        extra = espdu.getEntityType().getExtra();

        int len = vfMapping.size();
        String[] urlString = null;
        int currentMatched, previousMatched;
        previousMatched = -1;
        for (VRMLNodeType mappingNode : vfMapping) {
            currentMatched = 0;
            int[] compare = new int [7];

            idx = mappingNode.getFieldIndex("kind");
            compare[0] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("domain");
            compare[1] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("country");
            compare[2] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("category");
            compare[3] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("subcategory");
            compare[4] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("specific");
            compare[5] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("extra");
            compare[6] = (mappingNode.getFieldValue(idx)).intValue;

            if (kind.getValue() == compare[0]) {
                currentMatched++;
                if (domain.getValue() == compare[1]) {
                    currentMatched++;
                    if (country.getValue() == compare[2]) {
                        currentMatched++;
                        if (category == compare[3]) {
                            currentMatched++;
                            if (subcategory == compare[4]) {
                                currentMatched++;
                                if (specific == compare[5]) {
                                    currentMatched++;
                                    if (extra == compare[6]) {
                                        currentMatched++;
                                    } else if (compare[6] != 0) {
                                        currentMatched = 0;
                                    }
                                } else if (compare[5] != 0) {
                                    currentMatched = 0;
                                }
                            } else if (compare[4] != 0) {
                                currentMatched = 0;
                            }
                        } else if (compare[3] != 0) {
                            currentMatched = 0;
                        }
                    } else if (compare[2] != 0) {
                        currentMatched = 0;
                    }
                } else if (compare[1] != 0) {
                    currentMatched = 0;
                } else if (compare[0] != 0) {
                    currentMatched = 0;
                }
            }

            if (currentMatched > previousMatched) {
                idx = mappingNode.getFieldIndex("url");
                urlString = (mappingNode.getFieldValue(idx)).stringArrayValues;
                previousMatched = currentMatched;
            }
            if (currentMatched == 7) {
                break;
            }
        }

        idx = node.getFieldIndex("entityCountry");
        node.setValue(idx, country.getValue());
        idx = node.getFieldIndex("entityDomain");
        node.setValue(idx, domain.getValue());
        idx = node.getFieldIndex("entityCategory");
        node.setValue(idx, category);
        idx = node.getFieldIndex("entitySubcategory");
        node.setValue(idx, subcategory);
        idx = node.getFieldIndex("entityKind");
        node.setValue(idx, kind.getValue());
        idx = node.getFieldIndex("entitySpecific");
        node.setValue(idx, specific);
        idx = node.getFieldIndex("entityExtra");
        node.setValue(idx, extra);
//        idx = node.getFieldIndex("marking");
//        node.setValue(idx, urlString);

        idx = node.getFieldIndex("entityID");
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

        if (urlString != null) {
            VRMLNodeType inline =
                (VRMLNodeType)nodeFactory.createVRMLNode("Inline",false);

            idx = inline.getFieldIndex("url");

            inline.setValue(idx, urlString, urlString.length);
            inline.setFrameStateManager(stateManager);
            ((VRMLExternalNodeType)inline).setWorldUrl(worldURL);
            inline.setupFinished();

            idx = node.getFieldIndex("children");

            node.setValue(idx, inline);
        }

        synchronized(addedEntities) {
            addedEntities.add(node);
            newAddedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Methods defined  by VRMLExternalNodeType
    //----------------------------------------------------------

    @Override
    public int getLoadState() {
        return VRMLSingleExternalNodeType.LOAD_COMPLETE;
    }

    @Override
    public void setLoadState(int state) {
    }

    @Override
    public void setWorldUrl(String url) {
        if((url == null) || (url.length() == 0)) {
            return;
        }

        // check for a trailing slash. If it doesn't have one, append it.
        if(url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        worldURL = url;
    }

    @Override
    public String getWorldUrl() {
        return worldURL;
    }

    @Override
    public void setUrl(String[] newURL, int numValid) {
        // Ignored here
    }

    @Override
    public String[] getUrl() {
        return new String[0];
    }

    @Override
    public boolean checkValidContentType(String mimetype) {
        return true;
    }

    @Override
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {
    }

    @Override
    public void setLoadedURI(String uri) {
    }

    @Override
    public void addUrlListener(VRMLUrlListener ul) {
    }

    @Override
    public void removeUrlListener(VRMLUrlListener ul) {
    }

    @Override
    public void addContentStateListener(VRMLContentStateListener l) {
    }

    @Override
    public void removeContentStateListener(VRMLContentStateListener l) {
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

