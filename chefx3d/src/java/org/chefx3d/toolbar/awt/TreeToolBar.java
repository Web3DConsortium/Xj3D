/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.toolbar.awt;

// External Imports
import javax.swing.*;
import javax.swing.tree.*;

import java.awt.BorderLayout;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

// Local imports
import org.chefx3d.tool.*;

import org.chefx3d.model.WorldModel;
import org.chefx3d.catalog.Catalog;
import org.chefx3d.catalog.CatalogManager;
import org.chefx3d.catalog.CatalogManagerListener;
import org.chefx3d.toolbar.ToolBar;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.view.ViewManager;
import org.chefx3d.toolbar.ToolBarManager;

/**
 * This toolbar will use multiple trees to allow tool selection. A ToolGroup
 * that contains at least one non model will be a seperate tree. Each tree will
 * also have a global rollup.
 *
 * Direction will be ignored by this toolbar. It will always be vertical.
 *
 * An example might be:
 *
 * Locations: Washington Seattle Yakama Oregon Portland
 *
 * Barriers Water Fence
 *
 * Models Primitives Box Sphere
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public class TreeToolBar
    implements ToolBar, CatalogManagerListener, TreeSelectionListener {

    /** Text to use on the tree when we don't have anything selected */
    private static final String DEFAULT_CATALOG_TITLE =
        "No Catalog Selected";

    /** The world model */
    private WorldModel model;

    /** The panel to place the toolsTree */
    private JScrollPane toolsPanel;

    /** The tools tree */
    private JTree toolsTree;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The current catalog that we are showing. If none is set, is null */
    private Catalog currentCatalog;

    /** The name of the catalog that we are listening for */
    private String catalogName;

    /** Tree model that we use to represent catalogs */
    private TreeModel treeModel;

    /** The currently selected tool on the tree. */
    private Tool currentTool;

    /**
     * Create a new toolbar that works with the given model.
     * @param model
     */
    public TreeToolBar(WorldModel model) {
        this.model = model;
        toolsPanel = new JScrollPane();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        ToolBarManager.getToolBarManager().addToolBar(TreeToolBar.this);

        // define the tree
        TreeNode root = new DefaultMutableTreeNode(DEFAULT_CATALOG_TITLE);
        toolsTree = new JTree(root);
        toolsTree.setEditable(false);
        toolsTree.addTreeSelectionListener(TreeToolBar.this);
        toolsTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        toolsTree.setRootVisible(false);
        toolsTree.setShowsRootHandles(true);

        // finally, add the JTree to the panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolsTree, BorderLayout.CENTER);

        JViewport view = toolsPanel.getViewport();
        view.add(panel);
    }

    //----------------------------------------------------------
    // Methods defined by Toolbar
    //----------------------------------------------------------

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    @Override
    public void setTool(Tool tool) {
        Tool oldTool = currentTool;
        currentTool = tool;

        if (tool == null) {
            toolsTree.clearSelection();
        } else if(tool != oldTool) {
            CatalogTreeModel mod = (CatalogTreeModel)treeModel;
            TreePath path = mod.buildTreePath(tool);
            if (path != null) {
                toolsTree.setSelectionPath(path);
            }
        }
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
    // Methods defined by AuthoringComponent
    //----------------------------------------------------------

    /**
     * Get the component used to render this.
     *
     * @return The component
     */
    @Override
    public Object getComponent() {
        return toolsPanel;
    }

    //----------------------------------------------------------
    // Methods defined by TreeSelectionListener
    //----------------------------------------------------------

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Object source = e.getSource();

        if (!e.isAddedPath()) {
            // ignore deselection events
            return;
        }

        ToolGroupTreeNode node =
            (ToolGroupTreeNode)e.getPath().getLastPathComponent();

        ToolGroupChild toolItem = node.getToolObject();

        if(toolItem instanceof Tool) {
            Tool tool = (Tool)toolItem;
            currentTool = tool;
            ViewManager viewManager = ViewManager.getViewManager();
            viewManager.setTool(tool);

            ToolBarManager toolManager = ToolBarManager.getToolBarManager();
            toolManager.setTool(tool);
        } else if(toolItem instanceof ToolGroup) {
            ToolGroup group = (ToolGroup)toolItem;
            Tool tool = group.getTool();
            currentTool = tool;

            ViewManager viewManager = ViewManager.getViewManager();
            viewManager.setTool(tool);

            ToolBarManager toolManager = ToolBarManager.getToolBarManager();
            toolManager.setTool(tool);
        }
    }

    //----------------------------------------------------------
    // Methods defined by CatalogManagerListener
    //----------------------------------------------------------

    /**
     * A catalog has been added.
     *
     * @param catalog
     */
    @Override
    public void catalogAdded(Catalog catalog) {
        if(catalogName == null)
            return;

        if(catalogName.equals(catalog.getName())) {
            currentCatalog = catalog;
            treeModel = new CatalogTreeModel(catalog);
            toolsTree.setModel(treeModel);
        }
    }

    /**
     * A catalog has been removed.
     *
     * @param catalog
     */
    @Override
    public void catalogRemoved(Catalog catalog) {
        if(catalogName == null)
            return;

        if(catalogName.equals(catalog.getName())) {
            TreeNode root = new DefaultMutableTreeNode(DEFAULT_CATALOG_TITLE);
            treeModel = new DefaultTreeModel(root);
            toolsTree.setModel(treeModel);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Make the tree show this specific catalog
     *
     * @param catalogName the name of the catalog to view
     */
    public void setCatalog(String catalogName) {
        CatalogManager manager = CatalogManager.getCatalogManager();
        manager.addCatalogManagerListener(this);

        this.catalogName = catalogName;
        currentCatalog = manager.getCatalog(catalogName);

        if(currentCatalog != null) {
            treeModel = new CatalogTreeModel(currentCatalog);
            toolsTree.setModel(treeModel);
        }
    }
}
