/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property.awt;

// External Imports
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;


// Internal Imports
import org.chefx3d.property.DataEditor;
import org.chefx3d.model.*;
import org.chefx3d.model.Entity;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An editor that add's properties to the tree.
 *
 * @author Alan Hudson
 * @version $Revision: 1.20 $
 */
public class AddPropertyEditor extends DataEditor implements UserDataHandler,
        ActionListener {

    /** The component to return to the editor */
    private JPanel component;

    /** The fragment to add */
    private Node fragment;

    /** The icon to use or null */
    private ImageIcon icon;

    /** The tooltip for the add button */
    private String tooltip;

    /** A unique ID for properties */
    //private int propertyID;

    /**
     * Create a Add Property Editor.
     *
     * @param icon The icon for the add button
     * @param tooltip
     * @param fragment The nodes to add when activated
     */
    public AddPropertyEditor(Image icon, String tooltip, Node fragment) {

        if (icon != null) {
            this.icon = new ImageIcon(icon);
        } else {
            this.icon = null;
        }
        this.fragment = fragment;
        this.tooltip = tooltip;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();

    }

    /**
     * Create a Add Property Editor.
     *
     * @param icon The icon for the add button
     * @param tooltip
     * @param fragment The nodes to add when activated
     */
    public AddPropertyEditor(ImageIcon icon, String tooltip, Node fragment) {

        this.icon = icon;
        this.fragment = fragment;
        this.tooltip = tooltip;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();
    }

    // ----------------------------------------------------------
    // Methods required by the ActionListener
    // ----------------------------------------------------------

    /**
     * Activated when a user performs an action, such as hitting the enter key
     *
     * @param e The event
     */
    @Override
    public void actionPerformed(ActionEvent e) {

//System.out.println("AddPropertyEditor.actionPerformed()");

        String newName;

        // get the list of properties
        Entity entity = model.getEntity(entityID);
        Document properties = entity.getProperties(sheet);
        NodeList nodes = null;

        // search the list for matching names
        try {

            XPath xpath = XPathFactory.newInstance().newXPath();
            nodes = (NodeList) xpath.evaluate(name, properties,
                    XPathConstants.NODESET);

        } catch (XPathExpressionException ex) {

            errorReporter.errorReport("Cannot find property in the DOM property sheet", ex);

        }

        // org.chefx3d.util.DOMUtils.print(properties);

        // set default value;
        newName = name;

        // if match create an incremented number
        if (nodes.getLength() > 1) {
            newName = name + "__" + (nodes.getLength() - 1);
        }

//System.out.println("    newName: " + newName);

        // store using name
        AddPropertyCommand cmd = new AddPropertyCommand(model, entityID, sheet,
                newName, fragment);

        model.applyCommand(cmd);

    }

    // ----------------------------------------------------------
    // Methods overriding Object
    // ----------------------------------------------------------
    /**
     * Make a clone of this object.
     *
     * @return A clone of the object
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        AddPropertyEditor o = (AddPropertyEditor) super.clone();

        o.initUI();

        return o;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

    @Override
    public String getValue() {

        return null;

    }

    @Override
    public void setValue(String value) {
    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
    }

    @Override
    public JPanel getComponent() {
        return component;
    }

    /**
     * Initialize the GUI.
     */
    private void initUI() {
/*
System.out.println("AddPropertyEditor.initUI()");
System.out.println("fragment: ");
org.chefx3d.util.DOMUtils.print(fragment);
*/
        // Create the panel
        component = new JPanel(new BorderLayout());

        // Create the button
        JButton button;
        if (icon == null)
            button = new JButton("Add");
        else {
            Dimension iconDimension = new Dimension(icon.getIconWidth() + 5,
                    icon.getIconHeight() + 5);
            button = new JButton(icon);
            component.setMaximumSize(iconDimension);
            component.setMinimumSize(iconDimension);
            component.setPreferredSize(iconDimension);
        }
        button.setToolTipText(tooltip);
        button.addActionListener(this);

        component.add(button, BorderLayout.CENTER);

    }
}