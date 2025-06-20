/*****************************************************************************
 *                            (c) j3d.org 2002-2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
// None

// Local parser
// None

/**
 * Base representation of a part from the file that is not the header.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class LDrawRenderable extends LDrawColoredPart
{
    /** First point in the line */
    protected double[] start;

    /** Second point in the line */
    protected double[] end;

    /**
     * Construct the line information from the two points and colour.
     *
     * @param col The colour to render in. Most not be null
     * @param end
     */
    protected LDrawRenderable(LDrawColor col, double[] start, double[] end)
    {
        super(col);

        this.start = start;
        this.end = end;
    }

    /**
     * Get the coordinates of the first point (X, Y, Z)
     *
     * @return a reference to the internal position value
     */
    public double[] getStartPoint()
    {
        return start;
    }


    /**
     * Get the coordinates of the second point (X, Y, Z)
     *
     * @return a reference to the internal position value
     */
    public double[] getEndPoint()
    {
        return end;
    }
}
