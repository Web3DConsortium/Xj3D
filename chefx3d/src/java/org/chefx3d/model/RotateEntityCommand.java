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
public class RotateEntityCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The new entityID */
    private int entityID;

    /** The curent rotation */
    private float[] rot;

    /** The end rotation */
    private float[] endRot;

    /** The start rotation */
    private float[] startRot;

    /** Is this a local add */
    private boolean local;

    /** The transactionID */
    private int transactionID;

    /** The description of the <code>Command</code> */
    private String description;

    /** The flag to indicate transient status */
    private boolean transientState;

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
     * @param endRot
     * @param startRot The rotation(axis + angle in radians)
     */
    public RotateEntityCommand(WorldModel model, int transID, int entityID,
            float[] endRot, float[] startRot) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entityID = entityID;
        transactionID = transID;

        rot = new float[4];

        this.startRot = new float[4];
        this.startRot[0] = startRot[0];
        this.startRot[1] = startRot[1];
        this.startRot[2] = startRot[2];
        this.startRot[3] = startRot[3];

        this.endRot = new float[4];
        this.endRot[0] = endRot[0];
        this.endRot[1] = endRot[1];
        this.endRot[2] = endRot[2];
        this.endRot[3] = endRot[3];

        local = true;

        init();
    }

    public RotateEntityCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "RotateEntity -> " + entityID;

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
     * Get the transactionID for this command.
     *
     * @startRot the transactionID
     */
    @Override
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {

        // make the final move of the entity
        rot[0] = endRot[0];
        rot[1] = endRot[1];
        rot[2] = endRot[2];
        rot[3] = endRot[3];

        model.rotateEntity(local, entityID, rot, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {

        // System.out.println("RotateEntityCommand.undo()");
        // System.out.println(" rotate from: " + endRot[3]);
        // System.out.println(" rotate to: " + startRot[3]);

        rot[0] = startRot[0];
        rot[1] = startRot[1];
        rot[2] = startRot[2];
        rot[3] = startRot[3];

        model.rotateEntity(local, entityID, rot, null);
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
             * <RotateEntityCommand entityID='1' rotx='' roty='' rotz='' />
             */
            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<RotateEntityCommand entityID='");
            sbuff.append(entityID);
            sbuff.append("' tID='");
            sbuff.append(transactionID);
            sbuff.append("' rotx='");
            sbuff.append(rot[0]);
            sbuff.append("' roty='");
            sbuff.append(rot[1]);
            sbuff.append("' rotz='");
            sbuff.append(rot[2]);
            sbuff.append("' rota='");
            sbuff.append(rot[3]);
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
        startRot = new float[4];
        endRot = new float[4];

        d = e.getAttribute("rotx");
        rot[0] = Float.parseFloat(d);
        d = e.getAttribute("roty");
        rot[1] = Float.parseFloat(d);
        d = e.getAttribute("rotz");
        rot[2] = Float.parseFloat(d);
        d = e.getAttribute("rota");
        rot[3] = Float.parseFloat(d);

        endRot[0] = rot[0];
        endRot[1] = rot[1];
        endRot[2] = rot[2];
        endRot[3] = rot[3];

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