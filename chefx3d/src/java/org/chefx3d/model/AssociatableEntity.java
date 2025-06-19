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
 * Defines whether an Entity can have associations.
 *
 * @author Russell Dodds
 * @version $Revision: 1.2 $
 */
public interface AssociatableEntity extends Entity {

    /**
     * Associate an entity with this one.
     *
     * @param entityID The entityID
     */
    void addAssociation(int entityID);

    /**
     * Remove a child association.
     *
     * @param entityID The child's entityID
     */
    void removeAssociation(int entityID);

    /**
     * Retain the parent list of associations.
     *
     * @param entityID The entityID
     */
    void addParentAssociation(int entityID);

    /**
     * Remove the parent from the list of associations.
     *
     * @param entityID The entityID
     */
    void removeParentAssociation(int entityID);

    /**
     * Get the associated entities.
     *
     * @return The associates
     */
    int[] getAssociates();

    /**
     * Get the associated parent entities.
     *
     * @return The associates
     */
    int[] getParentAssociates();

}