/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2006
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

// Standard imports

// Application specific imports

/**
 * Notification of changes to the Command History Stacks. 
 * 
 * @author Russell Dodds
 * @version $Revision: 1.2 $
 */
public interface CommandListener {
  
    /**
     * A command was successfully executed
     */
    public void commandExecuted();

    /**
     * A command was successfully undone
     */
    public void commandUndone();

    /**
     * A command was successfully redone
     */
    public void commandRedone();

    /**
     * The command stack was cleared
     */
    public void commandCleared();

}
