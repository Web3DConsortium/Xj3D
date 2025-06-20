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

package org.j3d.aviatrix3d.output.graphics;

// External imports
import com.jogamp.opengl.GLContext;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.j3d.util.ErrorReporter;

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;

/**
 * Handles the rendering for a single output device - be it on-screen or off.
 * <p>
 * The code expects that everything is set up before each call of the display()
 * callback. It does not handle any recursive rendering requests as that is
 * assumed to have been sorted out before calling this renderer.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.20 $
 */
public interface RenderingProcessor
{
    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter);

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     */
    void setClearColor(float r, float g, float b, float a);

    /**
     * Set whether we should always force a local colour clear before
     * beginning any drawing. If this is set to false, then we can assume that
     * there is at least one background floating around that we can use to
     * clear whatever was drawn in the previous frame, and so we can ignore the
     * glClear(GL.GL_COLOR_BUFFER_BIT) call. The default is set to true.
     *
     * @param state true if we should always locally clear first
     */
    void setColorClearNeeded(boolean state);

    /**
     * Enable or disable two pass rendering of transparent objects. By default
     * it is disabled.
     *
     * @param state true if we should enable two pass rendering
     */
    void enableTwoPassTransparentRendering(boolean state);

    /**
     * Check the state of the two pass transprent rendering flag.
     *
     * @return true if two pass rendering of transparent objects is enabled
     */
    boolean isTwoPassTransparentEnabled();

    /**
     * If two pass rendering of transparent objects is enabled, this is the alpha
     * test value used when deciding what to render. The default value is 1.0. No
     * sanity checking is performed, but the value should be between [0,1].
     *
     * @param cutoff The alpha value at which to enable rendering
     */
    void setAlphaTestCutoff(float cutoff);

    /**
     * Get the current value of the alpha test cutoff number. Will always
     * return the currently set number regardless of the state of the
     * two pass rendering flag.
     *
     * @return The currently set cut off value
     */
    float getAlphaTestCutoff();

    /**
     * Update the list of items to be rendered to the current list. Draw them
     * at the next opportunity.
     *
     * @param otherData data to be processed before the rendering
     * @param nodes The list of nodes in sorted order
     * @param renderOps Operation to perform on each node
     * @param numValid The number of valid items in the array
     * @param envData environment data to be rendered with this scene
     */
    void setDrawableObjects(GraphicsRequestData otherData,
                                   GraphicsDetails[] nodes,
                                   RenderOp[] renderOps,
                                   int numValid,
                                   GraphicsEnvironmentData[] envData);

    /**
     * Set the buffer descriptor that represents the surface that this renderer
     * works with. Can be used to enable and disable the buffer at the
     * appropriate time for rendering.
     *
     * @param desc The descriptor of the buffer that this renders to
     */
    void setOwnerBuffer(BaseBufferDescriptor desc);

    /**
     * Fetch the owner buffer descriptor that is used by this renderer.
     *
     * @return The descriptor of the owner. May be null if not yet initialised
     */
    public BaseBufferDescriptor getOwnerBuffer();

    /**
     * Add a dependent child rendering buffer to this processor. If it is
     * already a current child, it will ignore the request.
     *
     * @param rend The renderable instance to be added
     * @param proc The processor associated with the renderable
     */
    void addChildBuffer(OffscreenBufferRenderable rend,
                               RenderingProcessor proc);

    /**
     * Request that the given buffer gets updated.
     *
     * @param rend The renderable instance to be added
     */
    void updateChildBuffer(OffscreenBufferRenderable rend);

    /**
     * Remove a dependent child rendering buffer to this processor. If it is
     * not a current child, it will ignore the request.
     *
     * @param rend The renderable instance to be added
     */
    void removeChildBuffer(OffscreenBufferRenderable rend);

    /**
     * Perform the pre-rendering tasks now, including enabling the context
     * for this buffer as needed.
     *
     * @param localContext The context to perform the rendering with
     */
    void prepareData(GLContext localContext);

    /**
     * In the prepare data call, it was found that the GL context had been
     * reinitialised. So, pass through all the current geometry now and
     * reinitialise everything that needs it.
     *
     * @param localContext The context to perform the rendering with
     */
    void reinitialize(GLContext localContext);

    /**
     * Draw to the drawable now. This causes the drawable's context to be made
     * current and the GL commands are issued. Derived classes should not
     * override this method, instead they should use the display()
     * or init() methods as needed.
     *
     * @param localContext The context to perform the rendering with
     * @param profilingData The timing and load data
     * @return false if the rendering should not continue
     */
    boolean render(GLContext localContext, GraphicsProfilingData profilingData);

    /**
     * Get the surface to VWorld transformation matrix.
     * The coordinate system is in the window-system interface:
     * The x,y position is the lower left corner, with height going up the
     * screen and width heading to the right.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param matrix The matrix to copy into
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    boolean getSurfaceToVWorld(int x,
                                   int y,
                                   int layer,
                                   int subLayer,
                                   Matrix4f matrix,
                                   String deviceId,
                                   boolean useLastFound);

    /**
     * Convert a pixel location to surface coordinates.
     * The coordinate system is in the window-system interface:
     * The x,y position is the lower left corner, with height going up the
     * screen and width heading to the right.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param position The converted position.  It must be preallocated.
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    boolean getPixelLocationInSurface(int x,
                                          int y,
                                          int layer,
                                          int subLayer,
                                          Point3f position,
                                          String deviceId,
                                          boolean useLastFound);

    /**
     * Get the Center Eye position in surface coordinates.
     * The coordinate system is in the window-system interface:
     * The x,y position is the lower left corner, with height going up the
     * screen and width heading to the right.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param position The current eye position.  It must be preallocated.
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    boolean getCenterEyeInSurface(int x,
                                      int y,
                                      int layer,
                                      int subLayer,
                                      Point3f position,
                                      String deviceId,
                                      boolean useLastFound);

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown.
     */
    void halt();

    /**
     * Add a surface info listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    void addSurfaceInfoListener(SurfaceInfoListener l);

    /**
     * Remove a surface info listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    void removeSurfaceInfoListener(SurfaceInfoListener l);
}
