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
 * @version $Revision: 1.19 $
 */
public class TextFieldEditor extends DataEditor
    implements UserDataHandler, ActionListener, FocusListener {

    private static final int MAX_TEXTFIELD_WIDTH = 14;

    /** The component to return to the editor */
    private JPanel component;

    /** The textfield for this editor */
    private JTextField textfield;

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
     * Create a new TextField editor
     *
     * @param initialValue
     * @param label the label
     * @param type The type is used to localize updates to the correct model item
     */
    public TextFieldEditor(String initialValue, String label, DataEditor.Types type) {

        this.type = type;
        this.currentValue = initialValue;
        this.label = label;
        this.isEnabled = true;
        isSaving = false;
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();
    }

    /**
     * Create a new TextField editor
     *
     * @param initialValue
     * @param label
     */
    public TextFieldEditor(String initialValue, String label) {

        this(initialValue, label, DataEditor.Types.ENTITY_PROPERTY);

    }

    /**
     * Create a new TextField editor
     *
     * @param initialValue
     */
    public TextFieldEditor(String initialValue) {

        this(initialValue, null, DataEditor.Types.ENTITY_PROPERTY);

    }

    /**
     * Create a new TextField editor
     *
     * @param initialValue
     * @param type The type is used to localize updates to the correct model item
     */
    public TextFieldEditor(String initialValue, DataEditor.Types type) {

        this(initialValue, null, type);

    }

    /**
     * Create a new TextField editor
     */
    public TextFieldEditor() {
        this("");
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

        TextFieldEditor o = (TextFieldEditor) super.clone();

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

        double[] currentPos;
        double[] newPos;
        float[] currentRot;
        float[] newRot;

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

        // everything is OK, process the change
        Entity entity = null;
        Command cmd = null;

        switch (type) {

        case ENTITY_POSITION:

            // get the current x, y, z values
            entity = model.getEntity(entityID);

            currentPos = new double[] {0, 0, 0};

            if (entity instanceof PositionableEntity) {
                ((PositionableEntity)entity).getPosition(currentPos);
            }
            newPos = currentPos.clone();

            switch (label) {
                case "x":
                    newPos[0] = Double.valueOf(getValue());
                    break;
                case "y":
                    newPos[1] = Double.valueOf(getValue());
                    break;
                case "z":
                    newPos[2] = Double.valueOf(getValue());
                    break;
            }

            // create the command to send
            cmd = new MoveEntityCommand(model, 0, entityID, newPos,
                    currentPos);

            break;

        case ENTITY_ROTATION:

            // get the current x, y, z, angle values
            entity = model.getEntity(entityID);
            currentRot = new float[] {0, 0, 0, 0};

            if (entity instanceof PositionableEntity) {
                ((PositionableEntity)entity).getRotation(currentRot);
            }
            newRot = currentRot.clone();

            switch (label) {
                case "x":
                    newRot[0] = Float.valueOf(getValue());
                    break;
                case "y":
                    newRot[1] = Float.valueOf(getValue());
                    break;
                case "z":
                    newRot[2] = Float.valueOf(getValue());
                    break;
                case "angle":
                    newRot[3] = Float.valueOf(getValue());
                    break;
            }

            // create the command to send
            cmd = new RotateEntityCommand(model, 0, entityID, newRot,
                    currentRot);

            break;

        case VERTEX_POSITION:

            if (entity instanceof SegmentableEntity) {
                // get the current x, y, z values
                entity = model.getEntity(entityID);

                int vertexID = ((SegmentableEntity)entity).getSelectedVertexID();
                SegmentVertex vertex = ((SegmentableEntity)entity).getSegmentSequence().getVertex(vertexID);

                currentPos = vertex.getPosition();
                newPos = currentPos.clone();

                switch (label) {
                    case "x":
                        newPos[0] = Double.valueOf(getValue());
                        break;
                    case "y":
                        newPos[1] = Double.valueOf(getValue());
                        break;
                    case "z":
                        newPos[2] = Double.valueOf(getValue());
                        break;
                }

    //System.out.println("TextFieldEditor.savePropertyChange()");
    //System.out.println("    x: " + newPos[0]);
    //System.out.println("    y: " + newPos[1]);
    //System.out.println("    z: " + newPos[2]);

                // create the command to send
                cmd = new MoveVertexCommand(model, 0, entityID, vertexID,
                        newPos, currentPos);
            }

            break;

        case VERTEX_ROTATION:

            errorReporter.messageReport("Vertex rotations not suported at this time.");
            break;


        case ENTITY_PROPERTY:

            entity = model.getEntity(entityID);

            // create the command to send
            cmd = new ChangePropertyCommand(entity, sheet, name,
                    currentValue, newValue);

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

        return processValue(textfield.getText());

    }

    @Override
    public void setValue(String value) {

        int columns = value.length();

        if (columns >= MAX_TEXTFIELD_WIDTH) {
            columns = MAX_TEXTFIELD_WIDTH;
        } else {
            columns += 1;
        }

        textfield.setColumns(columns);
        textfield.setText(value);
        textfield.setCaretPosition(0);

    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        textfield.setEnabled(isEnabled);
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

        // Create the textfield
        textfield = new JTextField();

        if (label != null) {

            JLabel compLabel = new JLabel(label);
            compLabel.setBorder(new EmptyBorder(0, 1, 0, 4));
            component.add(compLabel, BorderLayout.WEST);

        }

        // add the label to the panel
        component.add(textfield, BorderLayout.CENTER);

        if (currentValue != null) {
            setValue(currentValue);
        }

        textfield.addActionListener(this);
        textfield.addFocusListener(this);

        textfield.setEnabled(isEnabled);

    }

}