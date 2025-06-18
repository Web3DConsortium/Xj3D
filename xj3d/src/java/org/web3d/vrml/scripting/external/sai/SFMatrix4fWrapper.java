package org.web3d.vrml.scripting.external.sai;

/**
 * ***************************************************************************
 * Copyright North Dakota State University, 2001 Written By Bradley Vender
 * (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1 Please read
 * http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any purpose.
 * Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************
 */
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer;
import org.web3d.x3d.sai.Matrix4;
import org.web3d.x3d.sai.SFMatrix4f;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFVec3f;

/**
 * Representation of a SFRotation field.
 * <P>
 * Rotation values are specified according to the VRML IS Specification Section
 * 5.8 SFRotation and MFRotation.
 *
 */
class SFMatrix4fWrapper extends BaseFieldWrapper implements SFMatrix4f,
        ExternalEvent, ExternalOutputBuffer {

    /**
     * Default field value for null field arrays
     */
    static final float DEFAULT_FIELD_VALUE[] = new float[]{1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f};

    /**
     * The value stored in this buffer iff storedInput
     */
    float storedInputValue[];

    /**
     * The value stored in this buffer iff storedOutput
     */
    float storedOutputValue[];

    /**
     * Basic constructor for wrappers without preloaded values
     *
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    SFMatrix4fWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
            SAIEventAdapterFactory factory) {
        super(node, field, aQueue, factory);
        storedInputValue = new float[16];
        storedOutputValue = new float[16];
    }

    /**
     * Constructor to use when a value needs to be preloaded
     *
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load
     * into storedOutputValue
     */
    SFMatrix4fWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
            SAIEventAdapterFactory factory, boolean isInput) {
        this(node, field, aQueue, factory);
        if (isInput) {
            loadInputValue();
        } else {
            loadOutputValue();
        }
    }

    /**
     * Post any queued field values to the target field
     */
    @Override
    public void doEvent() {
        try {
            theNode.setValue(fieldIndex, storedInputValue, 16);
        } finally {
            storedInput = false;
        }
    }

    /**
     * Write the rotation value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where<BR>
     * value[0] = X component [0-1] <BR>
     * value[1] = Y component [0-1] <BR>
     * value[2] = Z component [0-1] <BR>
     * value[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too
     * small
     */
    public void getValue(float[] vec) {
        if (storedOutput) {
            System.arraycopy(storedOutputValue, 0, vec, 0, 16);
        } else {
            checkReadAccess();
            VRMLFieldData data = theNode.getFieldValue(fieldIndex);
            if (data.floatArrayValues == null) {
                System.arraycopy(DEFAULT_FIELD_VALUE, 0, vec, 0, 16);
            } else {
                System.arraycopy(data.floatArrayValues, 0, vec, 0, 16);
            }
        }
    }

    @Override
    public void initialize(VRMLNodeType srcNode, int fieldNumber) {
        theNode = srcNode;
        fieldIndex = fieldNumber;
    }

    @Override
    public boolean isConglomerating() {
        return false;
    }

    /**
     * Load the current field value from the underlying node and store it as the
     * input value.
     *
     */
    private void loadInputValue() {
        VRMLFieldData value = theNode.getFieldValue(fieldIndex);
        if (value.floatArrayValues == null) {
            System.arraycopy(DEFAULT_FIELD_VALUE, 0, storedInputValue, 0, 16);
        } else {
            System.arraycopy(value.floatArrayValues, 0, storedInputValue, 0, 16);
        }
        storedInput = true;
    }

    @Override
    public void loadOutputValue() {
        VRMLFieldData value = theNode.getFieldValue(fieldIndex);
        if (value.floatArrayValues == null) {
            System.arraycopy(DEFAULT_FIELD_VALUE, 0, storedOutputValue, 0, 16);
        } else {
            System.arraycopy(value.floatArrayValues, 0, storedOutputValue, 0, 16);
        }
        storedOutput = true;
    }

    @Override
    public void reset() {
        theNode = null;
        fieldIndex = -1;
        storedOutput = false;
    }

    @Override
    public void setFromArray(float[] value) {
        checkWriteAccess();
        SFMatrix4fWrapper queuedElement = this;
        // Input and output buffers do not mix
        if (storedInput || storedOutput) {
            queuedElement = new SFMatrix4fWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        System.arraycopy(value, 0, queuedElement.storedInputValue, 0, 16);
        theEventQueue.processEvent(queuedElement);
    }

    @Override
    public void getTransform(SFVec3f transform, SFRotation rotation, SFVec3f scale) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public Matrix4 multiplyColVector(SFVec3f vec3f) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void set(int row, int column) {
        throw new IllegalArgumentException("Not implemented");

    }

    @Override
    public Matrix4 multiplyRowVector(SFVec3f vec3f) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public float get(int row, int column) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public void setTransform(SFVec3f transform, SFRotation rotation, SFVec3f scale, SFRotation scaleOrientation, SFVec3f center) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public Matrix4 inverse() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public Matrix4 transpose() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public Matrix4 multiplyLeft(Matrix4 mat) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public Matrix4 multiplyRight(Matrix4 mat) {
        throw new IllegalArgumentException("Not implemented");
    }
}
