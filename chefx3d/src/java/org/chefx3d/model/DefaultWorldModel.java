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

package org.chefx3d.model;

// External Imports

// Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * The default world model.
 *
 * @author Alan Hudson
 * @version $Revision: 1.14 $
 */
class DefaultWorldModel extends BaseWorldModel {

    /** The controller that manages commands */
    private CommandController controller;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Create a DefaultWorldModel.
     *
     * @param controller The controller that manages commands
     */
    public DefaultWorldModel(CommandController controller) {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        this.controller = controller;
    }

    // ----------------------------------------------------------
    // Methods required by the WorldModel interface
    // ----------------------------------------------------------

    /**
     * Apply a command against the model.
     *
     * @param command The command
     */
    @Override
    public void applyCommand(Command command) {
        controller.execute(command);
    }

    /**
     * Undo the last change.
     */
    @Override
    public void undo() {
        controller.undo();
    }

    /**
     * Flush the undo history.
     */
    @Override
    public void clearHistory() {
        controller.clear();
    }

    /**
     * Returns true if there are any <code>Command</code>s to undo
     */
    @Override
    public boolean canUndo() {
        return controller.canUndo();
    }

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     */
    @Override
    public String getUndoDescription() {
        return controller.getUndoDescription();
    }

    /**
     * Redo the last change.
     */
    @Override
    public void redo() {
        controller.redo();
    }

    /**
     * Returns true if there are any <code>Command</code>s to redo
     */
    @Override
    public boolean canRedo() {
        return controller.canRedo();
    }

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     */
    @Override
    public String getRedoDescription() {
        return controller.getRedoDescription();
    }

    /**
     * Sets the ErrorReporter to use to display messages
     *
     * @param reporter
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        super.setErrorReporter(reporter);

        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    public void checkForDuplicateIDS() {
        // ignore
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------
}
