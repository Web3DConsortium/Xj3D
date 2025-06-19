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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Internal Imports
import org.chefx3d.util.DOMUtils;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A command for adding a property
 *
 * @author Alan Hudson
 * @version $Revision 1.1 $
 */
public class AddPropertyCommand implements Command {

    /** The model */
    private BaseWorldModel model;

    /** The new entityID */
    private int entityID;

    /** The property sheet */
    private String propertySheet;

    /** The property name */
    private String propertyName;

    /** The property value */
    private Node propertyValue;

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
     * Add a property
     *
     * @param model The model to change
     * @param entityID The entity which changed
     * @param propertySheet The property sheet that changed.
     * @param propertyName The property name that changed.
     * @param propertyValue The Node fragment
     */
    public AddPropertyCommand(WorldModel model, int entityID,
            String propertySheet, String propertyName, Node propertyValue) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;
        this.entityID = entityID;
        this.propertySheet = propertySheet;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;

        local = true;

        init();

    }

    /**
     * Add a property
     *
     * @param model The model to change
     */
    public AddPropertyCommand(WorldModel model) {
        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "AddProperty -> " + propertyName;

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
        model.addProperty(local, entityID, propertySheet, propertyName,
                propertyValue, null);
    }

    /**
     * Undo the affects of this command.
     */
    @Override
    public void undo() {
        // RUSS: need to implement model.removeProperty
        errorReporter.messageReport("Undo not implemented for AddProperty");
    }

    /**
     * Redo the affects of this command.
     */
    @Override
    public void redo() {
        // execute();
        errorReporter.messageReport("Redo not implemented for AddProperty");
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
             * <AddPropertyCommand entityID='' propSheet=''>
             *  <propName></propName>
             *  <propValue></propValue>
             * </AddPropertyCommand>
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<AddPropertyCommand entityID='");
            sbuff.append(entityID);
            sbuff.append("' propSheet='");
            sbuff.append(propertySheet);
            sbuff.append("'>");
            sbuff.append("<propName>");
            sbuff.append(propertyName);
            sbuff.append("</propName>");
            sbuff.append("<propValue>");
            sbuff.append(propertyValue.toString());
            sbuff.append("</propValue>");
            sbuff.append("</AddPropertyCommand>");

            //System.out.println(sbuff.toString());

            PrintStream ps = new PrintStream(os);
            ps.print(sbuff.toString());

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
        propertySheet = e.getAttribute("propSheet");

        list = doc.getElementsByTagName("propName");
        e = (Element) list.item(0);
        propertyName = e.getTextContent();

        list = doc.getElementsByTagName("propValue");
        e = (Element) list.item(0);
        propertyValue = e;

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