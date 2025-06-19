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

package org.chefx3d.toolbar;

// Standard Imports

// Application specific imports
import org.chefx3d.model.WorldModel;
import org.chefx3d.util.ErrorReporter;

/**
 * Factory for creating toolbars.
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public interface ToolBarFactory {

    /**
     * Create a new ToolBar instance.
     *
     * @param model
     * @param direction What direction it should go, VERTICAL or HORIZONATAL
     * @param collapse Should singleton tools be collapsed or kept as single
     *        menus.
     * @return The ToolBar
     */
    ToolBar createToolBar(WorldModel model, int direction, boolean collapse);

    /**
     * Register an error reporter with the instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    void setErrorReporter(ErrorReporter reporter);

}
