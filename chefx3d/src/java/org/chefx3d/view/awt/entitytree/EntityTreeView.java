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

package org.chefx3d.view.awt.entitytree;

// External Imports
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;


// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.tool.*;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.view.*;

/**
 * The current model view in a nested tree structure
 *
 * @author Russell Dodds
 * @version $Revision: 1.26 $
 */
public class EntityTreeView extends JScrollPane implements EntityTree,
ModelListener, EntityChangeListener, TreeSelectionListener {

    /** The world model */
    private WorldModel model;

    /** List if Entities */
    protected Entity[] entities;

    /** The entity tree */
    private JTree entityTree;

    /** The entity tree */
    // private EntityTreeModel treeModel;
    private DefaultTreeModel treeModel;

    /** A mapping of entities to nodes */
    private Map<Integer, List<EntityTreeNode>> entityNodes;

    /** The root tree node */
    private WorldTreeNode root;

    /** Are we in associateMode */
    private boolean associateMode;

    /** The ViewManager */
    private ViewManager vmanager;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** Flag to prevent making circular loops happen in selection */
    private boolean selectionInProgress;

    /**
     * View the WorldModel in a tree structure
     *
     * @param model The WorldModel that the tree is representing
     */
    public EntityTreeView(WorldModel model) {

        this.model = model;
        this.vmanager = ViewManager.getViewManager();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        selectionInProgress = false;

        model.addModelListener(EntityTreeView.this);

        associateMode = false;
        entityNodes = new HashMap<>();

        buildTreePanel();

        vmanager.addView(EntityTreeView.this);

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

//System.out.println("EntityTreeView.entityAdded()");

        if (entity.getType() == Tool.TYPE_WORLD) {

            // define the root
            treeModel.nodeStructureChanged(root);


        } else {

            // make sure it is not a duplicate add from the networking
            if (!entityNodes.containsKey(entity.getEntityID())) {

                EntityTreeNode childNode = new EntityTreeNode(entity, root);
                childNode.setErrorReporter(errorReporter);

                treeModel.insertNodeInto(childNode, root, root.getChildCount());

                // add to the entity-node map
                updateEntityNodeMap(childNode.getEntity(), childNode);

                // add a listener for changes
                model.addEntityChangeListener(entity, this);
            }
        }
    }

    /**
     * An entity was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The id
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {

        if (entity.getType() != Tool.TYPE_WORLD) {

            List<EntityTreeNode> nodeList = getEntityNodes(entity
                .getEntityID());

            if (nodeList == null)
                return;

            for (EntityTreeNode childNode : nodeList) {
                treeModel.removeNodeFromParent(childNode);
            }

            // remove from the entity-node map
            entityNodes.remove(entity.getEntityID());

            // remove the listener
            model.removeEntityChangeListener(entity, this);
        }
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

        //System.out.println("EntityTreeView.selectionChanged, associateMode: " + associateMode);
        //System.out.println("    hashCode: " + this.hashCode());

        if (associateMode)
            return;

        selectionInProgress = true;

        // TODO: deal with multiple selected entities

        // Highlight all the selected entity nodes
        if (selection.isEmpty()) {

            // If nothing is selected then clear the selection list
            entityTree.setSelectionPath(null);

        } else {

            // get the currently selected entity
            Selection selectedEntity = selection.get(0);
            int entityID = selectedEntity.getEntityID();
            int vertexID = selectedEntity.getVertexID();

            if (entityID >= 0) {

                // traverse the model, looking for ID
                List<EntityTreeNode> enodes = getEntityNodes(entityID);

                if ((enodes != null) && (enodes.size() > 0)) {

                    TreePath[] selectionPaths = new TreePath[enodes
                        .size()];

                    for (int i = 0; i < enodes.size(); i++) {

                        TreeNode treeNode = enodes.get(i);

                        if (vertexID != -1) {

                            selectionPaths[i] = entityTree.getSelectionPath();

                        } else {

                            selectionPaths[i] = getTreePath(treeNode);

                        }

                        if (treeNode.getParent() == null) {

                            // scroll to the actual instance of the entity
                            // (not associate nodes)
                            entityTree.scrollPathToVisible(selectionPaths[i]);

                        }

                    }
                    // highlight the entity throughout the tree
                    entityTree.setSelectionPaths(selectionPaths);

                } else {
                    // If nothing is selected set to the location root
                    entityTree.setSelectionPath(getTreePath(root));
                }

            } else {

                // If nothing is selected set to the location root
                if (root != null) {
                    entityTree.setSelectionPath(getTreePath(root));
                } else {
                    // If nothing is selected then clear the selection list
                    entityTree.setSelectionPath(null);
                }

            }
        }

        selectionInProgress = false;
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
    }

    /**
     * A vertex was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param position The position in world coordinates
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID, int vertexID,
        double[] position) {

        // Find the parent EntityTreeNode
        List<EntityTreeNode> eNodes = getEntityNodes(entityID);

        for (EntityTreeNode parentNode : eNodes) {
            //System.out.println("    parentNode children count: " + parentNode.getChildCount());

            // make sure it doesn't already exist
            Enumeration children = parentNode.children();
            while (children.hasMoreElements()) {
                VertexTreeNode childNode = (VertexTreeNode) children.nextElement();
                if (childNode.getVertexID() == vertexID) {
                    return;
                }
            }

            // create the child node
//System.out.println("    parentNode name: " + parentNode.getEntity().getName());

            Entity item = model.getEntity(entityID);

//System.out.println("    item name: " + item.getName());


            VertexTreeNode childNode = new VertexTreeNode(item, vertexID, parentNode);
            childNode.setErrorReporter(errorReporter);

            treeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());

            // add to the entity-node map
            //updateEntityNodeMap(childNode.getEntity(), childNode);

            // expand the tree to include the new node
            entityTree.expandPath(getTreePath(parentNode));
        }

    }

    /**
     * A vertex was updated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param propertyValue
     */
    @Override
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
     * @param position The position in world coordinates
     */
    @Override
    public void segmentVertexMoved(boolean local, int entityID, int vertexID,
        double[] position) {


        // Find the parent EntityTreeNode
        List<EntityTreeNode> eNodes = getEntityNodes(entityID);

        for (EntityTreeNode parentNode : eNodes) {
            treeModel.reload(parentNode);

            // add to the entity-node map
            //updateEntityNodeMap(childNode.getEntity(), childNode);

            // expand the tree to include the new node
            entityTree.expandPath(getTreePath(parentNode));
        }

    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     */
    @Override
    public void segmentVertexRemoved(boolean local, int entityID,
        int vertexID) {

        // Find the parent EntityTreeNode
        List<EntityTreeNode> eNodes = getEntityNodes(entityID);

        for (EntityTreeNode parentNode : eNodes) {
            // look for the child node
            for (int j = 0; j < parentNode.getChildCount(); j++) {
                VertexTreeNode childNode  = (VertexTreeNode) parentNode.getChildAt(j);

                if (vertexID == childNode.getVertexID()) {
                    treeModel.removeNodeFromParent(childNode);
                }

            }

            // expand the tree to include the new node
            entityTree.expandPath(getTreePath(parentNode));
        }

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
        // ignore
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

        // Find the parent EntityTreeNode
        List<EntityTreeNode> eNodes = getEntityNodes(parent);


        for (EntityTreeNode parentNode : eNodes) {
            EntityTreeNode childNode = new EntityTreeNode(model
                    .getEntity(child), parentNode);
            childNode.setErrorReporter(errorReporter);

            treeModel.insertNodeInto(childNode, parentNode, parentNode
                    .getChildCount());

            // add to the entity-node map
            updateEntityNodeMap(childNode.getEntity(), childNode);

            // expand the tree to include the new node
            entityTree.expandPath(getTreePath(parentNode));
        }

        associateMode = false;
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
        // TODO
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
        // ignore
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
        // ignore
    }

    /**
     * The entity was rotated.
     *
     * @param local Was this action initiated from the local UI
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
        // ignore
    }

    // ----------------------------------------------------------
    // Methods required by the TreeSelectionListener
    // ----------------------------------------------------------

    /**
     * Callback from the tree to indicate a visual selection has
     * changed.
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {

        if (!e.isAddedPath() || selectionInProgress) {
            // ignore deselection events
            return;
        }

        MutableTreeNode node = (MutableTreeNode) e.getPath()
            .getLastPathComponent();

        if (node instanceof EntityTreeNode) {

            Entity entity = ((EntityTreeNode) node).getEntity();

            Selection sel = new Selection(entity.getEntityID(), -1, -1);
            List<Selection> list = new ArrayList<>(1);
            list.add(sel);
            model.changeSelection(list);

        } else if (node instanceof VertexTreeNode) {

            int entityID = ((VertexTreeNode) node).getEntityID();
            int vertexID = ((VertexTreeNode) node).getVertexID();

            Selection sel = new Selection(entityID, -1, vertexID);
            List<Selection> list = new ArrayList<>(1);
            list.add(sel);
            model.changeSelection(list);

        } else if (node instanceof WorldTreeNode) {

            Entity entity = ((WorldTreeNode) node).getEntity();

            if (entity != null) {

                Selection sel = new Selection(entity.getEntityID(), -1, -1);
                List<Selection> list = new ArrayList<>(1);
                list.add(sel);
                model.changeSelection(list);

            }
        }
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Build the TreePath to use to open and scroll the JTree if needed
     *
     * @param treeNode the node to open
     */
    private TreePath getTreePath(TreeNode treeNode) {

        ArrayList<TreeNode> treePath = new ArrayList<>();

        treePath.add(treeNode);

        while (treeNode.getParent() != null) {
            treeNode = treeNode.getParent();
            treePath.add(0, treeNode);
        }

        TreeNode[] path = new TreeNode[treePath.size()];
        treePath.toArray(path);

        return new TreePath(path);
    }

    /**
     * Add entry to the map for later lookup
     *
     * @param entity
     * @param node
     */
    private void updateEntityNodeMap(Entity entity, EntityTreeNode node) {

        int entityID = entity.getEntityID();

        if (entityNodes.containsKey(entityID)) {

            List<EntityTreeNode> nodes = entityNodes.get(entityID);
            nodes.add(node);

        } else {

            List<EntityTreeNode> newNodes = new ArrayList<>();
            newNodes.add(node);

            entityNodes.put(entityID, newNodes);
        }
    }

    /**
     * Returns a thread safe ArrayList for all nodes with the ID provided
     *
     * @param entityID the ID of the entity of interest
     * @return a list of <code>EntityTreeNode</code>, if any
     */
    public List<EntityTreeNode> getEntityNodes(int entityID) {

        try {
            if (entityNodes != null && !entityNodes.isEmpty()) {

                List<EntityTreeNode> list = entityNodes.get(entityID);

                if (list != null)
                    return new CopyOnWriteArrayList(list);
            }
        } catch (NullPointerException e) {
            errorReporter.errorReport(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Build the JTree UI components
     *
     */
    private void buildTreePanel() {

        // define the root
        root = new WorldTreeNode(model);
        root.setErrorReporter(errorReporter);

        // define the model
        treeModel = new DefaultTreeModel(root);

        // create the JTree
        entityTree = new JTree(treeModel);

        EntityTreeCellRenderer cellRenderer = new EntityTreeCellRenderer();
        cellRenderer.setErrorReporter(errorReporter);
        entityTree.setCellRenderer(cellRenderer);

        entityTree.setRootVisible(true);
        entityTree.setShowsRootHandles(false);

        // Add the selection listener so we know when something has been
        // selected
        entityTree.addTreeSelectionListener(this);

        // only allow a single item to be selected for now
        entityTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        entityTree.setShowsRootHandles(true);

        // Expand the Tree
        entityTree.expandPath(new TreePath(entityTree.getModel().getRoot()));

        // fianlly, add the JTree to the panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(entityTree, BorderLayout.CENTER);

        JViewport view = this.getViewport();
        view.add(panel);
    }
}
