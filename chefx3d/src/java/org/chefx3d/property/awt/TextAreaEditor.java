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
 * An editor that allows text to be entered freely
 *
 * @author Russell Dodds
 * @version $Revision: 1.3 $
 */
public class TextAreaEditor extends DataEditor
    implements UserDataHandler, FocusListener {

    private static final int DEFAULT_ROWS = 3;

    private static final int MAX_TEXTFIELD_WIDTH = 24;

    /** The component to return to the editor */
    private JPanel component;

    /** The textfield for this editor */
    private JTextArea textarea;

    /** The label for this editor */
    private String label;

    /** The current value of this editor */
    private String currentValue;

    /** Allow edits true/false */
    private boolean isEnabled;

    /** Currently saving **/
    private boolean isSaving;

    /** Number of rows */
    private int rows;

    /**
     * Create a new TextArea editor
     *
     * @param initialValue the current value
     * @param rows The number of rows to display
     * @param label the label
     */
    public TextAreaEditor(String initialValue, int rows, String label) {

        this.rows = rows;
        this.currentValue = initialValue;
        this.label = label;
        this.isEnabled = true;
        isSaving = false;
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();
    }

    /**
     * Create a new TextArea editor
     *
     * @param initialValue the current value
     * @param rows The number of rows to display
     */
    public TextAreaEditor(String initialValue, int rows) {

        this(initialValue, rows, null);

    }

    /**
     * Create a new TextArea editor
     */
    public TextAreaEditor() {
        this("", DEFAULT_ROWS);
    }

    // ----------------------------------------------------------
    // Methods required by the FocusListener
    // ----------------------------------------------------------

    @Override
    public void focusGained(FocusEvent e) {

        // Save the currentValue for later retreval
        currentValue = getValue();

    }

    @Override
    public void focusLost(FocusEvent e) {

        // Save the change
        savePropertyChange();

    }

    // ----------------------------------------------------------
    // Methods overriding Object
    // ----------------------------------------------------------

    @Override
    public Object clone() throws CloneNotSupportedException {

        TextAreaEditor o = (TextAreaEditor) super.clone();

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

        // check to make sure the change is valid
        if (validator != null) {
            if (!validator.validate(newValue)) {
                JOptionPane.showMessageDialog(component,
                    validator.getMessage(),
                    "Data Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                setValue(currentValue);
                isSaving = false;
                return;
            }
        }

        Entity entity = model.getEntity(entityID);

        // create the command to send
        Command cmd = new ChangePropertyCommand(entity, sheet, name,
                currentValue, newValue);

        // apply the change to the model
        model.applyCommand(cmd);

        // Update the current value
        currentValue = newValue;

        isSaving = false;
    }

    @Override
    public String getValue() {

        return processValue(textarea.getText());

    }

    @Override
    public void setValue(String value) {

        textarea.setText(value);
        textarea.setCaretPosition(0);

    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        textarea.setEnabled(isEnabled);
    }

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

        // Create the textfield
        textarea = new JTextArea();

        if (label != null) {

            JLabel compLabel = new JLabel(label);
            compLabel.setBorder(new EmptyBorder(0, 1, 0, 4));
            component.add(compLabel, BorderLayout.WEST);

        }

        // add the textarea to the panel
        JScrollPane scrollTextarea = new JScrollPane(textarea);
        component.add(scrollTextarea, BorderLayout.CENTER);

        if (currentValue != null) {
            setValue(currentValue);
        }

        textarea.addFocusListener(this);
        textarea.setEnabled(isEnabled);
        textarea.setRows(rows);
        textarea.setColumns(MAX_TEXTFIELD_WIDTH);
        textarea.setLineWrap(true);
        textarea.setAutoscrolls(true);

    }

}