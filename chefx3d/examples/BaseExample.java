/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External Imports
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import org.chefx3d.actions.awt.*;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.property.*;
import org.chefx3d.property.awt.*;
import org.chefx3d.toolbar.*;
import org.chefx3d.toolbar.awt.*;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.view.*;
import org.chefx3d.view.awt.*;
import org.chefx3d.view.awt.entitytree.EntityTreeView;
import org.chefx3d.view.awt.entitytree.ViewTreeAction;
import org.chefx3d.view.awt.gt2d.GT2DView;
import org.j3d.aviatrix3d.output.graphics.BaseSurface;

/**
 * A simple example of how to use ChefX3D
 *
 * @author Russell Dodds
 * @version $Revision: 1.47 $
 */
public abstract class BaseExample implements ActionListener, MenuListener {

    /** The hardcoded base name for saving files */
    protected static final String BASENAME = "foo";

    /** The content path */
    protected static final String cpath = "catalog/";

    /** The image path */
    protected static final String ipath = "images/";

    private final String[] DISPLAY_PANELS =
        new String[] {"SMAL", "Cost", "Segment", "Vertex"};

    /** The saveX3D menu item */
    protected JMenuItem saveX3D;

    /** The exit menu item */
    protected JMenuItem exit;

    /** The launchX3D menu item */
    protected JMenuItem launchX3D;

    /** The Undo command menu item */
    protected JMenuItem undo;

    /** The Redo command menu item */
    protected JMenuItem redo;

    /** The clear command menu item */
    protected JMenuItem clear;

    /** The delete command menu item */
    protected JMenuItem delete;

    /** The launch treeView menu item */
    protected JMenuItem treeView;

    /** The elevation config menu item */
    protected JCheckBoxMenuItem elevationControl;

    protected ShowPlacementAction showPlacementPanel;

    /** The toolbar */
    protected ToolBar toolbar;

    /** The world model */
    protected WorldModel model;

    /** The view manager */
    protected ViewManager viewManager;

    /** The ToolBarManager */
    protected ToolBarManager toolBarManager;

    /** The main window */
    protected JFrame mainFrame;

    /** The external viewer */
    protected JFrame externalViewer;

    /** The external entityTreeView */
    protected JFrame externalTreeView;

    /** The 2d view */
    protected View view2d;

    /** The 3d view */
    protected ViewX3D view3d;

    /** The simulation viewer */
    protected StandaloneX3DViewer sv3d;

    /** The PropertyEditor to use */
    protected PropertyEditorFactory PropertyEditorFactory;

    /** The ToolBarFactory to use */
    protected ToolBarFactory toolbarFactory;

    /** The ViewFactory to use */
    protected ViewFactory viewFactory;

    protected PropertyEditor propertyEditor;

    /** The Command Controller */
    private CommandController controller;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /** Solve any classloading issues */
    protected ClassLoader loader;

    public BaseExample(int network, String server, String resource,
            String username, String passwd) {

        // Create the default error reporter
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        errorReporter.showLevel(ErrorReporter.DEBUG);

        // create the command controller
        controller = new DefaultCommandController();
        controller.setErrorReporter(errorReporter);

        // Create the 3d world
        model =
            WorldModelFactory.createModel(controller, network, server, resource,username, passwd);
        model.setErrorReporter(errorReporter);

        // Create a ViewManager
        viewManager = ViewManager.getViewManager();
        viewManager.setWorldModel(model);
        viewManager.setErrorReporter(errorReporter);

        // Create a toolBarManager
        toolBarManager = ToolBarManager.getToolBarManager();
        toolBarManager.setErrorReporter(errorReporter);

        // Create the property editor for the objects placed in the scene
        //propertyEditor = (new AWTPropertyEditorFactory()).createMultiTabEditor(model, 3);
        propertyEditor = new MultiTabPropertyEditor(model, 3, DISPLAY_PANELS, TextFieldEditor.class);
        propertyEditor.setErrorReporter(errorReporter);

        // Create the 2d view
        view2d = new GT2DView(model, ipath);
        view2d.setErrorReporter(errorReporter);

        // Create the ViewFactory for creating specific model views
        viewFactory = new AWTViewFactory();
        viewFactory.setErrorReporter(errorReporter);

        Map<String, String> params = new HashMap<>();

        // Solve the URL issue (TDN)
        loader = getClass().getClassLoader();
        URL url = loader.getResource("catalog/InitialWorld.x3dv");
        params.put(ViewFactory.PARAM_INITIAL_WORLD, url.toString());
        params.put(ViewFactory.PARAM_IMAGES_DIRECTORY, ipath);

        // Create the 3d view
        view3d = (ViewX3D) viewFactory.createView(model,
                ViewFactory.PERSPECTIVE_X3D_VIEW, params);
        view3d.setErrorReporter(errorReporter);

/*
        // TODO: Can we avoid this?
        if (view2d instanceof org.chefx3d.view.awt.PictureView) {
            org.chefx3d.view.awt.PictureView pv = (org.chefx3d.view.awt.PictureView) view2d;
            pv.setImageDirectory("images/");
        }

        if (view2d instanceof org.chefx3d.view.awt.GT2DView) {
            org.chefx3d.view.awt.GT2DView pv = (org.chefx3d.view.awt.GT2DView) view2d;
            pv.setImageDirectory("images/");
        }
*/

        // Setup the mainFrame and the contentPane for it
        mainFrame = new JFrame("ChefX3D");
        Container contentPane = mainFrame.getContentPane();

        BorderLayout layout = new BorderLayout();
        contentPane.setLayout(layout);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Now define the external viewer (Watcher3D)
        externalViewer = new JFrame("ChefX3D - Perspective View");
        externalViewer.setSize(800, 600);
        Container cpViewer = externalViewer.getContentPane();
        cpViewer.add((JComponent) view3d.getComponent(), BorderLayout.CENTER);
        externalViewer.setVisible(true);

        ((BaseSurface)view3d.getX3DComponent().getImplementation()).initContext();

        // Now define the external viewer
        EntityTreeView treePanel = new EntityTreeView(model);
        treePanel.setErrorReporter(errorReporter);
        externalTreeView = new JFrame("ChefX3D - Model Tree View");
        externalTreeView.setSize(300, 600);
        Container cpTreeView = externalTreeView.getContentPane();
        cpTreeView.add((JComponent) treePanel, BorderLayout.CENTER);
        externalTreeView.setVisible(false);

        // Add the menuBar to the mainFrame
        mainFrame.setJMenuBar(createMenus());

        // create the 2D viewer
        JPanel viewPanel = new JPanel(new BorderLayout());
        viewPanel.add((JComponent) view2d.getComponent(), BorderLayout.CENTER);

        // Create the properties panel
        JPanel propertyPanel = new JPanel(new BorderLayout());
        propertyPanel.add((JComponent) propertyEditor.getComponent(),
                BorderLayout.CENTER);

        // Add the components to the contentPane
        JScrollPane propertyScrollPane = new JScrollPane( propertyPanel );

        JSplitPane contentSplitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, viewPanel, propertyScrollPane );

        contentSplitPane.setOneTouchExpandable( true );

        // force the resize weight to work by setting the min size
        Dimension viewMinDim = new Dimension( 600, 600 );
        Dimension propMinDim = new Dimension( 0, 600 );
        viewPanel.setMinimumSize( viewMinDim );
        propertyScrollPane.setMinimumSize( propMinDim );
        propertyScrollPane.setPreferredSize( new Dimension( 400, 600 ) );

        // 60% to the view panel, 40% to the property panel
        contentSplitPane.setResizeWeight(0.4);

        JScrollPane toolPanel = createLeftPanel();

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, toolPanel, contentSplitPane );

        splitPane.setOneTouchExpandable( true );

        // force the resize weight to work by setting the min size
        toolPanel.setMinimumSize( propMinDim );
        contentSplitPane.setMinimumSize( propMinDim );

        // 80% to the content pane, 20% to the tool panel
        splitPane.setResizeWeight(0.8);

        contentPane.add( splitPane, BorderLayout.CENTER );

        // Populate the toolPanel
        createTools();

        mainFrame.setSize(1200, 700);
        mainFrame.setLocation(10, 10);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private JScrollPane createLeftPanel() {

        JTabbedPane tabbedPane = new JTabbedPane();

        // Create the toolbar
        //toolbarFactory = new AWTToolBarFactory();
        //toolbarFactory.setErrorReporter(errorReporter);
        //toolbar = toolbarFactory.createToolBar(model, ToolBar.VERTICAL, true);

        // Create the IconToolbar specifically
        IconToolBar iconTool = new IconToolBar(model);
        iconTool.setCatalog("Sample");


        // Add the toolbar
        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setPreferredSize(new Dimension(200, 700));
        toolPanel.add((JComponent) iconTool.getComponent(), BorderLayout.CENTER);

        tabbedPane.addTab("Tools", toolPanel);

        //TreeToolBar treeTool = new TreeToolBar();
        //treeTool.setCatalog("Sample");

        //JPanel treePanel = new JPanel(new BorderLayout());
        //treePanel.setPreferredSize(new Dimension(200, 700));
        //treePanel.add((JComponent) treeTool.getComponent(), BorderLayout.CENTER);

        //tabbedPane.addTab("Tool Tree", treePanel);

        // Create the scroll Panel UI
        JScrollPane scrollPane = new JScrollPane();
        JViewport viewport = scrollPane.getViewport();
        //viewport.add(toolPanel);
        viewport.add(tabbedPane);

        return scrollPane;
    }

    private JMenuBar createMenus() {

        // setup the menu system
        JMenuBar mb = new JMenuBar();

        // Define the FileMenuGroup
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        mb.add(file);

        // Define the Save Action
        saveX3D = new JMenuItem("Save X3D");
        saveX3D.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));
        saveX3D.setMnemonic(KeyEvent.VK_S);
        saveX3D.addActionListener(this);
        file.add(saveX3D);

        // Add a separator
        file.addSeparator();

        // Define the Exit Action
        ExitAction exitAction = new ExitAction();
        file.add(exitAction);

        // Define the Edit MenuGroup
        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        mb.add(edit);

        // Define the Undo Action
        UndoAction undoAction = new UndoAction(false, null, controller);
        edit.add(undoAction);

        // Define the Redo Action
        RedoAction redoAction = new RedoAction(false, null, controller);
        edit.add(redoAction);

        edit.addSeparator();

        // Define the Copy Action
        EntityCopyAction copyAction = new EntityCopyAction(false, null, model);
        edit.add(copyAction);

        // Define the Paste Action
        EntityPasteAction pasteAction = new EntityPasteAction(false, null, model, copyAction, edit);
        edit.add(pasteAction);

        edit.addSeparator();

        // Define the Delete Action
        DeleteAction deleteAction = new DeleteAction(false, null, model);
        edit.add(deleteAction);

        // Define the Delete All Action
        ResetAction resetAction = new ResetAction(false, null, model);
        edit.add(resetAction);

        // Define the MenuGroup
        JMenu view = new JMenu("View");
        edit.setMnemonic(KeyEvent.VK_V);
        mb.add(view);

        // Define the Menu Actions
        launchX3D = new JMenuItem("Launch X3D");
        launchX3D.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                ActionEvent.CTRL_MASK));
        launchX3D.setMnemonic(KeyEvent.VK_L);
        launchX3D.addActionListener(this);
        view.add(launchX3D);

        elevationControl = new JCheckBoxMenuItem("Dynamic Elevation Control");
        elevationControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                ActionEvent.CTRL_MASK));
        elevationControl.setMnemonic(KeyEvent.VK_H);
        elevationControl.addActionListener(this);
        view.addMenuListener(this);
        view.add(elevationControl);

        view.addSeparator();

        // Define the ViewTree Action
        ViewTreeAction viewTreeAction = new ViewTreeAction(model);
        view.add(viewTreeAction);

        // Define the ViewReport Action
        ViewCostReportAction viewCostReportAction = new ViewCostReportAction(model);
        view.add(viewCostReportAction);

        // Get the list of menu items
        //JCheckBoxMenuItem[] menuItems =
        //    ((MultiTabPropertyEditor) propertyEditor).getMenuItems();

        // add them to the menu
        //for (int i = 0; i < menuItems.length; i++) {
        //    view.add(menuItems[i]);
        //}

        return mb;

    }

    // ----------------------------------------------------------
    // Methods required by ActionListener
    // ----------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {

        Object action = e.getSource();

        FileWriter writer = null;
        if (action == saveX3D) {
            X3DExporter exporter = new X3DExporter("3.2", "Immersive", null,
                    null);
            try {
                writer = new FileWriter(BASENAME + ".x3d");
                exporter.export(model, writer);
            } catch (IOException ioe) {
               errorReporter.errorReport("ERROR: ", ioe);
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException ioe) {}
            }
        } else if (action == launchX3D) {

            // Close the Perspective View
            if (externalViewer != null) {
                view3d.shutdown();
                externalViewer.setVisible(false);
                externalViewer = null;
            }

            System.out.println("Saving X3D");
            X3DExporter exporter = new X3DExporter("3.2", "Immersive", null,
                    null);

            try {
                writer = new FileWriter(BASENAME + ".x3d");
                exporter.export(model, writer);
            } catch (IOException ioe) {
               errorReporter.errorReport("ERROR: ", ioe);
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException ioe) {}
            }

            if (sv3d != null) {
                sv3d.load(BASENAME + ".x3d");
            } else {
                sv3d = new StandaloneX3DViewer();
                sv3d.load(BASENAME + ".x3d");
            }

        } else if (action == elevationControl) {
            if((view3d != null) && (view3d instanceof ViewConfig)) {
                ViewConfig vc = (ViewConfig)view3d;
                vc.setConfigElevation(elevationControl.isSelected());
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by the MenuListener interface
    //----------------------------------------------------------

    /**
     * Ignored. Invoked when the menu is canceled.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void menuCanceled(MenuEvent evt) {
    }

    /**
     * Invoked when the menu is deselected. Enable the action when
     * the parent menu is not visible to support key bound events.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void menuDeselected(MenuEvent evt) {
        elevationControl.setEnabled(true);
    }

    /**
     * Invoked when a menu is selected. Enable this item if an
     * Entity is available in the copy buffer.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void menuSelected(MenuEvent evt) {
        if((view3d != null) && (view3d instanceof ViewConfig)) {
            elevationControl.setEnabled(true);
            ViewConfig vc = (ViewConfig)view3d;
            elevationControl.setSelected(vc.getConfigElevation());
        } else {
            elevationControl.setEnabled(false);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Create ChefX3D tools.
     */
    protected abstract void createTools();
}
