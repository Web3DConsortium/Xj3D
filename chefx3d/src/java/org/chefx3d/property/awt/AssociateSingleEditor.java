/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2006
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

//External Imports
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import javax.swing.*;

import org.w3c.dom.UserDataHandler;


// Internal Imports
import org.chefx3d.property.DataEditor;
import org.chefx3d.model.*;
import org.chefx3d.view.*;
import org.chefx3d.tool.Tool;
import org.chefx3d.view.ViewManager;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An editor that associates another entity. At the lowest level this is just an
 * entity reference. When this action is selected it will take the next
 * selection event matching the valid types as its value.
 *
 * @author Alan Hudson
 * @version
 */
public class AssociateSingleEditor extends DataEditor implements
        View, UserDataHandler, ActionListener, ModelListener, FocusListener {

    /** The view manager */
    protected ViewManager viewManager;

    /** The component to return to the editor */
    protected JPanel component;

    /** The text field */
    protected JTextField textfield;

    /** The icon to use or null */
    protected Icon icon;

    /** Are we waiting for a selection event */
    protected boolean associateMode;

    /** The valid types(Tool names) */
    protected String[] validTypes;

    /** The initial value */
    protected String initialValue;

    /** The current value */
    protected String currentValue;

    /** Currently saving **/
    protected boolean isSaving;

    /**
     * Create a Associate Single Editor.
     *
     * @param validTypes
     * @param icon The icon to use
     * @param initialValue
     */
    public AssociateSingleEditor(String[] validTypes, Icon icon,
            String initialValue) {

        this.initialValue = initialValue;

        if (validTypes != null) {
            this.validTypes = new String[validTypes.length];
            System.arraycopy(validTypes, 0, this.validTypes, 0,
                    validTypes.length);
        }

        this.icon = icon;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        isSaving = false;

        initUI();
    }

    /**
     * Create a Associate Single Editor.
     *
     * @param validTypes
     * @param icon The icon to use
     * @param initialValue
     */
    public AssociateSingleEditor(String[] validTypes, Image icon,
            String initialValue) {

        this(validTypes, icon != null ? new ImageIcon(icon) : null, initialValue);
    }

    /**
     * Set the parent windowing system component.
     *
     * @param parent The parent
     */
    public void setParent(Object parent) {
    }

    // ----------------------------------------------------------
    // Methods required by View
    // ----------------------------------------------------------

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    @Override
    public void setTool(Tool tool) {
        // ignore
    }

    /**
     * Go into associate mode. The next selection in any view will issue a
     * selection event and do nothing else.
     *
     * @param validTools A list of the valid tools. null string will be all
     *        valid. empty string will be none.
     */
    @Override
    public void enableAssociateMode(String[] validTools) {
        //System.out.println("AssociateSingleEditor.enableAssociateMode()");
        //System.out.println("    hashCode: " + this.hashCode());

        associateMode = true;
    }

    /**
     * Exit associate mode.
     */
    @Override
    public void disableAssociateMode() {
        //System.out.println("AssociateSingleEditor.disableAssociateMode()");
        //System.out.println("    hashCode: " + this.hashCode());

        associateMode = false;
    }


    /**
     * Get the viewID. This shall be unique per view on all systems.
     *
     * @return The unique view ID
     */
    @Override
    public long getViewID() {
        // TODO: What to do here
        return -1;
    }

    /**
     * Control of the view has changed.
     *
     * @param newMode The new mode for this view
     */
    @Override
    public void controlChanged(int newMode) {
        // ignore
    }

    /**
     * Set how helper objects are displayed.
     *
     * @param mode The mode
     */
    @Override
    public void setHelperDisplayMode(int mode) {
        // ignore
    }

    // ----------------------------------------------------------
    // Methods required by the ModelListener interface
    // ----------------------------------------------------------

    /**
     * An entity was added.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The unique entityID assigned by the view
     */
    @Override
    public void entityAdded(boolean local, Entity entity) {
        // ignore
    }

    /**
     * A segment was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param startVertexID The starting vertexID
     * @param endVertexID The starting vertexID
     */
    @Override
    public void segmentAdded(boolean local, int entityID,
            int segmentID, int startVertexID, int endVertexID) {

        // ignored
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The segment removed
     */
    @Override
    public void segmentRemoved(boolean local, int entityID,
            int segmentID) {

        // ignored
    }

    /**
     * A segment was added to an entity.
     *
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID assigned by the view
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID, int segmentID,
            double[] position) {
        // ignore
    }

    /**
     * An entity was removed.
     *
     * @param entity The id
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {
        // ignore
    }

    /**
     * A segment was removed.
     *
     * @param local
     * @param entityID The id
     * @param segmentID The segment id
     * @param position
     */
    public void segmentVertexRemoved(boolean local, int entityID,
            int segmentID, double[] position) {
        // ignore
    }

    /**
     * An entity has changed size.
     *
     * @param entityID The unique entityID assigned by the view
     * @param size The new size
     */
    @Override
    public void entitySizeChanged(boolean local, int entityID, float[] size) {
        // ignore
    }

    /**
     * An entity was associated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityAssociated(boolean local, int parent, int child) {
        //System.out.println("AssociateSingleEditor.entityAssociated()");
        associateMode = false;
    }

    /**
     * An entity was unassociated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityUnassociated(boolean local, int parent, int child) {
        //System.out.println("AssociateSingleEditor.entityUnassociated()");
        associateMode = false;
    }

    /**
     * The entity moved.
     *
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    @Override
    public void entityMoved(boolean local, int entityID, double[] position) {
        // ignore
    }

    /**
     * The entity was scaled.
     *
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    @Override
    public void entityScaled(boolean local, int entityID, float[] scale) {
        // ignore
    }

    /**
     * The entity was rotated.
     *
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
        // ignore
    }

    /**
     * The entity was selected.
     *
     * @param selection The list of selected entities. The last one is the
     *        latest.
     */
    @Override
    public void selectionChanged(List<Selection> selection) {

//System.out.println("AssociateSingleEditor.selectionChanged, associateMode: " + associateMode);
        //System.out.println("    hashCode: " + this.hashCode());

        if (associateMode) {

            if (selection.isEmpty())
                return;

            // Stick with the first selected as the property to edit
            Selection sel = selection.get(0);

            // TODO: Need to handle sub-selections
            Entity entity = model.getEntity(sel.getEntityID());

//System.out.println("    entityID: " + sel.getEntityID());

            if ((entity.getEntityID() < 0) ||
                (entity.getType() == Tool.TYPE_WORLD)) {

                errorReporter.messageReport("Cannot associate using the entity provided");

            } else {

                setValue(Integer.toString(entity.getEntityID()));

                processNewValue();

                currentValue = Integer.toString(entity.getEntityID());

                viewManager.disableAssociateMode();
                viewManager.removeView(this);

            }
        }
    }

    /**
     * User view information changed.
     *
     * @param pos The position of the user
     * @param rot The orientation of the user
     * @param fov The field of view changed(X3D Semantics)
     */
    @Override
    public void viewChanged(boolean local, double[] pos, float[] rot, float fov) {
        // ignore
    }

    /**
     * The master view has changed.
     *
     */
    @Override
    public void masterChanged(boolean local, long viewID) {
        // ignore
    }

    /**
     * The model has been reset.
     *
     * @param local Was this action initiated from the local UI
     */
    @Override
    public void modelReset(boolean local) {
        // ignore
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

    /**
     *
     * @return The UI component to extract the value from
     */
    @Override
    public String getValue() {
        return processValue(textfield.getText());
    }

    @Override
    public void setValue(String value) {
        textfield.setText(value);
    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        textfield.setEnabled(isEnabled);
    }

    /**
     *
     */
    @Override
    public JPanel getComponent() {
        return component;
    }

    // ----------------------------------------------------------
    // Methods required by the ActionListener interface
    // ----------------------------------------------------------
    /**
     * Process a new value.
     */
    protected void processNewValue() {

        // Save the change
        savePropertyChange();

    }

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

//System.out.println("AssociateSingleEditor.actionPerformed(ActionEvent evt)");
//System.out.println("    evt.getSource(): " + evt.getSource());

        if (evt.getSource() == textfield) {
            // Save the change
            savePropertyChange();
        } else {
            model.addModelListener(this);
            viewManager = ViewManager.getViewManager();
            viewManager.addView(this);

            //System.out.println("AssociateSingleEditor.actionPerformed()");
            viewManager.enableAssociateMode(validTypes);

            // Save the currentValue for later retreval
            currentValue = getValue();
//System.out.println("    currentValue: " + currentValue);
        }

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
        AssociateSingleEditor o = (AssociateSingleEditor) super.clone();

        o.validTypes = validTypes;
        o.initUI();

        return o;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

    /**
     * Save the value of the JComponent that had focus last
     */
    private void savePropertyChange() {

//System.out.println("AssociateSingleEditor.savePropertyChange()");

        // lets only save once, can be called twice if the user
        // hits enter, the lostFocus is then also called if the
        // errorWindow pops up
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
        validator = new InRangeValidator(-1, Integer.MAX_VALUE);

        if (!validator.validate(newValue)) {
            JOptionPane.showMessageDialog(component,
                validator.getMessage(),
                "Data Validation Error",
                JOptionPane.ERROR_MESSAGE);
            setValue(String.valueOf(currentValue));
            isSaving = false;
            return;
        }

        // check it is an entity in the system and that it is the valid type
        Entity associateEntity = model.getEntity(Integer.parseInt(newValue));
        if (associateEntity == null) {
            JOptionPane.showMessageDialog(component,
                    "The data specified does not match an entity in the world.",
                    "Data Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                setValue(String.valueOf(currentValue));
                isSaving = false;
                return;
        }  else {

            StringBuilder validNames = new StringBuilder();
            boolean valid = false;
            for (int i = 0; i < validTypes.length; i++) {
                validNames.append(validTypes[i]);
                if (i < validTypes.length - 1) {
                    validNames.append(", ");
                }
                if (associateEntity.getName().equals(validTypes[i])) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                JOptionPane.showMessageDialog(component,
                        "The data specified does not match a " +
                        "valid associate type\n [" +
                                validNames.toString() + "].",
                        "Data Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    setValue(String.valueOf(currentValue));
                    isSaving = false;
                    return;
            }
        }

        Entity entity = model.getEntity(entityID);

        // everything is OK, process the change
        ChangePropertyCommand cmd =
            new ChangePropertyCommand(entity, sheet, name, currentValue, newValue);

        model.applyCommand(cmd);

//System.out.println("AssociateSingleEditor.savePropertyChange()");
//System.out.println("    entityID: " + entityID);
//System.out.println("    newValue: " + newValue);

        AddAssociationCommand cmd2 = new AddAssociationCommand(model, entityID,
                Integer.parseInt(newValue));

        model.applyCommand(cmd2);

        isSaving = false;
    }


    /**
     * Initialize the GUI.
     */
    private void initUI() {

//System.out.println("AssociateSingleEditor.initUI()");

        // Create the panel
        component = new JPanel(new BorderLayout());

        textfield = new JTextField(initialValue, 3);
        component.add(textfield, BorderLayout.CENTER);

        JButton butt;
        if (icon == null)
            butt = new JButton("Associate");
        else {
            butt = new JButton(icon);
            Dimension iconDimension = new Dimension(icon.getIconWidth() + 5,
                    icon.getIconHeight() + 5);
            butt.setMaximumSize(iconDimension);
            butt.setPreferredSize(iconDimension);
        }
        butt.setToolTipText("Select an entity as the value");
        butt.addActionListener(this);
        textfield.addActionListener(this);
        textfield.addFocusListener(this);

        component.add(butt, BorderLayout.EAST);
    }
}