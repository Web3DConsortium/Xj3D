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

package org.web3d.vrml.renderer.norender.nodes.picking;

// Standard imports

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.picking.BaseLinePickSensor;

/**
 * Null-renderer implementation of a LinePickSensor node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class NRLinePickSensor extends BaseLinePickSensor
    implements NRVRMLNode {

    // Field index constants

    /**
     * Construct a new time sensor object
     */
    public NRLinePickSensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRLinePickSensor(VRMLNodeType node) {
        super(node);
    }
}
