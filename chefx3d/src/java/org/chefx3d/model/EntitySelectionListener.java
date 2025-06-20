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
 * Notification of changes in the selection of an entity.
 * 
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public interface EntitySelectionListener {
    
    /**
     * An entity has been selected
     * 
     * @param entityID The entity which changed
     * @param selected Status of selecting
     */
    public void selectionChanged(int entityID, boolean selected);
    
    /**
     * An entity has been highlighted
     * 
     * @param entityID The entity which changed
     * @param highlighted Status of highlighting
     */
    public void highlightChanged(int entityID, boolean highlighted);
   
}
