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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.*;

//Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.DOMUtils;

/**
 * A command for removing an entity.
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class RemoveEntityCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The entity to be removed */
    private Entity entity;

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
     * Remove an entity.
     *
     * @param model The model to change
     * @param entity The unique entity assigned by the view
     */
    public RemoveEntityCommand(WorldModel model, Entity entity) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entity = entity;

        local = true;

        init();
    }

    /**
     * Remove an entity.
     *
     * @param model The model to change
     */
    public RemoveEntityCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        if (entity != null) {
            description = "RemoveEntity -> " + entity.getEntityID();
        }

        transientState = false;
        undoableState = true;
    }


    /**
     * Set the local flag.
     *
     * @param isLocal is this a local update is this a local update
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
        model.removeEntity(local, entity, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        model.addEntity(local, entity, null);

        Selection selection = new Selection(-1, -1, -1);
        List<Selection> list = new ArrayList<>(1);
        list.add(selection);

        model.changeSelection(list);
    }

    /**
     * Undo the affects of this command.
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
     * @return The transactionID
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
             * <RemoveEntityCommand entityID='1' />
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<RemoveEntityCommand entityID='");
            sbuff.append(entity.getEntityID());
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
        entity = model.getEntity(Integer.parseInt(e.getAttribute("entityID")));

        local = false;

        init();

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
