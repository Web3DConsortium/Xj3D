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

/**
 * <p>
 * A property of a geometric object such as color, coordinate, normal.
 * @author Alan Hudson
 * </p>
 * @version $Revision: 1.7 $
 */
public interface VRMLGeometricPropertyNodeType extends VRMLNodeType {

    /**
     * Add a listener for the node properties changing. If the
     * listener is already added, ignore the request.
     *
     * @param l The listener instance to add
     */
    void addComponentListener(VRMLNodeComponentListener l);


    /**
     * Remove a listener for the node properties changing. If the
     * listener is already added, ignore the request.
     *
     * @param l The listener instance to remove
     */
    void removeComponentListener(VRMLNodeComponentListener l);
}
