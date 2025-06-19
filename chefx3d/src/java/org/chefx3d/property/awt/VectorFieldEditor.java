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
import java.awt.event.*;
import javax.swing.*;
import java.awt.Dimension;

import org.w3c.dom.UserDataHandler;

// Internal Imports
import org.chefx3d.property.DataEditor;
import org.chefx3d.model.*;
import org.chefx3d.util.DefaultErrorReporter;

import com.zookitec.layout.ComponentEF;
import com.zookitec.layout.ContainerEF;
import com.zookitec.layout.ExplicitConstraints;
import com.zookitec.layout.MathEF;

/**
 * An editor that allows for the editing of a vector (x, y, z)
 *
 * @author Russell Dodds
 * @version $Revision: 1.9 $
 */
public class VectorFieldEditor extends DataEditor
    implements UserDataHandler, ActionListener, FocusListener {

    private static final int MAX_TEXTFIELD_WIDTH = 24;

    private static final int VGAP = 4;

    private static final int HGAP = 16;

    /** The component to return to the editor */
    private JPanel component;

    /** The textfield for X */
    private JTextField textfieldX;

    /** The textfield for Y */
    private JTextField textfieldY;

    /** The textfield for Z */
    private JTextField textfieldZ;

    /** The editor updates what type of value */
    private DataEditor.Types type;

    /** The current value of this editor "x y z" */
    private String currentValue;

    /** Allow edits true/false */
    private boolean isEnabled;

    /** Currently saving **/
    private boolean isSaving;

    /**
     * Create a new Vector editor
     *
     * @param initialValue the current value of the vector "x y z"
     * @param type The type is used to localize updates to the correct model item
     */
    public VectorFieldEditor(String initialValue, DataEditor.Types type) {

        this.currentValue = initialValue;
        this.type = type;
        this.isEnabled = true;
        isSaving = false;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();
    }

    /**
     * Create a new Vector editor
     *
     * @param initialValue the current value of the vector "x y z"
     */
    public VectorFieldEditor(String initialValue) {

        this(initialValue, DataEditor.Types.ENTITY_PROPERTY);

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

    @Override
    public Object clone() throws CloneNotSupportedException {

        VectorFieldEditor o = (VectorFieldEditor) super.clone();

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
            if ((!validator.validate(textfieldX.getText())) ||
                (!validator.validate(textfieldY.getText())) ||
                (!validator.validate(textfieldZ.getText()))){

                JOptionPane.showMessageDialog(component,
                    validator.getMessage(),
                    "Data Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                setValue(currentValue);
                isSaving = false;
                return;
            }
        }

        //Entity entity = null;
        Command cmd = null;

        switch (type) {

        case ENTITY_PROPERTY:

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

    @Override
    public String getValue() {

        StringBuilder value = new StringBuilder();
        value.append(textfieldX.getText());
        value.append(" ");
        value.append(textfieldY.getText());
        value.append(" ");
        value.append(textfieldZ.getText());

        return processValue(value.toString());

    }

    @Override
    public void setValue(String value) {

        String[] newValue = value.split(" ");

        textfieldX.setColumns(calculateColumns(newValue[0]));
        textfieldX.setText(newValue[0]);
        textfieldX.setCaretPosition(0);

        textfieldY.setColumns(calculateColumns(newValue[1]));
        textfieldY.setText(newValue[1]);
        textfieldY.setCaretPosition(0);

        textfieldZ.setColumns(calculateColumns(newValue[2]));
        textfieldZ.setText(newValue[2]);
        textfieldZ.setCaretPosition(0);

    }

    /**
     * Calculate ho many columns to display
     *
     * @param value The string to insert into the textfield
     * @return The number of columns
     */
    private int calculateColumns(String value) {
        int columns = value.length();

        if (columns >= MAX_TEXTFIELD_WIDTH) {
            columns = MAX_TEXTFIELD_WIDTH;
        } else {
            columns += 1;
        }

        return columns;
    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        textfieldX.setEnabled(isEnabled);
        textfieldY.setEnabled(isEnabled);
        textfieldZ.setEnabled(isEnabled);
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
        component = new JPanel(new PropertyPanelLayout());
        component.setPreferredSize(new Dimension(100, 60));

        // add the x label
        JLabel xLabel = new JLabel("x");
        ExplicitConstraints ec = new ExplicitConstraints(xLabel);
        ec.setX(ContainerEF.left(component).add(0));
        ec.setY(ContainerEF.top(component).add(0));
        component.add(xLabel, ec);

        // add the textfieldX
        textfieldX = new JTextField();
        textfieldX.setColumns(10);
        textfieldX.setCaretPosition(0);
        textfieldX.addActionListener(this);
        textfieldX.addFocusListener(this);
        textfieldX.setEnabled(isEnabled);
        ec = new ExplicitConstraints(textfieldX);
        ec.setX(MathEF.add(ComponentEF.right(xLabel), VGAP));
        ec.setY(MathEF.add(ComponentEF.top(xLabel), 0));
        component.add(textfieldX, ec);

        // add the y label
        JLabel yLabel = new JLabel("y");
        ec = new ExplicitConstraints(yLabel);
        ec.setX(ContainerEF.left(component).add(0));
        ec.setY(MathEF.add(ComponentEF.bottom(xLabel), VGAP));
        component.add(yLabel, ec);

        // add the textfieldY
        textfieldY = new JTextField();
        textfieldY.setColumns(10);
        textfieldY.setCaretPosition(0);
        textfieldY.addActionListener(this);
        textfieldY.addFocusListener(this);
        textfieldY.setEnabled(isEnabled);
        ec = new ExplicitConstraints(textfieldY);
        ec.setX(MathEF.add(ComponentEF.right(yLabel), VGAP));
        ec.setY(MathEF.add(ComponentEF.top(yLabel), 0));
        component.add(textfieldY, ec);

        // add the z label
        JLabel zLabel = new JLabel("z");
        ec = new ExplicitConstraints(zLabel);
        ec.setX(ContainerEF.left(component).add(0));
        ec.setY(MathEF.add(ComponentEF.bottom(yLabel), VGAP));
        component.add(zLabel, ec);

        // add the textfieldZ
        textfieldZ = new JTextField();
        textfieldZ.setColumns(10);
        textfieldZ.setCaretPosition(0);
        textfieldZ.addActionListener(this);
        textfieldZ.addFocusListener(this);
        textfieldZ.setEnabled(isEnabled);
        ec = new ExplicitConstraints(textfieldZ);
        ec.setX(MathEF.add(ComponentEF.right(zLabel), VGAP));
        ec.setY(MathEF.add(ComponentEF.top(zLabel), 0));
        component.add(textfieldZ, ec);

        if (currentValue != null) {
            setValue(currentValue);
        }

    }

}