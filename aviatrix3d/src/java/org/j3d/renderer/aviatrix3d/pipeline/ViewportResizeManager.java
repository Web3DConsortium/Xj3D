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

package org.j3d.renderer.aviatrix3d.pipeline;

// External imports
import java.util.ArrayList;
import java.util.List;

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.Viewport;

/**
 * Convenience class for managing the resizing of the viewports based on
 * listener feedback from the surface.
 * <p>
 *
 * This class deals only with fullscreen resizing capabilities. All viewports
 * registered for management will resize to the full screen size handed to us
 * by the listener.
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public class ViewportResizeManager
    implements GraphicsResizeListener
{
    /** The list of viewports to manage */
    private List<Viewport> viewports;

    /** Flag saying that resizes have been received and not yet processed */
    private boolean newSizeSet;

    /** The lower left X position of the viewpoint, in pixels */
    private int viewX;

    /** The lower left Y position of the viewpoint, in pixels */
    private int viewY;

    /** The width of the viewport in pixels */
    private int viewWidth;

    /** The width of the viewport in pixels */
    private int viewHeight;

    /** Have we got any valid size set yet. */
    private boolean validSizeSet;

    /**
     * Create a new instance of this manager now.
     */
    public ViewportResizeManager()
    {
        viewports = new ArrayList<>();
        newSizeSet = false;
        validSizeSet = false;
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsResizeListener
    //---------------------------------------------------------------

    @Override
    public void graphicsDeviceResized(int x, int y, int width, int height)
    {
        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;
        newSizeSet = true;
        validSizeSet = true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Clock the updates that should be sent now. This should only be called
     * during the application update cycle callbacks. Ideally this should be
     * called as the first thing in updateSceneGraph() method so that anything
     * else that relies on view frustum projection will have the latest
     * information.
     */
    public void sendResizeUpdates()
    {
        if(!newSizeSet)
            return;

        for (Viewport vp : viewports) {
            vp.setDimensions(viewX, viewY, viewWidth, viewHeight);
        }

        newSizeSet = false;
    }

    /**
     * Add a viewport to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param view The viewport instance to use
     */
    public void addManagedViewport(Viewport view)
    {
        if((view == null) || viewports.contains(view))
            return;

        viewports.add(view);

        if(validSizeSet)
            view.setDimensions(viewX, viewY, viewWidth, viewHeight);
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param view The viewport instance to remove
     */
    public void removeManagedViewport(Viewport view)
    {
        if(view == null)
            return;

        viewports.remove(view);
    }

    /**
     * Clear all of the current viewports from the manager. Typically used when
     * you want to completely change the scene.
     */
    public void clear()
    {
        viewports.clear();
    }
}
