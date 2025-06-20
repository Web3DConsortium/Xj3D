package org.web3d.vrml.scripting.external.sai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.x3d.sai.*;
import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

/**
 * Representation of a MFVec2f field.
 * Implemented by doing a search/replace of MFVec2d.
 *
 */
class MFVec2fWrapper extends BaseFieldWrapper
    implements MFVec2f, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value to be sent to the rendering system iff storedInput */
    private float[] storedInputValue;

    /** The value to be sent to the rendering system iff storedOutput */
    private float[] storedOutputValue;

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    MFVec2fWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory) {
        super(node,field,aQueue,factory);
    }

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
     */
    MFVec2fWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory, boolean isInput) {
            this(node,field,aQueue,factory);
            if (isInput)
                loadInputValue();
            else
                loadOutputValue();
        }

    /**
     * @see org.web3d.x3d.sai.MFVec2f#append(float[])
     */
    @Override
    public void append(float[] value) {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFVec2fWrapper queuedElement=
                (MFVec2fWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFVec2fWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+2);
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value[0];
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value[1];
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.x3d.sai.MFColorRGBA#clear()
     */
    @Override
    public void clear() {
        setValue(0,new float[0]);
    }

    /** Post any queued field values to the target field */
    @Override
    public void doEvent() {
        try {
            theNode.setValue(fieldIndex,storedInputValue,storedInputLength);
        } finally {
            isSetOneValue=false;
            storedInput=false;
        }
    }

    /** Ensures that there is atleast a certain number of elements
     *  in the storedInputValue array.
     * @param newSize The size to ensure.
     */
    protected void ensureArraySize(int newSize) {
        if (storedInputValue!=null && newSize<storedInputValue.length)
            return;
        float newArray[]=new float[newSize];
        if (storedInputValue!=null)
            System.arraycopy(storedInputValue,0,newArray,0,storedInputLength);
        storedInputValue=newArray;
    }

    /**
     * Get the value of a particular vector value in the event out array.
     *
     * @param index The position to get the vectory value from.
     * @param vec The array to place the value in where.
     *    vec[0] = X <br>
     *    vec[1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small or
     *     the index was outside the current data array bounds.
     */
    @Override
    public void get1Value(int index, float[] vec) {
        if (storedOutput) {
            vec[0]=storedOutputValue[index];
            vec[1]=storedOutputValue[index+1];
        } else {
            checkReadAccess();
            VRMLFieldData value=theNode.getFieldValue(fieldIndex);
            // floatArrayValues may be null if numElements == 0
            if (index<0 || index>=value.numElements)
                throw new ArrayIndexOutOfBoundsException();
            else {
                vec[0]=value.floatArrayValues[index];
                vec[1]=value.floatArrayValues[index+1];
            }
        }
    }

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 3 times the size of the array.
     *
     * @param vec The array to be filled in where the
     *   vec[i + 0] = X <br>
     *   vec[i + 1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    @Override
    public void getValue(float[] vec) {
        if (storedOutput)
            System.arraycopy(storedOutputValue,0,vec,0,storedOutputValue.length);
        else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.numElements!=0)
                System.arraycopy(data.floatArrayValues,0,vec,0,data.numElements*2);
        }
    }

    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where <br>
     *    vec[i][0] = X <br>
     *    vec[i][1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    @Override
    public void getValue(float[][] vec) {
        if (storedOutput)
            ArrayUtils.raise2(storedOutputValue,storedOutputValue.length/2,vec);
        else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            ArrayUtils.raise2(data.floatArrayValues,data.numElements,vec);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#initialize(org.web3d.vrml.nodes.VRMLNodeType, int)
     */
    @Override
    public void initialize(VRMLNodeType srcNode, int fieldNumber) {
        theNode=srcNode;
        fieldIndex=fieldNumber;
    }

    /**
     * @see org.web3d.x3d.sai.MFVec2f#insertValue(int, float[])
     */
    @Override
    public void insertValue(int index, float[] value) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFVec2fWrapper queuedElement=
                (MFVec2fWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFVec2fWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+2);
            System.arraycopy(queuedElement.storedInputValue,index*2,queuedElement.storedInputValue,index*2+2,queuedElement.storedInputLength-index*2);
            queuedElement.storedInputValue[index*2]=value[0];
            queuedElement.storedInputValue[index*2+1]=value[1];
            queuedElement.storedInputLength+=2;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
     */
    @Override
    public boolean isConglomerating() {
        return isSetOneValue;
    }

    /** Load the current field value from the underlying node and store it as the input value.
     *
     */
    private void loadInputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedInputValue == null || storedInputValue.length!=value.numElements)
            storedInputValue=new float[value.numElements*2];
        if (value.numElements!=0)
            System.arraycopy(value.floatArrayValues,0,storedInputValue,0,value.numElements*2);
        storedInput=true;
        storedInputLength=storedInputValue.length;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    @Override
    public void loadOutputValue() {
        if(!isWritable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedOutputValue == null || storedOutputValue.length!=value.numElements)
            storedOutputValue=new float[value.numElements*2];
        if (value.numElements!=0)
            System.arraycopy(value.floatArrayValues,0,storedOutputValue,0,value.numElements*2);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFVec2f#removeValue(int)
     */
    @Override
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFVec2fWrapper queuedElement=
                (MFVec2fWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFVec2fWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            if (queuedElement.storedInputLength>0) {
                if (index*2+2<queuedElement.storedInputLength)
                    System.arraycopy(queuedElement.storedInputValue,
                    		index*2+2,
                    		queuedElement.storedInputValue,
                    		index*2,
                    		queuedElement.storedInputLength-index*2-2);
                queuedElement.storedInputLength-=2;
                if (newEvent)
                    theEventQueue.processEvent(queuedElement);
            } else {
                // Free up the buffer before throwing the exception
                if (newEvent)
                    queuedElement.isSetOneValue=false;
                throw new ArrayIndexOutOfBoundsException();
            }
        }

    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#reset()
     */
    @Override
    public void reset() {
        theNode=null;
        fieldIndex=-1;
        storedOutput=false;
    }

    /**
     * Set a particular vector value in the given eventIn array. To the VRML
     * world this will generate a full MFVec2f event with the nominated index
     * value changed.
     *  <p>
     * The value array must contain at least two elements. If the array
     * contains more than 2 values only the first 2 values will be used and
     * the rest ignored.
     *  <p>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 2 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the vector value
     * @param value The array of vector values where <br>
     *    value[0] = X <br>
     *    value[1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least 2
     *    values for the vector
     */
    @Override
    public void set1Value(int index, float[] value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
             MFVec2fWrapper queuedElement=
                (MFVec2fWrapper) theEventQueue.getLast(this);
             boolean newEvent=false;
             if (queuedElement==null || !queuedElement.isSetOneValue) {
                    // Input and output buffers do not mix
                    if (!storedInput && !storedOutput) {
                        queuedElement=this;
                        loadInputValue();
                        isSetOneValue=true;
                    } else {
                        queuedElement=new MFVec2fWrapper(
                            theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                        );
                        queuedElement.isSetOneValue=true;
                    }
                    newEvent=true;
            }
            queuedElement.storedInputValue[index*2]=value[0];
            queuedElement.storedInputValue[index*2+1]=value[1];
            if (newEvent)
                    theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the value of the array of 2D vectors. Input is an array of floats
     * If value[i] does not contain at least two values it will generate an
     * ArrayIndexOutOfBoundsException. If value[i] contains more than two items
     * only the first two values will be used and the rest ignored.
     *  <p>
     * If one or more of the values for value[i] are null then the resulting
     * event that is sent to the VRML scene graph is implementation dependent but
     * no error indicator will be set here.
     *
     * @param numVecs The number of items to copy from the array
     * @param value The array of vec2f values where <br>
     *    value[i] = X <br>
     *    value[i+1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least two
     *    values for the vector definition.
     */
    @Override
    public void setValue(int numVecs, float[] value) {
        checkWriteAccess();
        MFVec2fWrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls
        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement=new MFVec2fWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=numVecs*2)
            queuedElement.storedInputValue=new float[numVecs*2];
        System.arraycopy(value,0,queuedElement.storedInputValue,0,numVecs*2);
        queuedElement.storedInputLength=numVecs*2;
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Set the value of the array of 2D vectors. Input is an array of floats
     * If value[i] does not contain at least two values it will generate an
     * ArrayIndexOutOfBoundsException. If value[i] contains more than two items
     * only the first two values will be used and the rest ignored.
     *  <p>
     * If one or more of the values for value[i] are null then the resulting
     * event that is sent to the VRML scene graph is implementation dependent but
     * no error indicator will be set here.
     *
     * @param numVecs The number of items to copy from the array
     * @param value The array of vec2f values where <br>
     *    value[i][0] = X <br>
     *    value[i][1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least two
     *    values for the vector definition.
     */
    @Override
    public void setValue(int numVecs, float[][] value) {
        checkWriteAccess();
        MFVec2fWrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls
        if (isSetOneValue || storedInput || storedOutput)
            queuedElement=new MFVec2fWrapper(theNode,fieldIndex,theEventQueue,theEventAdapterFactory);
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=numVecs*2)
            queuedElement.storedInputValue=new float[numVecs*2];
        ArrayUtils.flatten2(value,numVecs,queuedElement.storedInputValue);
        queuedElement.storedInputLength=numVecs*2;
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Get the size of the underlying data array. The size is the number of
     * elements for that data type. So for an MFFloat the size would be the
     * number of float values, but for an MFVec3f, it is the number of vectors
     * in the returned array (where a vector is 3 consecutive array indexes in
     * a flat array).
     *
     * @return The number of elements in this field
     */
    @Override
    public int getSize() {
        if (storedOutput) {
            return storedOutputValue.length/2;
        } else {
            return theNode.getFieldValue(fieldIndex).numElements;
        }
    }
}
