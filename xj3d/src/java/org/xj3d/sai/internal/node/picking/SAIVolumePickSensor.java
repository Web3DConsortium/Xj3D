/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai.internal.node.picking;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.picking.VolumePickSensor;

/**
 * A concrete implementation of the VolumePickSensor node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIVolumePickSensor extends BaseNode implements VolumePickSensor {

    /**
     * The enabled inputOutput field
     */
    private SFBool enabled;

    /**
     * The isActive outputOnly field
     */
    private SFBool isActive;

    /**
     * The pickingGeometry inputOutput field
     */
    private SFNode pickingGeometry;

    /**
     * The pickTarget inputOutput field
     */
    private MFNode pickTarget;

    /**
     * The intersectionType initializeOnly field
     */
    private SFString intersectionType;

    /**
     * The pickedGeometry outputOnly field
     */
    private MFNode pickedGeometry;

    /**
     * The sortOrder initializeOnly field
     */
    private SFString sortOrder;

    /**
     * The objectType inputOutput field
     */
    private MFString objectType;

    /**
     * Constructor
     * @param bnf
     */
    public SAIVolumePickSensor(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the enabled boolean value.
     *
     * @return The enabled boolean value.
     */
    @Override
    public boolean getEnabled() {
        if (enabled == null) {
            enabled = (SFBool) getField("enabled");
        }
        return (enabled.getValue());
    }

    /**
     * Set the enabled field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setEnabled(boolean val) {
        if (enabled == null) {
            enabled = (SFBool) getField("enabled");
        }
        enabled.setValue(val);
    }

    /**
     * Return the isActive boolean value.
     *
     * @return The isActive boolean value.
     */
    @Override
    public boolean getIsActive() {
        if (isActive == null) {
            isActive = (SFBool) getField("isActive");
        }
        return (isActive.getValue());
    }

    /**
     * Return the pickingGeometry X3DNode value.
     *
     * @return The pickingGeometry X3DNode value.
     */
    @Override
    public X3DNode getPickingGeometry() {
        if (pickingGeometry == null) {
            pickingGeometry = (SFNode) getField("pickingGeometry");
        }
        return (pickingGeometry.getValue());
    }

    /**
     * Set the pickingGeometry field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setPickingGeometry(X3DNode val) {
        if (pickingGeometry == null) {
            pickingGeometry = (SFNode) getField("pickingGeometry");
        }
        pickingGeometry.setValue(val);
    }

    /**
     * Return the number of MFNode items in the pickTarget field.
     *
     * @return the number of MFNode items in the pickTarget field.
     */
    @Override
    public int getNumPickTarget() {
        if (pickTarget == null) {
            pickTarget = (MFNode) getField("pickTarget");
        }
        return (pickTarget.getSize());
    }

    /**
     * Return the pickTarget value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getPickTarget(X3DNode[] val) {
        if (pickTarget == null) {
            pickTarget = (MFNode) getField("pickTarget");
        }
        pickTarget.getValue(val);
    }

    /**
     * Set the pickTarget field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setPickTarget(X3DNode[] val) {
        if (pickTarget == null) {
            pickTarget = (MFNode) getField("pickTarget");
        }
        pickTarget.setValue(val.length, val);
    }

    /**
     * Return the intersectionType String value.
     *
     * @return The intersectionType String value.
     */
    @Override
    public String getIntersectionType() {
        if (intersectionType == null) {
            intersectionType = (SFString) getField("intersectionType");
        }
        return (intersectionType.getValue());
    }

    /**
     * Set the intersectionType field.
     *
     * @param val The String to set.
     */
    @Override
    public void setIntersectionType(String val) {
        if (intersectionType == null) {
            intersectionType = (SFString) getField("intersectionType");
        }
        intersectionType.setValue(val);
    }

    /**
     * Return the number of MFNode items in the pickedGeometry field.
     *
     * @return the number of MFNode items in the pickedGeometry field.
     */
    @Override
    public int getNumPickedGeometry() {
        if (pickedGeometry == null) {
            pickedGeometry = (MFNode) getField("pickedGeometry");
        }
        return (pickedGeometry.getSize());
    }

    /**
     * Return the pickedGeometry value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getPickedGeometry(X3DNode[] val) {
        if (pickedGeometry == null) {
            pickedGeometry = (MFNode) getField("pickedGeometry");
        }
        pickedGeometry.getValue(val);
    }

    /**
     * Return the sortOrder String value.
     *
     * @return The sortOrder String value.
     */
    @Override
    public String getSortOrder() {
        if (sortOrder == null) {
            sortOrder = (SFString) getField("sortOrder");
        }
        return (sortOrder.getValue());
    }

    /**
     * Set the sortOrder field.
     *
     * @param val The String to set.
     */
    @Override
    public void setSortOrder(String val) {
        if (sortOrder == null) {
            sortOrder = (SFString) getField("sortOrder");
        }
        sortOrder.setValue(val);
    }

    /**
     * Return the number of MFString items in the objectType field.
     *
     * @return the number of MFString items in the objectType field.
     */
    @Override
    public int getNumObjectType() {
        if (objectType == null) {
            objectType = (MFString) getField("objectType");
        }
        return (objectType.getSize());
    }

    /**
     * Return the objectType value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    @Override
    public void getObjectType(String[] val) {
        if (objectType == null) {
            objectType = (MFString) getField("objectType");
        }
        objectType.getValue(val);
    }

    /**
     * Set the objectType field.
     *
     * @param val The String[] to set.
     */
    @Override
    public void setObjectType(String[] val) {
        if (objectType == null) {
            objectType = (MFString) getField("objectType");
        }
        objectType.setValue(val.length, val);
    }
}
