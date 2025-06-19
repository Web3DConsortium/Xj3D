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
import org.chefx3d.util.DOMUtils;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A command for moving a segment sequence vertex.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class MoveVertexCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The new entityID */
    private int entityID;

    /** The vertexID */
    private int vertexID;

    /** The starting position */
    private double[] startPos;

    /** The ending position */
    private double[] endPos;

    /** The position */
    private double[] pos;

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
     * @param vertexID The unique vertexID assigned by the SegmentSequence
     * @param endPosition The end position in world coordinates(meters, Y-UP,
     *        X3D System).
     * @param startPosition The start position in world coordinates(meters,
     *        Y-UP, X3D System).
     */
    public MoveVertexCommand(WorldModel model, int transID, int entityID, int vertexID,
            double[] endPosition, double[] startPosition) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entityID = entityID;
        this.vertexID = vertexID;
        this.transactionID = transID;

        this.pos = new double[3];

        this.startPos = new double[3];
        this.startPos[0] = startPosition[0];
        this.startPos[1] = startPosition[1];
        this.startPos[2] = startPosition[2];

        this.endPos = new double[3];
        this.endPos[0] = endPosition[0];
        this.endPos[1] = endPosition[1];
        this.endPos[2] = endPosition[2];

        local = true;

        init();
    }

    public MoveVertexCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "MoveVertex -> " + vertexID;

        undoableState = true;
        transientState = false;
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
        // make the final move of the entity
        this.pos[0] = endPos[0];
        this.pos[1] = endPos[1];
        this.pos[2] = endPos[2];

        model.moveSegmentVertex(local, entityID, vertexID, pos, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        // return the entity to the starting location
        this.pos[0] = startPos[0];
        this.pos[1] = startPos[1];
        this.pos[2] = startPos[2];

        model.moveSegmentVertex(local, entityID, vertexID, pos, null);
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
             * <MoveEntityCommand entityID='1' px='' py='' pz='' cornerType='' />
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<MoveVertexCommand entityID='");
            sbuff.append(entityID);
            sbuff.append("' tID='");
            sbuff.append(transactionID);
            sbuff.append("' vertexID='");
            sbuff.append(vertexID);
            sbuff.append("' px='");
            sbuff.append(pos[0]);
            sbuff.append("' py='");
            sbuff.append(pos[1]);
            sbuff.append("' pz='");
            sbuff.append(pos[2]);
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
        endPos = new double[3];
        startPos = new double[3];

        d = e.getAttribute("px");
        pos[0] = Double.parseDouble(d);
        d = e.getAttribute("py");
        pos[1] = Double.parseDouble(d);
        d = e.getAttribute("pz");
        pos[2] = Double.parseDouble(d);

        endPos[0] = pos[0];
        endPos[1] = pos[1];
        endPos[2] = pos[2];

        entityID = Integer.parseInt(e.getAttribute("entityID"));
        vertexID = Integer.parseInt(e.getAttribute("vertexID"));
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