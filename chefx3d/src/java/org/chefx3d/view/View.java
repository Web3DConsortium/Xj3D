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

package org.chefx3d.view;

// External Imports
// None

// Internal Imports
import org.chefx3d.AuthoringComponent;
import org.chefx3d.tool.Tool;
import org.chefx3d.util.ErrorReporter;

/**
 * A view on the model. Authoring operations can be performed on any view.
 *
 * @author Alan Hudson
 * @version $Revision: 1.15 $
 */
public interface View extends AuthoringComponent {

    /** Display all helpers */
    int HELPER_ALL = 0;

    /** Display the selected entities helpers */
    int HELPER_SELECTED = 2;

    /** Display no helpers */
    int HELPER_NONE = 3;

    /** The view is the master view */
    int MODE_MASTER = 0;

    /** The view is a slaved view */
    int MODE_SLAVED = 1;

    /** The view is free navigated */
    int MODE_FREE_NAV = 2;

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    void setTool(Tool tool);

    /**
     * Go into associate mode. The next selection in any view will issue a
     * selection event and do nothing else.
     *
     * @param validTools A list of the valid tools. null string will be all
     *        valid. empty string will be none.
     */
    void enableAssociateMode(String[] validTools);

    /**
     * Exit associate mode.
     */
    void disableAssociateMode();

    /**
     * Set how helper objects are displayed.
     *
     * @param mode The mode
     */
    void setHelperDisplayMode(int mode);

    /**
     * Get the viewID. This shall be unique per view on all systems.
     *
     * @return The unique view ID
     */
    long getViewID();

    /**
     * Control of the view has changed.
     *
     * @param newMode The new mode for this view
     */
    void controlChanged(int newMode);

    /**
     * Register an error reporter with the view instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    void setErrorReporter(ErrorReporter reporter);

}
