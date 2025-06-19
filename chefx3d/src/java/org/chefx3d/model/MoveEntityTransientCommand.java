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

//External Imports
import java.io.OutputStream;
import java.io.PrintStream;
import org.w3c.dom.*;

//Internal Imports
import org.chefx3d.util.DOMUtils;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A command for moving an entity.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class MoveEntityTransientCommand implements Command, DeadReckonedCommand {
    /** The model */
    private BaseWorldModel model;

    /** The new entityID */
    private int entityID;

    /** The position */
    private double[] pos;

    /** The velocity */
    private float[] linearVelocity;

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
     * @param transactionID The transactionID
     * @param entityID The unique entityID assigned by the view
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     * @param velocity The velocity vector
     */
    public MoveEntityTransientCommand(WorldModel model, int transactionID,
            int entityID, double[] position, float[] velocity) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entityID = entityID;
        this.transactionID = transactionID;
        this.pos = new double[3];
        this.pos[0] = position[0];
        this.pos[1] = position[1];
        this.pos[2] = position[2];
        linearVelocity = new float[3];
        linearVelocity[0] = velocity[0];
        linearVelocity[1] = velocity[1];
        linearVelocity[2] = velocity[2];

        local = true;

        init();
    }

    public MoveEntityTransientCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization method.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        transientState = true;
        undoableState = false;
    }

    // ----------------------------------------------------------
    // Methods required by DeadReckonedCommand
    // ----------------------------------------------------------
    /**
     * Get the dead reckoning params.
     *
     * @param position The position data
     * @param orientation The orientation data
     * @param lVelocity The linear velocity
     * @param aVelocity The angular velocity
     */
    @Override
    public void getDeadReckoningParams(double[] position, float[] orientation,
            float[] lVelocity, float[] aVelocity) {

        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];

        lVelocity[0] = linearVelocity[0];
        lVelocity[1] = linearVelocity[1];
        lVelocity[2] = linearVelocity[2];
    }

    /**
     * Set the dead reckoning params.
     *
     * @param position The position data
     * @param orientation The orientation data
     * @param lVelocity The linear velocity
     * @param aVelocity The angular velocity
     */
    @Override
    public void setDeadReckoningParams(double[] position, float[] orientation,
            float[] lVelocity, float[] aVelocity) {

        pos[0] = position[0];
        pos[1] = position[1];
        pos[2] = position[2];

        linearVelocity[0] = lVelocity[0];
        linearVelocity[1] = lVelocity[1];
        linearVelocity[2] = lVelocity[2];
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
     * Get the position.
     *
     * @param position The preallocated array to fill in
     */
    protected void getPosition(double[] position) {
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        model.moveEntity(local, entityID, pos, null);
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
             * <MoveEntityTransientCommand entityID='1' tID='' px='' py='' pz=''
             * vx='' vy='' vz=''/>
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<MoveEntityTransientCommand entityID='");
            sbuff.append(entityID);
            sbuff.append("' tID='");
            sbuff.append(transactionID);
            sbuff.append("' px='");
            sbuff.append(String.format("%.3f", pos[0]));
            sbuff.append("' py='");
            sbuff.append(String.format("%.3f", pos[1]));
            sbuff.append("' pz='");
            sbuff.append(String.format("%.3f", pos[2]));
            sbuff.append("' vx='");
            sbuff.append(String.format("%.3f", linearVelocity[0]));
            sbuff.append("' vy='");
            sbuff.append(String.format("%.3f", linearVelocity[1]));
            sbuff.append("' vz='");
            sbuff.append(String.format("%.3f", linearVelocity[2]));
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

        pos = new double[3];
        d = e.getAttribute("px");
        pos[0] = Double.parseDouble(d);
        d = e.getAttribute("py");
        pos[1] = Double.parseDouble(d);
        d = e.getAttribute("pz");
        pos[2] = Double.parseDouble(d);

        String f;

        linearVelocity = new float[3];
        f = e.getAttribute("vx");
        linearVelocity[0] = Float.parseFloat(f);
        f = e.getAttribute("vy");
        linearVelocity[1] = Float.parseFloat(f);
        f = e.getAttribute("vz");
        linearVelocity[2] = Float.parseFloat(f);

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