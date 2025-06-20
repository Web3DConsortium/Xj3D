/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain;

// External imports
import java.awt.image.BufferedImage;

// Local imports
// none

/**
 * Terrain data that represents a static collection of information.
 * <p>
 *
 * A static terrain representation contains points that can all be loaded at
 * setup time. Typically used to represent smaller datasets, this interface
 * provides the representation of the data all at once and assumes a single
 * texture object covers the entire dataset. However, don't forget that Java3D
 * Texture object does allow multi-level mip-mapping within the texture
 * instance.
 *
 * <p>
 *
 * {@link TerrainData#getSourceDataType()} always returns
 * {@link TerrainData#STATIC_DATA}.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public interface StaticTerrainData extends TerrainData
{
    /**
     * Fetch the Texture that is used to cover the entire terrain. If no
     * texture is used, then return null. Assumes a single large texture for
     * the entire terrain.
     *
     * @return The texture instance to use or null
     */
    BufferedImage getTexture();

    /**
     * Get the width (number of points on the Y axis) of the grid.
     *
     * @return The number of points in the width if the grid
     */
    int getGridWidth();

    /**
     * Get the depth (number of points on the X axis) of the grid.
     *
     * @return The number of points in the depth of the grid
     */
    int getGridDepth();
}
