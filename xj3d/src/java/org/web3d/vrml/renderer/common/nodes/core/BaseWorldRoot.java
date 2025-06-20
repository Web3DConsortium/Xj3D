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

package org.web3d.vrml.renderer.common.nodes.core;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.renderer.common.nodes.BaseMetadataObjectNode;


/**
 * Implementation of the world root class.
 * <p>
 *
 * The world root has 3 fields available to use:
 * <ul>
 * <li>children</li>
 * <li>bboxSize</li>
 * <li>bboxCenter</li>
 * <li>bboxDisplay</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public abstract class BaseWorldRoot extends AbstractNode
    implements VRMLWorldRootNodeType {

    /** Index of the url field */
    private static final int FIELD_CHILDREN = LAST_NODE_INDEX + 1;

    /** Index of the Bounding box center bboxCenter field */
    private static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 2;

    /** Index of the Bounding box size bboxSize field */
    private static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 3;

    /** Index of the Bounding box display bbDisplay field */
    private static final int FIELD_BBOX_DISPLAY = LAST_NODE_INDEX + 4;

    /** The last index in use by this class */
    private static final int LAST_WORLD_INDEX = FIELD_BBOX_DISPLAY;

    /** The total number of fields in use */
    private static final int NUM_FIELDS = LAST_WORLD_INDEX + 1;

    /** Message for when the proto is not a valid VRMLChildNodeType */
    private static final String NON_CHILD_PROTO_MSG =
        "Proto at the root of the scene graph is not a valid X3DChildNode or LayerSet node: ";

    /** Message for when the node in setValue() is not a valid VRMLChildNodeType */
    private static final String NON_CHILD_NODE_MSG =
        "Node at the root of the scene graph is not a valid X3DChildNode or LayerSet node: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** The current bounds set. An array of size 3. */
    private final float[] bboxSize;

    /** The current center of the box. An array of size 3. */
    private final float[] bboxCenter;

    /** The current value of bboxDisplay */
    private boolean bboxDisplay;

    /** The list of children to use */
    protected List<VRMLNodeType> vfChildren;

    /**
     * The number of children nodes we added to Common, not the total. We don't
     * Add nodes to Common that have no Java3D scene graph object (such as
     * interpolators and timesensors). This count is so that we don't have
     * to enable the ALLOW_CHILDREN_READ on the group (an optimisation step)
     */
    protected int childCount;

    /** The contained scene of this world */
    private BasicScene scene;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);


        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");

        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_DISPLAY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "bboxDisplay");

        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "children");

        fieldMap.put("bboxSize", FIELD_BBOX_SIZE);
        fieldMap.put("bboxCenter", FIELD_BBOX_CENTER);
        fieldMap.put("bboxDisplay", FIELD_BBOX_DISPLAY);

        Integer i = FIELD_CHILDREN;
        fieldMap.put("children", i);
        fieldMap.put("set_children", i);
        fieldMap.put("children_changed", i);
    }

    /**
     * Construct an instance of this node.
     */
    public BaseWorldRoot() {
        super("WorldRoot");

        childCount = 0;
        vfChildren = new ArrayList<>();
        bboxSize = new float[] { -1, -1, -1};
        bboxCenter = new float[3];
        bboxDisplay = false;

        hasChanged = new boolean[NUM_FIELDS];
    }

    //----------------------------------------------------------
    // Methods defined by WorldRootNodeType
    //----------------------------------------------------------

    /**
     * Set the scene that is contained by this node, which happens to be an
     * execution space. The scene instance must be non-null.
     *
     * @param scene The scene to use
     */
    @Override
    public void setContainedScene(BasicScene scene) {
        if(scene == null)
            throw new NullPointerException("Scene instance is null");

        this.scene = scene;
    }

    /**
     * Set the ID of this world root to be the initial (index 0) layer.
     */
    @Override
    public void setRootWorld() {
        layerIds = new int[1];

        vfChildren.stream().filter((node) -> (node != null)).forEachOrdered((node) -> {
            updateRefs(node, true);
        });
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //----------------------------------------------------------

    @Override
    public void setBboxCenter(float[] newBboxCenter) {

        // direct copies are faster than System.arraycopy for small amounts
        bboxCenter[0] = newBboxCenter[0];
        bboxCenter[1] = newBboxCenter[1];
        bboxCenter[2] = newBboxCenter[2];
    }

    @Override
    public float[] getBboxCenter() {
        return bboxCenter;
    }

    @Override
    public void setBboxSize(float[] newBboxSize) {
        // direct copies are faster than System.arraycopy for small amounts
        bboxSize[0] = newBboxSize[0];
        bboxSize[1] = newBboxSize[1];
        bboxSize[2] = newBboxSize[2];
    }

    @Override
    public float[] getBboxSize() {
        return bboxSize;
    }

    @Override
    public void setBboxDisplay(boolean newBboxDisplay) {
        bboxDisplay = newBboxDisplay;
    }

    @Override
    public boolean getBboxDisplay() {
        return bboxDisplay;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExecutionSpace
    //----------------------------------------------------------

    /**
     * Get the contained scene graph that this instance has. This represents
     * everything about the internal scene that the node declaration wraps.
     * This is a real-time representation so that if it the nodes contains a
     * script that changes the internal representation then this instance will
     * be updated to reflect and changes made.
     *
     * @return The scene contained by this node instance
     */
    @Override
    public BasicScene getContainedScene() {
        return scene;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLGroupingNodeType
    //-------------------------------------------------------------

    /**
     * Get the children, provides a live reference not a copy
     *
     * @return An array of VRMLNodeTypes
     */
    @Override
    public VRMLNodeType[] getChildren() {
        VRMLNodeType kids[] = new VRMLNodeType[vfChildren.size()];

        vfChildren.toArray(kids);

        return kids;
    }

    @Override
    public void setChildren(VRMLNodeType[] newChildren) {
        clearChildren();

        hasChanged[FIELD_CHILDREN] = true;

        if(newChildren == null)
            return;

        for (VRMLNodeType newChildren1 : newChildren) {
            addChildNode(newChildren1);
        }
    }

    /**
     * Append a new child node to the existing collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     *
     * @param newChild The new child
     */
    @Override
    public void addChild(VRMLNodeType newChild) {
        if(newChild != null)
            addChildNode(newChild);

        hasChanged[FIELD_CHILDREN] = true;
    }

    /**
     * Remove an existing child node from the collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     * If it is not registered, silently ignores the request.
     *
     * @param oldChild The child to remove
     */
    @Override
    public void removeChild(VRMLNodeType oldChild) {
        if(oldChild == null)
            return;

        removeChildNode(oldChild);
        hasChanged[FIELD_CHILDREN] = true;
    }

    @Override
    public void setChildren(VRMLNodeType newChild) {
        clearChildren();

        if(newChild != null)
            addChildNode(newChild);

        hasChanged[FIELD_CHILDREN] = true;
    }

    /**
     * Returns the number of children
     *
     * @return The number of children
     */
    @Override
    public int getChildrenSize() {
        return vfChildren.size();
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
        if(index < 0  || index > LAST_WORLD_INDEX)
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
        return TypeConstants.WorldRootNodeType;
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
            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValues = bboxSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.clear();
                fieldData.floatArrayValues = bboxCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_DISPLAY:
                fieldData.clear();
                fieldData.booleanValue = bboxDisplay;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_CHILDREN:
                fieldData.clear();

                VRMLNodeType[] kids = new VRMLNodeType[vfChildren.size()];
                vfChildren.toArray(kids);

                fieldData.nodeArrayValues = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = childCount;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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

        childCount = vfChildren.size();
        VRMLNodeType kid;

        for(int i = 0; i < childCount; i++) {
            kid = vfChildren.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
    }

    /**
     * Change the reference count up or down by one for a given layer ID.
     * If there is no reference to the given layer ID previously, add one
     * now. A listing of the layer IDs that reference this node can be
     * retrieved through {@link #getLayerIds()}.
     *
     * @param layer The id of the layer to modify the ref count on
     * @param add true to increment the reference count, false to decrement
     */
    @Override
    public void updateRefCount(int layer, boolean add) {
        super.updateRefCount(layer, add);

        if(layerIds == null)
            return;

        int num_kids = vfChildren.size();

        for(int i = 0; i < num_kids; i++) {
            Object kid = vfChildren.get(i);

            if(kid != null)
                updateRefs((VRMLNodeType)kid, add);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BBOX_CENTER:
               if(!inSetup)
                   throwInitOnlyWriteException("bboxCenter");

                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
               if(!inSetup)
                   throwInitOnlyWriteException("bboxSize");

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
     * @throws InvalidFieldException The index does not match a known field
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        VRMLNodeType node = child;
        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    clearChildren();

                if(child != null)
                    addChildNode(node);

                hasChanged[FIELD_CHILDREN] = true;
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
     * @param children The new nodes to use for the children
     * @throws InvalidFieldException The index does not match a known field
     */
    @Override
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_CHILDREN:
                if(!inSetup)
                    clearChildren();

                // Only add non-null nodes here. This is different to the
                // normal grouping node where a null is a significant item
                // in a child list. For the world root, there is no such
                // value.
                for(int i = 0; i < numValid; i++ )
                    if(children[i] != null)
                        addChildNode(children[i]);

                hasChanged[FIELD_CHILDREN] = true;
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearChildren() {
        int num_kids = vfChildren.size();

        for(int i = 0; i < num_kids; i++) {
            VRMLNodeType n = vfChildren.get(i);

            if(n instanceof VRMLBoundedNodeType)
                ((VRMLBoundedNodeType)n).setShared(false);

            updateRefs(n, false);
            stateManager.registerRemovedNode(n);
        }


        childCount = 0;
        vfChildren.clear();
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     */
    protected void addChildNode(VRMLNodeType node) {

        if (node != null) {
            if (node instanceof VRMLBoundedNodeType) {
                ((VRMLBoundedNodeType) node).setShared(true);

            } else if (node instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                        ((VRMLProtoInstance) node).getImplementationNode();

                // Walk down the proto impl looking for the real node to check it
                // is the right type.
                while ((impl != null) && (impl instanceof VRMLProtoInstance)) {
                    impl = ((VRMLProtoInstance) impl).getImplementationNode();
                }

                if ((impl != null) && !(impl instanceof VRMLWorldRootChildNodeType)) {
                    throw new InvalidFieldValueException(NON_CHILD_PROTO_MSG
                            + node.getVRMLNodeName());
                }

            } else if (!(node instanceof VRMLWorldRootChildNodeType) &&
					   !(node instanceof BaseMetadataObjectNode)) { // TODO what about Layout
				// test scene http://x3dGraphics.com/examples/X3dForAdvancedModeling/AdditiveManufacturing/MetadataRootNodeTest.x3d
                throw new InvalidFieldValueException(NON_CHILD_NODE_MSG
                        + node.getVRMLNodeName());
            }

            updateRefs(node, true);

            vfChildren.add(node);
            childCount++;

            if (!inSetup) {
                stateManager.registerAddedNode(node);
            }
        }
    }

    /**
     * Add a single child node to the list of available children. Override to
     * provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     */
    protected void removeChildNode(VRMLNodeType node) {

        if (node != null) {

            updateRefs(node, false);

            if (node instanceof VRMLBoundedNodeType) {
                ((VRMLBoundedNodeType) node).setShared(false);
            }
            vfChildren.remove(node);
            childCount--;

            if (!inSetup) {
                if (stateManager == null) {
                    System.out.println("StateManager null on removeChild");
                } else {
                    stateManager.registerRemovedNode(node);
                }
            }
        }
    }
}
