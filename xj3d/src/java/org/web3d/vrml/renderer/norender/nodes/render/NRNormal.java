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

package org.web3d.vrml.renderer.norender.nodes.render;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.render.BaseNormal;

/**
 * norender implementation of a normal node.
 * <p>
 *
 * The normal node does not occupy a space in the Java 3D
 * scene graph as it is part of the GeometryArray class. This is used as
 * a VRML construct only. When VRML changes the values here, we pass them
 * back courtesy of the listeners to the children nodes.
 * <p>
 * Vectors are held internally as a flat array of values. The point list
 * returned will always be flat. We do this because Java3D takes point values
 * into the geometry classes as a single flat array. The array returned will
 * always contain exactly the number of points specified even though
 * internally we may do other things.
 * <p>
 * The effect of this is that point values may be routed out of this node as
 * a flat array of points rather than a 2D array. Receiving nodes should check
 * for this version as well. This implementation will handle being routed
 * either form.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class NRNormal extends BaseNormal
    implements NRVRMLNode {

    /**
     * Empty constructor
     */
    public NRNormal() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRNormal(VRMLNodeType node) {
        super(node);
    }
}
