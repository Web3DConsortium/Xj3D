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
import java.util.*;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

// Local imports
import org.chefx3d.catalog.Catalog;
import org.chefx3d.catalog.CatalogManager;
import org.chefx3d.catalog.CatalogListener;
import org.chefx3d.catalog.CatalogManagerListener;
import org.chefx3d.tool.Tool;
import org.chefx3d.tool.ToolGroup;
import org.chefx3d.toolbar.ToolBar;
import org.chefx3d.toolbar.ToolBarManager;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.model.WorldModel;
import org.chefx3d.tool.ToolGroupChild;
import org.chefx3d.view.ViewManager;

/**
 * This toolbar will use multiple trees to allow tool selection. A ToolGroup
 * that contains at least one non model will be a separate tree. Each tree will
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
 * @version $Revision: 1.12 $
 */
class MultiTreeToolBar
    implements ToolBar, TreeSelectionListener, CatalogManagerListener, CatalogListener {

    /** The panel to place the toolbar */
    private JSplitPane toolbar;

    // private JPanel toolbar;

    /** The panel to place the toolsTree */
    private JScrollPane toolsPanel;

    /** The panel to place the modelsTree */
    private JScrollPane modelsPanel;

    /** The world model */
    private WorldModel model;

    /** The view manager */
    private ViewManager viewManager;

    /** The catalog manager */
    private CatalogManager catalogManager;

    /** A map of tools to popup menus */
    // private HashMap<String, JPopupMenu> toolsMap;
    /** A map of MenuItems to ToolInstances */
    // private HashMap<JMenuItem, Tool> miMap;
    /** Should we collapse tools with one item */
    // private boolean collapse;
    /** The direction, horizontal or vertical */
    // private int direction;

    /** The sections map by type(type, ArrayList) */
    private Map<String, List<String>> sections;

    /** The tools tree */
    private JTree toolsTree;

    /** The models tree */
    private JTree modelsTree;

    /** The top level node for the tools section */
    private Vector<Vector<String>> toolsTop;

    /** The top level nodes for the models section */
    private Vector<String> modelsTop;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    public MultiTreeToolBar(WorldModel model, int direction,
        boolean collapseSingletons) {

        // toolsMap = new HashMap<String, JPopupMenu>();
        // miMap = new HashMap<JMenuItem, Tool>();

        // this.collapse = collapseSingletons;
        // this.direction = direction;
        this.model = model;
        viewManager = ViewManager.getViewManager();
        catalogManager = CatalogManager.getCatalogManager();
        catalogManager.addCatalogManagerListener(MultiTreeToolBar.this);

        sections = new HashMap<>();
        toolsTop = new Vector<>();
        modelsTop = new NamedVector("Models");

        toolbar = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        toolbar.setResizeWeight(0.3);

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        ToolBarManager.getToolBarManager().addToolBar(MultiTreeToolBar.this);
    }

    /**
     * Get the component used to render this.
     *
     * @return The component
     */
    @Override
    public Object getComponent() {
        return toolbar;
    }

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    @Override
    public void setTool(Tool tool) {
        // RUSS: Finish Implement
        if (tool == null) {
            toolsTree.clearSelection();
            modelsTree.clearSelection();
        } else {
            System.err.println("Unhandled case in Toolbar.setTool");
        }
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

        if (source == modelsTree) {
            // deselect toolsTree

            toolsTree.clearSelection();
        } else if (source == toolsTree) {
            // deselect modelsTree

            modelsTree.clearSelection();
        } else {
            errorReporter.messageReport("ERROR: Unknown source for valueChanged in: "
                    + this);
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
                .getLastPathComponent();

        Object nodeInfo = node.getUserObject();

        if (nodeInfo instanceof ToolWrapper) {
            ToolWrapper tw = (ToolWrapper) nodeInfo;
            viewManager.setTool(tw.getTool());
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
        rebuild();
    }

    /**
     * A group of tool groups have been added.
     *
     * @param name The catalog name
     * @param groups The list of tool groups added
     */
    @Override
    public void toolGroupsAdded(String name, List<ToolGroup> groups) {
        rebuild();
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
        rebuild();
    }

    /**
     * A group of tool groups have been removed.
     *
     * @param name The catalog name
     * @param groups The list of tool groups that have been removed
     */
    @Override
    public void toolGroupsRemoved(String name, List<ToolGroup> groups) {
        rebuild();
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
        catalog.addCatalogListener(this);
    }

    /**
     * A catalog has been removed.
     *
     * @param catalog
     */
    @Override
    public void catalogRemoved(Catalog catalog) {
        catalog.removeCatalogListener(this);
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Rebuild from the current set of tools
     */
    public void rebuild() {
        List<ToolGroup> tools = new ArrayList<>();
        List<Catalog> catalogs = catalogManager.getCatalogs();
        Iterator<Catalog> itr = catalogs.iterator();

        while(itr.hasNext()) {
            Catalog cat = itr.next();
            tools.addAll(cat.getToolGroups());
        }

        ToolGroup td;
        int len = tools.size();

        List children;
        Tool tool;
        ToolGroup childGroup;

        String name;

        modelsTop.clear();
        toolsTop.clear();

        String MODELS = "CHEFX3D_MODELS";
        String WORLDS = "CHEFX3D_WORLDS";

        for (int i = 0; i < len; i++) {
            td = tools.get(i);
            children = td.getChildren();

            name = td.getName();
//System.out.println(name + " len: " + children.size());
            // errorReporter.messageReport(name);

            boolean allModels = true;
            boolean allWorlds = true;

            int toolType;

            for (Object o : children) {
                if (o instanceof Tool) {
                    tool = (Tool) o;
                    toolType = tool.getToolType();
                    //errorReporter.messageReport(" Tool1: " + tool.getName());

                    if (toolType != Tool.TYPE_MODEL) {
                        allModels = false;
                    }
                    if (toolType != Tool.TYPE_WORLD) {
                        allWorlds = false;
                    }
                } else if (o instanceof ToolGroup) {
                    childGroup = (ToolGroup) o;
                    List sub_children = childGroup.getChildren();
                    for (Object sub_children1 : sub_children) {
                        if (sub_children1 instanceof Tool) {
                            tool = (Tool) sub_children1;
                            //errorReporter.messageReport(" Tool2: " + tool.getName());
                            toolType = tool.getToolType();
                            if (toolType != Tool.TYPE_MODEL) {
                                allModels = false;
                            }
                            if (toolType != Tool.TYPE_WORLD) {
                                allWorlds = false;
                            }
                        }
                    }
                } else {
                    errorReporter.messageReport("Invalid item in Tools: " + o);
                }
            }

            if (allModels) {
                List<String> models = sections.get(MODELS);

                if (models == null) {
                    models = new ArrayList();
                    sections.put(MODELS, models);
                }

                // errorReporter.messageReport("Lost title: " + name);
                addSection(modelsTop, name, children);

                // models.addAll(children);
            } else if (allWorlds) {
                List<String> list = sections.get(WORLDS);

                if (list == null) {
                    list = new ArrayList();
                    sections.put(WORLDS, list);
                }

                list.addAll(children);
            } else {
                List<String> list = sections.get(name);

                if (list == null) {
                    list = new ArrayList();
                    sections.put(name, list);
                }

                list.addAll(children);
            }
        }

        // ArrayList models = (ArrayList) sections.get(MODELS);
        sections.remove(MODELS);
        List<String> worlds = sections.get(WORLDS);
        sections.remove(WORLDS);

        addSection(toolsTop, "Locations", worlds);
        // addSection(modelsTop, "Models", models);

        for (Map.Entry<String, List<String>> entry : sections.entrySet()) {
            addSection(toolsTop, entry.getKey(), entry.getValue());
        }

        sections.clear();

        if (toolsTree != null) {
            toolsTree.removeTreeSelectionListener(this);

            toolsTree = new JTree(toolsTop);
            toolsTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            toolsTree.addTreeSelectionListener(this);
            toolsPanel = new JScrollPane(toolsTree);
            toolbar.setTopComponent(toolsPanel);
            // toolbar.add(toolsPanel, BorderLayout.NORTH);

            modelsTree.removeTreeSelectionListener(this);
            modelsTree = new JTree(modelsTop);
            modelsTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) modelsTree
                    .getModel().getRoot();
            root.setUserObject("Models");
            /*
             * if (modelsTree.getRowCount() > 0) modelsTree.expandRow(0);
             */
            modelsTree.addTreeSelectionListener(this);

            modelsPanel = new JScrollPane(modelsTree);
            toolbar.setBottomComponent(modelsPanel);
            // toolbar.add(modelsPanel, BorderLayout.SOUTH);
        } else {
            toolsTree = new JTree(toolsTop);
            toolsTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            toolsTree.addTreeSelectionListener(this);
            toolsPanel = new JScrollPane(toolsTree);
            toolbar.setTopComponent(toolsPanel);
            // toolbar.add(toolsPanel, BorderLayout.NORTH);

            modelsTree = new JTree(modelsTop);
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) modelsTree
                    .getModel().getRoot();
            root.setUserObject("Models");

            modelsTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            modelsTree.addTreeSelectionListener(this);
            /*
             * if (modelsTree.getRowCount() > 0) modelsTree.expandRow(0);
             */
            modelsPanel = new JScrollPane(modelsTree);
            toolbar.setBottomComponent(modelsPanel);
            // toolbar.add(modelsPanel, BorderLayout.SOUTH);

        }

        modelsTree.setRootVisible(true);
        // Object user_object = root.getUserObject();

        // errorReporter.messageReport("root: " + root + " class: " + root.getClass());
        // errorReporter.messageReport("user: " + user_object + " class: " +
        // user_object.getClass());
        Icon fouoIcon = new FOUOIcon2();
        toolsTree.setCellRenderer(new MyRenderer(fouoIcon));
    }

    /**
     * Add a section to the tree.
     *
     * @param section The section name
     * @param worlds The list of models
     */
    private void addSection(Vector top, String section, List<String> worlds) {
        Tool tool;
        boolean found = false;

        if (worlds != null) {
            Vector group = null;

            if (top.size() > 0) {
                NamedVector<String> nv = new NamedVector(section);

                int idx = top.indexOf(nv);

                if (idx != -1) {
                    group = (Vector) top.get(idx);
                    found = true;
                }
            }

            if (group == null) {
                group = new NamedVector(section);
            }
            Iterator itr = worlds.iterator();
            Object t;
            ToolGroup tg;

            while (itr.hasNext()) {
                t = itr.next();

                if (t instanceof Tool) {
                    tool = (Tool) t;
                    group.add(new ToolWrapper(tool));
                } else {
                    tg = (ToolGroup) t;
                    List<ToolGroupChild> sub_children = tg.getChildren();
                    Vector<ToolWrapper> sub_group = new NamedVector(tg.getName());

                    for (Object sub_children1 : sub_children) {
                        if (sub_children1 instanceof Tool) {
                            tool = (Tool) sub_children1;
                            // sub_group.add(tool.getName());
                            sub_group.add(new ToolWrapper(tool));
                        }
                    }

                    group.add(sub_group);
                }
            }

            if (!found)
                top.add(group);
        } else {
            errorReporter.messageReport("Nothing to add for section: " + section);
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

}

class FOUOIcon2 implements Icon, SwingConstants {
    private int width = 9;

    private int height = 18;

    private int[] xPoints = new int[4];

    private int[] yPoints = new int[4];

    public FOUOIcon2() {
        xPoints[0] = 0;
        yPoints[0] = -1;
        xPoints[1] = 0;
        yPoints[1] = height;
        xPoints[2] = width;
        yPoints[2] = height / 2;
        xPoints[3] = width;
        yPoints[3] = height / 2 - 1;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (c.isEnabled()) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.gray);
        }

        g.translate(x, y);
        g.fillPolygon(xPoints, yPoints, xPoints.length);
        g.translate(-x, -y); // Restore graphics object
        g.setColor(c.getForeground());
    }
}

// TODO: Move to seperate class once selection is handled, name might change
class NamedVector<E> extends Vector<E> {

    /** version id */
    private static final long serialVersionUID = 1L;

    private String name;

    public NamedVector(String name) {
        // super = new Vector<Object>();
        this.name = name;
    }

    public NamedVector(String name, E elements[]) {
        this.name = name;
        for (int i = 0, n = elements.length; i < n; i++) {
            add(elements[i]);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    // ---------------------------------------------------------------
    // Methods defined by Object
    // ---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NamedVector))
            return false;
        else {
            NamedVector o2 = (NamedVector) o;

            return name.equals(o2.getName());
        }

    }

    /**
     * Compare this object for order to the given object.
     *
     * @param o The object to be compared
     * @return zero if equals, negative less, positive greater
     */
    public int compareTo(Object o) throws ClassCastException {
        NamedVector nv = (NamedVector) o;

        return name.compareTo(nv.getName());
    }
}

class MyRenderer extends DefaultTreeCellRenderer {
    Icon tutorialIcon;

    /** version id */
    private static final long serialVersionUID = 1L;

    public MyRenderer(Icon icon) {
        tutorialIcon = icon;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);

        if (leaf && isFOUO(value)) {
            setIcon(tutorialIcon);
            setToolTipText("This book is in the Tutorial series.");
        } else {
            setToolTipText(null); // no tool tip
        }

        return this;
    }

    protected boolean isFOUO(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        Object o = node.getUserObject();

        if (!(o instanceof ToolWrapper))
            return false;

        ToolWrapper nodeInfo = (ToolWrapper) o;

        Tool tool = nodeInfo.getTool();

        return tool.getClassificationLevel() > 0;
    }
}
