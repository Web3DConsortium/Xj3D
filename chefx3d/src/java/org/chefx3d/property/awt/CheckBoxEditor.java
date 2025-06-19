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
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.UserDataHandler;

// Internal Imports
import org.chefx3d.property.DataEditor;
import org.chefx3d.model.*;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An editor that allows selection from a combo box
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class CheckBoxEditor extends DataEditor
    implements UserDataHandler, ItemListener {

    /** The component to return to the editor */
    private JPanel component;

    /** The textfield for this editor */
    private JCheckBox checkbox;

    /** The label for this editor */
    private String label;

    /** The editor updates what type of value */
    private DataEditor.Types type;

    /** The current value of this editor */
    private String currentValue;

    /** Allow edits true/false */
    private boolean isEnabled;

    /** Currently saving **/
    private boolean isSaving;

    /**
     * Create a new CheckBoxEditor.
     *
     * @param checked the current value of the field
     * @param label the label
     * @param type The type is used to localize updates to the correct model item
     *
     */
    public CheckBoxEditor(boolean checked, String label, DataEditor.Types type) {

        if (checked) {
            this.currentValue = "true";
        } else {
            this.currentValue = "false";
        }

        this.type = type;
        this.label = label;
        this.isEnabled = true;
        isSaving = false;
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();
    }

    /**
     * Create a new CheckBoxEditor.  By default
     * it updates entity properties
     *
     * @param checked the current value of the field
     * @param label the label
     *
     */
    public CheckBoxEditor(boolean checked, String label) {

        this(checked, label, DataEditor.Types.ENTITY_PROPERTY);

    }

    /**
     * Create a new CheckBoxEditor with no label.  By default
     * it updates entity properties
     *
     * @param checked the current value of the field
     *
     */
    public CheckBoxEditor(boolean checked) {

        this(checked, null, DataEditor.Types.ENTITY_PROPERTY);

    }

    /**
     * Create a new CheckBoxEditor with no label
     *
     * @param checked the current value of the field
     * @param type The type is used to localize updates to the correct model item
     */
    public CheckBoxEditor(boolean checked, DataEditor.Types type) {

        this(checked, null, type);

    }

    // ----------------------------------------------------------
    // Methods required by the ItemListener
    // ----------------------------------------------------------

    /**
     * Actived when a user performs an action
     *
     * @param e The event
     */
    @Override
    public void itemStateChanged(ItemEvent e) {

       savePropertyChange();

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

        CheckBoxEditor o = (CheckBoxEditor) super.clone();

        o.initUI();

        return o;
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Save the value of the JComponent that had focus last
     *
     */
    private void savePropertyChange() {

        // lets only save once, can be called twice if the user
        //   hits enter, the lostFocus is then also called if the
        //   errorWindow pops up
        if (isSaving) {
            return;
        } else {
            isSaving = true;
        }

        // Get the value of the component
        String newValue = getValue();

        // only update if there has been a change
        if (newValue.equals(currentValue)) {
            isSaving = false;
            return;
        }

        // everything is OK, process the change
        Command cmd = null;

        switch (type) {

        case ENTITY_PROPERTY:

            System.out.println("CheckBoxEditor.savePropertyChange()");
            System.out.println("    newValue: " + newValue);
            System.out.println("    currentValue: " + currentValue);

            Entity entity = model.getEntity(entityID);

            // create the command to send
            cmd = new ChangePropertyCommand(entity, sheet, name,
                    currentValue, newValue);

            break;

        case VERTEX_PROPERTY:
            if (model.getEntity(entityID) instanceof SegmentableEntity) {
                int vertexID = ((SegmentableEntity)model.getEntity(entityID)).getSelectedVertexID();

                // create the command to send
                cmd = new UpdateVertexCommand(model, entityID, vertexID, sheet,
                        name, currentValue, newValue);
            }
            break;

        }

        // apply the change to the model
        model.applyCommand(cmd);

        // Update the current value
        currentValue = newValue;

        isSaving = false;
    }

    /**
     *
     * @return
     */
    @Override
    public String getValue() {

        if (checkbox.isSelected()) {
            return processValue("true");
        } else {
            return processValue("false");
        }

    }

    @Override
    public void setValue(String value) {

        if (value.equals("true")) {
            checkbox.setSelected(true);
        } else {
            checkbox.setSelected(false);
        }

    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        checkbox.setEnabled(isEnabled);
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getComponent() {
        return component;
    }

    /**
     * Initialize the GUI.
     */
    private void initUI() {

        // Create the panel
        component = new JPanel(new BorderLayout());

        // Create the checkbox
        checkbox = new JCheckBox();

        if (label != null) {

            JLabel compLabel = new JLabel(label);
            compLabel.setBorder(new EmptyBorder(0, 1, 0, 4));
            component.add(compLabel, BorderLayout.WEST);

        }

        // add the checkbox to the panel
        component.add(checkbox, BorderLayout.CENTER);

        if (currentValue != null) {
            setValue(currentValue);
        }

        checkbox.addItemListener(this);
        checkbox.setEnabled(isEnabled);

    }

}