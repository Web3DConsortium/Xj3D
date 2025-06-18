/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.dis;

// External imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.dis.BaseDISEntityTypeMapping;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OGL renderer implementation of a DISEntityTypeMapping node.
 * <p>
 *
 * This node is purely informational within the scene graph. It does not have
 * a renderable representation.
 *
 * @author Vivian Gottesman, Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OGLDISEntityTypeMapping extends BaseDISEntityTypeMapping
    implements OGLVRMLNode {

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public OGLDISEntityTypeMapping() {
        super();
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLDISEntityTypeMapping(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    @Override
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    //----------------------------------------------------------
    // Methods overriding BaseGroup class.
    //----------------------------------------------------------

    @Override
    public void setupFinished() {
        if(!inSetup) {
            return;
        }

        super.setupFinished();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to initialise the OpenGL data structures.
     */
    private void init() {
    }
}
