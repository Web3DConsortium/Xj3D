/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.navigation;

// Standard imports
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLViewDependentNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common base implementation of a billboard node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public class BaseBillboard extends BaseGroupingNode
    implements VRMLViewDependentNodeType {

    /**
     *
     */
    protected static final int[] SECONDARY_TYPE = {
        TypeConstants.ViewDependentNodeType
    };

    /** Field Index */
    protected static final int FIELD_AXISOFROTATION = LAST_GROUP_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_BILLBOARD_INDEX = FIELD_AXISOFROTATION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_BILLBOARD_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> FIELD_MAP;

    /** Indices of the fields that are MFNode or SFnode */
    private static int[] nodeFields;

    /** SFVec3f axisOfRotation */
    protected float[] vfAxisOfRotation;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        FIELD_MAP = new HashMap<>(NUM_FIELDS * 2);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFNode",
                                 "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                  "SFVec3f",
                                 "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFVec3f",
                                 "bboxSize");
        fieldDecl[FIELD_BBOX_DISPLAY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "bboxDisplay");

        fieldDecl[FIELD_AXISOFROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFVec3f",
                                 "axisOfRotation");


        Integer idx = FIELD_METADATA;
        FIELD_MAP.put("metadata", idx);
        FIELD_MAP.put("set_metadata", idx);
        FIELD_MAP.put("metadata_changed", idx);

        idx = FIELD_CHILDREN;
        FIELD_MAP.put("children", idx);
        FIELD_MAP.put("set_children", idx);
        FIELD_MAP.put("children_changed", idx);

        idx = FIELD_ADDCHILDREN;
        FIELD_MAP.put("addChildren", idx);
        FIELD_MAP.put("set_addChildren", idx);

        idx = FIELD_REMOVECHILDREN;
        FIELD_MAP.put("removeChildren", idx);
        FIELD_MAP.put("set_removeChildren", idx);

        FIELD_MAP.put("bboxCenter",FIELD_BBOX_CENTER);
        FIELD_MAP.put("bboxSize",FIELD_BBOX_SIZE);
        FIELD_MAP.put("bboxDisplay", FIELD_BBOX_DISPLAY);

        idx = FIELD_AXISOFROTATION;
        FIELD_MAP.put("axisOfRotation", idx);
        FIELD_MAP.put("set_axisOfRotation", idx);
        FIELD_MAP.put("axisOfRotation_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseBillboard() {
        super("Billboard");

        hasChanged = new boolean[LAST_BILLBOARD_INDEX + 1];
        vfAxisOfRotation = new float[] {0, 1, 0};
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseBillboard(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("axisOfRotation");
            VRMLFieldData field = node.getFieldValue(index);

            vfAxisOfRotation[0] = field.floatArrayValues[0];
            vfAxisOfRotation[1] = field.floatArrayValues[1];
            vfAxisOfRotation[2] = field.floatArrayValues[2];
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNode class.
    //----------------------------------------------------------
    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = FIELD_MAP.get(fieldName);

        return (index == null) ? -1 : index;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_BILLBOARD_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.GroupingNodeType;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    @Override
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

   /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_AXISOFROTATION:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;

                fieldData.floatArrayValues = vfAxisOfRotation;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_AXISOFROTATION :
                    destNode.setValue(destIndex, vfAxisOfRotation, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_AXISOFROTATION :
                vfAxisOfRotation[0] = value[0];
                vfAxisOfRotation[1] = value[1];
                vfAxisOfRotation[2] = value[2];
                if(!inSetup) {
                    hasChanged[FIELD_AXISOFROTATION] = true;
                    fireFieldChanged(FIELD_AXISOFROTATION);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
