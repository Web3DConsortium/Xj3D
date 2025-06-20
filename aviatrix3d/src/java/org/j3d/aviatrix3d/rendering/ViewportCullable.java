/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * A cullable that represents a single visual composite layer.
 * <p>
 *
 * A layer has zero or more viewports to process.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface ViewportCullable extends Cullable
{
    /**
     * Check to see if this viewport is valid. A valid viewport has both a
     * non zero width and height.
     *
     * @return true if this viewport has something usable to render
     */
    boolean isValid();

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @param viewportIndex the index of the view port
     * @return The layer cullable at the given index or null
     */
    ViewportLayerCullable getCullableLayer(int viewportIndex);

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero
     */
    int numCullableChildren();
}
