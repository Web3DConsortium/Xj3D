/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2006
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
import org.chefx3d.util.ErrorReporter;

/**
 * A history of commands changed within the model.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface CommandController {

    /**
     * Add a <code>Command</code> to the history list without performing it.
     *
     * @param command the action to remember
     */
    void execute(Command command);

    /**
     * Execute the <code>Command</code> at the top of the stacks
     */
    void undo();

    /**
     * Returns true if there are any <code>Command</code>s to undo
     * @return true if there are any <code>Command</code>s to undo
     */
    boolean canUndo();

    /**
     * Execute the <code>Command</code> at the top of the redo stack
     */
    void redo();

    /**
     * Returns true if there are any <code>Command</code>s to redo
     * @return true if there are any <code>Command</code>s to redo
     */
    boolean canRedo();

    /**
     * Remove all <code>Command</code>s from the history
     */
    void clear();

    /**
     * Set the maximum size of the command history
     *
     * @param size The new size to assign
     */
    void setSize(int size);

    /**
     * Return the maximum size assigned to the history
     * @return the maximum size assigned to the history
     */
    int getSize();

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     * @return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     */
    String getUndoDescription();

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     * @return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     */
    String getRedoDescription();

    /**
     * Add a listener for Property changes. Duplicates will be ignored.
     *
     * @param l The listener.
     */
    void addCommandHistoryListener(CommandListener l);

    /**
     * Remove a listener for Property changes.
     *
     * @param l The listener.
     */
    void removeCommandHistoryListener(CommandListener l);

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    void setErrorReporter(ErrorReporter reporter);

}
