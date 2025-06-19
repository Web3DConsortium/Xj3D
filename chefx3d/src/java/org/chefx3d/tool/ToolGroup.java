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

package org.chefx3d.tool;

// External Imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * Describes a group of tools and tool groups.
 * <p>
 *
 * A tool group contains other groups and tools as needed by the catalog
 * structure. In addition, it may contain a tool that is used to directly
 * render or create an entity representation of this group.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public class ToolGroup implements ToolGroupChild, Comparable<ToolGroup> {

    /** Error message when the user code barfs */
    private static final String TOOL_ADD_ERROR_MSG =
        "Error sending ToolGroup tool addition to: ";

    /** Error message when the user code barfs */
    private static final String TOOL_REMOVE_ERROR_MSG =
        "Error sending ToolGroup tool removal to: ";

    /** Error message when the user code barfs */
    private static final String TOOL_GROUP_ADD_ERROR_MSG =
        "Error sending ToolGroup group addition to: ";

    /** Error message when the user code barfs */
    private static final String TOOL_GROUP_REMOVE_ERROR_MSG =
        "Error sending ToolGroup group removal to: ";

    /** Default error message when sending the error message fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending ToolGroup listener to: ";

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The name of this ToolGroup */
    private String name;

    /** ToolGroup can have a tool it represents */
    private Tool tool;

    /** The list of tools and group children in the group */
    private List<ToolGroupChild> children;

    /** A list of all the group children of this group */
    private List<ToolGroup> groupChildren;

    /** A list of all the tool children of this group */
    private List<Tool> toolChildren;

    /** The groupListener(s) for group changes at this level */
    private ToolGroupListener groupListener;

    /** Tools mapped by their name */
    private Map<String, Tool> toolsByNameMap;

    /** Tool groups mapped by their name */
    private Map<String, ToolGroup> groupsByNameMap;

    /** The parent of this tool */
    private ToolGroupChild toolParent;

    /**
     * Create a new tool descriptor.
     *
     * @param name The groups name
     */
    public ToolGroup(String name) {
        this.name = name;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        children = new ArrayList<>();
        toolsByNameMap = new HashMap<>();
        groupsByNameMap = new HashMap<>();
        groupChildren = new ArrayList<>();
        toolChildren = new ArrayList<>();
     }

    /**
     * Create a new tool descriptor that contains the list of tools and
     * tool groups as children.
     *
     * @param name The groups name
     * @param children The list of children. This can be a Tool or a ToolGroup.
     */
    public ToolGroup(String name, List<ToolGroupChild> children) {
        this(name);

        deepCopy(children);
    }

    //----------------------------------------------------------
    // Methods defined by Comparable<Tool>
    //----------------------------------------------------------

    /**
     * Return compare based on string ordering
     * @param t another ToolGroup to compare with
     */
    @Override
    public int compareTo(ToolGroup t) {
        return name.compareTo(t.name);

    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * override of Objects equals test
     * @param o another ToolGroup to compare with
     */
    @Override
    public boolean equals(Object o) {

        if (o instanceof ToolGroup) {
            ToolGroup check = (ToolGroup)o;
            if (name.equals(check.name)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------
    // Methods defined by ToolGroupChild
    //----------------------------------------------------------

    /**
     * Get the tool's name.
     *
     * @return The name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Return the parent of this tool group child. If there is no parent
     * reference the parent is either the catalog or this is an orphaned item.
     *
     * @return The current parent of this item
     */
    @Override
    public ToolGroupChild getParent() {
        return toolParent;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Search in this group's direct children for a tool of the given
     * name.
     *
     * @param name The name of the tool to look for
     * @return The matching tool instance or null if not found
     */
    public Tool getTool(String name) {
        return toolsByNameMap.get(name);
    }

    /**
     * Search in this group's direct children for a tool of the given
     * name.
     *
     * @param name The name of the group to look for
     * @return The matching group instance or null if not found
     */
    public ToolGroup getToolGroup(String name) {
        return groupsByNameMap.get(name);
    }

    /**
     * Set the tool this group represents
     *
     * @param tool
     */
    public void setTool(Tool tool) {
        if(this.tool != null) {
            this.tool.setParent(null);
        }

        this.tool = tool;

        if(this.tool != null) {
            this.tool.setParent(this);
        }
    }

    /**
     * Get the tool this group represents
     *
     * @return tool
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Add a tool to the group
     *
     * @param tool the tool instance to add
     */
    public void addTool(Tool tool) {

        String toolName = tool.getName();

        if(!toolsByNameMap.containsKey(toolName)) {
            toolsByNameMap.put(toolName, tool);
            children.add(tool);
            toolChildren.add(tool);
            tool.setParent(this);
            fireToolAdded(tool);

        }
    }

    /**
     * Add a tool to the group
     *
     * @param group The group instance
     */
    public void addToolGroup(ToolGroup group) {
        String groupName = group.getName();

        if(!groupsByNameMap.containsKey(groupName)) {
            children.add(group);
            groupChildren.add(group);
            groupsByNameMap.put(groupName, group);
            group.setParent(this);

            group.setErrorReporter(errorReporter);
            fireToolGroupAdded(group);
        }
    }

    /**
     * Remove a tool from the group
     *
     * @param tool
     */
    public void removeTool(Tool tool) {
        String toolName = tool.getName();

        if(toolsByNameMap.containsKey(toolName)) {
            children.remove(tool);
            toolChildren.remove(tool);
            toolsByNameMap.remove(toolName);
            tool.setParent(null);
            fireToolRemoved(tool);
        }
    }

    /**
     * Remove a toolgroup from the group. Does nothing if the group is not
     * currently a child of this group.
     *
     * @param group The group instance to remove
     */
    public void removeToolGroup(ToolGroup group) {
        String groupName = group.getName();

        if(groupsByNameMap.containsKey(groupName)) {
            children.remove(group);
            groupChildren.remove(group);
            groupsByNameMap.remove(groupName);
            group.setParent(null);
            group.setErrorReporter(null);

            fireToolGroupRemoved(group);
        }
    }

    /**
     * Get all the children of this group.
     *
     * @return The list of children
     */
    public List<ToolGroupChild> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Get all the tool children of this group.
     *
     * @return The list of children
     */
    public List<Tool> getTools() {
        return Collections.unmodifiableList(toolChildren);
    }

    /**
     * Get all the group children of this group.
     *
     * @return The list of children
     */
    public List<ToolGroup> getToolGroups() {
        return Collections.unmodifiableList(groupChildren);
    }

    /**
     * Add a new groupListener for this group. Duplicate requests are
     * ignored.
     *
     * @param l The groupListener instance to add
     */
    public void addToolGroupListener(ToolGroupListener l) {
        groupListener = ToolGroupListenerMulticaster.add(groupListener, l);
    }

    /**
     * Delete a groupListener for this group. If not added currently, nothing
     * happens.
     *
     * @param l The groupListener instance to remove
     */
    public void removeToolGroupListener(ToolGroupListener l) {
        groupListener = ToolGroupListenerMulticaster.remove(groupListener, l);
    }

    /**
     * Notify listeners of tools added.
     *
     * @param tools The tools added
     */
    private void fireToolAdded(Tool tool) {
        try {
            if(groupListener != null) {
                ToolGroupEvent evt =
                    new ToolGroupEvent(this, tool, ToolGroupEvent.TOOL_ADDED);

                groupListener.toolAdded(evt);
            }
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOL_ADD_ERROR_MSG + groupListener,
                                          (Exception)th);
            else {
                System.err.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notify listeners of tools removed.
     *
     * @param tools The tools removed
     */
    private void fireToolRemoved(Tool tool) {
        try {
            if(groupListener != null) {
                ToolGroupEvent evt =
                    new ToolGroupEvent(this, tool,
                        ToolGroupEvent.TOOL_REMOVED);
                groupListener.toolRemoved(evt);
            }
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOL_REMOVE_ERROR_MSG + groupListener,
                                          (Exception)th);
            else {
                System.err.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * A tool has been removed.  Batched removes will come through the
     * toolsRemoved method.
     *
     * @param group The toolGroup removed from
     * @param child The child group that was removed
     */
    private void fireToolGroupAdded(ToolGroup child) {
        try {
            if(groupListener != null) {
                ToolGroupEvent evt =
                    new ToolGroupEvent(this, child,
                        ToolGroupEvent.GROUP_ADDED);

                groupListener.toolGroupAdded(evt);
            }
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOL_GROUP_ADD_ERROR_MSG + groupListener,
                                          (Exception)th);
            else {
                System.err.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * A tool has been removed. Batched removes will come through the
     * toolsRemoved method.
     *
     * @param group The toolGroup removed from
     * @param child The child group that was removed
     */
    private void fireToolGroupRemoved(ToolGroup child) {
        try {
            if(groupListener != null) {
                ToolGroupEvent evt =
                    new ToolGroupEvent(this, child,
                        ToolGroupEvent.GROUP_REMOVED);

                groupListener.toolGroupRemoved(evt);
            }
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOL_GROUP_REMOVE_ERROR_MSG + groupListener,
                                          (Exception)th);
            else {
                System.err.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Make a deep copy of a list of children into our local list.
     *
     * @param list The list instance to copy
     */
    private void deepCopy(List<ToolGroupChild> list) {

        // TODO: This is really a shallow copy need to fix
        children.addAll(list);

        for (ToolGroupChild kid : list) {
            if(kid instanceof Tool) {
                Tool t = (Tool)kid;
                toolsByNameMap.put(t.getName(), t);
                toolChildren.add(t);
            } else if(kid instanceof ToolGroup) {
                ToolGroup tg = (ToolGroup)kid;
                groupsByNameMap.put(tg.getName(), tg);
                groupChildren.add(tg);
            }
        }
    }

    /**
     * Set the tool parent to be this new object. Null clears the reference.
     * Package private because only ToolGroup should be calling this.
     */
    void setParent(ToolGroupChild parent) {
        toolParent = parent;
    }
}
