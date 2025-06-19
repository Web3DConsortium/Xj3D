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

// External imports
import java.io.OutputStream;
import java.io.PrintStream;
import org.w3c.dom.*;

// Internal Imports
import org.chefx3d.util.DOMUtils;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A command for changing the master view.
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class ChangeMasterCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The new viewID */
    private long viewID;

    /** Is this a local add */
    private boolean local;

    /** The description of the <code>Command</code> */
    private String description;

    /** The flag to indicate transient status */
    private boolean transientState;

    /** The flag to indicate undoable status */
    private boolean undoableState;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Change the master view.
     *
     * @param model The model to change
     * @param viewID The unique viewID to set as master
     */
    public ChangeMasterCommand(WorldModel model, long viewID) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.viewID = viewID;

        local = true;

        init();
    }

    /**
     * Remove an entity.
     *
     * @param model The model to change
     */
    public ChangeMasterCommand(WorldModel model) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "ChangeMaster -> " + viewID;

        undoableState = false;
        transientState = false;
    }

    /**
     * Set the local flag.
     *
     * @param isLocal is this a local update

     */
    @Override
    public void setLocal(boolean isLocal) {
        local = isLocal;
    }

    /**
     * Is the command locally generated.
     *
     * @return Is local
     */
    @Override
    public boolean isLocal() {
        return local;
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        model.setMaster(local, viewID, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        errorReporter.messageReport("Undo not allowed for ChangeMaster");
    }

    /**
     * Redo the affects of this command.
     */
    @Override
    public void redo() {
        errorReporter.messageReport("Redo not allowed for ChangeMaster");
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
        switch (method) {
        case METHOD_XML:
            /*
             * <ChangeMasterCommand viewID='1' />
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<ChangeMasterCommand viewID='");
            sbuff.append(viewID);
            sbuff.append("' />");

            String st = sbuff.toString();

            PrintStream ps = new PrintStream(os);
            ps.print(st);
            break;
        case METHOD_XML_FAST_INFOSET:
            errorReporter.messageReport("Unsupported serialization method");
            break;
        }
    }

    /**
     * Deserialize a stream
     *
     * @param st The xml string to deserialize
     */
    @Override
    public void deserialize(String st) {
        Document doc = DOMUtils.parseXML(st);

        Element e = (Element) doc.getFirstChild();
        viewID = Long.parseLong(e.getAttribute("viewID"));

        local = false;
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