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

package org.web3d.vrml.renderer.common.nodes.rigidphysics;

// External imports
import org.odejava.*;

import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

/**
 * Implementation of the CollidableShape node.
 * <p>
 *
 * The base class provides most of the basic functionality, including
 * interacting with the ODE portions. However, for returning the scene graph
 * object appropriate for the renderer will require the derived class to take
 * care of it.
 * <p>
 *
 * The X3D definition of CollidableShape is:
 * <pre>
 * CollidableShape : X3DNBodyCollidableNode {
 *   SFNode     [in,out] metadata       NULL     [X3DMetadataObject]
 *   SFBool     [in,out] enabled     TRUE
 *   SFVec3f    [in,out] position       0 0 0    (-&#8734;,&#8734;)
 *   SFRotation [in,out] rotation       0 0 1 0  [0,1]
 *   SFVec3f    []       bboxCenter     0 0 0    (-&#8734;,&#8734;)
 *   SFVec3f    []       bboxSize       -1 -1 -1 [0,&#8734;) or -1 -1 -1
 *   SFBool              bboxDisplay    FALSE
 *   SFNode     []       shape          NULL     [Shape]
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public abstract class BaseCollidableShape extends BaseCollidableNode {

    // Field index constants

    /** The field index for shape */
    protected static final int FIELD_SHAPE = LAST_COLLIDABLE_INDEX + 1;

    /** Last index used by this base node */
    protected static final int LAST_SHAPE_INDEX = FIELD_SHAPE;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_SHAPE_INDEX + 1;

    /** Message for when the node in setValue() is not a primitive */
    protected static final String SHAPE_PROTO_MSG =
        "shape field proto value does not describe a Shape node.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String SHAPE_NODE_MSG =
        "shape field node value does not describe a Shape node.";

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    // The VRML field values

    /** Proto version of the shape node */
    protected VRMLProtoInstance pShape;

    /** exposedField SFNode shape NULL */
    protected VRMLShapeNodeType vfShape;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_SHAPE,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_DISPLAY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "bboxDisplay");
        fieldDecl[FIELD_SHAPE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "shape");
        fieldDecl[FIELD_TRANSLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "translation");
        fieldDecl[FIELD_ROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "rotation");

        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_TRANSLATION;
        fieldMap.put("translation", idx);
        fieldMap.put("set_translation", idx);
        fieldMap.put("translation_changed", idx);

        idx = FIELD_ENABLED;
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = FIELD_ROTATION;
        fieldMap.put("rotation", idx);
        fieldMap.put("set_rotation", idx);
        fieldMap.put("rotation_changed", idx);

        fieldMap.put("bboxCenter", FIELD_BBOX_CENTER);
        fieldMap.put("bboxSize", FIELD_BBOX_SIZE);
        fieldMap.put("bboxDisplay", FIELD_BBOX_DISPLAY);
        fieldMap.put("shape", FIELD_SHAPE);
    }

    /**
     * Construct a new Collidable shape node object.
     */
    public BaseCollidableShape() {
        super("CollidableShape");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseCollidableShape(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLNBodyCollidableNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        if(pShape != null)
            pShape.setupFinished();
        else if(vfShape != null)
            vfShape.setupFinished();

        // At this point walk through the shape to find the proxy geometry.
        // If it was loaded from an externproto that is not resolved yet, well
        // we don't handle that case yet!
        createODEGeom();

        super.setupFinished();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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
     * @return A shape of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_SHAPE_INDEX)
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
            case FIELD_SHAPE:
                fieldData.clear();
                if(pShape != null)
                    fieldData.nodeValue = pShape;
                else
                    fieldData.nodeValue = vfShape;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_SHAPE:
	        if(!inSetup)
	            throwInitOnlyWriteException("shape");

                setShape(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set node content as replacement for the shape field.
     *
     * @param shape The new Shape node to use
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setShape(VRMLNodeType shape)
        throws InvalidFieldValueException {

        VRMLShapeNodeType node;
        VRMLNodeType old_node;

        if(pShape != null)
            old_node = pShape;
        else
            old_node = vfShape;

        if(shape instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)shape).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLShapeNodeType))
                throw new InvalidFieldValueException(SHAPE_PROTO_MSG);

            node = (VRMLShapeNodeType)impl;
            pShape = (VRMLProtoInstance)shape;

        } else if(shape != null && !(shape instanceof VRMLShapeNodeType)) {
            throw new InvalidFieldValueException(SHAPE_NODE_MSG);
        } else {
            pShape = null;
            node = (VRMLShapeNodeType)shape;
        }

        vfShape = node;
        if(shape != null)
            updateRefs(shape, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }

    /**
     * Internal convenience method to take the shape node and walk into the
     * geometry to find out what it holds.
     */
    private void createODEGeom() {
        if(vfShape == null)
            return;

        VRMLNodeType g_node = vfShape.getGeometry();

        if(g_node == null)
            return;

        // walk the geometry proto to find the real geom
        VRMLGeometryNodeType geom;

        if(g_node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)vfShape).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl == null) || !(impl instanceof VRMLGeometryNodeType))
                return;

            geom = (VRMLGeometryNodeType)impl;
        } else
            geom = (VRMLGeometryNodeType)g_node;

        // we now have a real geometry node, we need to convert this to a
        // PlaceableGeom instance. Do this by mapping the node name across to
        // an appropriate class instance. Not all geometry types are mappable.
        String node_name = geom.getVRMLNodeName();
        int index;
        VRMLFieldData field;

        switch (node_name) {
            case "Box":
                index = geom.getFieldIndex("size");
                field = geom.getFieldValue(index);
                odeGeom = new GeomBox(field.floatArrayValues[0],
                        field.floatArrayValues[1],
                        field.floatArrayValues[2]);
                break;
            case "Sphere":
                index = geom.getFieldIndex("radius");
                field = geom.getFieldValue(index);
                odeGeom = new GeomSphere(field.floatValue);
                break;
            case "Cone":
                {
                    index = geom.getFieldIndex("bottomRadius");
                    field = geom.getFieldValue(index);
                    float radius = field.floatValue;
                    index = geom.getFieldIndex("height");
                    field = geom.getFieldValue(index);
                    //            odeGeom = new GeomCone(radius, field.floatValue);
                    
                    // This creates nothing.  Native calls have been disabled for at
                    // least since 27 FEB 2007 when the java.net team branched with the
                    // ODE JNI code
                    odeGeom = new GeomCone();
                    break;
                }
            case "Cylinder":
            {
                index = geom.getFieldIndex("radius");
                field = geom.getFieldValue(index);
                float radius = field.floatValue;
                index = geom.getFieldIndex("height");
                field = geom.getFieldValue(index);
//            odeGeom = new GeomCappedCylinder(radius, field.floatValue);
                // On 02 MAR 2007, the API for ODE was refactored
                odeGeom = new GeomCapsule(radius, field.floatValue);
                    break;
                }
            case "ElevationGrid":
                System.out.println("CollidableShape does not handle ElevationGrid proxy yet");
                break;
            case "IndexedFaceSet":
                break;
            case "TriangleSet":
                index = geom.getFieldIndex("coord");
                field = geom.getFieldValue(index);
                VRMLNodeType coord_node = (VRMLNodeType)field.nodeValue;
                index = coord_node.getFieldIndex("point");
                field = coord_node.getFieldValue(index);
                odeGeom = new GeomTriMesh(field.floatArrayValues,
                        field.numElements);
                break;
            case "TriangleStripSet":
            case "TriangleFanSet":
            case "IndexedTriangleSet":
            case "IndexedTriangleStripSet":
            case "IndexedTriangleFanSet":
                System.out.println("CollidableShape does not handle triangle data proxy yet");
                break;
        }
    }
}
