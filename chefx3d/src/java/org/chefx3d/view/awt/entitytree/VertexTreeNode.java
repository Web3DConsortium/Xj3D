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
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * An implementation of the MutableTreeNode that represents a single model
 * entity within a JTree.
 *
 * @author Russell Dodds
 * @version $Revision: 1.6 $
 */
public class VertexTreeNode implements MutableTreeNode {

    /** The EntityID this vertex is associated with */
    private Entity entity;

    /** The vertex ID, the index really */
    private int vertexID;

    /** The user's stored data */
    private Object userData;

    /** Mapping of nodes (key) to TreeNode (value) for reverse lookups */
    private HashMap<Integer, VertexTreeNode> vertexMap;

    /**
     * The direct children of this node. The list contains both attribute and
     * element children of this node. All attributes appear first in the list.
     */
    private ArrayList<MutableTreeNode> children;

    /** The parent node of this one */
    private MutableTreeNode parent;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the tree node that represents the given DOM Node.
     * If the node allows events, then this will register itself as a listener.
     *
     * @param entity The DOM node this tree node represents
     * @param vertexID
     * @param parent
     */
    public VertexTreeNode(Entity entity, int vertexID, MutableTreeNode parent) {
        this.entity = entity;
        this.vertexID = vertexID;
        this.parent = parent;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        children = new ArrayList<>();
        vertexMap = new HashMap<>();

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

        int vertex = ((VertexTreeNode) child).getVertexID();
        vertexMap.put(vertex, (VertexTreeNode) child);

    }

    /**
     * Remove the child at the given index position. If there is no child there
     * it will do nothing.
     * @param index
     */
    @Override
    public void remove(int index) {
        children.remove(index);
    }

    /**
     * Remove the given tree node from the list of children of this node.
     *
     * @param child The node to remove
     */
    @Override
    public void remove(MutableTreeNode child) {
        children.remove(child);
    }

    /**
     * Remove this node from it's parent. If this is the root node then this
     * will ignore the request.
     */
    @Override
    public void removeFromParent() {
        if (parent == null)
            return;

        parent.remove(this);
    }

    /**
     * Set the parent node of this node to the new value.
     *
     * @param parent The new node to use as a parent
     */
    @Override
    public void setParent(MutableTreeNode parent) {
        this.parent = parent;
    }

    /**
     * Add some user data to this object.
     *
     * @param obj The data to be stored
     */
    @Override
    public void setUserObject(Object obj) {
        userData = obj;
    }

    // ----------------------------------------------------------
    // Methods required by TreeNode.
    // ----------------------------------------------------------

    /**
     * Get the list of children of this node as an enumeration. If the node
     * could have children, but does not at the moment, it will return an empty
     * enumeration.
     *
     * @return An enumeration, possibly empty of the children
     */
    @Override
    public Enumeration children() {
        return Collections.enumeration(children);
    }

    /**
     * Check to see if this node allows children. For the purposes of the DOM
     * view of the world, a leaf and allowing children are the same thing. We do
     * not consider whether the node is an X3D node type or not.
     *
     * @return true if this node allows children
     */
    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    /**
     * Get the child at the given index position. If there is no child there it
     * will return null.
     *
     * @param index The position to check
     * @return The tree node at the index
     */
    @Override
    public TreeNode getChildAt(int index) {
        return children.get(index);
    }

    /**
     * Get the number of children of this node. The children count is of the
     * tree node children, not the DOM children. Tree children includes the
     * attributes as well
     *
     * @return The number of children of this child
     */
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
        return parent;
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
    public void add(VertexTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * @return The EntityID
     */
    public int getEntityID() {
        return entity.getEntityID();
    }

    /**
     * @return The VertexID node
     */
    public int getVertexID() {
        return vertexID;
    }

    /**
     * @return The Vertex postion information
     */
    public double[] getVertexPosition() {
        if (entity  instanceof SegmentableEntity) {
            SegmentSequence fence =
                ((SegmentableEntity)entity).getSegmentSequence();

            int index = fence.indexOf(vertexID);

//System.out.println("VertexTreeNode.getVertexPosition()");
//System.out.println("vertexID: " + vertexID);
//System.out.println("vertexIndex: " + index);

            SegmentVertex vertex = fence.getVertex(vertexID);

            if (vertex != null) {
//System.out.println("vertex position: " + fence.getVertex(vertexID).getPosition()[0] + ", " +  fence.getVertex(vertexID).getPosition()[2]);
                return vertex.getPosition();
            }

        }

        return new double[] {0, 0, 0};

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
    VertexTreeNode getTreeNodeChild(int child) {
        return vertexMap.get(child);
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
