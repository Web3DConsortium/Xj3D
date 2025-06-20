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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a StaticGroup node.
 * <p>
 *
 * <b>Properties</b>
 * <p>
 * The following properties are used by this class
 * <ul>
 * <li><code>org.web3d.vrml.nodes.staticgroup.dispose</code> Boolean value
 *     describing whether the StaticGroup should dispose of the VRML
 *     node children. This is an efficiency measure that will allow an
 *     implementation to remove unneeded memory. However, it also means
 *     that they can no longer be traversed, so if your application is
 *     trying to traverse the scene graph, it will not be able to use the
 *     nodes later on. Useful for a runtime optimisation, no good if you are
 *     writing an editor.
 * </li>
 * <li><code>org.web3d.vrml.nodes.staticgroup.compact</code> Boolean value
 *     describing whether the StaticGroup should compact the VRML
 *     scene graph below this node or leave it in an expanded state. This is
 *     an efficiency measure that will allow an implementation to flatten
 *     the scene graph if desired. Useful for debugging but also means the
 *     runtime scene graph will probably be different from the original
 *     loaded from file.
 * </li>
 * </ul>
 *
 * The base implementation provided by this class does not do anything with
 * these properties. Their values are only provided as a hook for higher level
 * functionality. This is because most of the action would take place in the
 * setupFinished() method and it would dispose/compact the children before the
 * derived class had a chance to do anything with the children nodes. It is
 * assumed that a derived class would know when and where it is safe to dispose
 * of the children.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseStaticGroup extends AbstractNode
    implements VRMLBoundedNodeType {

    /** Property describing whether to dispose of the kids or not. */
    private static final String DISPOSE_STATE_PROP =
        "org.web3d.vrml.nodes.staticgroup.dispose";

    /** Property describing whether to compact the kids or not. */
    private static final String COMPACT_STATE_PROP =
        "org.web3d.vrml.nodes.staticgroup.compact";

    /** Default state to assume for the property disposal if not given */
    private static final boolean DEFAULT_DISPOSE_STATE = false;

    /** Default state to assume for the property compaction if not given */
    private static final boolean DEFAULT_COMPACT_STATE = false;

    /** Index of the children field */
    protected static final int FIELD_CHILDREN = LAST_NODE_INDEX + 1;

    /** Index of the Bounding box center bboxCenter field */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 2;

    /** Index of the Bounding box size bboxSize field */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 3;
    
    /** Index of the Bounding box display bboxDisplay field */
    protected static final int FIELD_BBOX_DISPLAY = LAST_NODE_INDEX + 4;
    
    /** Index of the Bounding box display bboxDisplay field */
    protected static final int FIELD_VISIBLE = LAST_NODE_INDEX + 5;

    /** The last field index used by this class */
    protected static final int LAST_STATICGROUP_INDEX = FIELD_VISIBLE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_STATICGROUP_INDEX + 1;

    /** Message for trying to add children to this node at runtime */
    private static final String NO_CHANGE_MSG =
        "You cannot add or remove nodes from a StaticGroup at runtime";

    /** Message when there is a bindable child */
    private static final String USE_BIND_MSG =
        "New node contains bindables when this grouping node is already " +
        "USEd. Ignoring the request";

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /**
     * Global flag to specify if the StaticGroup should dispose of its
     * children after setupFinished() is called as an optimisation measure.
     */
    protected static final boolean disposeChildren;

    /**
     * Global flag to specify if the StaticGroup should compact its
     * children nodes during the setupFinish().
     */
    protected static final boolean compactChildren;

    /** MFNode children NULL */
    protected List<VRMLNodeType> vfChildren;

    /** SFVec3f bboxCenter NULL */
    protected float[] vfBboxCenter;

    /** SFVec3f bboxSize NULL */
    protected float[] vfBboxSize;
    
    /** SFBool bboxDisplay false */
    protected boolean vfBboxDisplay;
    
    /** SFBool visible true */
    protected boolean vfVisible;

    /** Counter for the number of sharing references this has */
    protected int shareCount;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFNode",
                                 "children");

        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                  "SFVec3f",
                                 "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFVec3f",
                                 "bboxSize");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("children",FIELD_CHILDREN);
        fieldMap.put("bboxCenter",FIELD_BBOX_CENTER);
        fieldMap.put("bboxSize",FIELD_BBOX_SIZE);

        // fetch the system property defining the values
        boolean[] vals = AccessController.doPrivileged((PrivilegedAction<boolean[]>) () -> new boolean[]{
            Boolean.getBoolean(DISPOSE_STATE_PROP),
            Boolean.getBoolean(COMPACT_STATE_PROP)
        });

        disposeChildren = vals[0];
        compactChildren = vals[1];
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    protected BaseStaticGroup() {
        super("StaticGroup");

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};
        vfBboxDisplay = false;
        vfVisible = true;
        vfChildren = new ArrayList<>();

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseStaticGroup(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        VRMLBoundedNodeType b_node = (VRMLBoundedNodeType)node;

        float[] field = b_node.getBboxCenter();

        vfBboxCenter[0] = field[0];
        vfBboxCenter[1] = field[1];
        vfBboxCenter[2] = field[2];

        field = b_node.getBboxSize();

        vfBboxSize[0] = field[0];
        vfBboxSize[1] = field[1];
        vfBboxSize[2] = field[2];
        
        boolean b = b_node.getBboxDisplay();
        vfBboxDisplay = b;
        
        b = b_node.getVisible();
        vfVisible = b;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLBoundedNodeType interface.
    //-------------------------------------------------------------

    @Override
    public float[] getBboxCenter () {
        return vfBboxCenter;
    }

    @Override
    public float[] getBboxSize () {
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

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    @Override
    public boolean isShared() {
    	return true;
    }

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    @Override
    public void setShared(boolean used) {
    	//ignore
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNodeType class.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        super.setupFinished();

        int num_kids = vfChildren.size();
        VRMLNodeType kid;

        for(int i = 0; i < num_kids; i++) {
            kid = vfChildren.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
    }

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
        if(index < 0  || index > LAST_STATICGROUP_INDEX)
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
            case FIELD_CHILDREN:
                VRMLNodeType kids[] = new VRMLNodeType[vfChildren.size()];
                vfChildren.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValues = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
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

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.StaticNodeType;
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldFormatException, InvalidFieldValueException,
               InvalidFieldException {

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
            
            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    throw new InvalidFieldAccessException(NO_CHANGE_MSG);

                addChildNode(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    throw new InvalidFieldAccessException(NO_CHANGE_MSG);

                for(int i = 0; i < numValid; i++ )
                    addChildNode(children[i]);

                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------


    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box center to set
     */
    public void setBboxCenter(float[] val) throws InvalidFieldValueException {
        if(!inSetup)
            throw new InvalidFieldAccessException(
                "Cannot set bboxCenter after initialization");

        vfBboxCenter[0] = val[0];
        vfBboxCenter[1] = val[1];
        vfBboxCenter[2] = val[2];
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box size to set
     * @throws InvalidFieldValueException The bounds is not valid
     */
    public void setBboxSize(float[] val) throws InvalidFieldValueException {
        if(!inSetup)
            throw new InvalidFieldAccessException(
                "Cannot set bboxSize after initialization");

        FieldValidator.checkBBoxSize(getVRMLNodeName(),val);

        vfBboxSize[0] = val[0];
        vfBboxSize[1] = val[1];
        vfBboxSize[2] = val[2];
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

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        boolean new_bindable =
            ((node instanceof VRMLBindableNodeType) ||
             ((node instanceof VRMLGroupingNodeType) &&
              ((VRMLGroupingNodeType)node).containsBindableNodes()));

        if(new_bindable)
            throw new InvalidFieldValueException(USE_BIND_MSG);

        if(node instanceof VRMLGroupingNodeType) {
            ((VRMLBoundedNodeType)node).setShared(true);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            if(impl instanceof VRMLGroupingNodeType) {
                ((VRMLBoundedNodeType)impl).setShared(true);
            }
        }

        vfChildren.add(node);
    }
}
