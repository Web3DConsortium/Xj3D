/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property.awt;

/**
 *
 * @author Russell Dodds
 * @version $Revision: 1.5 $
 */
public interface TabMenuItems {

    int TYPE_PLACEMENT = 0;
    int TYPE_VERTEX = 1;

    /**
     * Add the tab to the end of the list
     *
     * @param type
     * @return
     */
    boolean showTab(int type);

    /**
     * Remove the tab from the list
     *
     * @param type
     * @return
     */
    boolean hideTab(int type);

    /**
     * Get the current state
     *
     * @param type
     * @return
     */
    boolean isTabVisible(int type);

}
