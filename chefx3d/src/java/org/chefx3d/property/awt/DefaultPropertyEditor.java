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
import java.util.*;
import javax.swing.*;
import org.w3c.dom.Node;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.property.PropertyEditor;
import org.chefx3d.view.ViewManager;
import org.chefx3d.tool.Tool;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A property editor.
 *
 * @author Alan Hudson
 * @version $Revision: 1.29 $
 */
public class DefaultPropertyEditor extends JScrollPane implements
        PropertyEditor, ModelListener, EntityChangeListener,
        PropertyStructureListener {

    /** List of property sheets to display by default */
    private final String[] DEFAULT_DISPLAY_PANELS =
        new String[] {"SMAL", "Cost", "Segment", "Vertex"};

    /** The world model */
    private WorldModel model;

    /** The ViewManager */
    private ViewManager vmanager;

    /** The number of element levels to skip */
    private int skipLevels;

    /** Are we in associateMode */
    private boolean associateMode;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /** List of panels to display */
    private String[] displayPanels;

    /** The default data editor to use */
    private Class<?> defaultEditor;

    /**
     * A default property editor sheet
     *
     * @param model
     * @param skipLevels
     */
    public DefaultPropertyEditor(WorldModel model, int skipLevels) {

        this.model = model;
        this.vmanager = ViewManager.getViewManager();
        this.skipLevels = skipLevels;

        associateMode = false;
        displayPanels = DEFAULT_DISPLAY_PANELS;
        defaultEditor = TextFieldEditor.class;

        // Create the default error reporter
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        model.addModelListener(DefaultPropertyEditor.this);
        model.addPropertyStructureListener(DefaultPropertyEditor.this);
    }

    /**
     *
     * @param model
     * @param skipLevels
     * @param displayPanels
     * @param defaultEditor
     */
    public DefaultPropertyEditor(WorldModel model, int skipLevels,
            String[] displayPanels, Class<?> defaultEditor) {

        this(model, skipLevels);

        this.displayPanels = displayPanels;
        this.defaultEditor = defaultEditor;

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
        associateMode = true;
    }

    /**
     * Exit associate mode.
     */
    @Override
    public void disableAssociateMode() {
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

    /**
     * Get the component used to render this.
     *
     * @return The component
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    // ----------------------------------------------------------
    // Methods required by ModelListener
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
     * An entity was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The id
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {

        buildPropertyPanel(-1, null);

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

        System.out.println("DefaultPropertyEditor.selectionChanged, associateMode: " + associateMode);
        System.out.println("    hashCode: " + this.hashCode());

        if (associateMode)
            return;

        // TODO: deal with multiple selected entities
        if (selection.isEmpty()) {

            // If nothing is selected then clear the tab
            buildPropertyPanel(-1, null);

        } else {

            // get the currently selected entity
            Selection selectedEntity = selection.get(0);
            int entityId = selectedEntity.getEntityID();

            if (entityId >= 0) {

                if (associateMode) {

                    associateMode = false;

                } else {

                    buildPropertyPanel(entityId, null);

                }

            } else {

                buildPropertyPanel(-1, null);

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
     * The model has been reset.
     *
     * @param local Was this action initiated from the local UI
     */
    @Override
    public void modelReset(boolean local) {
        // ignore
    }

    // ----------------------------------------------------------
    // Methods required by EntityChangeListener
    // ----------------------------------------------------------

    /**
     * An entity was associated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityAssociated(boolean local, int parent, int child) {
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
     * The entity was rotated.
     *
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
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
     * An entity has changed size.
     *
     * @param entityID The unique entityID assigned by the view
     * @param size The new size in meters
     */
    @Override
    public void entitySizeChanged(boolean local, int entityID, float[] size) {
        // ignore
    }

    /**
     * An entity was unassociated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityUnassociated(boolean local, int parent, int child) {
        // ignore
    }

    /**
     * A property changed.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propSheet
     * @param propName The property which changed
     * @param newValue The new value.
     */
    @Override
    public void propertyChanged(boolean local, int entityID, String propSheet,
            String propName, Object newValue) {

        // method handled by child PropertyData object

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
     * A segment was added to the sequence.
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
     * A vertex was added to an entity.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param position The x position in world coordinates
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID,
            int vertexID, double[] position) {
        // ignore
    }

    /**
     * A vertex was updated.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param propertyName The name of the property to update
     * @param propertyValue The value to set
     */
    @Override
    public void segmentVertexUpdated(boolean local, int entityID,
            int vertexID, String propertySheet, String propertyName, String propertyValue) {
        // ignore
    }

    /**
     * A vertex was moved.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param position The x position in world coordinates
     */
    @Override
    public void segmentVertexMoved(boolean local, int entityID,
            int vertexID, double[] position) {
        // ignore
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     */
    @Override
    public void segmentVertexRemoved(boolean local, int entityID,
            int vertexID) {
        // ignore
    }

    // ----------------------------------------------------------
    // Methods required by PropertyStructureListener
    // ----------------------------------------------------------

    /**
     * A property was added.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propSheet The property sheet which has the property
     * @param propName The property which changed
     * @param propValue
     */
    @Override
    public void propertyAdded(boolean local, int entityID, String propSheet,
            String propName, Node propValue) {

        buildPropertyPanel(entityID, propName);

    }

    /**
     * A property was removed.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The name of the property
     */
    @Override
    public void propertyRemoved(boolean local, int entityID,
            String propertySheet, String propertyName) {

        buildPropertyPanel(entityID, null);

    }

    // ----------------------------------------------------------
    // Methods required by PropertyEditor
    // ----------------------------------------------------------

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Create the panel from the currently selected entity.
     *
     */
    private void buildPropertyPanel(int entityId, String propName) {
        if (entityId > -1)
            model.addEntityChangeListener(model.getEntity(entityId), this);

        // now get the property sheet data
        PropertyData data =
            new PropertyData(model, vmanager, skipLevels, entityId, displayPanels, defaultEditor);

        data.setErrorReporter(errorReporter);

        // get the view port to add content
        JViewport viewPort = this.getViewport();

        JPanel propertyPanel = new JPanel();

        // now add content to the pane
        if (data.getComponent() instanceof HashMap) {
            propertyPanel = (JPanel) ((Map) data.getComponent())
                    .get(DEFAULT_DISPLAY_PANELS[0]);
        } else if (data.getComponent() instanceof JPanel) {
            propertyPanel = (JPanel) data.getComponent();
        }

        viewPort.add(propertyPanel);

        viewPort.revalidate();

    }

}
