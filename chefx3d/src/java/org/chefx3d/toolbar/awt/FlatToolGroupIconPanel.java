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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

// Local imports
import org.chefx3d.tool.*;
import org.chefx3d.model.WorldModel;

import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.view.ViewManager;
import org.chefx3d.toolbar.ToolBarManager;

/**
 * A panel that takes a root tool group and shows all groups and tools
 * underneath it in a flat structure.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
class FlatToolGroupIconPanel extends JPanel
    implements ToolGroupListener, ItemListener {

    /** The size of the icons in the outlookToolbar in pixels */
    private static final Dimension ICON_SIZE = new Dimension(92, 92);

    /** Font to use to draw the text on the icons */
    private static final Font ICON_FONT = new Font("Arial", Font.BOLD, 10);

    /** Margin around the image and everywhere for the buttons */
    private static final Insets ICON_MARGIN = new Insets(2, 2, 2, 2);

    /** The world model */
    private WorldModel model;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** Root tool group for this panel. */
    private ToolGroup rootToolGroup;

    /** The button group to add all toggle buttons to */
    private ButtonGroup buttonGroup;

    /**
     * The hidden button that is never on the user interface. Used to turn
     * off all buttons in the tool group when no tool from this group is
     * selected.
     */
    private JToggleButton hiddenButton;

    /** True to show the tool group's tool */
    private final boolean showToolGroupTools;

    /** Map from the tool name to the corresponding button */
    private Map<String, JToggleButton> nameToButtonMap;

    /** Map from the tool name to the Tool instance */
    private Map<String, Tool> nameToToolMap;

    /**
     * The currently active tool. May be null when the active tool is
     * not on this panel.
     */
    private Tool currentTool;

    /**
     * Construct a new instance that works on the given world.
     *
     * @param model The world model
     * @param toolGroup The root group for this panel
     * @param showToolGroupTools true if the tool from ToolGroup should be
     *   included in the display.
     */
    FlatToolGroupIconPanel(WorldModel model, ToolGroup toolGroup, boolean showToolGroupTools) {
        super(new GridLayout(0, 2));

        this.model = model;
        rootToolGroup = toolGroup;
        this.showToolGroupTools = showToolGroupTools;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        nameToButtonMap = new HashMap<>();
        nameToToolMap = new HashMap<>();

        initialBuild();

        toolGroup.addToolGroupListener(FlatToolGroupIconPanel.this);
    }

    //----------------------------------------------------------
    // Methods defined by ToolGroupListener
    //----------------------------------------------------------

    /**
     * A tool has been added.  Batched additions will come through
     * the toolsAdded method.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void toolAdded(ToolGroupEvent evt) {
        Tool tool = (Tool)evt.getChild();
        addToolButton(tool);
    }

    /**
     * A tool group has been added. Batched adds will come through the
     * toolsAdded method.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void toolGroupAdded(ToolGroupEvent evt) {
        ToolGroup group = (ToolGroup)evt.getChild();
        addToolGroup(group);
    }

    /**
     * A tool has been removed. Batched removes will come through the
     * toolsRemoved method.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void toolRemoved(ToolGroupEvent evt) {
        Tool tool = (Tool)evt.getChild();
        removeToolButton(tool);
    }

    /**
     * A tool has been removed.  Batched removes will come through the
     * toolsRemoved method.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void toolGroupRemoved(ToolGroupEvent evt) {
        ToolGroup group = (ToolGroup)evt.getChild();
        removeToolGroup(group);
    }

    // ----------------------------------------------------------
    // Methods defined by ItemListener
    // ----------------------------------------------------------

    /**
     * Invoked when an item has been selected or deselected by the user.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void itemStateChanged(ItemEvent evt) {
        if(evt.getStateChange() != ItemEvent.SELECTED)
            return;

        JToggleButton button = (JToggleButton)evt.getSource();

        String name = button.getActionCommand();
        Tool tool = nameToToolMap.get(name);
        if(currentTool != tool) {
            ViewManager viewManager = ViewManager.getViewManager();
            viewManager.setTool(tool);

            ToolBarManager toolManager = ToolBarManager.getToolBarManager();
            toolManager.setTool(tool);
        }
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    public void setTool(Tool tool) {
        Tool oldTool = currentTool;

        currentTool = tool;

        if(tool == null) {
            if(oldTool != null) {
                hiddenButton.setSelected(true);
            }
        } else if(tool != oldTool) {
            String name = tool.getName();
            JToggleButton button = nameToButtonMap.get(name);

            if(button != null)
                button.setSelected(true);
            else
                currentTool = null;
        }
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

    /**
     * Build the basics of the groups based on root tool group.
     */
    private void initialBuild() {
        hiddenButton = new JToggleButton("If you see me, we're screwed");

        buttonGroup = new ButtonGroup();
        buttonGroup.add(hiddenButton);

        addToolGroup(rootToolGroup);
    }

    /**
     * Recursive method to add child groups of the given group.
     * If the flag is set, it will also add a button for this group's tool.
     *
     * @parem parentGroup The group to add the children for
     */
    private void addToolGroup(ToolGroup parentGroup) {
        if(showToolGroupTools) {
            Tool tool = parentGroup.getTool();
            addToolButton(tool);
        }

        List<ToolGroupChild> children = parentGroup.getChildren();

        for (ToolGroupChild child : children) {
            if(child instanceof ToolGroup) {
                ToolGroup group = (ToolGroup)child;
                addToolGroup(group);
                group.addToolGroupListener(this);
            } else if(child instanceof Tool) {
                addToolButton((Tool)child);
            }
        }
    }

    /**
     * Remove this group and any child tools/tool groups below it.
     *
     * @param parentGroup The group to remove from the system
     */
    private void removeToolGroup(ToolGroup parentGroup) {
        if(showToolGroupTools) {
            Tool tool = parentGroup.getTool();
            removeToolButton(tool);
        }

        List<ToolGroupChild> children = parentGroup.getChildren();

        for (ToolGroupChild child : children) {
            if(child instanceof ToolGroup) {
                ToolGroup group = (ToolGroup)child;
                removeToolGroup(group);
                group.removeToolGroupListener(this);
            } else if(child instanceof Tool) {
                removeToolButton((Tool)child);
            }
        }
    }

    /**
     * Add a button to the panel that represents this tool.
     *
     * @param tool The tool to add the button for
     */
    private void addToolButton(Tool tool) {

        String iconPath = tool.getInterfaceIcon(2);
        String name = tool.getName();

        JToggleButton button = new JToggleButton(name);
        button.setBorderPainted(true);
        button.setRolloverEnabled(true);
        button.setActionCommand(name);
        button.setToolTipText(name);
        button.setText(name);
        button.setPreferredSize(ICON_SIZE);
        button.setMaximumSize(ICON_SIZE);
        button.setMinimumSize(ICON_SIZE);
        button.setFont(ICON_FONT);
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setHorizontalTextPosition(AbstractButton.CENTER);
        button.setMargin(ICON_MARGIN);
        button.addItemListener(this);

        buttonGroup.add(button);

        Toolkit tk = getToolkit();

        //get the icon image to use
        Image image = null;
        URL iconURL = null;
        if (iconPath.startsWith("http:")) {

            try {
                iconURL = new URL(iconPath);
            } catch (MalformedURLException me) {
                errorReporter.errorReport("Invalid icon location: " + iconPath, me);
            }

            image = tk.createImage(iconURL);

        } else {
            File iconFile = new File(iconPath);
            if (iconFile.exists()) {
                image = tk.createImage(iconFile.getAbsolutePath());
            }
        }

        // try to obtain the file using the class path
        if(image == null) {

            iconURL = ClassLoader.getSystemResource(iconPath);
            if(iconURL != null)
                image = tk.createImage(iconURL);

            // Fallback for WebStart
            if(image == null) {

                iconURL = FlatToolGroupIconPanel.class.getClassLoader().getResource(iconPath);
                if (iconURL != null)
                    image = tk.createImage(iconURL);

            }
        }

        if (image != null) {
            // may want to check on dimensions here and not rescale it if
            // we don't have to.
            image = image.getScaledInstance(64, 64, 0);
            button.setIcon(new ImageIcon(image));
        }

        nameToButtonMap.put(name, button);
        nameToToolMap.put(name, tool);

        add(button);
    }

    /**
     * Remove the given button from the system.
     */
    private void removeToolButton(Tool tool) {
        String name = tool.getName();

        JToggleButton button = nameToButtonMap.get(name);
        button.removeItemListener(this);
        remove(button);

        buttonGroup.remove(button);
        nameToButtonMap.remove(name);
        nameToToolMap.remove(name);
    }
}
