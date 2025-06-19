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

package org.chefx3d.property;

// External Imports
import org.w3c.dom.*;

// Internal Imports
import org.chefx3d.model.WorldModel;
import org.chefx3d.model.EntityChangeListener;
import org.chefx3d.AuthoringComponent;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * Specification of a data editor and its parameters for a field.
 *
 * @author Alan Hudson
 * @version $Revision: 1.19 $
 */
public abstract class DataEditor
    implements EntityChangeListener, Cloneable, UserDataHandler, AuthoringComponent {

    public static enum Types {ENTITY_PROPERTY, ENTITY_POSITION,
        ENTITY_ROTATION, VERTEX_PROPERTY, VERTEX_POSITION, VERTEX_ROTATION,
        SEGMENT_PROPERTY};

    /** The world model */
    protected WorldModel model;

    /** The entity this editor belongs to */
    protected int entityID;

    /** The property sheet */
    protected String sheet;

    /** The property name being edited */
    protected String name;

    /** The validator assigned */
    protected DataValidator validator;

    /** The field were editing */
    protected Node field;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    // ----------------------------------------------------------
    // Methods required by AuthoringComponent interface
    // ----------------------------------------------------------

    /**
     * Gets the UI component
     *
     */
    @Override
    public abstract Object getComponent();

    // ----------------------------------------------------------
    // EntityChangeListener
    // ----------------------------------------------------------
    /**
     * A segment was split
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param vertexID The starting vertexID
     */
    @Override
    public void segmentSplit(boolean local, int entityID,
            int segmentID, int vertexID) {

        // ignored
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
     * A vertex was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param position The x position in world coordinates
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID,
            int vertexID, double[] position) {

        // ignored
    }

    /**
     * A vertex was updated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param propertyName The name of the property to change
     * @param newValue The updated value
     */
    @Override
    public void segmentVertexUpdated(boolean local, int entityID, int vertexID,
            String propertySheet, String propertyName, String newValue) {

        // ignored
    }

    /**
     * A vertex was moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param newPosition The new position in world coordinates
     */
    @Override
    public void segmentVertexMoved(boolean local, int entityID,
            int vertexID, double[] newPosition) {

        // ignored
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     */
    @Override
    public void segmentVertexRemoved(boolean local, int entityID,
            int vertexID) {

        // ignored
    }

    /**
     * An entity has changed size.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param size The new size
     */
    @Override
    public void entitySizeChanged(boolean local, int entityID, float[] size) {

        // ignored
    }

    /**
     * An entity was associated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityAssociated(boolean local, int parent, int child) {

        // ignored
    }

    /**
     * An entity was unassociated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityUnassociated(boolean local, int parent, int child) {
        // ignored
    }

    /**
     * The entity moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    @Override
    public void entityMoved(boolean local, int entityID, double[] position) {
        // ignored
    }

    /**
     * The entity was scaled.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    @Override
    public void entityScaled(boolean local, int entityID, float[] scale) {
        // ignored
    }

    /**
     * The entity was rotated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
        // ignored
    }

    /**
     * A property changed.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The property that has changed
     * @param propertyValue The value being set.
     */
    @Override
    public void propertyChanged(boolean local, int entityID,
            String propertySheet, String propertyName, Object propertyValue) {

        if (propertyName.equals(name)) {
            if (field instanceof Attr) {
                Attr val = (Attr) field;
                setValue(val.getValue());
            } if (field instanceof Text) {
                Text val = (Text) field;
                setValue(val.getData());
            } else {
                System.err.println("Unsupported Node type in DataEditor");
            }
        }
    }

    // ----------------------------------------------------------
    // Methods required by UserDataHandler interface
    // ----------------------------------------------------------

    @Override
    public void handle(short operation, String key, Object data, Node src,
            Node dst) {

        switch (operation) {
        case UserDataHandler.NODE_CLONED:
        case UserDataHandler.NODE_ADOPTED:
        case UserDataHandler.NODE_IMPORTED:
            try {

                //System.out.println("UserDataHandler.handle");
                //System.out.println("    key: " + key);


                DataEditor[] origEditors = (DataEditor[]) data;
                int len = origEditors.length;

                //System.out.println("    len: " + len);

                DataEditor[] newEditors = new DataEditor[len];

                for (int i = 0; i < len; i++) {
                    newEditors[i] = (DataEditor) origEditors[i]
                            .clone();
                }

                dst.setUserData(key, newEditors, this);

            } catch (CloneNotSupportedException e) {
                // shouldn't happen
                errorReporter.errorReport(e.getClass().getName(), e);
            }
            break;
        default:
            System.err.println("*** Unhandled type: " + operation);
        }
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Get the current value of the DataEditor
     * @return the current value of the DataEditor
     */
    public abstract String getValue();

    /**
     * Set the value of the DataEditor
     * @param value the current value for the DataEditor
     */
    public abstract void setValue(String value);

    /**
     * Set the DataEditor's state, enabled/disabled
     * @param isEnabled the DataEditor's state, enabled/disabled
     */
    public abstract void setEnabled(boolean isEnabled);


    /**
     * Set the World Model being used.
     *
     * @param model The world model
     */
    public void setModel(WorldModel model) {
        this.model = model;
    }

    /**
     * Set the entity being edited.
     *
     * @param entityID The current entity
     */
    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    /**
     * Set the property sheet.
     *
     * @param name The sheet
     */
    public void setPropertySheet(String name) {
        this.sheet = name;
    }

    /**
     * Get the property name.
     *
     * @return The name
     */
    public String getPropertyName() {
        return name;
    }

    /**
     * Set the property name.
     *
     * @param name The name
     */
    public void setPropertyName(String name) {
        this.name = name;
    }

    /**
     * Get the validator.
     *
     * @return The validator
     */
    public DataValidator getValidator() {
        return validator;
    }

    /**
     * Set the validator.
     *
     * @param validator
     */
    public void setValidator(DataValidator validator) {
        this.validator = validator;
    }

    /**
     * Set the field this editor is modifying. An editor is responsible for
     * keeping this field up to date.
     *
     * @param field The field to update
     */
    public void setField(Node field) {
        this.field = field;
    }

    /**
     * Process the value after its been changed. This is a user hook to allow
     * post processing without having to rewrite the class.
     *
     * @param value The new value
     * @return The modified value.
     */
    public String processValue(String value) {
        return value.trim();
    }

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}