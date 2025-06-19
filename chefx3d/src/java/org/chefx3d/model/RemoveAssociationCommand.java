/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2006-2007
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
import java.io.OutputStream;

//Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A command for removing an association.
 *
 * @author Alan Hudson
 * @version $Revision: 1.13 $
 */
public class RemoveAssociationCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The new parent */
    private int parentID;

    /** The child */
    private int childID;

    /** Is this a local add */
    private boolean local;

    /** The description of the <code>Command</code> */
    private String description;

    /** The flag to indicate transient status */
    private boolean transientState;

    /** The flag to indicate undoable status */
    private boolean undoableState;

    /** The transactionID */
    private int transactionID;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Remove an association
     * @param model
     * @param parent
     * @param child
     */
    public RemoveAssociationCommand(WorldModel model, int parent, int child) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.parentID = parent;
        this.childID = child;

        local = true;

        init();
    }

    /**
     * Remove an association
     * @param model
     */
    public RemoveAssociationCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "RemoveAssociation -> " + parentID;

        transientState = false;
        undoableState = true;
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        model.unassociateEntities(local, parentID, childID, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        model.associateEntities(local, parentID, childID, null);
    }

    /**
     * Redo the affects of this command.
     */
    @Override
    public void redo() {
        execute();
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
        return transactionID;
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
             * <AddAssociationCommand entityID='1' px='' py='' pz='' rotx=''
             * roty='' rotz='' rota='' name='' />
             */

            // How to handle tool?
            // Right now this will use the Model.findTool service which assumes
            // local installs
            // Consider converting to JAXB
            /*
             * StringBuilder sbuff = new StringBuilder(); sbuff.append("<AddEntityCommand
             * entityID='"); sbuff.append(entityID); sbuff.append("' px='");
             * sbuff.append(pos[0]); sbuff.append("' py='");
             * sbuff.append(pos[1]); sbuff.append("' pz='");
             * sbuff.append(pos[2]); sbuff.append("' rotx='");
             * sbuff.append(rot[0]); sbuff.append("' roty='");
             * sbuff.append(rot[1]); sbuff.append("' rotz='");
             * sbuff.append(rot[2]); sbuff.append("' rota='");
             * sbuff.append(rot[3]); sbuff.append("' name='");
             * sbuff.append(tool.getName()); sbuff.append("' />");
             *
             * String st = sbuff.toString();
             *
             * PrintStream ps = new PrintStream(os); ps.print(st);
             */
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
        /*
         * Document doc = DOMUtils.parseXML(st);
         *
         * Element e = (Element) doc.getFirstChild(); String toolName =
         * e.getAttribute("name");
         *
         * System.out.println("ToolName: " + toolName); tool =
         * model.findTool(toolName); local = false;
         *
         * String d;
         *
         * pos = new double[3]; d = e.getAttribute("px"); pos[0] =
         * Double.parseDouble(d); d = e.getAttribute("py"); pos[1] =
         * Double.parseDouble(d); d = e.getAttribute("pz"); pos[2] =
         * Double.parseDouble(d);
         *
         * rot = new float[4];
         *
         * d = e.getAttribute("rotx"); rot[0] = Float.parseFloat(d); d =
         * e.getAttribute("roty"); rot[1] = Float.parseFloat(d); d =
         * e.getAttribute("rotz"); rot[2] = Float.parseFloat(d); d =
         * e.getAttribute("rota"); rot[3] = Float.parseFloat(d);
         *
         * entityID = Integer.parseInt(e.getAttribute("entityID"));
         */
        // local = false;
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