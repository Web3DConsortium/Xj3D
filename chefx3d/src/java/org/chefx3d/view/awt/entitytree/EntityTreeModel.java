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

// External imports
import java.util.*;

import javax.swing.tree.*;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.tool.Tool;

/**
 * The current world model view in a tree model structure
 *
 * @author Russell Dodds
 * @version $Revision: 1.7 $
 */
public class EntityTreeModel implements ModelListener {

    /** root tree node */
    private WorldTreeNode rootNode;

    private WorldModel model;

    /** A mapping of entities to nodes */
    private Map<Integer, List<EntityTreeNode>> entityNodes;

    /**
     * Create a new tree model that represents the given model entity.
     *
     * @param root The DOM node representing the root of the tree
     */
    public EntityTreeModel(TreeNode root) {

        rootNode = (WorldTreeNode) root;
        entityNodes = new HashMap<>();

        // add the model listener
        model = rootNode.getModel();
        model.addModelListener(EntityTreeModel.this);

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

        /*
         * Tool tool = entity.getTool();
         *
         * if (tool.getToolType() != Tool.TYPE_WORLD) {
         *  // append a new node on the end of the treemodel EntityTreeNode
         * child = new EntityTreeNode(entity, rootNode);
         *  }
         */

    }

    public void insertNodeInto(MutableTreeNode newChild,
            MutableTreeNode parent, int index) {

        // add to the node
        parent.insert(newChild, index);

        // add to the entity-node map
        updateEntityNodeMap(((EntityTreeNode) newChild).getEntity(),
                (EntityTreeNode) newChild);

        // create the indicies
        int[] indicies = new int[1];
        indicies[0] = index;

        // create the children
        Object[] children = new Object[1];
        children[0] = newChild;

        // fire the event
        // super.fireTreeNodesInserted(this, super.getPathToRoot(newChild),
        // indicies, children);

    }

    /**
     * An entity was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The id
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {

        List<EntityTreeNode> nodelist = getEntityNodes(entity
                .getEntityID());

        for (EntityTreeNode nodelist1 : nodelist) {
            nodelist1.removeFromParent();
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
        // handled by the TreeView
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
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Build children objects for the requested node type. If the node is an
     * attribute then do nothing. Listeners will be added to the root item but
     * not the children. This is to prevent odd mixups if the children have
     * listeners but have not yet had their children built. The viewable tree
     * would get very mixed up then
     *
     * @param root The root object to add children for
     */
    private void buildChildren(MutableTreeNode parent) {

        if (parent instanceof EntityTreeNode) {

            // get all the entity information
            Entity entity = ((EntityTreeNode) parent).getEntity();
            Entity[] entities = model.getModelData();

            // get the associations of the entity
            if (entity instanceof AssociatableEntity) {
                int[] associates = ((AssociatableEntity)entity).getAssociates();

                if (associates.length > 0) {

                    for (int i = 0; i < associates.length; i++) {

                        // Get the information
                        entity = entities[associates[i]];

                        // Create the new node
                        EntityTreeNode associate = new EntityTreeNode(entity,
                                parent);

                        // Add the node to the child
                        ((EntityTreeNode) parent).add(associate);

                        // add to the entity-node map
                        updateEntityNodeMap(entity, associate);

                    }

                }
            }
        } else if (parent instanceof WorldTreeNode) {

            buildTreeModel();

        }

    }

    /**
     * Build children objects for the requested node type. If the node is an
     * attribute then do nothing. Listeners will be added to the root item but
     * not the children. This is to prevent odd mixups if the children have
     * listeners but have not yet had their children built. The viewable tree
     * would get very mixed up then
     *
     * @param root The root object to add children for
     */
    private void buildTreeModel() {

        Entity entity;
        EntityTreeNode child;
        EntityTreeNode associate;

        // Get all the entities
        Entity[] entities = model.getModelData();

        // loop and add
        for (int i = 0; i < entities.length; i++) {
            entity = entities[i];

            if (entity != null) {

                if (entity.getType() != Tool.TYPE_WORLD) {

                    child = new EntityTreeNode(entity, rootNode);
                    rootNode.add(child);

                    // add to the entity-node map
                    updateEntityNodeMap(entity, child);

                    // Check to see if there are any associations
                    if (entity instanceof AssociatableEntity) {
                        int[] associates = ((AssociatableEntity)entity).getAssociates();
                        if (associates.length > 0) {

                            for (int j = 0; j < associates.length; j++) {

                                // Get the information
                                entity = entities[associates[i]];

                                // Create the new node
                                associate = new EntityTreeNode(entity, child);

                                // Add the node to the child
                                child.add(associate);

                                // add to the entity-node map
                                updateEntityNodeMap(entity, associate);

                            }
                        }
                    }
                }
            }
        }
    }

    private TreeNode[] buildTreePath(TreeNode treeNode) {

        ArrayList<TreeNode> treePath = new ArrayList<>();

        treePath.add(treeNode);

        while (treeNode.getParent() != null) {
            treeNode = treeNode.getParent();
            treePath.add(0, treeNode);
        }

        TreeNode[] path = new TreeNode[treePath.size()];
        treePath.toArray(path);

        return path;

    }

    /**
     * Add entry to the map for later lookup
     *
     * @param entity
     * @param node
     */
    private void updateEntityNodeMap(Entity entity, EntityTreeNode node) {

        List<EntityTreeNode> nodes;
        int entityID = entity.getEntityID();

        if (entityNodes.containsKey(entityID)) {

            nodes = entityNodes.get(entityID);
            nodes.add(node);

        } else {

            nodes = new ArrayList<>();
            nodes.add(node);

            entityNodes.put(entityID, nodes);

        }

    }

    /**
     * Returns a HashMap containing entityId => node list
     *
     * @return entityNodes
     */
    public Map<Integer, List<EntityTreeNode>> getEntityNodeMap() {

        return entityNodes;

    }

    /**
     * Returns an ArrayList all nodes with the ID provided
     *
     * @param entityID
     * @return list of <code>EntityTreeNode</code>
     */
    public List<EntityTreeNode> getEntityNodes(int entityID) {

        return entityNodes.get(entityID);

    }

}
