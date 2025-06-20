/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.shape;

// External imports
import java.util.HashMap;
import java.util.Map;
import org.web3d.util.PropertyTools;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base renderer implementation of a shape node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.22 $
 */
public abstract class BaseShape extends AbstractNode
    implements VRMLShapeNodeType, VRMLBoundedNodeType, LocalColorsListener {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        {TypeConstants.BoundedNodeType};

    /** Index for the appearance field */
    protected static final int FIELD_APPEARANCE = LAST_NODE_INDEX + 1;

    /** Index for the geometry field */
    protected static final int FIELD_GEOMETRY = LAST_NODE_INDEX + 2;

    /** Index for the bboxSize field */
    private static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 3;

    /** Index for the bboxCenter field */
    private static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 4;

    /** Index for the bboxDisplay field */
    private static final int FIELD_BBOX_DISPLAY = LAST_NODE_INDEX + 5;

    /** Index for the visible field */
    private static final int FIELD_VISIBLE = LAST_NODE_INDEX + 6;

    /** Index for the castShadow field */
    private static final int FIELD_CASTSHADOW = LAST_NODE_INDEX + 7;

    /** The last field index used by this class */
    protected static final int LAST_SHAPE_INDEX = FIELD_CASTSHADOW;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_SHAPE_INDEX + 1;

    protected static final String FORCE_LIGHTING_PROP =
            "org.web3d.vrml.renderer.common.nodes.shape.forceLighting";

    /** The default forceLighting value */
    protected static final boolean DEFAULT_FORCE_LIGHTING = false;

    /** The value read from the system property for ForceLighting */
    protected static boolean forceLighting;


    /** Message for when the proto is not a Geometry */
    protected static final String GEOMETRY_PROTO_MSG =
        "Proto does not describe a Geometry object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String GEOMETRY_NODE_MSG =
        "Node does not describe a Geometry object";

    /** Message for when the proto is not a Appearance */
    protected static final String APPEARANCE_PROTO_MSG =
        "Proto does not describe a Appearance object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String APPEARANCE_NODE_MSG =
        "Node does not describe a Appearance object";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** Proto version of the appearance */
    protected VRMLProtoInstance pAppearance;

    /** SFNode appearance NULL */
    protected VRMLAppearanceNodeType vfAppearance;

    /** Proto version of the Geometry */
    protected VRMLProtoInstance pGeometry;

    /** SFNode geometry NULL */
    protected VRMLGeometryNodeType vfGeometry;

    /** SFVec3f bboxCenter NULL */
    protected float[] vfBboxCenter;

    /** SFVec3f bboxSize NULL */
    protected float[] vfBboxSize;

    /** SFBool bboxDisplay false */
    protected boolean vfBboxDisplay;

    /** SFBool visible true */
    protected boolean vfVisible;

    /** SFBool castShadow true */
    protected boolean vfCastShadow;

    /** Counter for the number of sharing references this has */
    protected int shareCount;

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_APPEARANCE,
            FIELD_GEOMETRY,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_APPEARANCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "appearance");
        fieldDecl[FIELD_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "geometry");
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
        fieldDecl[FIELD_VISIBLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "visible");
        fieldDecl[FIELD_CASTSHADOW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "castShadow");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_APPEARANCE;
        fieldMap.put("appearance", idx);
        fieldMap.put("set_appearance", idx);
        fieldMap.put("appearance_changed", idx);

        idx = FIELD_GEOMETRY;
        fieldMap.put("geometry", idx);
        fieldMap.put("set_geometry", idx);
        fieldMap.put("geometry_changed", idx);

        fieldMap.put("bboxSize", FIELD_BBOX_SIZE);
        fieldMap.put("bboxCenter", FIELD_BBOX_CENTER);
        fieldMap.put("bboxDisplay", FIELD_BBOX_DISPLAY);
        fieldMap.put("visible", FIELD_VISIBLE);
        fieldMap.put("castShadow", FIELD_CASTSHADOW);

        forceLighting = PropertyTools.fetchSystemProperty(FORCE_LIGHTING_PROP,
                DEFAULT_FORCE_LIGHTING);
    }

    /**
     * Construct a new default shape node implementation.
     */
    protected BaseShape() {
        super("Shape");

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};
        vfBboxDisplay = false;
        vfVisible = true;
        vfCastShadow = true;

        hasChanged = new boolean[LAST_SHAPE_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    protected BaseShape(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        float[] field = ((VRMLBoundedNodeType)node).getBboxCenter();

        vfBboxCenter[0] = field[0];
        vfBboxCenter[1] = field[1];
        vfBboxCenter[2] = field[2];

        field = ((VRMLBoundedNodeType)node).getBboxSize();

        vfBboxSize[0] = field[0];
        vfBboxSize[1] = field[1];
        vfBboxSize[2] = field[2];

        boolean b = ((VRMLBoundedNodeType)node).getBboxDisplay();
        vfBboxDisplay = b;
         b = ((VRMLBoundedNodeType)node).getVisible();
        vfVisible = b;
         b = ((BaseShape)node).getCastShadow();
        vfCastShadow = b;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLAppearanceNodeType
    //-------------------------------------------------------------

    /**
     * Get node content for <code>appearance</code>.
     *
     * @return The current appearance
     */
    @Override
    public VRMLNodeType getAppearance() {
        if (pAppearance != null) {
            return pAppearance;
        } else {
            return vfAppearance;
        }
    }

    /**
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param app The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setAppearance(VRMLNodeType app)
        throws InvalidFieldValueException {

        VRMLAppearanceNodeType node;
        VRMLNodeType old_node;

        if(pAppearance != null)
            old_node = pAppearance;
        else
            old_node = vfAppearance;

        if(app instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)app).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLAppearanceNodeType))
                throw new InvalidFieldValueException(APPEARANCE_PROTO_MSG);

            node = (VRMLAppearanceNodeType)impl;
            pAppearance = (VRMLProtoInstance)app;

        } else if(app != null && !(app instanceof VRMLAppearanceNodeType)) {
            throw new InvalidFieldValueException(APPEARANCE_NODE_MSG);
        } else {
            pAppearance = null;
            node = (VRMLAppearanceNodeType)app;
        }

        vfAppearance = node;
        if(app != null)
            updateRefs(app, true);

        if(old_node != null)
            updateRefs(old_node, false);

        updateAppAndGeom();

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(app != null)
                stateManager.registerAddedNode(app);

            hasChanged[FIELD_APPEARANCE] = true;
            fireFieldChanged(FIELD_APPEARANCE);
        }

    }

    /**
     * Get node content for <code>geometry</code>
     *
     * @return The current geometry field
     */
    @Override
    public VRMLNodeType getGeometry() {
        if (pGeometry != null) {
            return pGeometry;
        } else {
            return vfGeometry;
        }
    }

    /**
     * Set node content as replacement for <code>geometry</code>.
     *
     * @param geom The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setGeometry(VRMLNodeType geom)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;

        if(pGeometry != null)
            old_node = pGeometry;
        else
            old_node = vfGeometry;

        if(geom instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)geom).getImplementationNode();

                // Walk down the proto impl looking for the real node to check it
                // is the right type.
                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) && !(impl instanceof VRMLGeometryNodeType))
                    throw new InvalidFieldValueException(GEOMETRY_PROTO_MSG);

                pGeometry = (VRMLProtoInstance)geom;
                vfGeometry = (VRMLGeometryNodeType)impl;

        } else if(geom != null && !(geom instanceof VRMLGeometryNodeType)) {
            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);
        } else {
            pGeometry = null;
            vfGeometry = (VRMLGeometryNodeType) geom;
        }

        updateAppAndGeom();

        if(geom != null)
            updateRefs(geom, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(geom != null)
                stateManager.registerAddedNode(geom);

            hasChanged[FIELD_GEOMETRY] = true;
            fireFieldChanged(FIELD_GEOMETRY);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //----------------------------------------------------------

    @Override
    public float[] getBboxCenter() {
        return vfBboxCenter;
    }

    @Override
    public float[] getBboxSize() {
        return vfBboxSize;
    }

    @Override
    public boolean getBboxDisplay() {
        return vfBboxDisplay;
    }

    @Override
    public boolean getVisible() {
        return vfVisible;
    }

    public boolean getCastShadow() {
        return vfCastShadow;
    }

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    @Override
    public boolean isShared() {
        return (shareCount > 1);
    }

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    @Override
    public void setShared(boolean used) {
        if(used)
            shareCount++;
        else
            shareCount--;
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

        super.setupFinished();

        if(pAppearance != null)
            pAppearance.setupFinished();
        else if(vfAppearance != null)
            vfAppearance.setupFinished();

        if(pGeometry != null)
            pGeometry.setupFinished();
        else if(vfGeometry != null)
            vfGeometry.setupFinished();

        updateAppAndGeom();
    }

    //----------------------------------------------------------
    // Methods defined by LocalColorsListener
    //----------------------------------------------------------

    /**
     * The localColors state has change.
     *
     * @param enabled True if the geometry has local colors.
     * @param hasAlpha true with the local color also contains alpha values
     */
    @Override
    public void localColorsChanged(boolean enabled, boolean hasAlpha) {
        if(vfAppearance != null)
            vfAppearance.setLocalColor(enabled, hasAlpha);
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
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_SHAPE_INDEX)
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
        return TypeConstants.ShapeNodeType;
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
            case FIELD_APPEARANCE:
                fieldData.clear();
                if(pAppearance != null)
                    fieldData.nodeValue = pAppearance;
                else
                    fieldData.nodeValue = vfAppearance;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEOMETRY:
                fieldData.clear();
                if(pGeometry != null)
                    fieldData.nodeValue = pGeometry;
                else
                    fieldData.nodeValue = vfGeometry;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValues = vfBboxSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.clear();
                fieldData.floatArrayValues = vfBboxCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_DISPLAY:
                fieldData.clear();
                fieldData.booleanValue = vfBboxDisplay;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_VISIBLE:
                fieldData.clear();
                fieldData.booleanValue = vfVisible;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_CASTSHADOW:
                fieldData.clear();
                fieldData.booleanValue = vfCastShadow;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
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
                case FIELD_APPEARANCE :
                    if(pAppearance != null)
                        destNode.setValue(destIndex, pAppearance);
                    else
                        destNode.setValue(destIndex, vfAppearance);
                    break;
                case FIELD_GEOMETRY :
                    if(pGeometry != null)
                        destNode.setValue(destIndex, pGeometry);
                    else
                        destNode.setValue(destIndex, vfGeometry);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException | InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    @Override
    public void setValue(int index, boolean value) {
        switch(index)
        {
            case FIELD_BBOX_DISPLAY:
                setBboxDisplay(value);
                break;

            case FIELD_VISIBLE:
                setVisible(value);
                break;

            case FIELD_CASTSHADOW:
                setCastShadow(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided does not fit this
     *    type of node
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_BBOX_CENTER:
                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
                setBboxSize(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
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
            case FIELD_APPEARANCE:
                setAppearance(node);
                break;

            case FIELD_GEOMETRY:
                setGeometry(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Convenience method to set a new value the vfBboxCenter fields
     *
     * @param newBboxCenter The new center of the bounding box
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    after setup has finished
     */
    @Override
    public void setBboxCenter(float[] newBboxCenter)
        throws InvalidFieldAccessException {
        if(!inSetup)
            throw new InvalidFieldAccessException("bboxCenter is initializeOnly");

        vfBboxCenter[0] = newBboxCenter[0];
        vfBboxCenter[1] = newBboxCenter[1];
        vfBboxCenter[2] = newBboxCenter[2];
    }

    /**
     * Convenience method to set a new value for the vfBboxSize field.
     *
     * @param newBboxSize The new size for the bounding box
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    after setup has finished
     */
    @Override
    public void setBboxSize(float[] newBboxSize)
        throws InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException("bboxSize is initializeOnly");

        FieldValidator.checkBBoxSize("BaseShape.bboxSize", newBboxSize);

        vfBboxSize[0] = newBboxSize[0];
        vfBboxSize[1] = newBboxSize[1];
        vfBboxSize[2] = newBboxSize[2];
    }

    /** Set the bounding box display for this node. Only used by the field
     * parser
     *
     * @param val the display value to set
     */
    @Override
    public void setBboxDisplay(boolean val) {
        vfBboxDisplay = val;
    }

    /** Set visible field for this node. Only used by the field
     * parser
     *
     * @param val the display value to set
     */
    @Override
    public void setVisible(boolean val) {
        vfVisible = val;
    }

    /** Set castShadow field for this node. Only used by the field
     * parser
     *
     * @param val the display value to set
     */
    public void setCastShadow(boolean val) {
        vfCastShadow = val;
    }

    /**
     * Transfer all the stuff between appearance and geometry.
     */
    private void updateAppAndGeom() {
        if((vfAppearance == null) || (vfGeometry == null))
            return;

        vfAppearance.setSolid(vfGeometry.isSolid());
        vfAppearance.setLightingEnabled(vfGeometry.isLightingEnabled());
        vfAppearance.setLocalColor(vfGeometry.hasLocalColors(),
                                   vfGeometry.hasLocalColorAlpha());
        vfGeometry.addLocalColorsListener(this);
        vfAppearance.setCCW(vfGeometry.isCCW());

        int num_tex = 0;
        // Does not handle texture protos currently
        VRMLNodeType tex = vfAppearance.getTexture();

        if(tex != null) {
            if(tex instanceof VRMLComposedTextureNodeType) {
               VRMLComposedTextureNodeType vct =
                   (VRMLComposedTextureNodeType)tex;

                num_tex =  vct.getNumberTextures();
            } else {
                num_tex = 1;
            }
        }

        vfGeometry.setTextureCount(num_tex);
    }
}
