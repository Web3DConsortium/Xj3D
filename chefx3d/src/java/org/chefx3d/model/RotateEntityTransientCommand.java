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
import org.w3c.dom.*;

//Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.DOMUtils;

/**
 * A command for moving an entity.
 *
 * @author Alan Hudson
 * @version $Revision 1.1 $
 */
public class RotateEntityTransientCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The new entityID */
    private int entityID;

    /** The rotation */
    private float[] rot;

    /** Is this a local add */
    private boolean local;

    /** The description of the <code>Command</code> */
    private String description;

    /** The flag to indicate transient status */
    private boolean transientState;

    /** The transactionID */
    private int transactionID;

    /** The flag to indicate undoable status */
    private boolean undoableState;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Add an entity.
     *
     * @param model The model to change
     * @param transID The transactionID
     * @param entityID The unique entityID assigned by the view
     * @param rotation The rotation(axis + angle in radians)
     */
    public RotateEntityTransientCommand(WorldModel model, int transID,
            int entityID, float[] rotation) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        transactionID = transID;
        this.entityID = entityID;
        this.rot = new float[4];
        this.rot[0] = rotation[0];
        this.rot[1] = rotation[1];
        this.rot[2] = rotation[2];
        this.rot[3] = rotation[3];

        init();

        local = true;
    }

    public RotateEntityTransientCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        transientState = true;
        undoableState = false;
    }

    /**
     * Set the local flag.
     *
     * @param isLocal is this a local update Is this a local update
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
        model.rotateEntity(local, entityID, rot, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        // ignore
    }

    /**
     * Redo the affects of this command.
     */
    @Override
    public void redo() {
        // ignore
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
             * <RotateEntityCommand tID='' entityID='1' rx='' ry='' rz='' />
             */
            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<RotateEntityTransientCommand entityID='");
            sbuff.append(entityID);
            sbuff.append("' tID='");
            sbuff.append(transactionID);
            sbuff.append("' rx='");
            sbuff.append(String.format("%.3f", rot[0]));
            sbuff.append("' ry='");
            sbuff.append(String.format("%.3f", rot[1]));
            sbuff.append("' rz='");
            sbuff.append(String.format("%.3f", rot[2]));
            sbuff.append("' ra='");
            sbuff.append(String.format("%.3f", rot[3]));
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

        String d;

        rot = new float[4];

        d = e.getAttribute("rx");
        rot[0] = Float.parseFloat(d);
        d = e.getAttribute("ry");
        rot[1] = Float.parseFloat(d);
        d = e.getAttribute("rz");
        rot[2] = Float.parseFloat(d);
        d = e.getAttribute("ra");
        rot[3] = Float.parseFloat(d);

        entityID = Integer.parseInt(e.getAttribute("entityID"));
        transactionID = Integer.parseInt(e.getAttribute("tID"));

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