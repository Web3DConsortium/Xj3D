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

package org.web3d.vrml.renderer.common.nodes.group;

// Standard imports
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Transform that allows an arbitrary matrix field.
 * <p>
 *
 * This base class does not automatically update the underlying transform
 * with each set() call. These calls only update the local field values,
 * but not the transform that would be used in the rendering code. To make
 * sure this is updated, call the {@link #updateMatrix()} method and then
 * use the updated matrix in your rendering code.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class BaseMatrixTransform extends BaseGroupingNode {

    /** Field Index */
    protected static final int FIELD_MATRIX = LAST_GROUP_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_TRANSFORM_INDEX = FIELD_MATRIX;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TRANSFORM_INDEX + 1;

    /** High-Side epsilon float = 0 */
    private static final float ZEROEPS = 0.0001f;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFMatrix4f matrix */
    protected float[] vfMatrix;

    /** Working variables for the computation */
    protected Matrix4f tmatrix;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

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
        fieldDecl[FIELD_MATRIX] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFMatrix4f",
                                     "matrix");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_CHILDREN;
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = FIELD_ADDCHILDREN;
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = FIELD_REMOVECHILDREN;
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter", FIELD_BBOX_CENTER);
        fieldMap.put("bboxSize", FIELD_BBOX_SIZE);
        fieldMap.put("bboxDisplay", FIELD_BBOX_DISPLAY);

        idx = FIELD_MATRIX;
        fieldMap.put("matrix", idx);
        fieldMap.put("set_matrix", idx);
        fieldMap.put("matrix_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseMatrixTransform() {
        super("MatrixTransform");

        hasChanged = new boolean[LAST_TRANSFORM_INDEX + 1];

        tmatrix = new Matrix4f();
        vfMatrix = new float[16];

        // Initialize to Identity
        vfMatrix[0] = 1;
        vfMatrix[5] = 1;
        vfMatrix[10] = 1;
        vfMatrix[15] = 1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseMatrixTransform(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("matrix");
            VRMLFieldData field = node.getFieldValue(index);
            System.arraycopy(field.floatArrayValues, 0, vfMatrix, 0, 15);
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propagate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    @Override
    public void allEventsComplete() {
        updateMatrix();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTransform interface.
    //----------------------------------------------------------

    /**
     * Set the matrix value.
     *
     * @param mat The new matrix
     * @throws InvalidFieldValueException The matrix was null or too small
     */
    public void setMatrix(float[] mat)
        throws InvalidFieldValueException {

        if(mat == null || mat.length < 16)
            throw new InvalidFieldValueException("Matrix value null or < 16");
        System.arraycopy(mat, 0, vfMatrix, 0, 16);

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_MATRIX] = true;
            fireFieldChanged(FIELD_MATRIX);
        }
    }

    /**
     * Get the current matrix.
     *
     * @return The current matrix
     */
    public float[] getMatrix() {
        return vfMatrix;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        Integer index = fieldMap.get(fieldName);

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
        if(index < 0  || index > LAST_TRANSFORM_INDEX)
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

        fieldData.clear();
        fieldData.numElements = 1;
        fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;

        switch(index) {
            case FIELD_MATRIX:
                fieldData.floatArrayValues = vfMatrix;
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
                case FIELD_MATRIX:
                    destNode.setValue(destIndex, vfMatrix, 16);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace(System.err);
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
            case FIELD_MATRIX:
                setMatrix(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal methods of the class
    //----------------------------------------------------------

    /**
     * Calculate transforms needed to handle VRML semantics and place the
     * results in the matrix variable of this class.
     *  formula: T x C x R x SR x S x -SR x -C
     */
    protected void updateMatrix() {
        tmatrix.set(vfMatrix);
        // Convert Row Matrix form to Col matrix form
        tmatrix.transpose();
    }
}
