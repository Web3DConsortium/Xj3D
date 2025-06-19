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
import java.util.*;

// Listed explicity to avoid clashes with java.util.List
import java.awt.Container;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;

import com.l2fprod.common.swing.JOutlookBar;

// Local imports
import org.chefx3d.catalog.CatalogManager;
import org.chefx3d.catalog.CatalogManagerListener;
import org.chefx3d.catalog.Catalog;
import org.chefx3d.catalog.CatalogListener;

import org.chefx3d.model.WorldModel;
import org.chefx3d.tool.Tool;
import org.chefx3d.tool.ToolGroup;
import org.chefx3d.toolbar.ToolBar;
import org.chefx3d.toolbar.ToolBarManager;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.view.ViewManager;

/**
 * This outlookToolbar will use icons and a stack outlookToolbar to display
 * items. Only one group will be displayed at a time.
 *
 * @author Russell Dodds
 * @version $Revision: 1.12 $
 */
public class IconToolBar
    implements ToolBar, CatalogManagerListener, CatalogListener {

    /** The panel to place the catalog items */
    private JOutlookBar outlookToolbar;

    /** The world model */
    private WorldModel model;

    /** The view manager */
    private ViewManager viewManager;

    /** The catalog manager */
    private CatalogManager catalogManager;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The catalog that we should be watching for changes on */
    private String catalogName;

    /** Map from the root tool group name to the holding panel */
    private Map<String, FlatToolGroupIconPanel> toolnameToPanelMap;

    /** List of all the current tool panels */
    private List<JPanel> toolPanels;

    /** True to show the tool group's tool */
    private final boolean showToolGroupTools;

    /**
     * Create a new tool bar that does not show the tool group tools.
     * @param model the WorldModel
     */
    public IconToolBar(WorldModel model) {
        this(model, false);
    }

    /**
     * Construct a new instance of the toolbar with the option to show or
     * hide the ToolGroup's tool.
     *
     * @param model the WorldModel
     * @param showToolGroupTools true if the tool from ToolGroup should be
     *   included in the display.
     */
    public IconToolBar(WorldModel model, boolean showToolGroupTools) {
        this.model = model;
        this.showToolGroupTools = showToolGroupTools;

        viewManager = ViewManager.getViewManager();
        catalogManager = CatalogManager.getCatalogManager();
        catalogManager.addCatalogManagerListener(IconToolBar.this);

        outlookToolbar = new JOutlookBar();
        toolnameToPanelMap = new HashMap<>();
        toolPanels = new ArrayList<>();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        ToolBarManager.getToolBarManager().addToolBar(IconToolBar.this);
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
        for (JPanel panel : toolPanels) {
            ((FlatToolGroupIconPanel)panel).setTool(tool);
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
        return outlookToolbar;
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    /**
     * Process an action event from one of the icon buttons.
     *
     * @param e The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent e) {

        String toolName = e.getActionCommand();

        if (toolName != null) {
            Tool tool = CatalogManager.getCatalogManager().findTool(toolName);
            viewManager.setTool(tool);
        }

    }

    //----------------------------------------------------------
    // Methods defined by CatalogListener
    //----------------------------------------------------------

    /**
     * A tool group has been added. Batched adds will come through the
     * toolsAdded method.
     *
     * @param name The catalog name
     * @param group The toolGroup added to
     */
    @Override
    public void toolGroupAdded(String name, ToolGroup group) {
        if(!name.equals(catalogName))
            return;

        addToolPanel(group);
    }

    /**
     * A group of tool groups have been added.
     *
     * @param name The catalog name
     * @param groups The list of tool groups added
     */
    @Override
    public void toolGroupsAdded(String name, List<ToolGroup> groups) {
        if(!name.equals(catalogName))
            return;

        for (ToolGroup group : groups) {
            addToolPanel(group);
        }
    }

    /**
     * A tool has been removed.  Batched removes will come through the
     * toolsRemoved method.
     *
     * @param name The catalog name
     * @param group The toolGroup removed from
     */
    @Override
    public void toolGroupRemoved(String name, ToolGroup group) {
        if(!name.equals(catalogName))
            return;

        removeToolPanel(group);
    }

    /**
     * A group of tool groups have been removed.
     *
     * @param name The catalog name
     * @param groups The list of tool groups that have been removed
     */
    @Override
    public void toolGroupsRemoved(String name, List<ToolGroup> groups) {
        if(!name.equals(catalogName))
            return;

        for (ToolGroup group : groups) {
            removeToolPanel(group);
        }
    }

    // ----------------------------------------------------------
    // Methods defined by CatalogManagerListener
    // ----------------------------------------------------------

    /**
     * A catalog has been added.
     *
     * @param catalog
     */
    @Override
    public void catalogAdded(Catalog catalog) {
        if(catalogName == null)
            return;

        if(catalogName.equals(catalog.getName()))
            catalog.addCatalogListener(this);
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

        // If the catalog that we're watching is removed, clean out the
        // outlookToolbar.
        if(catalogName.equals(catalog.getName())) {
            outlookToolbar.removeAll();
            toolnameToPanelMap.clear();
            catalog.removeCatalogListener(this);
        }
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Change which catalog that this outlookToolbar should be watching for changes
     * on.
     *
     * @param catalogName The name of the catalog to watch.
     */
    public void setCatalog(String catalogName) {
        this.catalogName = catalogName;
    }

    /**
     * Add a new panel for the given tool group.
     *
     * @param group The group to add the panel for
     */
    private void addToolPanel(ToolGroup group) {
        FlatToolGroupIconPanel panel = new FlatToolGroupIconPanel(model, group, false);

        String name = group.getName();

        toolnameToPanelMap.put(name, panel);
        toolPanels.add(panel);

        JPanel makeTop = new JPanel(new BorderLayout());
        makeTop.add(panel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(makeTop);
        scroll.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        outlookToolbar.addTab(name, scroll);
    }

    /**
     * Remove the panel that belongs to the given tool group.
     *
     * @param group The group to remove the panel of
     */
    private void removeToolPanel(ToolGroup group) {
        String name = group.getName();

        JPanel panel = toolnameToPanelMap.get(name);
        toolnameToPanelMap.remove(name);
        toolPanels.remove(panel);

        // Need to go up 2 panels to get to the scroll pane.
        Container scrollPane = panel.getParent().getParent();
        outlookToolbar.remove(scrollPane);

    }
}
