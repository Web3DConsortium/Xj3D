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
 * A command for updating the segment sequence vertex.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class UpdateVertexCommand implements Command {
    /** The model */
    private BaseWorldModel model;

    /** The new entityID */
    private int entityID;

    /** The vertexID */
    private int vertexID;

    /** The propertySheet */
    private String propertySheet;

    /** The propertyName */
    private String propertyName;

    /** The origValue */
    private String origValue;

    /** The newValue */
    private String newValue;

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
     * Update the property of a vertex.
     *
     * @param model The model to change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the SegmentSequence
     * @param propertySheet The name of the sheet to save to
     * @param propertyName The name of the property being saved (xpath)
     * @param origValue The current value so we can undo to this value
     * @param newValue The new value to set
     */
    public UpdateVertexCommand(WorldModel model, int entityID, int vertexID,
            String propertySheet, String propertyName, String origValue, String newValue) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entityID = entityID;
        this.vertexID = vertexID;

        this.propertySheet = propertySheet;
        this.propertyName = propertyName;
        this.origValue = origValue;
        this.newValue = newValue;

        local = true;

        init();
    }

    public UpdateVertexCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "UpdateVertex -> " + vertexID;

        undoableState = true;
        transientState = false;
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
        model.updateSegmentVertex(local, entityID, vertexID, propertySheet, propertyName, newValue, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        model.updateSegmentVertex(local, entityID, vertexID, propertySheet, propertyName, origValue, null);
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
            sbuff.append("<UpdateVertexCommand entityID='");
            sbuff.append(entityID);
            sbuff.append("' vertexID='");
            sbuff.append(vertexID);
            sbuff.append("' propSheet='");
            sbuff.append(propertySheet);
            sbuff.append("'>");
            sbuff.append("<propName>");
            sbuff.append(propertyName);
            sbuff.append("</propName>");
            sbuff.append("<originalValue>");
            sbuff.append(origValue);
            sbuff.append("</originalValue>");
            sbuff.append("<newValue>");
            sbuff.append(newValue);
            sbuff.append("</newValue>");
            sbuff.append("</UpdateVertexCommand>");

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
        NodeList list;

        Document doc = DOMUtils.parseXML(st);

        Element e = (Element) doc.getFirstChild();

        entityID = Integer.parseInt(e.getAttribute("entityID"));
        vertexID = Integer.parseInt(e.getAttribute("vertexID"));
        propertySheet = e.getAttribute("propSheet");

        list = doc.getElementsByTagName("propName");
        e = (Element) list.item(0);
        propertyName = e.getTextContent();

        list = doc.getElementsByTagName("originalValue");
        e = (Element) list.item(0);
        origValue = e.getTextContent();

        list = doc.getElementsByTagName("newValue");
        e = (Element) list.item(0);
        newValue = e.getTextContent();

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