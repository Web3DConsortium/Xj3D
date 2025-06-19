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

//Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.DOMUtils;

/**
 * A command for changing a property
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ChangePropertyTransientCommand implements Command {

    /** The new entity */
    private Entity entity;

    /** The property sheet */
    private String propertySheet;

    /** The property name */
    private String propertyName;

    /** The old value */
    private Object originalValue;

    /** The new value */
    private Object newValue;

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
     * Change a property
     *
     * @param entity The entity which changed
     * @param propertySheet The property sheet changed.
     * @param originalValue
     * @param propertyName The property which changed. A blank property name
     *        means the whole tree changed.
     * @param newValue The new value.
     */
    public ChangePropertyTransientCommand(Entity entity, String propertySheet,
            String propertyName, Object originalValue, Object newValue) {

/*
System.out.println("ChangePropertyTransientCommand()");
System.out.println("    entityID: " + entityID);
System.out.println("    propertySheet: " + propertySheet);
System.out.println("    propertyName: " + propertyName);
*/
        // Cast to package definition to access protected methods
        this.entity = entity;
        this.propertyName = propertyName;
        this.propertySheet = propertySheet;
        this.originalValue = originalValue;
        this.newValue = newValue;

        local = true;

        init();

    }

    public ChangePropertyTransientCommand(Entity entity) {

//System.out.println("ChangePropertyCommand network constructor");

        // Cast to package definition to access protected methods
        this.entity = entity;

        init();
    }

    /**
     * Common initialization code.
     */
    private void init() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        description = "ChangeProperty -> " + propertyName;

        undoableState = false;
       transientState = true;
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

        entity.setProperty(propertySheet, propertyName, newValue);

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
             * <ChangePropertyCommand entityID='' propSheet=''>
             *  <propName></propName>
             *  <originalValue></originalValue>
             *  <newValue></newValue>
             * </ChangePropertyCommand>
             */

            StringBuilder sbuff = new StringBuilder();
            sbuff.append("<ChangePropertyCommand entityID='");
            sbuff.append(entity.getEntityID());
            sbuff.append("' propSheet='");
            sbuff.append(propertySheet);
            sbuff.append("'>");
            sbuff.append("<propName>");
            sbuff.append(propertyName);
            sbuff.append("</propName>");
            sbuff.append("<originalValue>");
            sbuff.append(originalValue.toString());
            sbuff.append("</originalValue>");
            sbuff.append("<newValue>");
            sbuff.append(newValue.toString());
            sbuff.append("</newValue>");
            sbuff.append("</ChangePropertyCommand>");

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

        // TODO: need to serialize/deserialize an Entity correctly

        int entityID = Integer.parseInt(e.getAttribute("entityID"));
        propertySheet = e.getAttribute("propSheet");

        list = doc.getElementsByTagName("propName");
        e = (Element) list.item(0);
        propertyName = e.getTextContent();

        list = doc.getElementsByTagName("originalValue");
        e = (Element) list.item(0);
        originalValue = e.getTextContent();

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