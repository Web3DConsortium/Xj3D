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

//Internal Imports
import org.chefx3d.util.ErrorReporter;

/**
 * A command to change the model.
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public interface Command {
    
    int METHOD_XML = 0;

    int METHOD_XML_FAST_INFOSET = 1;

    /**
     * Execute the <code>Command</code>.
     */
    void execute();

    /**
     * Undo the affects of this <code>Command</code>.
     */
    void undo();

    /**
     * Redo the affects of this <code>Command</code>.
     */
    void redo();

    /**
     * Get the text description of this <code>Command</code>.
     * @return
     */
    String getDescription();

    /**
     * Set the text description of this <code>Command</code>.
     * @param desc
     */
    void setDescription(String desc);

    /**
     * Return transient status.
     * @return
     */
    boolean isTransient();

    /**
     * Is the command locally generated.
     *
     * @return Is local
     */
    boolean isLocal();

    /**
     * Get the transactionID for this command.
     *
     * @return the transaction ID
     */
    int getTransactionID();

    /**
     * Set the local flag.
     *
     * @param isLocal is this a local update

     */
    void setLocal(boolean isLocal);

    /**
     * Return undoable status.
     * @return
     */
    boolean isUndoable();

    /**
     * Serialize this command.
     *
     * @param method What method should we use
     * @param os The stream to output to
     */
    void serialize(int method, OutputStream os);

    /**
     * Deserialize a stream.
     *
     * @param st The xml string to deserialize
     */
    void deserialize(String st);

    /**
     * Sets the ErrorReporter to use to display messages
     *
     * @param reporter
     */
    void setErrorReporter(ErrorReporter reporter);

}