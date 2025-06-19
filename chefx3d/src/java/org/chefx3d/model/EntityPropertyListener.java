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
// None

// Internal Imports
// None

/**
 * Notification of changes in the property values of an entity.
 *
 * @author Russell Dodds
 * @version $Revision: 1.3 $
 */
public interface EntityPropertyListener {

    /**
     * A property was added.
     *
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The name of the property
     */
    void propertyAdded(int entityID,
            String propertySheet, String propertyName);

    /**
     * A property was removed.
     *
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The name of the property
     */
    void propertyRemoved(int entityID,
            String propertySheet, String propertyName);

    /**
     * A property was updated.
     *
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The name of the property
     */
    void propertyUpdated(int entityID,
            String propertySheet, String propertyName);

}
