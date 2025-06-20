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

package org.xj3d.sai.internal.node.networking;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.networking.Anchor;

/**
 * A concrete implementation of the Anchor node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIAnchor extends BaseNode implements Anchor {

    /**
     * The children inputOutput field
     */
    private MFNode children;

    /**
     * The addChildren inputOnly field
     */
    private MFNode addChildren;

    /**
     * The removeChildren inputOnly field
     */
    private MFNode removeChildren;

    /**
     * The bboxCenter initializeOnly field
     */
    private SFVec3f bboxCenter;

    /**
     * The bboxSize initializeOnly field
     */
    private SFVec3f bboxSize;

    /**
     * The description inputOutput field
     */
    private SFString description;

/** The autoRefresh inputOutput field */
private double autoRefresh;

/** The autoRefreshTimeLimit inputOutput field */
private double autoRefreshTimeLimit;

    /**
     * The parameter inputOutput field
     */
    private MFString parameter;

    /**
     * The url inputOutput field
     */
    private MFString url;

    /**
     * Constructor
     * @param node
     * @param refQueue
     * @param fac
     * @param fal
     * @param bnf
     */
    public SAIAnchor(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the number of MFNode items in the children field.
     *
     * @return the number of MFNode items in the children field.
     */
    @Override
    public int getNumChildren() {
        if (children == null) {
            children = (MFNode) getField("children");
        }
        return (children.getSize());
    }

    /**
     * Return the children value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getChildren(X3DNode[] val) {
        if (children == null) {
            children = (MFNode) getField("children");
        }
        children.getValue(val);
    }

    /**
     * Set the children field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setChildren(X3DNode[] val) {
        if (children == null) {
            children = (MFNode) getField("children");
        }
        children.setValue(val.length, val);
    }

    /**
     * Set the addChildren field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void addChildren(X3DNode[] val) {
        if (addChildren == null) {
            addChildren = (MFNode) getField("addChildren");
        }
        addChildren.setValue(val.length, val);
    }

    /**
     * Set the removeChildren field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void removeChildren(X3DNode[] val) {
        if (removeChildren == null) {
            removeChildren = (MFNode) getField("removeChildren");
        }
        removeChildren.setValue(val.length, val);
    }

    /**
     * Return the bboxCenter value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getBboxCenter(float[] val) {
        if (bboxCenter == null) {
            bboxCenter = (SFVec3f) getField("bboxCenter");
        }
        bboxCenter.getValue(val);
    }

    /**
     * Set the bboxCenter field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setBboxCenter(float[] val) {
        if (bboxCenter == null) {
            bboxCenter = (SFVec3f) getField("bboxCenter");
        }
        bboxCenter.setValue(val);
    }

    /**
     * Return the bboxSize value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getBboxSize(float[] val) {
        if (bboxSize == null) {
            bboxSize = (SFVec3f) getField("bboxSize");
        }
        bboxSize.getValue(val);
    }

    /**
     * Set the bboxSize field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setBboxSize(float[] val) {
        if (bboxSize == null) {
            bboxSize = (SFVec3f) getField("bboxSize");
        }
        bboxSize.setValue(val);
    }

    /**
     * Return the description String value.
     *
     * @return The description String value.
     */
    @Override
    public String getDescription() {
        if (description == null) {
            description = (SFString) getField("description");
        }
        return (description.getValue());
    }

    /**
     * Set the description field.
     *
     * @param val The String to set.
     */
    @Override
    public void setDescription(String val) {
        if (description == null) {
            description = (SFString) getField("description");
        }
        description.setValue(val);
    }

    /**
     * Return the number of MFString items in the parameter field.
     *
     * @return the number of MFString items in the parameter field.
     */
    @Override
    public int getNumParameter() {
        if (parameter == null) {
            parameter = (MFString) getField("parameter");
        }
        return (parameter.getSize());
    }

    /**
     * Return the parameter value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    @Override
    public void getParameter(String[] val) {
        if (parameter == null) {
            parameter = (MFString) getField("parameter");
        }
        parameter.getValue(val);
    }

    /**
     * Set the parameter field.
     *
     * @param val The String[] to set.
     */
    @Override
    public void setParameter(String[] val) {
        if (parameter == null) {
            parameter = (MFString) getField("parameter");
        }
        parameter.setValue(val.length, val);
    }

    /**
     * Return the number of MFString items in the url field.
     *
     * @return the number of MFString items in the url field.
     */
    @Override
    public int getNumUrl() {
        if (url == null) {
            url = (MFString) getField("url");
        }
        return (url.getSize());
    }

    /**
     * Return the url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    @Override
    public void getUrl(String[] val) {
        if (url == null) {
            url = (MFString) getField("url");
        }
        url.getValue(val);
    }

    /**
     * Set the url field.
     *
     * @param val The String[] to set.
     */
    @Override
    public void setUrl(String[] val) {
        if (url == null) {
            url = (MFString) getField("url");
        }
        url.setValue(val.length, val);
    }

    /**
     * Get the autoRefresh value to associate with the link.
     * @return The current autoRefresh value
     */
    @Override
    public double getAutoRefresh() {
        return autoRefresh;
    }

    /**
     * Set the autoRefresh value for this link.
     * @param newValue The new autoRefresh value to set
     */
    @Override
    public void setAutoRefresh (double newValue) {
        autoRefresh = newValue;
    }

    /**
     * Get the autoRefreshTimeLimit value to associate with the link.
     * @return The current TimeLimit value
     */
    @Override
    public double getAutoRefreshTimeLimit() {
        return autoRefreshTimeLimit;
    }

    /**
     * Set the autoRefreshTimeLimit value for this link.
     * @param newValue The new autoRefreshTimeLimit value to set
     */
    @Override
    public void setAutoRefreshTimeLimit (double newValue) {
        autoRefreshTimeLimit = newValue;
    }
}
