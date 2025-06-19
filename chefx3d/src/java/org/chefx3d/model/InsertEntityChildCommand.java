/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2006 - 2007
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

//External Imports
import java.io.OutputStream;

// Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A command for adding a child to an entity at a particular location.
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public class InsertEntityChildCommand implements Command {

    /** The parent entity */
    private Entity parent;

    /** The child entity */
    private Entity child;

    /** The index */
    private int index;

    /** The description of the <code>Command</code> */
    private String description;

    /** The flag to indicate transient status */
    private boolean transientState;

    /** The flag to indicate undoable status */
    private boolean undoableState;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Add a property to an entity.
     *
     * @param parent The entity to change
     * @param child The entity to add
     * @param index
     */
    public InsertEntityChildCommand(Entity parent, Entity child, int index) {

        this.parent = parent;
        this.child = child;
        this.index = index;

        description = "InsertEntityChild -> " + parent.getName();

        init();

    }

    /**
     * Common initialization.
     */
    private void init() {

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        undoableState = true;
        transientState = false;

    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        parent.insertChildAt(index, child);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        parent.removeChild(child);
    }

    /**
     * Redo the affects of this command.
     */
    @Override
    public void redo() {
        execute();
    }

    /**
     * Get the text description of this <code>Command</code>.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the text description of this <code>Command</code>.
     * @param desc
     */
    @Override
    public void setDescription(String desc) {
        description = desc;
    }

    /**
     * Get the state of this <code>Command</code>.
     * @return
     */
    @Override
    public boolean isTransient() {
        return transientState;
    }

    /**
     * Get the transactionID for this command.
     *
     * @return the transaction ID
     */
    @Override
    public int getTransactionID() {
        return 0;
    }

    /**
     * Is the command locally generated.
     *
     * @return Is local
     */
    @Override
    public boolean isLocal() {
        return false;
    }

    /**
     * Set the local flag.
     *
     * @param isLocal is this a local update

     */
    @Override
    public void setLocal(boolean isLocal) {
        // ignore
    }

    /**
     * Get the undo setting of this <code>Command</code>. true =
     * <code>Command</code> may be undone false = <code>Command</code> may
     * never undone
     * @return
     */
    @Override
    public boolean isUndoable() {
        return undoableState;
    }

    /**
     * Serialize this command.
     *
     * @param method What method should we use
     * @param os The stream to output to
     */
    @Override
    public void serialize(int method, OutputStream os) {
        errorReporter.messageReport("Networking Unsupported");
    }

    /**
     * Deserialize a stream
     *
     * @param st The xml string to deserialize
     */
    @Override
    public void deserialize(String st) {
        errorReporter.messageReport("Networking Unsupported");
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
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
