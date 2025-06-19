/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.view.awt.gt2d;

// External Imports
// None

// Local imports
import org.chefx3d.model.Entity;
import org.chefx3d.tool.Tool;

/**
 * Maps entities to renderers.
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public interface EntityRendererMapper {

    /**
     * Get the renderer for the entity provided
     *
     * @param entity The entity to draw
     */
    public ToolRenderer getRenderer(Entity entity);

    /**
     * Create an instance of the render to use
     *
     * @param tool
     */
    public void setRenderer(String name, ToolRenderer renderer);

}
