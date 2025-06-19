/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.model;

/**
 * Notification of changes in an entity's position, rotation, or size.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface EntityPositionListener {

    /**
     * The entity's position changed.
     *
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    void positionChanged(int entityID, double[] position);

    /**
     * The entity's rotation changed.
     *
     * @param entityID the id
     * @param rotation The rotation(axis + angle in radians)
     */
    void rotationChanged(int entityID, float[] rotation);

    /**
     * The entity's size changed.
     *
     * @param entityID the id
     * @param size The bounding box size(x,y,z)
     */
    void sizeChanged(int entityID, float[] size);
}
