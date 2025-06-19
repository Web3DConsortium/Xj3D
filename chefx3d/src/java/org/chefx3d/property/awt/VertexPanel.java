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
 * @version $Revision: 1.8 $
 *
 */
public class VertexPanel extends JPanel {

    private static final int VGAP = 4;

    private static final int HGAP = 16;

    private static final EmptyBorder border = new EmptyBorder(0, 1, 0, 1);

    /** The world model */
    private WorldModel model;

    /** The current entityID */
    private Entity entity;

    /** Map name of item to value */
    private HashMap<String, DataEditor> editorList;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * Constructor
     *
     * @param model
     * @param entity
     */
    public VertexPanel(WorldModel model, Entity entity) {

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

    public void setVertexID(int vertexID) {

        updateEditorValue("vertex/@vertexID", vertexID);

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

    /*
    public void setRotationX(float x) {

        updateEditorValue("/ChefX3D/VertexParams/FenceVertex/rotation/@x", x);

    }

    public void setRotationY(float y) {

        updateEditorValue("/ChefX3D/VertexParams/FenceVertex/rotation/@y", y);

    }

    public void setRotationZ(float z) {

        updateEditorValue("/ChefX3D/VertexParams/FenceVertex/rotation/@z", z);

    }

    public void setRotationAngle(float angle) {

        updateEditorValue("/ChefX3D/VertexParams/FenceVertex/rotation/@angle", angle);

    }
    */

    private void updateEditorValue(String name, Object value) {

        if (editorList.containsKey(name)) {
            DataEditor text = editorList.get(name);
            text.setValue(String.valueOf(value));
        }

    }

    /**
     * Create the panel from the currently selected entity.
     *
     */
    private void buildPropertyPanel() {

        boolean updateY = true;
        boolean updatePosition = true;

        JPanel editor;

        // Setup the panel
        setName("Placement");
        setLayout(new PropertyPanelLayout());

        if ((entity != null) && (entity instanceof SegmentableEntity)) {

            int vertexID = ((SegmentableEntity)entity).getSelectedVertexID();

            // determine if we should allow the position to be updated
            if (((SegmentableEntity)entity).isFixedLength()) {
                updateY = false;
                updatePosition = false;

                /*
                SegmentSequence segments = entity.getSegmentSequence();
                if (!segments.isStart(vertexID) && !segments.isEnd(vertexID)) {
                    updatePosition = false;
                }
                */

            }

            // add the position heading label
            JLabel vertexLabel = new JLabel("FenceVertex");
            vertexLabel.setBorder(border);
            ExplicitConstraints ec = new ExplicitConstraints(vertexLabel);
            ec.setX(ContainerEF.left(this).add(HGAP));
            ec.setY(ContainerEF.top(this).add(VGAP));
            add(vertexLabel, ec);

            editor = addPropertyEditor("vertex/@vertexID",
                    String.valueOf(vertexID), 1, vertexLabel, DataEditor.Types.VERTEX_PROPERTY, false);

            // add the position heading label
            JLabel posLabel = new JLabel("Position");
            posLabel.setBorder(border);
            ec = new ExplicitConstraints(posLabel);
            ec.setX(MathEF.add(ContainerEF.left(this), (HGAP * 2)));
            ec.setY(MathEF.add(ComponentEF.bottom(editor), VGAP));
            add(posLabel, ec);

            // Add the z, y, z coordinates
            double[] pos = new double[] {0, 0, 0};
            if (entity instanceof PositionableEntity) {
                ((PositionableEntity)entity).getPosition(pos);
            }
            
            editor = addPropertyEditor("position/@x",
                    String.valueOf(pos[0]), 2, posLabel, DataEditor.Types.VERTEX_POSITION, updatePosition);
            editor = addPropertyEditor("position/@y",
                    String.valueOf(pos[1]), 2, editor, DataEditor.Types.VERTEX_POSITION, updateY);
            editor = addPropertyEditor("position/@z",
                    String.valueOf(pos[2]), 2, editor, DataEditor.Types.VERTEX_POSITION, updatePosition);

            /*
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
            entity.getRotation(rot);

            editor = addPropertyEditor("/ChefX3D/VertexParams/FenceVertex/rotation/@x",
                    String.valueOf(rot[0]), 1, rotLabel, TextFieldEditor.TYPE_ROTATION);
            editor = addPropertyEditor("/ChefX3D/VertexParams/FenceVertex/rotation/@y",
                    String.valueOf(rot[1]), 1, editor, TextFieldEditor.TYPE_ROTATION);
            editor = addPropertyEditor("/ChefX3D/VertexParams/FenceVertex/rotation/@z",
                    String.valueOf(rot[2]), 1, editor, TextFieldEditor.TYPE_ROTATION);
            editor = addPropertyEditor("/ChefX3D/VertexParams/FenceVertex/rotation/@angle",
                    String.valueOf(rot[3]), 1, editor, TextFieldEditor.TYPE_ROTATION);
            *
            */
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
            JComponent previousComponent, DataEditor.Types type, boolean isEnabled) {

        // parse the name
        String displayName = name.substring(name.indexOf("/@") + 2);

        // setup the editor
        TextFieldEditor text = new TextFieldEditor(value, displayName, type);
        text.setModel(model);
        text.setPropertyName(name);
        text.setEntityID(entity.getEntityID());
        text.setEnabled(isEnabled);

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

    /**
     *
     * @param name
     * @param column
     * @param previousComponent
     * @param isEnabled
     */
    /*
    private JPanel addCornerTypeEditor(String name, int column,
            JComponent previousComponent, boolean isEnabled) {

        String tooltip = "Corner Type";
        String[] cornerTypeValidValues = new String[] {SegmentVertex.DEFAULT_CORNER_TYPE, "End", "Corner"};

        // setup the editor
        ComboBoxEditor combo = new ComboBoxEditor(tooltip, cornerTypeValidValues,
                SegmentVertex.DEFAULT_CORNER_TYPE, "cornerType", DataEditor.Types.VERTEX_PROPERTY);

        combo.setModel(model);
        combo.setPropertyName(name);
        combo.setEntityID(entity.getEntityID());
        combo.setEnabled(isEnabled);

        // add the editor
        ExplicitConstraints ec = new ExplicitConstraints(combo.getComponent());
        ec.setX(MathEF.add(ContainerEF.left(this), (HGAP * (column + 1))));
        ec.setY(MathEF.add(ComponentEF.bottom(previousComponent), VGAP));
        add(combo.getComponent(), ec);

        editorList.put(name, combo);

        // remember the previous component for alignment
        return combo.getComponent();
    }
    */

}
