/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.DirectPosition;

// Local imports
// None

/**
 * Holds the coordinates for a position within some coordinate reference system
 * for transformation within Xj3D's height data generator.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class GeoPosition implements DirectPosition {

    /** The position held by this instance */
    private double[] position;

    /**
     * Create a new instance of this class. The initial position is set to the
     * origin.
     */
    GeoPosition() {
        position = new double[3];
    }

    /**
     * Create a new instance of this class that copies the initial position from
     * the given array.
     *
     * @param pos The initial position to initialise to
     */
    GeoPosition(double [] pos) {
        this(); // invoke default constructor
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    //----------------------------------------------------------
    // Methods defined by DirectPosition
    //----------------------------------------------------------

    @Override
    public int getDimension() {
        return position.length;
    }

    @Override
    public double[] getCoordinates() {
        return position;
    }

    @Override
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        return position[dimension];
    }

    @Override
    public void setOrdinate(int dimension, double value)
        throws IndexOutOfBoundsException {
        position[dimension] = value;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return null;
    }

    @Override
    public Object clone() {
        return new GeoPosition(position);
    }

    //----------------------------------------------------------
    // Methods defined by Position
    //----------------------------------------------------------

    /**
     * Returns the direct position (i.e. itself)
     *
     * @return <code>this</code>
     */
    @Override
    public DirectPosition getPosition() {
        return this;
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Set a new position to the given values.
     *
     * @param pos The initial position vector to initialise to
     */
    void setPosition(double [] pos) {
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    /**
     * Set a new position to the given values.
     *
     * @param x The x component of the position
     * @param y The y component of the position
     * @param z The z component of the position
     */
    void setPosition(double x, double y, double z) {
        setPosition(new double[] {x, y, z});
    }
}
