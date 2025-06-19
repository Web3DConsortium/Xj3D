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
import org.chefx3d.tool.Tool;
import org.chefx3d.view.ViewManager;
import org.chefx3d.property.PropertyEditor;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A multiple tab property editor.
 *
 * @author Russell Dodds
 * @version $Revision: 1.38 $
 */
public class MultiTabPropertyEditor extends JScrollPane
    implements PropertyEditor, EntityChangeListener, PropertyStructureListener,
        ModelListener, TabMenuItems {

    /** List of property sheets to display by default */
    private final String[] DEFAULT_DISPLAY_PANELS = {"SMAL", "Cost", "Segment", "Vertex"};

    /** The world model */
    private WorldModel model;

    /** The ViewManager */
    private ViewManager vmanager;

    /** The number of element levels to skip */
    private int skipLevels;

    /** Are we in associateMode */
    private boolean associateMode;

    /** Display the placement tab */
    private boolean showPlacement;

    /** A panel of position and rotation */
    private PlacementPanel placementPanel;

    /** Display the vertex tab */
    private boolean showVertex;

    /** A panel of vertex information */
    private VertexPanel vertexPanel;

    /** A tabbed panel for displaying various property sheets */
    private JTabbedPane tabbedPane;

    private JCheckBoxMenuItem[] menuItems;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /** The name of the currently selected tab */
    private String selectedTab;

    /** List of panels to display */
    private String[] displayPanels;

    /** The default data editor to use */
    private Class defaultEditor;

    /**
     *
     * @param model
     * @param skipLevels
     */
    public MultiTabPropertyEditor(WorldModel model, int skipLevels) {

        this.model = model;
        this.vmanager = ViewManager.getViewManager();
        this.skipLevels = skipLevels;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        associateMode = false;
        displayPanels = DEFAULT_DISPLAY_PANELS;
        defaultEditor = TextFieldEditor.class;

        model.addModelListener(MultiTabPropertyEditor.this);
        model.addPropertyStructureListener(MultiTabPropertyEditor.this);

        menuItems = new JCheckBoxMenuItem[2];
        menuItems[0] = new PlacementCheckBoxMenuItem(this);
        menuItems[1] = new VertexCheckBoxMenuItem(this);

        // display tabs by default
        showTab(TabMenuItems.TYPE_PLACEMENT);
        showTab(TabMenuItems.TYPE_VERTEX);

        vmanager.addView(MultiTabPropertyEditor.this);
    }

    /**
     *
     * @param model
     * @param skipLevels
     * @param displayPanels
     * @param defaultEditor
     */
    public MultiTabPropertyEditor(WorldModel model, int skipLevels,
            String[] displayPanels, Class defaultEditor) {

        this(model, skipLevels);

        this.displayPanels = new String[displayPanels.length];
        System.arraycopy(displayPanels, 0, this.displayPanels, 0, displayPanels.length);
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
     * Return the property data in the required format
     *
     */
    @Override
    public Object getComponent() {
        return this;
    }

    /**
     * Return the possible menu items
     *
     * @return the possible menu items
     */
    public JCheckBoxMenuItem[] getMenuItems() {
        return menuItems;
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

    /**
     * The master view has changed.
     *
     * @param viewID The view which is master
     */
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

//System.out.println("MultiTabPropertyEditor.selectionChanged()");
//System.out.println("    selection.size(): " + selection.size());

        if (associateMode)
            return;

        // TODO: deal with multiple selected entities
        if (selection.isEmpty()) {

            // If nothing is selected then clear the tab
            buildPropertyPanel(-1, null);

        } else {

            // get the currently selected entity
            Selection selectedEntity = selection.get(0);
            int entityID = selectedEntity.getEntityID();

            if (entityID >= 0) {

                if (associateMode) {

                    associateMode = false;

                } else {

                    buildPropertyPanel(entityID, null);
                    updateVertexPanel(entityID, null);

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

        if (showPlacement && (placementPanel != null)) {
            placementPanel.setPositionX(position[0]);
            placementPanel.setPositionY(position[1]);
            placementPanel.setPositionZ(position[2]);
        }

    }

    /**
     * The entity was rotated.
     *
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {

        if (showPlacement && (placementPanel != null)) {
            placementPanel.setRotationX(rotation[0]);
            placementPanel.setRotationY(rotation[1]);
            placementPanel.setRotationZ(rotation[2]);
            placementPanel.setRotationAngle(rotation[3]);
        }

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
     * A segment was split.
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

        updateVertexPanel(entityID, position);

    }

    @Override
    public void segmentVertexUpdated(boolean local, int entityID,
            int vertexID, String propertySheet, String propertyName,
            String propertyValue) {

        updateVertexPanel(entityID, null);

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

        updateVertexPanel(entityID, position);

    }

    @Override
    public void segmentVertexRemoved(boolean local, int entityID,
            int vertexID) {

        //buildPropertyPanel(entityID, null);

    }

    // ----------------------------------------------------------
    // Methods required by PropertyStructureListener
    // ----------------------------------------------------------

    @Override
    public void propertyAdded(boolean local, int entityID, String propSheet,
            String propName, Node propValue) {

        // rebuild panel
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
    // Methods required by TabMenuItems
    // ----------------------------------------------------------

    /**
     * Interface to show/hide the various property tabs
     *
     */
    @Override
    public final boolean showTab(int type) {

        switch (type) {
            case TabMenuItems.TYPE_PLACEMENT:
                showPlacementTab(true);
                break;
            case TabMenuItems.TYPE_VERTEX:
                showVertexTab(true);
                break;
        }

        return true;

    }

    /**
     * Interface to show/hide the various property tabs
     *
     */
    @Override
    public boolean hideTab(int type) {

        switch (type) {
        case TabMenuItems.TYPE_PLACEMENT:
            showPlacementTab(false);
            break;
        case TabMenuItems.TYPE_VERTEX:
            showVertexTab(false);
            break;
        }

        return false;

    }

    @Override
    public boolean isTabVisible(int type) {

        switch (type) {
        case TabMenuItems.TYPE_PLACEMENT:
            return showPlacement;
        case TabMenuItems.TYPE_VERTEX:
            return showVertex;
        }

        return false;

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
     * Build the property panels, one panel per sheet
     *
     * @param entityId the id of the entity selected
     * @param propName the name of the last property edited
     */
    private void buildPropertyPanel(int entityID, String propName) {


//System.out.println("MultiTabPropertyEditor.buildPropertyPanel()");
//System.out.println("    entityID: " + entityID);

        // get the currently selected Tab
        if ((tabbedPane != null) && (tabbedPane.getSelectedIndex() != -1)) {
            selectedTab = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        }

        // get the view port to add content
        JViewport viewPort = this.getViewport();

        if (entityID > -1) {

            Entity entity = model.getEntity(entityID);

            model.addEntityChangeListener(entity, this);

            tabbedPane = new JTabbedPane();

            // now get the property sheet data
            PropertyData data =
                new PropertyData(model, vmanager, skipLevels, entityID, displayPanels, defaultEditor);

            //propagate the errorReporter
            data.setErrorReporter(errorReporter);

            Map<String, JPanel> propertyPanels =
                (Map<String, JPanel>) data.getComponent();

            // iterate through each property sheet
            if (propertyPanels != null) {

                Iterator<Map.Entry<String, JPanel>> index =
                    propertyPanels.entrySet().iterator();

                while (index.hasNext()) {

                    Map.Entry<String, JPanel> mapEntry = index.next();

                    /*
                    if (mapEntry.getKey().equals("Behavior")) {
                        JPanel foo = mapEntry.getValue();
                        System.out.println("******");
                        for (int i = 0; i < foo.getComponents().length; i++) {
                            System.out.println("    name: " + foo.getComponents()[i].getName());
                        }
                    }
                    */
                    // add the tab
                    tabbedPane.addTab(mapEntry.getKey(), mapEntry.getValue());

                }

                if (entity instanceof SegmentableEntity) {
                    // if a vertex is selected then display the vertex tab
                    if (entity.isSegmentedEntity() && (((SegmentableEntity)entity).getSelectedVertexID() >= 0)) {
                        vertexPanel =  new VertexPanel(model, entity);
                        vertexPanel.setErrorReporter(errorReporter);
                        showVertexTab(showVertex);
                    }

                    // if no vertex is selected then display the entity placement tab
                    if ((((SegmentableEntity)entity).getSelectedVertexID() == -1) &&
                         (entity.getType() != Tool.TYPE_WORLD)){
                        placementPanel = new PlacementPanel(model, entity);
                        placementPanel.setErrorReporter(errorReporter);
                        showPlacementTab(showPlacement);
                    }
                }

                viewPort.add(tabbedPane);

            }

            if (tabbedPane.getTabCount() <= 0) {
                viewPort.add(new JPanel());
            }

        } else {

            viewPort.add(new JPanel());

        }

        viewPort.revalidate();

        int len = 0;

        if (tabbedPane != null)
            len = tabbedPane.getTabCount();

        // set the selected tab
        for (int i = 0; i < len; i++) {

            if (tabbedPane.getTitleAt(i).equals(selectedTab)) {

                tabbedPane.setSelectedIndex(i);
                break;

            }
        }
    }

    /**
     * Add or remove the tab depending on the current state
     *
     */
    private void showPlacementTab(boolean showPlacement) {

        // decide if we should display
        if (tabbedPane != null) {

            if (showPlacement) {

                tabbedPane.addTab("Placement", placementPanel);

            } else {

                for (int i = 0; i < tabbedPane.getTabCount(); i++) {

                    if (tabbedPane.getTitleAt(i).equals("Placement")) {

                        tabbedPane.remove(i);

                    }
                }
            }
        }

        this.showPlacement = showPlacement;

    }

    /**
     * Add or remove the tab depending on the current state
     *
     */
    private void showVertexTab(boolean showVertex) {

        // decide if we should display
        if (tabbedPane != null) {

            if (showVertex) {

                tabbedPane.addTab("Placement", vertexPanel);

            } else {

                for (int i = 0; i < tabbedPane.getTabCount(); i++) {

                    if (tabbedPane.getTitleAt(i).equals("Placement")) {

                        tabbedPane.remove(i);

                    }
                }
            }
        }

        this.showVertex = showVertex;

    }

    /**
     * Send an update the vertex panel
     *
     * @param entityId
     * @param position
     */
    private void updateVertexPanel(int entityId, double[] position) {

        Entity entity = model.getEntity(entityId);

        if (entity instanceof SegmentableEntity) {

            SegmentVertex vertex = ((SegmentableEntity)entity).getSelectedVertex();

            if (vertex != null) {

                // get the current position if not supplied
                if (position == null) {
                    position = vertex.getPosition();
                }

                // update the panel
                if (showVertex && (vertexPanel != null)) {

                    vertexPanel.setVertexID(((SegmentableEntity)entity).getSelectedVertexID());
                    vertexPanel.setPositionX(position[0]);
                    vertexPanel.setPositionY(position[1]);
                    vertexPanel.setPositionZ(position[2]);

                }
            }
        }
    }

}
