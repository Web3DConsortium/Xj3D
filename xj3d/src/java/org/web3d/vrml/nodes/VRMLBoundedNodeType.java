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
package org.web3d.vrml.nodes;

// Standard imports

import org.web3d.vrml.lang.InvalidFieldAccessException;


// Application specific imports
// none

/**.
 * <p>
 * Indicates that a node contains a bounding box field
 * </p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public interface VRMLBoundedNodeType extends VRMLChildNodeType {

    /**
     * Get the current value of field the bboxCenter field.
     * The default value is <code>0 0 0</code>.
     *
     * @return The value of bboxCenter(SFVec3f)
     */
    float[] getBboxCenter();
    
    /** Set a new value for field attribute <b>bboxCenter</b>
     * 
     * @param newBboxCenter the new value for the bounding box display
     */
    void setBboxCenter(float[] newBboxCenter) throws InvalidFieldAccessException;

    /**
     * Get the current value of the bboxSize field.
     * The default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bboxSize(SFVec3f)
     */
    float[] getBboxSize();
    
    /** Set a new value for field attribute <b>bboxSize</b>
     * 
     * @param newBboxSize the new value for the bounding box display
     */
    void setBboxSize(float[] newBboxSize) throws InvalidFieldAccessException;
    
    /** Get the current value of the bboxDisplay field.
     * The default value is <code>false</code>.
     * 
     * @return the value of the bboxDisplay(SFBool)
     */
    boolean getBboxDisplay();
    
    /** Get the current value of the visible field.
     * The default value is <code>true</code>.
     * 
     * @return the value of the visible(SFBool)
     */
    boolean getVisible();
    
    /** Set a new value for field attribute <b>bboxDisplay</b>
     * 
     * @param newBboxDisplay the new value for the bounding box display
     */
    void setBboxDisplay(boolean newBboxDisplay);
    
    /** Set a new value for field attribute <b>visible</b>
     * 
     * @param newVisible the new value for the bounding box display
     */
    void setVisible(boolean newVisible);

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    boolean isShared();

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    void setShared(boolean used);
}
