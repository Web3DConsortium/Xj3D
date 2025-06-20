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
package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.VRMLExecutionSpace;

/**
 * An abstract representation of the root node of a world.
 * <p>
 * We need a root node representation to act as a standard place to collate
 * all the nodes of a world. While the XML encoding provides a single root
 * node in the X3D element, the UTF8 encoding does not. This class acts as
 * that sort of collector. For the XML code or the UTF8 code, we would expect
 * more concrete implementations of this interface to be derived.
 * <p>
 * While this could be a {@link VRMLGroupingNodeType} we really don't want
 * to extend that interface. It implies that this node could then be inserted
 * anywhere into the scene graph. That we want to prevent. Instead, we just copy
 * all the methods here. We also copy the bounded node methods because these
 * might be useful. We do not require implementations to figure out the bounds,
 * but if they do then it would be nice to grab that information.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public interface VRMLWorldRootNodeType
    extends VRMLNodeType, VRMLExecutionSpace {

    /**
     * Set the ID of this world root to be the initial (index 0) layer.
     */
    void setRootWorld();

    /**
     * Set the scene that is contained by this world, which happens to be an
     * execution space. The scene instance must be non-null.
     *
     * @param scene The scene to use
     */
    void setContainedScene(BasicScene scene);

    /**
     * Accessor method to set a new value for field attribute <b>bboxCenter</b>
     *
     * @param newBboxCenter The new center of the bounding box
     */
    void setBboxCenter (float[] newBboxCenter);

    /**
     * Accessor method to get current value of field <b>bboxCenter</b>.
     * Default value is <code>0 0 0</code>.
     *
     * @return Value of bboxCenter(SFVec3f)
     */
    float[] getBboxCenter ();

    /**
     * Accessor method to set a new value for field attribute <b>bboxSize</b>
     *
     * @param newBboxSize The new size for the bounding box
     */
    void setBboxSize (float[] newBboxSize);

    /**
     * Accessor method to get current value of field <b>bboxSize</b>.
     * Default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bboxSize(SFVec3f)
     */
    float[] getBboxSize ();
    
    /** Set a new value for field attribute <b>bboxDisplay</b>
     * 
     * @param newBboxDisplay the new value for the bounding box display
     */
    void setBboxDisplay(boolean newBboxDisplay);
    
    /** Get the current value of the field <b>bboxDisplay</b>.
     * Default value is <code>FALSE</code>.
     * 
     * @return 
     */
    boolean getBboxDisplay();

    /**
     * Get the children, provides a live reference not a copy
     *
     * @return An array of VRMLNodes
     */
    VRMLNodeType[] getChildren();

    /**
     * Modifier method to set the children field.
     * If passed null this method will act like removeChildren
     *
     * @param newChildren Array of new children
     */
    void setChildren(VRMLNodeType[] newChildren);

    /**
     * Modifier method to set the children field.
     * Creates an array containing only newChild.
     * If passed null this method will act like removeChildren
     *
     * @param newChild The new child
     */
    void setChildren(VRMLNodeType newChild);

    /**
     * Returns the number of children
     *
     * @return The number of children
     */
    int getChildrenSize();

    /**
     * Append a new child node to the existing collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     *
     * @param newChild The new child
     */
    void addChild(VRMLNodeType newChild);

    /**
     * Remove an existing child node from the collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     * If it is not registered, silently ignores the request.
     *
     * @param removeChild The child to remove
     */
    void removeChild(VRMLNodeType removeChild);
}
