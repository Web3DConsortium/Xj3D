/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.catalog;

// External Imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.chefx3d.tool.Tool;
import org.chefx3d.tool.ToolGroup;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A grouping of tools.
 *
 * @author Alan Hudson
 */
public class Catalog {
    /** Error message when the user code barfs */
    private static final String TOOLS_ADD_ERROR_MSG =
        "Error sending list of tools addition message: ";

    /** Error message when the user code barfs */
    private static final String TOOLS_REMOVE_ERROR_MSG =
        "Error sending list of tools removal message: ";

    /** Error message when the user code barfs */
    private static final String TOOL_GROUP_ADD_ERROR_MSG =
        "Error sending single tool group addition message: ";

    /** Error message when the user code barfs */
    private static final String TOOL_GROUP_REMOVE_ERROR_MSG =
        "Error sending single tool removal message: ";

    /** Default error message when sending the error message fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending catalog listener message: ";

    /** The list of all tools */
    private List<ToolGroup> tools;

    /** The tool groups mapped by name */
    private Map<String, ToolGroup> toolGroupsByName;

    /** The unique catalog name */
    private final String name;

    /** The major version */
    private int majorVersion;

    /** The minor version */
    private int minorVersion;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The listener(s) to this catalog */
    private CatalogListener listener;

    /**
     * Construct an instance of the catalog with the given name and
     * version information.
     * @param name
     * @param majorVersion
     * @param minorVersion
     */
    public Catalog(String name, int majorVersion, int minorVersion) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;

        tools = new ArrayList<>();
        toolGroupsByName = new HashMap<>();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
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
     * Get the name of the catalog.
     *
     * @return The catalog name
     */
    public String getName() {
        return name;
    }

    /**
     * Add a tool to the named group.
     *
     * @param groupName The group to add to
     * @param tool The new tool
     */
    public void addTool(String groupName, Tool tool) {

        ToolGroup group = toolGroupsByName.get(groupName);

        // Adding it to the group will fire a group listener event so no
        // need to do anything locally.
        if (group != null) {
            group.addTool(tool);
        }
    }

    /**
     * Remove a tool.
     *
     * @param groupName The group to remove from
     * @param tool The tool
     */
    public void removeTool(String groupName, Tool tool) {

        ToolGroup group = toolGroupsByName.get(groupName);

        // Adding it to the group will fire a group listener event so no
        // need to do anything locally.
        if (group != null) {
            group.removeTool(tool);
        }
    }

    /**
     * Add a toolGroup.
     *
     * @param group The new ToolGroup
     */
    public void addToolGroup(ToolGroup group) {

        ToolGroup check = toolGroupsByName.get(group.getName());

        // if the group exists merge them
        if (check != null) {
            // Ignore the merge if the two tool groups are the same
            // instance.
            if(check == group)
                return;

            for (int i = 0; i < group.getChildren().size(); i++) {

                if (group.getChildren().get(i) instanceof Tool) {
                    check.addTool((Tool) group.getChildren().get(i));
                } else {
                    check.addToolGroup((ToolGroup) group.getChildren().get(i));
                }
            }
        } else {
            tools.add(group);
            toolGroupsByName.put(group.getName(), group);
            fireToolGroupAdded(name, group);
        }
    }

    /**
     * Remove a toolGroup.
     *
     * @param group The ToolGroup to remove
     */
    public void removeToolGroup(ToolGroup group) {

        String groupName = group.getName();

        if(!toolGroupsByName.containsKey(groupName)) {
            String msg = "Catalog " + name +
                         " does not contain ToolGroup " +
                         groupName + " to remove it";
            errorReporter.errorReport(msg, null);
        } else {
            tools.remove(group);
            toolGroupsByName.remove(name);

            fireToolGroupRemoved(name, group);
        }
    }

    /**
     * Add a list of tools.
     *
     * @param newTools A list of Tools
     */
    public void addTools(List<ToolGroup> newTools) {

        for (ToolGroup group : newTools) {
            addToolGroup(group);
        }
    }

    /**
     * Remove a list of tools.
     *
     * @param oldTools A list of Tools
     */
    public void removeTools(List<ToolGroup> oldTools) {

        List<ToolGroup> removed = new ArrayList<>();

        for (ToolGroup grp : oldTools) {
            String groupName = grp.getName();

            if(!toolGroupsByName.containsKey(groupName)) {
                String msg = "Catalog " + name +
                        " does not contain ToolGroup " +
                        groupName + " to remove it";
                errorReporter.errorReport(msg, null);
            } else {
                toolGroupsByName.remove(groupName);
                removed.add(grp);
            }
        }

        tools.removeAll(oldTools);

        // Was oldTools, but didn't think that was right (TDN)
        fireToolsRemoved(name, removed);
    }

    /**
     * Get the toolGroup in this catalog.
     *
     * @param groupName
     * @return The toolGroup
     */
    public ToolGroup getToolGroup(String groupName) {
        return toolGroupsByName.get(groupName);
    }

    /**
     * Get the list of top-level tool groups in this catalog. The list
     * returned is a new unmodifiable list of the items in this catalog.
     *
     * @return A new list instance containing the top-level groups.
     */
    public List<ToolGroup> getToolGroups() {
        return Collections.unmodifiableList(tools);
    }

    /**
     * Return all the ToolGroups held by the catalog.
     *
     * @return
     */
    public List<ToolGroup> getAllGroupsFlattened() {

        List<ToolGroup> ret_val = new ArrayList<>();

        getGroups(tools, ret_val);

        return ret_val;
    }


    /**
     * Add a CatalogListener. Duplicates are ignored.
     *
     * @param l The listener
     */
    public void addCatalogListener(CatalogListener l) {
        listener = CatalogListenerMulticaster.add(listener, l);
    }

    /**
     * Remove a CatalogListener. If it is not currently registered, the
     * request is silently ignored.
     *
     * @param l The listener
     */
    public void removeCatalogListener(CatalogListener l) {
        listener = CatalogListenerMulticaster.remove(listener, l);
    }

    /**
     * Traverse a list of ToolGroups and Tools to generate
     * a flat list of ToolGroups
     */
    private void getGroups(List<ToolGroup> source, List<ToolGroup> dest) {
        int len = source.size();

        for(int i=0; i < len; i++) {
            Object o = source.get(i);

            if (o instanceof ToolGroup) {
                ToolGroup group = (ToolGroup)o;
                dest.add(group);

                List<ToolGroup> children = group.getToolGroups();
                getGroups(children, dest);
            }
        }
    }

    /**
     * Notify listeners of tools added.
     *
     * @param name The catalog added to
     * @param tools The tools added
     */
    private void fireToolsAdded(String name, List<ToolGroup> groups) {
        try {
            if(listener != null)
                listener.toolGroupsAdded(name, groups);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOLS_ADD_ERROR_MSG + listener,
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
     * @param name The catalog changed
     * @param groups The tools removed
     */
    private void fireToolsRemoved(String name, List<ToolGroup> groups) {
        try {
            if(listener != null)
                listener.toolGroupsRemoved(name, groups);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOLS_REMOVE_ERROR_MSG + listener,
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
     * @param name The catalog name
     * @param group The toolGroup removed from
     * @param child The child group that was removed
     */
    private void fireToolGroupAdded(String name, ToolGroup group) {
        try {
            if(listener != null)
                listener.toolGroupAdded(name, group);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOL_GROUP_ADD_ERROR_MSG + listener,
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
     * @param name The catalog name
     * @param group The toolGroup removed from
     * @param child The child group that was removed
     */
    private void fireToolGroupRemoved(String name, ToolGroup group) {
        try {
            if(listener != null)
                listener.toolGroupRemoved(name, group);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(TOOL_GROUP_REMOVE_ERROR_MSG + listener,
                                          (Exception)th);
            else {
                System.err.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }
}
