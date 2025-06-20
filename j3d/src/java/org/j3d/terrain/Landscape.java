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
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

// Local imports
import org.j3d.geom.terrain.HeightMapGeometry;

import org.j3d.util.I18nManager;
import org.j3d.util.frustum.ViewFrustum;

/**
 * Representation of a piece of rendered terrain data.
 * <p>
 *
 * The landscape is used to control what it rendered on screen as the user
 * moves about the virtual environment. This instance does not need to maintain
 * all the polygons on the screen at any one time, but may control them as
 * needed.
 * <p>
 *
 * This object is independent of the culling algorithm. It represents something
 * that can be placed in a scenegraph and have view information passed to it
 * without the need to know the specific algorithm in use. To implement a
 * specific algorithm (eg ROAM) you would extend this class and implement the
 * {@link #setView(Tuple3f, Vector3f)} method. Every time that the scene
 * changes, you will be notified by this method. That means you should perform
 * any culling/LOD and update the scene graph at this point. This will be
 * called at most once per frame.
 * <p>
 *
 * If you are going to use this class with the navigation code, then you
 * should also make the internal geometry not pickable, and make this item
 * pickable. In this way, the navigation code will find this top-level
 * terrain definition and use it directly to make the code much faster. None
 * of these capabilities are set within this implementation, so it is up to
 * the third-party code to make it so via calls to the appropriate methods.
 * <p>
 *
 * The landscape provides an appearance generator for letting the end user
 * application control appearance settings. If this is not set then particular
 * implementation is free to do what it likes.
 *
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>nullViewFrustumMsg: The view frustum in the constructor is null </li>
 * <li>nullTerrainDataMsg: The terrain data in the constructor is null </li>
 * </ul>
 *
 * @author Justin Couch, based on original ideas from Paul Byrne
 * @version $Revision: 1.11 $
 */
public abstract class Landscape
    implements HeightMapGeometry
{
    /** Message for the view frustum instance being null */
    private static final String NULL_VIEW_FRUSTUM_MSG_PROP =
        "org.j3d.terrain.Landscape.nullViewFrustumMsg";

    /** Message for the Terrain Data instance being null */
    private static final String NULL_TERRAIN_MSG_PROP =
        "org.j3d.terrain.Landscape.nullTerrainDataMsg";

    /** The current viewing frustum that is seeing the landscape */
    protected ViewFrustum landscapeView;

    /** Raw terrain information to be rendered */
    protected TerrainData terrainData;

    /**
     * Create a new Landscape with the set view and data. If either are not
     * provided, an exception is thrown. Uses the default appearance generator.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @throws IllegalArgumentException either parameter is null
     */
    public Landscape(ViewFrustum view, TerrainData data)
    {
        if(view == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_VIEW_FRUSTUM_MSG_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(data == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_TERRAIN_MSG_PROP);
            throw new IllegalArgumentException(msg);
        }

        terrainData = data;
        landscapeView = view;
    }

    //----------------------------------------------------------
    // Methods required by HeightMapGeometry
    //----------------------------------------------------------

    /**
     * Get the height at the given X,Z coordinate in the local coordinate
     * system. This implementation delegates to the underlying terrain data
     * to do the real resolution.
     *
     * @param x The x coordinate for the height sampling
     * @param z The z coordinate for the height sampling
     * @return The height at the current point or NaN
     */
    @Override
    public float getHeight(float x, float z)
    {
        return terrainData.getHeight(x, z);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Initialise the landscape ready for viewing. This should be called
     * before you add the parent branchgroup to the live scenegraph as the
     * implementation code will need to construct the renderable scene graph.
     * It also sets the initial position so that if the terrain is using
     * tilable datasets it can determine where to start building from.
     *
     * @param position The position the user is in the virtual world
     * @param direction The orientation of the user's gaze
     */
    public abstract void initialize(Tuple3f position, Vector3f direction);

    /**
     * Set the current viewing direction for the user. The user is located
     * at the given point and looking in the given direction. All information
     * is assumed to be in world coordinates.
     *
     * @param position The position the user is in the virtual world
     * @param direction The orientation of the user's gaze
     */
    public abstract void setView(Tuple3f position, Vector3f direction);
}
