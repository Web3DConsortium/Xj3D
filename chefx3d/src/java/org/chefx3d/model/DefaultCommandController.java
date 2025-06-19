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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import static org.chefx3d.util.DefaultErrorReporter.getDefaultReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A history of commands changed within the model.
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public class DefaultCommandController
    implements CommandController {

    private static final int DEFAULT_SIZE = 200;

    /** The size of the stack of <code>Command</code>s. */
    private int maxSize;

    /** The list of <code>Command</code>s able to undo. */
    private Stack<Command> undoCommands;

    /** The list of <code>Command</code>s able to redo. */
    private Stack<Command> redoCommands;

    /** The list of CommandHistoryListeners. */
    protected List<CommandListener> commandListeners;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Create a commandHistory object. Set the maximum size to
     * <code>DEFAULT_SIZE</code> and create the data structure to store the
     * <code>Command</code>s
     */
    public DefaultCommandController() {
        this(DEFAULT_SIZE);
    }

    /**
     * Create a commandHistory object. Set the maximum size to <code>size</code>
     * and create the data structure to store the <code>Command</code>s
     *
     * @param size The maximum size of the stack
     */
    public DefaultCommandController(int size) {
        maxSize = size;
        undoCommands = new Stack<>();
        redoCommands = new Stack<>();

        commandListeners = new ArrayList<>();

        errorReporter = getDefaultReporter();
    }

    /**
     * Add a <code>Command</code> to the history list without performing it.
     *
     * @param command the action to remember
     */
    @Override
    public void execute(Command command) {

//System.out.println("CommandController.execute()");
//System.out.println("    command: " + command.getDescription());
//System.out.println("    isTransient: " + command.isTransient());
//System.out.println("    isUndoable: " + command.isUndoable());

        try {

            // execute the command
            command.execute();

            // add the element to the stack
            if (!command.isTransient() && command.isUndoable()) {
                undoCommands.push(command);
            }

            // resize to the maxSize, this will trim the oldest items
            if (undoCommands.size() > maxSize) {
                undoCommands.setSize(maxSize);
            }

            // flush the redo stack, we never want to redo after a
            // new command has been added
            redoCommands.clear();

            // finally, notify listeners of the change
            CommandListener l;
            for (Iterator<CommandListener> i =
                commandListeners.iterator(); i.hasNext();) {

                l = i.next();
                l.commandExecuted();
            }

        } catch (Exception e) {
            errorReporter.errorReport("Execution of " + command.getDescription() + " command failed.", e);
        }

    }

    /**
     * Execute the <code>Command</code> at the top of the stacks
     */
    @Override
    public void undo() {

        if (canUndo() == true) {

            // Get the command from the undo stack and perform undo
            Command command = undoCommands.pop();

//System.out.println("CommandController.undo()");
//System.out.println("    command: " + command.getDescription());
//System.out.println("    isTransient: " + command.isTransient());
//System.out.println("    isUndoable: " + command.isUndoable());

            try {

                command.undo();

                // Move the command to the top of the redo stack
                redoCommands.push(command);

                // Trim the redo stack
                if (redoCommands.size() > maxSize) {
                    redoCommands.setSize(maxSize);
                }

                // finally, notify listeners of the change
                CommandListener l;
                for (Iterator<CommandListener> i =
                    commandListeners.iterator(); i.hasNext();) {

                    l = i.next();
                    l.commandUndone();
                }

            } catch (Exception e) {
                errorReporter.errorReport("Undo of " + command.getDescription() + " command failed.", e);
            }

         } else {
            errorReporter.messageReport("Cannot undo requested command, nothing in the undo history.");
        }
    }

    /**
     * Returns true if there are any <code>Command</code>s to undo
     * @return true if there are any <code>Command</code>s to undo
     */
    @Override
    public boolean canUndo() {
        return undoCommands.size() > 0;
    }

    /**
     * Execute the <code>Command</code> at the top of the redo stack
     */
    @Override
    public void redo() {
        if (canRedo() == true) {
            // Get the command from the redo stack and perform redo
            Command command = redoCommands.pop();

//System.out.println("CommandController.redo()");
//System.out.println("    command: " + command.getDescription());
//System.out.println("    isTransient: " + command.isTransient());
//System.out.println("    isUndoable: " + command.isUndoable());

            try {

                command.redo();

                // Move the command to the top of the undo stack
                undoCommands.push(command);

                // Trim the undo stack
                if (undoCommands.size() > maxSize) {
                    undoCommands.setSize(maxSize);
                }

                // finally, notify listeners of the change
                CommandListener l;
                for (Iterator<CommandListener> i =
                    commandListeners.iterator(); i.hasNext();) {

                    l = i.next();
                    l.commandRedone();
                }

            } catch (Exception e) {
                errorReporter.errorReport("Redo of " + command.getDescription() + " command failed.", e);
            }

        } else {
            errorReporter.messageReport("Cannot redo requested command, nothing in the redo history");
        }
    }

    /**
     * Returns true if there are any <code>Command</code>s to redo
     * @return true if there are any <code>Command</code>s to redo
     */
    @Override
    public boolean canRedo() {
        return redoCommands.size() > 0;
    }

    /**
     * Remove all <code>Command</code>s from the history
     */
    @Override
    public void clear() {
        undoCommands.clear();
        redoCommands.clear();

        // finally, notify listeners of the change
        CommandListener l;
        for (Iterator<CommandListener> i =
            commandListeners.iterator(); i.hasNext();) {

            l = i.next();
            l.commandCleared();
        }

    }

    /**
     * Set the maximum size of the command history
     *
     * @param size The new size to assign
     */
    @Override
    public void setSize(int size) {
        maxSize = size;
    }

    /**
     * Return the maximum size assigned to the history
     * @return the maximum size assigned to the history
     */
    @Override
    public int getSize() {
        return maxSize;
    }

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     * @return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     */
    @Override
    public String getUndoDescription() {
        if (canUndo() == true) {
            return undoCommands.peek().getDescription();
        } else {
            return null;
        }
    }

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     * @return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     */
    @Override
    public String getRedoDescription() {
        if (canRedo() == true) {
            return redoCommands.peek().getDescription();
        } else {
            return null;
        }
    }

    /**
     * Add a listener for Property changes. Duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addCommandHistoryListener(CommandListener l) {
        if (!commandListeners.contains(l)) {
            commandListeners.add(l);
        }
    }

    /**
     * Remove a listener for Property changes.
     *
     * @param l The listener.
     */
    @Override
    public void removeCommandHistoryListener(CommandListener l) {
        commandListeners.remove(l);
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = getDefaultReporter();
    }


}
