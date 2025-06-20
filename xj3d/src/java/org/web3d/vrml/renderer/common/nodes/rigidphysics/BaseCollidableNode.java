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
import javax.vecmath.Matrix4f;

import org.odejava.PlaceableGeom;

import org.openmali.vecmath2.AxisAngle3f;
import org.openmali.vecmath2.Vector3f;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Base implementation of the X3DCollidableNode abstract type.
 * <p>
 *
 * The base class provides most of the basic functionality, including
 * interacting with the ODE portions. However, for returning the scene graph
 * object appropriate for the renderer will require the derived class to take
 * care of it.
 * <p>
 *
 * The basic (X3D) definition of X3DNBodyCollidableNode is:
 * <pre>
 * X3DNBodyCollidableNode {
 *   SFNode     [in,out] metadata    NULL     [X3DMetadataObject]
 *   SFBool     [in,out] enabled     TRUE
 *   SFVec3f    [in,out] position    0 0 0    (-&#8734;,&#8734;)
 *   SFRotation [in,out] rotation    0 0 1 0  [0,1]
 *   SFVec3f    []       bboxCenter  0 0 0    (-&#8734;,&#8734;)
 *   SFVec3f    []       bboxSize    -1 -1 -1 [0,&#8734;) or -1 -1 -1
 *   SFBool  [in,out] bboxDisplay    FALSE
 *   SFBool  [in,out] visible        TRUE
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class BaseCollidableNode extends AbstractNode
    implements VRMLNBodyCollidableNodeType {

    // Field index constants

    /** The field index for bboxSize */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 1;

    /** The field index for bboxCenter */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 2;
    
    /** The field index for bboxDisplay */
    protected static final int FIELD_BBOX_DISPLAY = LAST_NODE_INDEX + 3;
    
    /** The field index for visible */
    protected static final int FIELD_VISIBLE = LAST_NODE_INDEX + 4;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 5;

    /** The field index for rotation */
    protected static final int FIELD_ROTATION = LAST_NODE_INDEX + 6;

    /** The field index for translation */
    protected static final int FIELD_TRANSLATION = LAST_NODE_INDEX + 7;

    /** Last index used by this base node */
    protected static final int LAST_COLLIDABLE_INDEX = FIELD_TRANSLATION;

    // The VRML field values

    /** field SFVec3f bboxCenter 0, 0, 0 */
    protected float[] vfBboxCenter;

    /** field SFVec3f bboxSize [-1, -1, -1] */
    protected float[] vfBboxSize;
    
    /** field SFBool bboxDisplay false */
    protected boolean vfBboxDisplay;
    
    /** field SFBool visible true */
    protected boolean vfVisible;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of translation field */
    protected float[] vfTranslation;

    /** The value of the rotation field */
    protected float[] vfRotation;

    // Other vars

    /** The ODE shape of the geometry */
    protected PlaceableGeom odeGeom;

    /** Matrix representing the last calculated position */
    protected Matrix4f tmatrix;

    /** AxisAngle for setting this in the matrix */
    private AxisAngle3f angleTmp;

    /** A vector for fetching the position from ODE */
    private Vector3f positionTmp;

    /**
     * Construct a new generalised joint node object.
     *
     * @param name The VRML name of this node
     */
    public BaseCollidableNode(String name) {
        super(name);

        vfTranslation = new float[3];
        vfRotation = new float[] { 0, 0, 1, 0 };

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};
        vfBboxDisplay = false;
        vfVisible = true;
        vfEnabled = true;

        tmatrix = new Matrix4f();
        tmatrix.setIdentity();
        angleTmp = new AxisAngle3f();
        positionTmp = new Vector3f();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected void copy(VRMLNBodyCollidableNodeType node) {

        float[] trans = node.getTranslation();

        vfTranslation[0] = trans[0];
        vfTranslation[1] = trans[1];
        vfTranslation[2] = trans[2];

        float[] rot = node.getRotation();

        vfRotation[0] = rot[0];
        vfRotation[1] = rot[1];
        vfRotation[2] = rot[2];
        vfRotation[3] = rot[3];

        float[] field = node.getBboxCenter();

        vfBboxCenter[0] = field[0];
        vfBboxCenter[1] = field[1];
        vfBboxCenter[2] = field[2];

        field = node.getBboxSize();

        vfBboxSize[0] = field[0];
        vfBboxSize[1] = field[1];
        vfBboxSize[2] = field[2];
        
        boolean b = node.getBboxDisplay();
        vfBboxDisplay = b;
        b = node.getVisible();
        vfVisible = b;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNBodyCollidableNodeType
    //-------------------------------------------------------------

    /**
     * Get the ODE shape of the geometry node.
     *
     * @return The PlaceableGeom instance for this collidable
     */
    @Override
    public PlaceableGeom getODEGeometry() {
        return odeGeom;
    }

    /**
     * ODE computation has finished, so go update the field values and the
     * rendering API structures with the final computed values.
     */
    @Override
    public void updateFromODE() {
        if(!vfEnabled)
            return;

        odeGeom.getPosition(positionTmp);
        odeGeom.getAxisAngle(angleTmp);

        // Transpose from org.openmali.vecmath2 to javax.vecmath for aviatrix3d
        javax.vecmath.AxisAngle4f a4f = new javax.vecmath.AxisAngle4f();
        a4f.angle = angleTmp.getAngle();
        a4f.x = angleTmp.getX();
        a4f.y = angleTmp.getY();
        a4f.z = angleTmp.getZ();
        tmatrix.set(a4f);

        javax.vecmath.Vector3f v3f = new javax.vecmath.Vector3f();
        v3f.x = positionTmp.getX();
        v3f.y = positionTmp.getY();
        v3f.z = positionTmp.getZ();
        tmatrix.setTranslation(v3f);

        vfTranslation[0] = positionTmp.getX();
        vfTranslation[1] = positionTmp.getY();
        vfTranslation[2] = positionTmp.getZ();

        vfRotation[0] = angleTmp.getX();
        vfRotation[1] = angleTmp.getY();
        vfRotation[2] = angleTmp.getZ();
        vfRotation[3] = angleTmp.getAngle();

        hasChanged[FIELD_TRANSLATION] = true;
        hasChanged[FIELD_ROTATION] = true;
    }

    /**
     * Set a new state for the enabled field.
     *
     * @param state True if this sensor is to be enabled
     */
    @Override
    public void setEnabled(boolean state) {
        if(state != vfEnabled) {
            vfEnabled = state;

            if(!inSetup) {
                if(odeGeom != null)
                    odeGeom.setEnabled(vfEnabled);

                hasChanged[FIELD_ENABLED] = true;
                fireFieldChanged(FIELD_ENABLED);
            }
        }
    }

    /**
     * Get the current value of the enabled field. Default value is
     * <code>true</code>.
     *
     * @return The value of the enabled field
     */
    @Override
    public boolean isEnabled() {
        return vfEnabled;
    }

    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    @Override
    public void setRotation(float[] rot)
        throws InvalidFieldValueException {

        if(rot == null)
            throw new InvalidFieldValueException("Rotation value null");

        vfRotation[0] = rot[0];
        vfRotation[1] = rot[1];
        vfRotation[2] = rot[2];
        vfRotation[3] = rot[3];

        // Save recalcs during the setup phase
        if(!inSetup) {
            if(odeGeom != null)
                odeGeom.setAxisAndAngle(vfRotation[0],
                                        vfRotation[1],
                                        vfRotation[2],
                                        vfRotation[3]);

            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_ROTATION] = true;
            fireFieldChanged(FIELD_ROTATION);
        }
    }

    /**
     * Get the current rotation component of the transform.
     *
     * @return The current rotation
     */
    @Override
    public float[] getRotation() {
        return vfRotation;
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    @Override
    public void setTranslation(float[] tx)
        throws InvalidFieldValueException {

        if(tx == null)
            throw new InvalidFieldValueException("Translation value null");

        vfTranslation[0] = tx[0];
        vfTranslation[1] = tx[1];
        vfTranslation[2] = tx[2];

        // Save recalcs during the setup phase
        if(!inSetup) {
            if(odeGeom != null)
                odeGeom.setPosition(vfTranslation[0],
                                    vfTranslation[1],
                                    vfTranslation[2]);

            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_TRANSLATION] = true;
            fireFieldChanged(FIELD_TRANSLATION);
        }
    }

    /**
     * Get the current translation component of the transform.
     *
     * @return The current translation
     */
    @Override
    public float[] getTranslation() {
        return vfTranslation;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
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

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * The end of the frame has been reached, update the matrix now.
     */
    @Override
    public void allEventsComplete() {
        updateMatrix();
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

        updateMatrix();

        if(odeGeom != null) {
            odeGeom.setPosition(vfTranslation[0],
                                vfTranslation[1],
                                vfTranslation[2]);
            odeGeom.setAxisAndAngle(vfRotation[0],
                                    vfRotation[1],
                                    vfRotation[2],
                                    vfRotation[3]);
            odeGeom.setEnabled(vfEnabled);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.nBodyCollidableNodeType;
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

            case FIELD_ENABLED:
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ROTATION:
                fieldData.clear();
                fieldData.floatArrayValues = vfRotation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TRANSLATION:
                fieldData.clear();
                fieldData.floatArrayValues = vfTranslation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
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
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_ROTATION:
                    destNode.setValue(destIndex, vfRotation, 4);
                    break;

                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("Collidable.sendRoute: No field! " + srcIndex);
            ife.printStackTrace(System.err);
        } catch(InvalidFieldValueException ifve) {
            System.err.println("Collidable.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This is
     * be used to set SFBool field types isActive, enabled and loop.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ENABLED:
                setEnabled(value);
                break;
                
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
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

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

            case FIELD_TRANSLATION:
                setTranslation(value);
                break;

            case FIELD_ROTATION:
                setRotation(value);
                break;
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Recalculate the transformation matrix given the current axis/angle and
     * translation values. The result is placed in tmatrix.
     */
    protected void updateMatrix() {
        angleTmp.set(vfRotation);

        // Transpose from org.openmali.vecmath2 to javax.vecmath for aviatrix3d
        javax.vecmath.AxisAngle4f a4f = new javax.vecmath.AxisAngle4f();
        a4f.angle = angleTmp.getAngle();
        a4f.x = angleTmp.getX();
        a4f.y = angleTmp.getY();
        a4f.z = angleTmp.getZ();
        tmatrix.set(a4f);

        tmatrix.m03 = vfTranslation[0];
        tmatrix.m13 = vfTranslation[1];
        tmatrix.m23 = vfTranslation[2];
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box center to set
     */
    public void setBboxCenter(float[] val) {
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
        FieldValidator.checkBBoxSize(getVRMLNodeName(),val);

        vfBboxSize[0] = val[0];
        vfBboxSize[1] = val[1];
        vfBboxSize[2] = val[2];
    }
    
    /** Set the bboxDisplay field for this node. Only used by the field parser at setup
     * 
     * @param val the boolean value to set for the bboxDisplay field
     */
    public void setBboxDisplay(boolean val) {
        vfBboxDisplay = val;
    }
    
    /** Set the visible field for this node. Only used by the field parser at setup
     * 
     * @param val the boolean value to set for the visible field
     */
    public void setVisible(boolean val) {
        vfVisible = val;
    }
}
