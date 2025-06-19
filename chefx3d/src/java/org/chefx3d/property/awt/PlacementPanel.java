/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005 - 2007
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
import com.zookitec.layout.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.property.DataEditor;
import org.chefx3d.property.DataValidator;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;


/**
 *
 * @author Russell Dodds
 * @version $Revision: 1.10 $
 *
 */
public class PlacementPanel extends JPanel {

    private static final int VGAP = 4;

    private static final int HGAP = 16;

    private static final EmptyBorder border = new EmptyBorder(0, 1, 0, 1);

    /** The world model */
    private WorldModel model;

    /** The current entityID */
    private Entity entity;

    /** Map name of item to value */
    private Map<String, TextFieldEditor> editorList;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * Constructor
     *
     * @param model
     * @param entity
     */
    public PlacementPanel(WorldModel model, Entity entity) {

        this.model = model;
        this.entity = entity;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        editorList = new HashMap<>();

        buildPropertyPanel();

    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Register an error reporter with the PlacementPanel instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    public void setPositionX(double x) {

        updateEditorValue("position/@x", x);

    }

    public void setPositionY(double y) {

        updateEditorValue("position/@y", y);

    }

    public void setPositionZ(double z) {

        updateEditorValue("position/@z", z);

    }

    public void setRotationX(float x) {

        updateEditorValue("rotation/@x", x);

    }

    public void setRotationY(float y) {

        updateEditorValue("rotation/@y", y);

    }

    public void setRotationZ(float z) {

        updateEditorValue("rotation/@z", z);

    }

    public void setRotationAngle(float angle) {

        updateEditorValue("rotation/@angle", angle);

    }

    private void updateEditorValue(String name, double value) {

        if (editorList.containsKey(name)) {
            TextFieldEditor text = editorList.get(name);
            text.setValue(String.valueOf(value));
        }

    }

    /**
     * Create the panel from the currently selected entity.
     *
     */
    private void buildPropertyPanel() {

        JPanel editor;

        // Setup the panel
        setName("Placement");
        setLayout(new PropertyPanelLayout());

        if ((entity != null) && (entity instanceof PositionableEntity)) {

            // add the position heading label
            JLabel posLabel = new JLabel("Position");
            posLabel.setBorder(border);

            ExplicitConstraints ec = new ExplicitConstraints(posLabel);
            ec.setX(ContainerEF.left(this).add(HGAP));
            ec.setY(ContainerEF.top(this).add(VGAP));

            // Add to the panel
            add(posLabel, ec);

            // Add the z, y, z coordinates
            double[] pos = new double[3];
            ((PositionableEntity)entity).getPosition(pos);

            editor = addPropertyEditor("position/@x", String.valueOf(pos[0]),
                    1, posLabel, DataEditor.Types.ENTITY_POSITION);
            editor = addPropertyEditor("position/@y", String.valueOf(pos[1]),
                    1, editor, DataEditor.Types.ENTITY_POSITION);
            editor = addPropertyEditor("position/@z", String.valueOf(pos[2]),
                    1, editor, DataEditor.Types.ENTITY_POSITION);

            // add the rotation heading label
            JLabel rotLabel = new JLabel("Rotation");
            rotLabel.setBorder(border);

            ec = new ExplicitConstraints(rotLabel);
            ec.setX(ContainerEF.left(this).add(HGAP));
            ec.setY(MathEF.add(ComponentEF.bottom(editor), VGAP));

            // Add to the panel
            add(rotLabel, ec);

            // Add the z, y, z coordinates
            float[] rot = new float[4];
            ((PositionableEntity)entity).getRotation(rot);

            editor = addPropertyEditor("rotation/@x", String.valueOf(rot[0]),
                    1, rotLabel, DataEditor.Types.ENTITY_ROTATION);
            editor = addPropertyEditor("rotation/@y", String.valueOf(rot[1]),
                    1, editor, DataEditor.Types.ENTITY_ROTATION);
            editor = addPropertyEditor("rotation/@z", String.valueOf(rot[2]),
                    1, editor, DataEditor.Types.ENTITY_ROTATION);
            editor = addPropertyEditor("rotation/@angle", String
                    .valueOf(rot[3]), 1, editor, DataEditor.Types.ENTITY_ROTATION);

        }

    }

    /**
     *
     * @param name
     * @param value
     * @param column
     * @param previousComponent
     */
    private JPanel addPropertyEditor(String name, String value, int column,
            JComponent previousComponent, DataEditor.Types type) {

        // parse the name
        String displayName = name.substring(name.indexOf("/@") + 2);

        // setup the editor
        TextFieldEditor text = new TextFieldEditor(value, displayName, type);
        text.setModel(model);
        text.setPropertyName(name);
        text.setEntityID(entity.getEntityID());

        DataValidator validator = new IsNumberValidator(IsNumberValidator.numberTypes.FLOAT);
        text.setValidator(validator);

        // add the editor
        ExplicitConstraints ec = new ExplicitConstraints(text.getComponent());
        ec.setX(MathEF.add(ContainerEF.left(this), (HGAP * (column + 1))));
        ec.setY(MathEF.add(ComponentEF.bottom(previousComponent), VGAP));
        add(text.getComponent(), ec);

        editorList.put(name, text);

        // remember the previous component for alignment
        return text.getComponent();
    }

}
