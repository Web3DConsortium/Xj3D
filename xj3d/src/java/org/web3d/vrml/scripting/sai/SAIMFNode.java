/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;

// Local imports
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a MFNode field.
 *  <p>
 * Get the values of a node array. The java <code>null</code> reference is
 * treated to be equivalent to the VRML <code>NULL</code> field values.
 *  <p>
 * It is not illegal to construct an array where some members of the array are
 * null pointers. Due to no specification on the intended result in the VRML
 * specification, the response given by the browser is implementation
 * dependent. Calls will not generate an exception, but the value of actual
 * event received from the scene graph may vary until the issue is resolved.
 *
 * @version 1.0 30 April 1998
 */
class SAIMFNode extends BaseMField
    implements MFNode, NodeField {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 8;

    /** Local array of data */
    private VRMLNodeType[] localValue;

    /** The SAI wrapper for the node */
    private BaseNode[] saiNodes;

    /** Factory used for field generation */
    private FieldFactory fieldFactory;

    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;

    /** The BaseNode factory */
    private BaseNodeFactory baseNodeFactory;
    
    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFNode(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFNode
    //----------------------------------------------------------

    /**
     * Removes all values in the field and changes the field size to zero.
     */
    @Override
    public void clear() {
        // Overrides the base to make sure we also just go through and clear
        // the arrays so that extra node refs are not held
        super.clear();

        for(int i = 0; i < numElements; i++) {
            localValue[i] = null;
            saiNodes[i] = null;
        }
    }

    /** Places a new value at the end of the existing value, increasing
     *  the field length accordingly.
     *
     * @param value The value to append
     */
    @Override
    public void append(X3DNode value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements)) {
            VRMLNodeType[] tmp1 = new VRMLNodeType[numElements + ARRAY_INC];
            BaseNode[] tmp2 = new BaseNode[numElements + ARRAY_INC];

            System.arraycopy(localValue, 0, tmp1, 0, numElements);
            System.arraycopy(saiNodes, 0, tmp2, 0, numElements);

            localValue = tmp1;
            saiNodes = tmp2;

        }

        if((value != null) && !value.isRealized())
            value.realize();

        saiNodes[numElements] = (BaseNode)value;
        localValue[numElements] =
            (value != null) ? ((BaseNode)value).getImplNode() : null;

        numElements++;
        dataChanged = true;
    }

    /**
     * Inserts a value into an existing index of the field.  Current field values
     * from the index to the end of the field are shifted down and the field
     * length is increased by one to accommodate the new element.
     *
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     *
     * @param index The position at which to insert
     * @param value The new element to insert
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    @Override
    public void insertValue(int index, X3DNode value)
        throws ArrayIndexOutOfBoundsException {
        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements)) {
            VRMLNodeType[] tmp1 = new VRMLNodeType[numElements + ARRAY_INC];
            BaseNode[] tmp2 = new BaseNode[numElements + ARRAY_INC];

            System.arraycopy(localValue, 0, tmp1, 0, numElements);
            System.arraycopy(saiNodes, 0, tmp2, 0, numElements);

            localValue = tmp1;
            saiNodes = tmp2;
        }

        System.arraycopy(localValue,
                         index,
                         localValue,
                         index + 1,
                         numElements - index);

        System.arraycopy(saiNodes,
                         index,
                         saiNodes,
                         index + 1,
                         numElements - index);


        if((value != null) && !value.isRealized())
            value.realize();

        saiNodes[index] = (BaseNode)value;
        localValue[index] =
            (value != null) ? ((BaseNode)value).getImplNode() : null;

        numElements++;

        dataChanged = true;
    }

    /**
     * Removes one value from the field.  Values at indices above the
     * removed element will be shifted down by one and the size of the
     * field will be reduced by one.
     *
     * @param index The position of the value to remove.
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    @Override
    public void removeValue(int index)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(REMOVE_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);
            
        if (index == numElements - 1) {
            
            localValue[index] = null;
            saiNodes[index] = null;
            
        } else {
            
            System.arraycopy(localValue,
                    index + 1,
                    localValue,
                    index,
                    numElements - index + 1);

            System.arraycopy(saiNodes,
                    index + 1,
                    saiNodes,
                    index,
                    numElements - index + 1);
            
        }        

        numElements--;
        dataChanged = true;
    }

    /**
     * Write the value of the array of the nodes to the given array. Individual
     * elements in the array may be null depending on the implementation
     * of the browser and whether it maintains null references.
     *
     * @param nodes The node array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    @Override
    public void getValue(X3DNode[] nodes) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(saiNodes, 0, nodes, 0, numElements);
    }

    /**
     * Get a particular node value in the given eventOut array.
     *  <p>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the Node value is
     * NULL because the browser implementation keeps null references then this
     * method will return a null pointer without throwing any exception.
     *  <p>
     * @param index The position to read the values from
     * @return The node reference
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    @Override
    public X3DNode get1Value(int index) {
        checkAccess(false);

        return saiNodes[index];
    }

    /**
     * Set the value of the array of nodes. Input is an array of valid Node
     * references. If the length is zero or the node reference is null, then
     * the actions to take are according to the class introduction above. If the
     * array contains a null reference then th resulting event passed to the
     * eventIn is implementation dependent
     *  <p>
     * If any of the node references have had their dispose methods called, an
     * InvalidNodeException will be generated and no event sent to the
     * scene graph.
     *
     * @param node The number of nodes to copy from this array
     * @param value The array of node references
     * @exception InvalidNodeException At least one node has been "disposed" of
     */
    @Override
    public void setValue(int size, X3DNode[] value) {

        checkAccess(true);

        if((localValue == null) || (localValue.length < size)) {
            localValue = new VRMLNodeType[size];
            saiNodes = new BaseNode[size];
        }

        numElements = size;
        System.arraycopy(value, 0, saiNodes, 0, size);

        for(int i = 0; i < size; i++) {
            if((value[i] != null) && !value[i].isRealized()) {
                value[i].realize();
                ((BaseNode)value[i]).setAccessValid(accessPermitted);
            }

            localValue[i] =
                (value[i] != null) ? ((BaseNode)value[i]).getImplNode() : null;
        }

        dataChanged = true;
    }

    /**
     * Set a particular node value in the given eventIn array. To the VRML
     * world this will generate a full MFNode event with the nominated index
     * value changed.
     *  <p>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the Node value is
     * null the behaviour will be undefined as far as generating an event to the
     * scene graph is concerned in order to be consistent with the behaviour
     * described in the class introduction. This method call will not generate
     * an exception if the node reference is null.
     *  <p>
     * If the node reference passed to this method has already had the dispose
     * method called then an InvalidNodeException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The node reference
     *
     * @exception InvalidNodeException The node has been "disposed" of
     * @exception ArrayIndexOutOfBoundsException The index was out of bounds of the
     *     array currently.
     */
    @Override
    public void set1Value(int index, X3DNode value) {

        checkAccess(true);

        if((value != null) && !value.isRealized())
            value.realize();

        saiNodes[index] = (BaseNode)value;
        localValue[index] =
            (value != null) ? ((BaseNode)value).getImplNode() : null;

        dataChanged = true;
    }

    //----------------------------------------------------------
    // Methods defined by NodeField
    //----------------------------------------------------------

    /**
     * A recursive check if this node field or any of the children nodes have
     * changed. Used when directOutput is true on the script node and we
     * need to check if the children have updated, regardless of whether
     * we have. If anything has changed, update it during this call.
     */
    @Override
    public void updateNodeAndChildren() {

        for(int i = 0; i < numElements; i++) {
            if(saiNodes[i] != null)
                saiNodes[i].updateNodeAndChildren();
        }

        if(dataChanged) {
            node.setValue(fieldIndex, localValue, numElements);
            dataChanged = false;
        }
    }

    /**
     * A recursive check if the underlying node has changed any values from
     * it's current wrapper values and update the wrapper with the latest.
     * Used when directOutput is true on the script node and we
     * need to check if the children have updated, regardless of whether
     * we have. If anything has changed, update it during this call.
     */
    @Override
    public void updateFieldAndChildren() {

        updateField();

        for(int i = 0; i < numElements; i++) {
            if(saiNodes[i] != null)
                saiNodes[i].updateFields();
        }
    }

    /**
     * Chained method to control whether operations are valid on the fields
     * of this node instance right now. Works on only the fields that
     * still have valid internal references.
     *
     * @param valid True if access operations are now permitted.
     */
    @Override
    public void setAccessValid(boolean valid) {
        super.setAccessValid(valid);

        for(int i = 0; i < numElements; i++)
            if(saiNodes[i] != null)
                saiNodes[i].setAccessValid(valid);
    }

    /**
     * Set the field factory used to create field instances for the
     * X3DNode implementation.
     *
     * @param fac The factory to use for the field generation
     */
    @Override
    public void setFieldFactory(FieldFactory fac) {
        fieldFactory = fac;
    }

    /**
     * Set the reference queue used for managing fields.
     *
     * @param queue The queue to use for each field
     */
    @Override
    public void setFieldReferenceQueue(ReferenceQueue queue) {
        fieldQueue = queue;
    }
    
    /**
     * Set the node factory used to create node wrapper instances
     *
     * @param fac The factory to use for node wrapper generation
     */
    @Override
    public void setNodeFactory(BaseNodeFactory fac) {
        baseNodeFactory = fac;
    }

    //----------------------------------------------------------
    // Methods defined by BaseField
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    @Override
    void updateNode() {
        node.setValue(fieldIndex, localValue, numElements);
        dataChanged = false;
    }

    /**
     * Notification to the field to update its field values from the
     * underlying node.
     */
    @Override
    void updateField() {
        if(!isReadable())
            return;

        VRMLFieldData data = node.getFieldValue(fieldIndex);

        // Check to see if anything has changed
        if(numElements != data.numElements) {

            if(data.numElements == 0) {
                for(int i = 0; i < data.numElements; i++)
                    saiNodes[i] = null;
            } else {

                if(data.numElements < numElements) {
                    for(int i = 0; i < data.numElements; i++) {
                        if(data.nodeArrayValues[i] == localValue[i])
                            continue;

                        localValue[i] = data.nodeArrayValues[i];

                        if(localValue[i] != null) {
                            //saiNodes[i] = new BaseNode(localValue[i],
                            //                           fieldQueue,
                            //                           fieldFactory,
                            //                           fieldAccessListener);
                            saiNodes[i] = (BaseNode)baseNodeFactory.getBaseNode( localValue[i] );
                            saiNodes[i].setAccessValid(accessPermitted);
                        } else {
                            saiNodes[i] = null;
                        }
                    }

                    for(int i = data.numElements; i < numElements; i++) {
                        localValue[i] = null;
                        saiNodes[i] = null;
                    }
                } else {
                    if((localValue == null) ||
                       (localValue.length < data.numElements)) {
                        VRMLNodeType[] tmp1 = new VRMLNodeType[data.numElements];
                        BaseNode[] tmp2 = new BaseNode[data.numElements];

                        if(localValue != null) {
                            System.arraycopy(localValue, 0, tmp1, 0, numElements);
                            System.arraycopy(saiNodes, 0, tmp2, 0, numElements);
                        }

                        localValue = tmp1;
                        saiNodes = tmp2;
                    }

                    for(int i = 0; i < data.numElements; i++) {
                        if(data.nodeArrayValues[i] == localValue[i])
                            continue;

                        localValue[i] = data.nodeArrayValues[i];

                        if(localValue[i] != null) {
                            //saiNodes[i] = new BaseNode(localValue[i],
                            //                           fieldQueue,
                            //                           fieldFactory,
                            //                           fieldAccessListener);
                            saiNodes[i] = (BaseNode)baseNodeFactory.getBaseNode( localValue[i] );
                            saiNodes[i].setAccessValid(accessPermitted);
                        } else {
                            saiNodes[i] = null;
                        }
                    }
                }
            }

            numElements = data.numElements;
        } else {
            for(int i = 0; i < numElements; i++) {
                if(data.nodeArrayValues[i] == localValue[i])
                    continue;

                localValue[i] = data.nodeArrayValues[i];

                if(localValue[i] != null) {
                    //saiNodes[i] = new BaseNode(localValue[i],
                    //                           fieldQueue,
                    //                           fieldFactory,
                    //                           fieldAccessListener);
                    saiNodes[i] = (BaseNode)baseNodeFactory.getBaseNode( localValue[i] );
                    saiNodes[i].setAccessValid(accessPermitted);
                } else {
                    saiNodes[i] = null;
                }

            }
        }

        dataChanged = false;
    }
}
