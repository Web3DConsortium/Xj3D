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

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * An OpenGL LineStripArray.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>minStripSizeMsg: Error message when the strip list has at least one
 *     strip with less than 3 vertices listed.</li>
 * <li>totalCoordCountMsg: Error message when the user doesn't provide
 *     enough coords for the strip counts.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class LineStripArray extends BufferGeometry
{
    /** Message when the strip count is < 3 */
    private static final String MIN_COUNT_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.LineStripArray.minStripSizeMsg";

    /** Message when the total count is less than strip totals */
    private static final String TOTAL_COUNT_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.LineStripArray.totalCoordCountMsg";


    /** The strip counts of valid triangles for each strip */
    private int[] stripCounts;

    /** The number of valid strips to read from the array */
    private int numStrips;

    /**
     * Constructs a LineStripArray with default values.
     */
    public LineStripArray()
    {
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or not.
     * Returns true if the defined number of coordinates and strips are both
     * non-zero.
     *
     * @return true when the geometry is visible
     */
    @Override
    protected boolean isVisible()
    {
        return super.isVisible() && numStrips != 0;
    }

    //----------------------------------------------------------
    // Methods defined by GeometryRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this renderable object.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        // No coordinates, do nothing.
        if((numStrips == 0) || ((vertexFormat & COORDINATE_MASK) == 0))
            return;

        setVertexState(gl);

        // Loop through the provided arrays using the sub-range
        // draw capabilities of glDrawArrays to create multiple
        // strips being drawn.
        int strip_offset = 0;
        for(int i = 0; i < numStrips; i++)
        {
            gl.glDrawArrays(GL.GL_LINE_STRIP,
                            strip_offset,
                            stripCounts[i]);

            strip_offset += stripCounts[i];
        }

        clearVertexState(gl);
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
        LineStripArray geom = (LineStripArray)o;
        return compareTo(geom);
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
        if(!(o instanceof LineStripArray))
            return false;
        else
            return equals((LineStripArray)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the number of valid strips to use. A check is performed to make
     * sure that the number of vertices high enough to support the total
     * of all the strip counts so make sure to call setVertex() with the
     * required array length before calling this method. Each strip must be
     * a minumum length of three.
     *
     * @param counts The array of counts
     * @param num The number of valid items to read from the array
     * @throws IllegalArgumentException Invalid total strip count or
     *   individual strip count < 2
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setStripCount(int[] counts, int num)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        int total = 0;

        for(int i = 0; i < num; i++)
        {
            if(counts[i] < 2)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(MIN_COUNT_PROP) + i;
                throw new IllegalArgumentException(msg);
            }

            total += counts[i];
        }

        if(stripCounts == null || stripCounts.length < num)
            stripCounts = new int[num];

        if(total > numCoords)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(TOTAL_COUNT_PROP);
            throw new IllegalArgumentException(msg);
        }

        numStrips = num;

        if(num > 0)
            System.arraycopy(counts, 0, stripCounts, 0, num);
    }

    /**
     * Get the number of valid strips that are defined for this geometry.
     *
     * @return a positive number
     */
    public int getValidStripCount()
    {
        return numStrips;
    }

    /**
     * Get the sizes of the valid strips. The passed array must be big enough
     * to contain all the strips.
     *
     * @param values An array to copy the strip values into
     */
    public void getStripCount(int[] values)
    {
        System.arraycopy(stripCounts, 0, values, 0, numStrips);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(LineStripArray ta)
    {
        if(ta == null)
            return 1;

        if(ta == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(LineStripArray ta)
    {
        return (ta == this);
    }
}
