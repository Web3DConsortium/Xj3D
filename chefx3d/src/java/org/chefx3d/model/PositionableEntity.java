/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
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

// External Imports

// Internal Imports

/**
 * Defines the requirements for accessing an Entity's position, rotation, or size.
 *
 * @author Russell Dodds
 * @version $Revision: 1.3 $
 */
public interface PositionableEntity extends Entity {

    /**
     * Get the current position of the entity.
     *
     * @param pos The array to place the position in.
     */
    void getPosition(double[] pos);

    /**
     * Set the current position of the entity.
     *
     * @param pos The new position value.
     */
    void setPosition(double[] pos);

    /**
     * Get the current rotation of the entity
     *
     * @param rot The array to place the rotation in.
     */
    void getRotation(float[] rot);

    /**
     * Set the current rotation of the entity.
     *
     * @param rot The new rotation value.
     */
    void setRotation(float[] rot);

    /**
     * Get the size of this entity.
     *
     * @param size The array to place the size in.
     */
    void getSize(float[] size);

    /**
     * Compare the position of this Entity with the provided Entity.
     *
     * @param compare The Entity to compare to
     * @return true if same location, false otherwise
     */
    boolean samePosition(Entity compare);

    /**
     * Add a listener for positioning changes.
     *
     * @param listener The listener to add
     */
    void addEntityPositionListener(EntityPositionListener listener);

    /**
     * Remove a listener for positioning changes.
     *
     * @param listener The listener to remove.
     */
    void removeEntityPositionListener(EntityPositionListener listener);
}