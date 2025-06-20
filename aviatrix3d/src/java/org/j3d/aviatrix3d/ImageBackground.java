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

package org.j3d.aviatrix3d;

// External imports
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * Background node for 2D scenes that draws an image on screen as the
 * background.
 * <p>
 *
 * This will set the background colour to a single colour for the entire
 * viewport and then draw the image over the top of the colour, including
 * blending any transparency. The image may be scaled or not to fit the window
 * or it may be repeated without scale, to cover the entire viewport.
 *
 * If used, this will override the setClearColor() on
 * {@link org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice}. Note that
 * if the user has turned the useClearColor flag off in the base class, that
 * this node effectively will do nothing.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class ImageBackground extends Background
{
    /**
     * Constructs a background node for a base colour of black and no image
     * set.
     */
    public ImageBackground()
    {
        super();
    }

    /**
     * Construct a background node for a user-provided colour. The colour
     * provided should have 3 or 4 elements. If 3 are provided, a fully opaque
     * background is assumed. If less than 3 elements are provided, an exception
     * is generated. If the array is null, this assumes the a default black
     * background.
     *
     * @param c The array of colours to use, or null
     * @throws IllegalArgumentException The colour array is not long enough
     */
    public ImageBackground(float[] c)
        throws IllegalArgumentException
    {
        super(c);
    }

    //----------------------------------------------------------
    // Methods defined by BackgroundRenderable
    //----------------------------------------------------------

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * renderable background. Pure 2D backgrounds do not need transformation
     * stacks or frustums set up to render - they can blit straight to the
     * screen as needed.
     *
     * @return True if this is 2D background, false if this is 3D
     */
    @Override
    public boolean is2D()
    {
        return true;
    }

    //----------------------------------------------------------
    // Methods defined by ObjectRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        if(useClearColor)
        {
            gl.glClearColor(color[0], color[1], color[2], color[3]);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Renderable o)
        throws ClassCastException
    {
        ImageBackground bg = (ImageBackground)o;
        return compareTo(bg);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof ImageBackground))
            return false;
        else
            return equals((ImageBackground)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param bg The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ImageBackground bg)
    {
        if(bg == null)
            return 1;

        if(bg == this)
            return 0;

        int res = compareColor4(color, bg.color);
        if(res != 0)
            return res;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param bg The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ImageBackground bg)
    {
        if(bg == this)
            return true;

        if(bg == null)
            return false;

        return equalsColor4(color, bg.color);
    }
}
