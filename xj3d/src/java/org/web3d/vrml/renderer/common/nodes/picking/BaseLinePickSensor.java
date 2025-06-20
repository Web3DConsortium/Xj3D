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

package org.web3d.vrml.renderer.common.nodes.picking;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;


/**
 * Implementation of the common LinePickSensor node for all renderers.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public abstract class BaseLinePickSensor extends BasePickingNode {

    /** The field index for pickedPoint */
    protected static final int FIELD_PICKED_POINT = LAST_PICK_INDEX + 1;

    /** The field index for pickedNormal */
    protected static final int FIELD_PICKED_NORMAL = LAST_PICK_INDEX + 2;

    /** The field index for pickedTexCoord */
    protected static final int FIELD_PICKED_TEXCOORD = LAST_PICK_INDEX + 3;

    /** Last index used by this base node */
    protected static final int LAST_LINE_INDEX = FIELD_PICKED_TEXCOORD;

    /** The total number of fields in this node */
    protected static final int NUM_FIELDS = LAST_LINE_INDEX + 1;

    /** Error message for the geometry pick type */
    private static final String PICK_GEOM_MSG = "The pickGeometry type is " +
        "invalid for LinePickSensor. It must be either LineSet or IndexedLineSet";

    /** Message for when the proto is not a Geometry */
    protected static final String LINE_PROTO_MSG =
        "Proto does not describe a LineSet or IndexedLineSet object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String LINE_NODE_MSG =
        "Node does not describe a LineSet or IndexedLineSet object";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** Value for outputOnly field pickedPoint */
    private float[] vfPickedPoint;

    /** Value for outputOnly field pickedNormal */
    private float[] vfPickedNormal;

    /** Value for outputOnly field pickedTexCoord */
    private float[] vfPickedTextureCoordinate;

    /** Number of points that are valid in the pickedPoint/Normal/tex array */
    private int numPickedItems;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] {
            FIELD_PICK_TARGET,
            FIELD_PICKING_GEOMETRY,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_PICKING_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "pickingGeometry");
        fieldDecl[FIELD_PICK_TARGET] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "pickTarget");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_PICKED_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "pickedGeometry");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_INTERSECTION_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "intersectionType");
        fieldDecl[FIELD_SORT_ORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "sortOrder");
        fieldDecl[FIELD_PICKED_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFVec3f",
                                     "pickedPoint");
        fieldDecl[FIELD_PICKED_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFVec3f",
                                     "pickedNormal");
        fieldDecl[FIELD_PICKED_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFVec3f",
                                     "pickedTextureCoordinate");
        fieldDecl[FIELD_OBJECT_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "objectType");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_PICKING_GEOMETRY;
        fieldMap.put("pickingGeometry", idx);
        fieldMap.put("set_pickingGeometry", idx);
        fieldMap.put("pickingGeometry_changed", idx);

        idx = FIELD_PICK_TARGET;
        fieldMap.put("pickTarget", idx);
        fieldMap.put("set_pickTarget", idx);
        fieldMap.put("pickTarget_changed", idx);

        idx = FIELD_ENABLED;
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = FIELD_OBJECT_TYPE;
        fieldMap.put("objectType", idx);
        fieldMap.put("set_objectType", idx);
        fieldMap.put("objectType_changed", idx);

        fieldMap.put("sortOrder", FIELD_SORT_ORDER);
        fieldMap.put("intersectionType", FIELD_INTERSECTION_TYPE);

        fieldMap.put("isActive", FIELD_IS_ACTIVE);
        fieldMap.put("pickedGeometry", FIELD_PICKED_GEOMETRY);
        fieldMap.put("pickedPoint", FIELD_PICKED_POINT);
        fieldMap.put("pickedNormal", FIELD_PICKED_NORMAL);
        fieldMap.put("pickedTextureCoordinate", FIELD_PICKED_TEXCOORD);
    }

    /**
     * Construct a new time sensor object
     */
    public BaseLinePickSensor() {
        super("LinePickSensor", PICK_GEOM_MSG);

        hasChanged = new boolean[NUM_FIELDS];
        validGeometryNodeNames.add("LineSet");
        validGeometryNodeNames.add("IndexedLineSet");
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseLinePickSensor(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLPickingSensorNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * Get the picking type that this class represents. A shortcut way of
     * quickly determining the picking strategy to be used by the internal
     * implementation to avoid unnecessary calculations.
     *
     * @return One of the *_PICK constants
     */
    @Override
    public int getPickingType() {
        return LINE_PICK;
    }

    /**
     * Notification that this sensor has just been clicked on to start the
     * pick action.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    @Override
    public void notifyPickStart(int numPicks,
                                VRMLNode[] nodes,
                                float[] points,
                                float[] normals,
                                float[] texCoords) {
        processCoordsOutput(numPicks, points, normals, texCoords);

        super.notifyPickStart(numPicks, nodes, points, normals, texCoords);
    }

    /**
     * Notify the drag sensor that a sensor is currently dragging this device
     * and that it's position and orientation are as given.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    @Override
    public void notifyPickChange(int numPicks,
                                 VRMLNode[] nodes,
                                 float[] points,
                                 float[] normals,
                                 float[] texCoords) {
        processCoordsOutput(numPicks, points, normals, texCoords);

        super.notifyPickChange(numPicks, nodes, points, normals, texCoords);
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
        if(index < 0  || index > LAST_LINE_INDEX)
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

        switch(index) {
            case FIELD_PICKED_POINT:
                fieldData.clear();
                fieldData.floatArrayValues = vfPickedPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPickedItems;
                break;

            case FIELD_PICKED_NORMAL:
                fieldData.clear();
                fieldData.floatArrayValues = vfPickedNormal;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPickedItems;
                break;

            case FIELD_PICKED_TEXCOORD:
                fieldData.clear();
                fieldData.floatArrayValues = vfPickedTextureCoordinate;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPickedItems;
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
                case FIELD_PICKED_POINT:
                    destNode.setValue(destIndex,
                                      vfPickedPoint,
                                      numPickedItems * 3);
                    break;

                case FIELD_PICKED_NORMAL:
                    destNode.setValue(destIndex,
                                      vfPickedNormal,
                                      numPickedItems * 3);
                    break;

                case FIELD_PICKED_TEXCOORD:
                    destNode.setValue(destIndex,
                                      vfPickedTextureCoordinate,
                                      numPickedItems * 2);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseLinePicker.sendRoute: No field! " + srcIndex);
            ife.printStackTrace(System.err);
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseLinePicker.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }


    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Process the point, normal etc output as output values.
     *
     * @param numPicks Number of valid items in each array
     * @param points Array of points that are the intersection points
     * @param normals Array of normals that are the intersection points
     * @param texCoords Array of texture coordinates that are the intersection points
     */
    private void processCoordsOutput(int numPicks,
                                     float[] points,
                                     float[] normals,
                                     float[] texCoords) {

        // Don't generate output for coordinates if it is only bounds based.
        if(intersectionType == INTERSECT_BOUNDS)
            return;

        int size = numPicks * 3;
        if((vfPickedPoint == null) || (vfPickedPoint.length < size)) {
            vfPickedPoint = new float[size];
            vfPickedNormal = new float[size];
            vfPickedTextureCoordinate = new float[size];
        }

        System.arraycopy(points, 0, vfPickedPoint, 0, size);
        System.arraycopy(normals, 0, vfPickedNormal, 0, size);
        System.arraycopy(texCoords, 0, vfPickedTextureCoordinate, 0, size);

        hasChanged[FIELD_PICKED_POINT] = true;
        hasChanged[FIELD_PICKED_NORMAL] = true;
        hasChanged[FIELD_PICKED_TEXCOORD] = true;

        numPickedItems = numPicks;

        fireFieldChanged(FIELD_PICKED_POINT);
        fireFieldChanged(FIELD_PICKED_NORMAL);
        fireFieldChanged(FIELD_PICKED_TEXCOORD);
    }
}
