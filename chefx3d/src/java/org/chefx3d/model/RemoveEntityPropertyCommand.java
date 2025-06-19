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
 * A command for removing a property from an entity.
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public class RemoveEntityPropertyCommand implements Command {

    /** The entity */
    private Entity entity;

    /** The property sheet */
    private String propertySheet;

    /** The property name */
    private String propertyName;

    /** The original value */
    private Object originalValue;

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
     * @param entity The entity to change
     * @param propertySheet The property sheet changed.
     * @param propertyName The property which changed. A blank property name
     *        means the whole tree changed.
     * @param originalValue The original value.
     */
    public RemoveEntityPropertyCommand(Entity entity, String propertySheet,
            String propertyName, Object originalValue) {

        this.entity = entity;
        this.propertySheet = propertySheet;
        this.propertyName = propertyName;
        this.originalValue = originalValue;

        description = "RemoveEntityProperty -> " + entity.getName();

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
        entity.removeProperty(propertySheet, propertyName);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        entity.addProperty(propertySheet, propertyName, originalValue);
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
     * @return the transactionID
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
     * @param isLocal is this a local update is this a local update
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
