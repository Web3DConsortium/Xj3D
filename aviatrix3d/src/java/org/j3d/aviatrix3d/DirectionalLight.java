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
import com.jogamp.opengl.GL2;

// Local imports
// None

/**
 * Representation of a directional light source.
 * <p>
 *
 * A directional light has a direction, but no location. This implementation
 * uses the OpenGL convention of having the light vector pointed in the
 * opposite direction to where you think the light is pointing.
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class DirectionalLight extends Light
{
    /** The colour of the light */
    private float[] direction;

    /**
     * Creates a light with the colour set to black.
     */
    public DirectionalLight()
    {
        super(DIRECTIONAL_TYPE);

        direction = new float[4];
    }

    /**
     * Create a light with the given base colour. Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     */
    public DirectionalLight(float[] col)
        throws IllegalArgumentException
    {
        super(DIRECTIONAL_TYPE, col);

        direction = new float[4];
    }

    /**
     * Create a light with the given base colour. Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @param dir The direction of the light
     */
    public DirectionalLight(float[] col, float[] dir)
        throws IllegalArgumentException
    {
        super(DIRECTIONAL_TYPE, col);

        direction = new float[] { dir[0], dir[1], dir[2], 0 };
    }

    //---------------------------------------------------------------
    // Methods defined by ComponentRenderable
    //---------------------------------------------------------------

    @Override
    public void render(GL2 gl, Object lightId)
    {
        int l_id = (int) lightId;

        gl.glLightfv(l_id, GL2.GL_AMBIENT, ambientColor, 0);
        gl.glLightfv(l_id, GL2.GL_POSITION, direction, 0);
        gl.glLightfv(l_id, GL2.GL_DIFFUSE, diffuseColor, 0);
        gl.glLightfv(l_id, GL2.GL_SPECULAR, specularColor, 0);

        gl.glEnable(l_id);
    }

    @Override
    public void postRender(GL2 gl, Object lightId)
    {
        if (lightId != null)
            gl.glDisable((Integer) lightId);
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    @Override
    protected void markBoundsDirty()
    {
        if(parent != null)
            parent.markBoundsDirty();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the direction to the new value. Direction is a vector that the
     * light is shining.
     *
     * @param dir The new direction value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDirection(float[] dir)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        direction[0] = dir[0];
        direction[1] = dir[1];
        direction[2] = dir[2];
    }

    /**
     * Set the direction to the new value. Direction is a vector that the
     * light is shining.
     *
     * @param x The x component of the direction value to use
     * @param y The y component of the direction value to use
     * @param z The z component of the direction value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDirection(float x, float y, float z)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
    }

    /**
     * Retrieve the current direction vector from the light.
     *
     * @param dir An array to copy the direction into
     */
    public void getDirection(float[] dir)
    {
        dir[0] = direction[0];
        dir[1] = direction[1];
        dir[2] = direction[2];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param l The light instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    @Override
    public int compareTo(Light l)
    {
        int res = super.compareTo(l);
        if(res != 0)
            return res;

        DirectionalLight dl = (DirectionalLight)l;

        return compareColor3(direction, dl.direction);
    }
}
