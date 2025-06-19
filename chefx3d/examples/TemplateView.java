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

// External Imports
import javax.swing.*;
import org.chefx3d.model.*;
import org.chefx3d.view.*;

import java.awt.BorderLayout;
import java.util.List;

import org.chefx3d.tool.Tool;

import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

// Internal Imports
// None yet

/**
 * A template view that can be copied to other applications and used as the base
 * for developing your own custom view.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class TemplateView extends JPanel
    implements View, ModelListener {

    /** The ViewManager */
    private ViewManager vmanager;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * View the WorldModel in a tree structure
     *
     * @param model The WorldModel that the tree is representing
     */
    public TemplateView(WorldModel model) {
        super(new BorderLayout());

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        vmanager = ViewManager.getViewManager();
        vmanager.addView(TemplateView.this);

        model.addModelListener(TemplateView.this);
    }

    //----------------------------------------------------------
    // Methods defined by View
    //----------------------------------------------------------

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
    }

    /**
     * Exit associate mode.
     */
    @Override
    public void disableAssociateMode() {
    }


    /**
     * Get the viewID. This shall be unique per view on all systems.
     *
     * @return The unique view ID
     */
    @Override
    public long getViewID() {
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

    /**
     * Return the property data in the required format
     */
    @Override
    public Object getComponent() {
        return this;
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by ModelListener
    //----------------------------------------------------------

    /**
     * An entity was added.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The unique entityID assigned by the view
     */
    @Override
    public void entityAdded(boolean local, Entity entity) {
    }

    @Override
    public void entityRemoved(boolean local, Entity entity) {
    }

    @Override
    public void masterChanged(boolean local, long viewID) {
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
     * The model has been reset.
     *
     * @param local Was this action initiated from the local UI
     */
    @Override
    public void modelReset(boolean local) {
        // ignore
    }

    //----------------------------------------------------------
    // Methods defined by EntityChangeListener
    //----------------------------------------------------------

    /**
     * A property changed.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propSheet
     * @param propName The property which changed
     * @param newValue The new value.
     */
    public void propertyChanged(boolean local, int entityID, String propSheet,
        String propName, Object newValue) {
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
    public void segmentAdded(boolean local, int entityID,
            int segmentID, int startVertexID, int endVertexID) {
    }

    /**
     * A segment was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param vertexID The starting vertexID
     */
    public void segmentSplit(boolean local, int entityID,
            int segmentID, int vertexID) {

        // ignored
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The segment removed
     */
    public void segmentRemoved(boolean local, int entityID,
            int segmentID) {
    }

    /**
     * A vertex was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param position The x position in world coordinates
     */
    public void segmentVertexAdded(boolean local, int entityID, int vertexID,
        double[] position) {
    }

    /**
     * A vertex was updated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param propertyName
     * @param propertySheet
     * @param propertyValue
     */
    public void segmentVertexUpdated(boolean local, int entityID, int vertexID,
        String propertyName, String propertySheet, String propertyValue) {
        // ignore
    }

    /**
     * A vertex was moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param position The x position in world coordinates
     */
    public void segmentVertexMoved(boolean local, int entityID, int vertexID,
        double[] position) {
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     */
    public void segmentVertexRemoved(boolean local, int entityID,
        int vertexID) {
    }

    /**
     * An entity has changed size.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param size The new size
     */
    public void entitySizeChanged(boolean local, int entityID, float[] size) {
        // ignore
    }

    /**
     * An entity was associated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    public void entityAssociated(boolean local, int parent, int child) {
        // ignore
    }

    /**
     * An entity was unassociated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    public void entityUnassociated(boolean local, int parent, int child) {
        // ignore
    }

    /**
     * The entity moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    public void entityMoved(boolean local, int entityID, double[] position) {
        // ignore
    }

    /**
     * The entity was scaled.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    public void entityScaled(boolean local, int entityID, float[] scale) {
        // ignore
    }

    /**
     * The entity was rotated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID
     * @param rotation The rotation(axis + angle in radians)
     */
    public void entityRotated(boolean local, int entityID, float[] rotation) {
        // ignore
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
}
