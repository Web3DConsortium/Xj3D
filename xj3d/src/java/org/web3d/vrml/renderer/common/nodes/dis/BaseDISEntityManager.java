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

package org.web3d.vrml.renderer.common.nodes.dis;

// External imports
import edu.nps.moves.dis7.pdus.Pdu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a EntityManager node functionality.
 * <p>
 * Notifies content when an entity arrives or leaves.  Nodes which are
 * locally controlled by the simulation are ignored(ie any node which
 * matches the entityID,siteID,appID and has a networkWriter mode).
 *
 * AddedEntities events are issued when an EntityStatePdu is first detected.
 * Entities arrivals will only be notified once a simulation, unless
 * a removedEntity event is issued.
 *
 * RemovedEntities events issued when an EntityStatePdu has not arrived
 * within the DIS allowed heartbeat period.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public abstract class BaseDISEntityManager extends AbstractNode
        implements VRMLNetworkInterfaceNodeType, VRMLDISManagerNodeType {

    /** Protocol implemented */
    protected static final String PROTOCOL = "DIS";

    /** Field Index */
    protected static final int FIELD_SITE_ID = LAST_NODE_INDEX + 1;

    protected static final int FIELD_APPLICATION_ID = LAST_NODE_INDEX + 2;

    protected static final int FIELD_ADDRESS = LAST_NODE_INDEX + 3;

    protected static final int FIELD_PORT = LAST_NODE_INDEX + 4;

    protected static final int FIELD_ADDED_ENTITIES = LAST_NODE_INDEX + 5;

    protected static final int FIELD_REMOVED_ENTITIES = LAST_NODE_INDEX + 6;

    protected static final int FIELD_MAPPING = LAST_NODE_INDEX + 7;

    /** The last field index used by this class */
    protected static final int LAST_ENTITY_MANAGER_INDEX = FIELD_MAPPING;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_ENTITY_MANAGER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> FIELD_MAP;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** siteID SFInt32 0 */
    protected int vfSiteID;

    /** applicationID   SFInt32 0 */
    protected int vfApplicationID;

    /** address   SFString "localhost" */
    protected String vfAddress;

    /** port   SFInt32 0 */
    protected int vfPort;

    /** mapping MFNode [] */
    protected List<VRMLNodeType> vfMapping;

    /** addedEntities MFNode [] */
    protected List<VRMLNodeType> vfAddedEntities;

    /** removedEntities MFNode [] */
    protected List<VRMLNodeType> vfRemovedEntities;

    /** Factory for creating EspduTransform nodes */
    protected VRMLNodeFactory nodeFactory;

    /** Internal scratch var for dealing with added/removed children */
    private VRMLNodeType[] nodeTmp;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[]{FIELD_METADATA, FIELD_MAPPING};

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];

        FIELD_MAP = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFNode",
                "metadata");
        fieldDecl[FIELD_SITE_ID] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFInt32",
                "siteID");

        fieldDecl[FIELD_ADDRESS] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFString",
                "address");

        fieldDecl[FIELD_PORT] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFInt32",
                "port");

        fieldDecl[FIELD_APPLICATION_ID] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFInt32",
                "applicationID");

        fieldDecl[FIELD_MAPPING] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "MFNode",
                "mapping");

        fieldDecl[FIELD_ADDED_ENTITIES] =
                new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                "MFNode",
                "addedEntities");

        fieldDecl[FIELD_REMOVED_ENTITIES] =
                new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                "MFNode",
                "removedEntities");

        Integer idx = FIELD_METADATA;
        FIELD_MAP.put("metadata", idx);
        FIELD_MAP.put("set_metadata", idx);
        FIELD_MAP.put("metadata_changed", idx);

        idx = FIELD_SITE_ID;
        FIELD_MAP.put("siteID", idx);
        FIELD_MAP.put("set_siteID", idx);
        FIELD_MAP.put("siteID_changed", idx);

        idx = FIELD_APPLICATION_ID;
        FIELD_MAP.put("applicationID", idx);
        FIELD_MAP.put("set_applicationID", idx);
        FIELD_MAP.put("applicationID_changed", idx);

        idx = FIELD_ADDRESS;
        FIELD_MAP.put("address", idx);
        FIELD_MAP.put("set_address", idx);
        FIELD_MAP.put("address_changed", idx);

        idx = FIELD_PORT;
        FIELD_MAP.put("port", idx);
        FIELD_MAP.put("set_port", idx);
        FIELD_MAP.put("port_changed", idx);

        idx = FIELD_MAPPING;
        FIELD_MAP.put("mapping", idx);
        FIELD_MAP.put("set_mapping", idx);
        FIELD_MAP.put("mapping_changed", idx);

        idx = FIELD_ADDED_ENTITIES;
        FIELD_MAP.put("addedEntities", idx);
        FIELD_MAP.put("addedEntities_changed", idx);

        idx = FIELD_REMOVED_ENTITIES;
        FIELD_MAP.put("removedEntities", idx);
        FIELD_MAP.put("removedEntities_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseDISEntityManager() {
        super("DISEntityManager");

        hasChanged = new boolean[LAST_ENTITY_MANAGER_INDEX + 1];

        vfSiteID = 0;
        vfApplicationID = 0;
        vfPort = 0;
        vfAddress = "";
        vfAddedEntities = new ArrayList<>();
        vfRemovedEntities = new ArrayList<>();
        vfMapping = new ArrayList<>();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseDISEntityManager(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("siteID");
            VRMLFieldData field = node.getFieldValue(index);
            vfSiteID = field.intValue;

            index = node.getFieldIndex("applicationID");
            field = node.getFieldValue(index);
            vfApplicationID = field.intValue;

            index = node.getFieldIndex("address");
            field = node.getFieldValue(index);
            vfAddress = field.stringValue;

            index = node.getFieldIndex("port");
            field = node.getFieldValue(index);
            vfPort = field.intValue;

        } catch (VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    @Override
    public void allEventsComplete() {
    }

    @Override
    public int getPrimaryType() {
        return TypeConstants.NetworkInterfaceNodeType;
    }

    @Override
    public void setupFinished() {
        if (!inSetup) {
            return;
        }

        super.setupFinished();

        int len = vfMapping.size();
        VRMLNodeType node;
        for (int i = 0; i < len; i++) {
            node = vfMapping.get(i);
            node.setupFinished();
        }

        inSetup = false;
    }

    //----------------------------------------------------------------
    // Methods defined by VRMLNetworkInterfaceNodeType
    //----------------------------------------------------------------
    
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public int getRole() {
        return VRMLNetworkInterfaceNodeType.ROLE_MANAGER;
    }

    @Override
    public void addNetworkRoleListener(NetworkRoleListener l) {
        // Ingore as we never change roles
    }

    @Override
    public void removeNetworkRoleListener(NetworkRoleListener l) {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType interface.
    //----------------------------------------------------------
    
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = FIELD_MAP.get(fieldName);

        return (index == null) ? -1 : index;
    }

    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0 || index > LAST_ENTITY_MANAGER_INDEX) {
            return null;
        }

        return fieldDecl[index];
    }
    
    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

   @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();
        fieldData.numElements = 1;
        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;

        int num_kids;

        switch (index) {
            case FIELD_SITE_ID:
                fieldData.intValue = vfSiteID;
                break;

            case FIELD_APPLICATION_ID:
                fieldData.intValue = vfApplicationID;
                break;

            case FIELD_PORT:
                fieldData.intValue = vfPort;
                break;

            case FIELD_ADDRESS:
                fieldData.stringValue = vfAddress;
                break;

            case FIELD_MAPPING:
                VRMLNodeType kids[] = new VRMLNodeType[vfMapping.size()];
                vfMapping.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValues = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
                break;

            case FIELD_ADDED_ENTITIES:
                num_kids = vfAddedEntities.size();

                if ((nodeTmp == null) || (nodeTmp.length < num_kids)) {
                    nodeTmp = new VRMLNodeType[num_kids];
                }
                vfAddedEntities.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValues = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_REMOVED_ENTITIES:
                num_kids = vfRemovedEntities.size();

                if ((nodeTmp == null) || (nodeTmp.length < num_kids)) {
                    nodeTmp = new VRMLNodeType[num_kids];
                }
                vfRemovedEntities.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValues = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

   @Override
    public void sendRoute(double time,
            int srcIndex,
            VRMLNodeType destNode,
            int destIndex) {

        int num_kids;

        try {
            switch (srcIndex) {
                case FIELD_SITE_ID:
                    destNode.setValue(destIndex, vfSiteID);
                    break;
                case FIELD_PORT:
                    destNode.setValue(destIndex, vfPort);
                    break;
                case FIELD_ADDRESS:
                    destNode.setValue(destIndex, vfAddress);
                    break;
                case FIELD_MAPPING:
                    VRMLNodeType kids[] = new VRMLNodeType[vfMapping.size()];
                    vfMapping.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
                    break;
                case FIELD_ADDED_ENTITIES:
                    num_kids = vfAddedEntities.size();

                    if ((nodeTmp == null) || (nodeTmp.length < num_kids)) {
                        nodeTmp = new VRMLNodeType[num_kids];
                    }
                    vfAddedEntities.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;
                case FIELD_REMOVED_ENTITIES:
                    num_kids = vfRemovedEntities.size();

                    if ((nodeTmp == null) || (nodeTmp.length < num_kids)) {
                        nodeTmp = new VRMLNodeType[num_kids];
                    }
                    vfRemovedEntities.toArray(nodeTmp);

                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch (InvalidFieldException ife) {
            System.err.println("BaseTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace(System.err);
        } catch (InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                    ifve.getMessage());
        }
    }

    @Override
    public void setValue(int index, VRMLNodeType[] children, int numValid)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_MAPPING:
                if (!inSetup) {
                    vfMapping.clear();
                }

                vfMapping.addAll(Arrays.asList(children));

                if (!inSetup) {
                    hasChanged[FIELD_MAPPING] = true;
                    fireFieldChanged(FIELD_MAPPING);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    @Override
    public void setValue(int index, VRMLNodeType child)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_MAPPING:
                if (!inSetup) {
                    vfMapping.clear();
                }

                vfMapping.add(child);

                if (!inSetup) {
                    hasChanged[FIELD_MAPPING] = true;
                    fireFieldChanged(FIELD_MAPPING);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }

   @Override
    public void setValue(int index, int value)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_SITE_ID:
                vfSiteID = value;

                if (!inSetup) {
                    hasChanged[FIELD_SITE_ID] = true;
                    fireFieldChanged(FIELD_SITE_ID);
                }
                break;
            case FIELD_APPLICATION_ID:
                vfApplicationID = value;
                if (!inSetup) {
                    hasChanged[FIELD_APPLICATION_ID] = true;
                    fireFieldChanged(FIELD_APPLICATION_ID);
                }
                break;
            case FIELD_PORT:
                vfPort = value;
                if (!inSetup) {
                    hasChanged[FIELD_PORT] = true;
                    fireFieldChanged(FIELD_PORT);
                }
                break;
            default:
                super.setValue(index, value);
        }
    }

    @Override
    public void setValue(int index, String value)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_ADDRESS:
                vfAddress = value;
                if (!inSetup) {
                    hasChanged[FIELD_ADDRESS] = true;
                    fireFieldChanged(FIELD_ADDRESS);
                }
                break;

            default:
                super.setValue(index, value);
        }

    }

    //----------------------------------------------------------
    // Methods for the VRMLDISNodeType
    //----------------------------------------------------------
    
    @Override
    public int getSiteID() {
        return vfSiteID;
    }

    @Override
    public int getAppID() {
        return vfApplicationID;
    }

    @Override
    public int getEntityID() {
        return 0;
    }

    @Override
    public String getAddress() {
        return vfAddress;
    }

    @Override
    public int getPort() {
        return vfPort;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String[] getAuthServer() {
        return null;
    }

    @Override
    public String getMucServer() {
        return null;
    }

    @Override
    public String getMucRoom() {
        return null;
    }

    @Override
    public void setIsActive(boolean active) {
    }

    @Override
    public boolean valuesToWrite() {
        return false;
    }

    @Override
    public Pdu getState() {
        return null;
    }

    @Override
    public void packetArrived(Pdu pdu) {
        // ingored
    }

    //----------------------------------------------------------
    // Internal methods of the class
    //----------------------------------------------------------
}
