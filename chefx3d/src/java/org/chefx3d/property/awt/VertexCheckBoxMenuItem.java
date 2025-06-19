/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005
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

// External Imports
import javax.swing.*;


// Internal Imports
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An action that can be used to toggle the placement panel off/on
 * <p>
 * 
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public class VertexCheckBoxMenuItem extends JCheckBoxMenuItem {

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * Create an instance of the action class.
     * 
     * @param editorPanel
     */
    public VertexCheckBoxMenuItem(TabMenuItems editorPanel) {

        // Create the default error reporter
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        // Create the menu action
        ShowVertexAction action = new ShowVertexAction(editorPanel);
        setAction(action);

        // set default
        setState(editorPanel.showTab(TabMenuItems.TYPE_VERTEX));

    }

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
