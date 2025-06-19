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

// Standard library imports
import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.UserDataHandler;

// Application specific imports
import org.chefx3d.model.*;
import org.chefx3d.property.BaseComboBoxEditor;
import org.chefx3d.property.DataEditor;

/**
 * An editor that allows selection from a combo box
 *
 * @author Alan Hudson
 * @version $Revision: 1.21 $
 */
public class ComboBoxEditor extends BaseComboBoxEditor implements
        UserDataHandler, ActionListener, FocusListener {

    /** The component to return to the editor */
    protected JPanel component;

    /** The combobox */
    protected JComboBox combo;

    /** The value of this editor */
    protected String currentValue;

    /** The label for this editor */
    protected String label;

    /**
     * Create a ComboBoxEditor
     *
     * @param tooltip The icon tooltip
     * @param validValues The valid values
     * @param initialValue The initial value
     * @param type
     */
    public ComboBoxEditor(String tooltip, String[] validValues,
            String initialValue, DataEditor.Types type) {

        super(tooltip, validValues, initialValue, type);

        initUI();

    }

    /**
     * Create a ComboBoxEditor.
     *
     * @param tooltip The icon tooltip
     * @param validValues The valid values
     * @param label The label to be displayed
     * @param initialValue The initial value
     * @param type
     */
    public ComboBoxEditor(String tooltip, String[] validValues,
            String initialValue, String label, DataEditor.Types type) {

        super(tooltip, validValues, initialValue, type);
        this.label = label;

        initUI();

    }

    /**
     * Create a ComboBoxEditor.
     *
     * @param tooltip The icon tooltip
     * @param validValues The valid values
     * @param mappedValues The values to actually return
     * @param initialValue The initial value
     * @param type
     */
    public ComboBoxEditor(String tooltip, String[] validValues,
            Map<String, String> mappedValues, String initialValue,
            DataEditor.Types type) {

        super(tooltip, validValues, mappedValues, initialValue, type);
        initUI();

    }

    // ----------------------------------------------------------
    // Methods required by the FocusListener
    // ----------------------------------------------------------
    /**
     * Activated when a user selects the TextField
     *
     * @param e The event
     */
    @Override
    public void focusGained(FocusEvent e) {

        // Save the currentValue for later retreval
        currentValue = getValue();

    }

    /**
     * Activated when a user moves cursor out of focus of the TextField
     *
     * @param e The event
     */
    @Override
    public void focusLost(FocusEvent e) {

        // Save the change
        savePropertyChange();

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

        // Save the change
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
        ComboBoxEditor o = (ComboBoxEditor) super.clone();

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

        // Get the value of the component
        String newValue = getValue();

        // only update if there has been a change
        if (!newValue.equals(currentValue)) {

            String final_val;
            Command cmd = null;

            if (mappedValues != null) {
                final_val = mappedValues.get(newValue);
                if (final_val == null) {
                    errorReporter.messageReport("No mapping for field: " + name
                            + " value: " + newValue);
                    final_val = newValue;
                }
            } else {
                final_val = newValue;
            }
//System.out.println("ComboBoxEditor.savePropertyChange()");
//System.out.println("    type: " + type.toString());
//System.out.println("    value: " + final_val);

            switch (type) {
                case ENTITY_PROPERTY:

                    Entity entity = model.getEntity(entityID);

                    cmd = new ChangePropertyCommand(entity, sheet,
                            name, currentValue, final_val);
                    break;
                case VERTEX_PROPERTY:
                    if (model.getEntity(entityID) instanceof SegmentableEntity) {
                        int vertexID = ((SegmentableEntity)model.getEntity(entityID)).getSelectedVertexID();

                        // create the command to send
                        cmd = new UpdateVertexCommand(model, entityID, vertexID, sheet,
                            name, currentValue, final_val);
                    }
                    break;
            }

            // apply the change to the model
            model.applyCommand(cmd);

            // Update the current value
            currentValue = final_val;

        }

    }

    @Override
    public JPanel getComponent() {
        return component;
    }

    @Override
    public String getValue() {

        String value = (String) combo.getSelectedItem();
        return processValue(value);

    }

    /**
     *
     */
    @Override
    public void setValue(String value) {

        if (mappedValues == null) {
            combo.setSelectedItem(value);
        } else {
            // Go find the value
            Iterator itr = mappedValues.entrySet().iterator();
            Map.Entry entry;
            String val;
            boolean found = false;

            while (itr.hasNext()) {
                entry = (Map.Entry) itr.next();
                val = (String) entry.getValue();
                if (val.equals(value)) {
                    combo.setSelectedItem(entry.getKey());
                    found = true;
                    break;
                }
            }

            if (!found)
                errorReporter.messageReport("Could not find value: " + value);
        }

    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        combo.setEditable(isEnabled);
    }

    /**
     * Initialize the GUI.
     */
    private void initUI() {

        component = new JPanel(new BorderLayout());
        combo = new JComboBox(validValues);

        if (label != null) {

            JLabel compLabel = new JLabel(label);
            compLabel.setBorder(new EmptyBorder(0, 1, 0, 4));
            component.add(compLabel, BorderLayout.WEST);

        }

        component.add(combo, BorderLayout.CENTER);

        if (initialValue != null) {
            setValue(initialValue);
        }

        combo.setToolTipText(tooltip);
        combo.addActionListener(this);


    }
}