/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
// None

/**
 * A renderable that represents a separate rendering space to an offscreen
 * buffer, rather than to the main buffer.
 * <p>
 *
 * This class encapsulates both pbuffer and frame buffer object renderable
 * capabilities.
 * <p>
 *
 * <b>Assumptions and Requirements</b>
 * <ul>
 * <li> An explicit size must be set. If either size dimension is not set
 *      the buffer is not rendered to. </li>
 * <li> Conventions follow the usual OpenGL requirements. For example, a
 *      1D texture needs to be created with the height of 1 and width of
 *      the required texture size.
 * </li>
 * <li> There are no checks for nPoT size requirements. It will create whatever
 *     size is requested.
 * </li>
 * <li>The GLCapabilities is used to construct the underlying buffer when
 *     creating pbuffers. It is used as a guide when creating FBOs. In both
 *     cases, the format is used to describe what texture format the buffer is
 *     applied as after the rendering takes place. For example, an offscreen
 *     buffer may need both depth and colour values to render a scene, but it
 *     only needs to apply the colour buffers to the texture as it is
 *     rendered on the object.
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.7 $
 */
public interface OffscreenBufferRenderable
    extends OffscreenRenderTargetRenderable
{
    /** Interpret the texture format as alpha only */
    int FORMAT_ALPHA = GL.GL_ALPHA;

    /** Interpret the texture format as intensity only */
    int FORMAT_INTENSITY = GL2.GL_INTENSITY;

    /** Interpret the texture format as luminance only */
    int FORMAT_LUMINANCE = GL.GL_LUMINANCE;

    /** Interpret the texture format as intensity-alpha */
    int FORMAT_LUMINANCE_ALPHA = GL.GL_LUMINANCE_ALPHA;

    /** Interpret the texture format as RGB */
    int FORMAT_RGB = GL.GL_RGB;

    /** Interpret the texture format as RGBA */
    int FORMAT_RGBA = GL.GL_RGBA;

    /** Interpret the texture format as a depth component texture */
    int FORMAT_DEPTH_COMPONENT = GL2.GL_DEPTH_COMPONENT;

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param col An array of at least length 4 to copy values into
     */
    void getClearColor(float[] col);

    /**
     * Fetch the format of the texture that this buffer should be interpreted
     * as when the texture is applied to the containing object.
     *
     * @return One of the FORMAT_ constants from this interface
     */
    int getFormat();

    /**
     * Get the number of render targets that this offscreen renderable manages. This
     * should always return at least 1, being itself.
     *
     * @return A value greater than zero
     */
    int getNumRenderTargets();

    /**
     * Get the child render target at the given index. If the index 0 is given, this
     * will return a reference to ourselves.
     *
     * @param index The index of the target to fetch
     * @return The render target at the given index
     */
    OffscreenRenderTargetRenderable getRenderTargetRenderable(int index);

    /**
     * Check to see if the depth buffer has its own separate renderable object.
     * Used when the offscreen needs to create the depth buffer separately as
     * a texture to use in shading.
     *
     * @return True if a separate depth texture is wanted
     */
    boolean hasSeparateDepthRenderable();

    /**
     * If a separate depth render target has been requested, return the
     * renderable for that object now. If not requested, this returns null.
     *
     * @return The depth target renderable or null
     */
    OffscreenRenderTargetRenderable getDepthRenderable();

    /**
     * Check to see if this buffer has resized since the last time it was used.
     * If so, recreate the underlying setup, but keep everything else the same.
     * Will reset the flag on read.
     *
     * @return true if the buffer has resized, requiring reallocation of the
     *   underlying buffer objects
     */
    boolean hasBufferResized();
}
