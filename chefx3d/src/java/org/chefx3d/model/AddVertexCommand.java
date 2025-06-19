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

// External Imports
import java.io.OutputStream;
import java.io.PrintStream;

//Internal Imports
import org.chefx3d.util.DOMUtils;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A command for adding a segment to an entity.
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class AddVertexCommand implements Command {

    /** The model */
    private BaseWorldModel model;

    /** The entityID */
    private int entityID;

    /** The vertexID */
    private int vertexID;

    /** The position */
    private double[] pos;

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
     * Add a segment to an entity.
     *
     * @param model The model to change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the SegmentSequence class
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    public AddVertexCommand(WorldModel model, int entityID, int vertexID,
            double[] position) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entityID = entityID;
        this.vertexID = vertexID;

        this.pos = new double[3];
        this.pos[0] = position[0];
        this.pos[1] = position[1];
        this.pos[2] = position[2];

        local = true;

        init();
    }

    /**
     * Add a segment to an entity.
     *
     * @param model The model to change
     */
    public AddVertexCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }


    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "AddVertex -> " + vertexID;

        undoableState = true;
        transientState = false;
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        model.addSegmentVertex(local, entityID, vertexID, pos, null);
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
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        model.removeSegmentVertex(local, entityID, vertexID, null);
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
     * Get the transient state of this <code>Command</code>.
     * @return
     */
    @Override
    public boolean isTransient() {
        return transientState;
    }

    /**
     * Set the transient state of this <code>Command</code>.
     * @param bool
     */
    public void setTransient(boolean bool) {
        transientState = bool;
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
             * <AddVertexCommand entityID='' vertexID=''
             *  px='' py='' pz='' />
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<AddVertexCommand entityID='");
            sbuff.append(entityID);
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

        entityID = Integer.parseInt(e.getAttribute("entityID"));
        vertexID = Integer.parseInt(e.getAttribute("vertexID"));

        String d;
        pos = new double[3];
        d = e.getAttribute("px");
        pos[0] = Double.parseDouble(d);
        d = e.getAttribute("py");
        pos[1] = Double.parseDouble(d);
        d = e.getAttribute("pz");
        pos[2] = Double.parseDouble(d);

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