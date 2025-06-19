/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.tool.Tool;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * An implementation of the MutableTreeNode that represents a single model
 * entity within a JTree.
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.6 $
 */
public class WorldTreeNode implements MutableTreeNode {

    /** The Entity node this represents */
    private WorldModel model;

    /** The user's stored data */
    private Object userData;

    /** Mapping of nodes (key) to TreeNode (value) for reverse lookups */
    private Map<Entity, MutableTreeNode> nodeMap;

    /**
     * The direct children of this node. The list contains both attribute and
     * element children of this node. All attributes appear first in the list.
     */
    private List<MutableTreeNode> children;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the tree node that represents the given DOM Node.
     * If the node allows events, then this will register itself as a listener.
     *
     * @param model The DOM node this tree node represents
     */
    public WorldTreeNode(WorldModel model) {
        this.model = model;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        children = new ArrayList<>();
        nodeMap = new HashMap<>();

    }

    // ----------------------------------------------------------
    // Methods required by MutatableTreeNode.
    // ----------------------------------------------------------

    /**
     * Insert the child at the given position.
     *
     * @param child The new child to insert
     * @param index The position to insert the child into
     */
    @Override
    public void insert(MutableTreeNode child, int index) {

        if (index == children.size())
            children.add(child);
        else
            children.add(index, child);

        child.setParent(this);

        Entity ent = ((EntityTreeNode) child).getEntity();
        nodeMap.put(ent, child);

    }

    @Override
    public void remove(int index) {
        children.remove(index);
    }

    @Override
    public void remove(MutableTreeNode child) {
        children.remove(child);
    }

    @Override
    public void removeFromParent() {
        // root node, cannot remove from parent
    }

    @Override
    public void setParent(MutableTreeNode parent) {
        // root node, no parent can be set
    }

    @Override
    public void setUserObject(Object obj) {
        userData = obj;
    }

    // ----------------------------------------------------------
    // Methods required by TreeNode.
    // ----------------------------------------------------------

    @Override
    public Enumeration children() {
        return Collections.enumeration(children);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int index) {
        return children.get(index);
    }

    @Override
    public int getChildCount() {

        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    /**
     * Get the index of the given tree node.
     *
     * @param node The node to find the index of
     * @return The index of the given node or -1 if not found
     */
    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * Get the parent node of this node. If this is the root of the tree, the
     * return value is null.
     *
     * @return The parent node
     */
    @Override
    public TreeNode getParent() {
        return null;
    }

    /**
     * Check to see if this instance is a leaf node
     *
     * @return true if this is a leaf and cannot have children
     */
    @Override
    public boolean isLeaf() {
        return (getChildCount() == 0);
    }

    // ----------------------------------------------------------
    // Miscellaneous local methods.
    // ----------------------------------------------------------

    /**
     * Convinience method to add child to the ned of the list
     *
     * @param child The treenode to add
     */
    public void add(EntityTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Get the DOM node that this tree node represents. Used by the renderer to
     * build custom information about the node type.
     *
     * @return The Entuty node
     */
    public WorldModel getModel() {
        return model;
    }

    /**
     * Get the DOM node that this tree node represents. Used by the renderer to
     * build custom information about the node type.
     *
     * @return The Entity node
     */
    public Entity getEntity() {
        Entity entity;
        Entity[] entities = model.getModelData();
        int len = entities.length;

        for (int i = 0; i < len; i++) {
            entity = entities[i];
            if ((entity != null) &&
                (entity.getType() == Tool.TYPE_WORLD)) {
                return entity;
            }
        }

        return null;
    }

    /**
     * Get the user data stored in this object.
     *
     * @return The currently set user data
     */
    public Object getUserData() {
        return userData;
    }

    /**
     * Do a reverse lookup of the children to find the tree node that
     * corresponds to the given Node instance.
     */
    MutableTreeNode getTreeNodeChild(Entity child) {
        return nodeMap.get(child);
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
