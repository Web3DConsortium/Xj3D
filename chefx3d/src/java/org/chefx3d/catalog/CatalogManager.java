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
import java.util.*;

// Local imports
import org.chefx3d.tool.Tool;
import org.chefx3d.tool.ToolGroup;
import org.chefx3d.tool.ToolGroupChild;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * Manage catalogues.  This class is the main owner for catalogues and tools.
 *
 * @author Alan Hudson
 */
public class CatalogManager {

    /** The singleton class */
    private static CatalogManager catalogManager;

    /** The list of all catalogs */
    private List<Catalog> catalogs;

    /** Map of all catalogs by name */
    private Map<String, Catalog> catalogsByNameMap;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The catalog listeners */
    private List<CatalogManagerListener> listeners;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CatalogManager() {
        catalogs = new ArrayList<>();
        catalogsByNameMap = new HashMap<>();

        listeners = new ArrayList<>();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the singleton CatalogManager.
     *
     * @return The CatalogManager
     */
    public static CatalogManager getCatalogManager() {
        if (catalogManager == null) {
            catalogManager = new CatalogManager();
        }

        return catalogManager;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

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
     * Add a catalog to the system. If it is already added, this will silently
     * ignore the request.
     *
     * @param catalog The catalog to add
     */
    public void addCatalog(Catalog catalog) {

        String name = catalog.getName();

        if(!catalogsByNameMap.containsKey(name)) {
            catalogs.add(catalog);
            catalogsByNameMap.put(name, catalog);
            fireCatalogAdded(catalog);
        }
    }

    /**
     * Remove a catalog from the system. If it is not currently registered
     * silently ignore the request.
     *
     * @param name The catalog to remove
     */
    public void removeCatalog(String name) {

        if(catalogsByNameMap.containsKey(name)) {
            Catalog c = catalogsByNameMap.get(name);
            catalogsByNameMap.remove(name);
            catalogs.remove(c);
            catalogsByNameMap.remove(name);

            fireCatalogRemoved(c);
        }
    }

    /**
     * Get a list of the catalogs. Returns an immutable
     * list representing the current collection of catalogs the manager
     * contains
     *
     * @return The catalogs
     */
    public List<Catalog> getCatalogs() {
        return Collections.unmodifiableList(catalogs);
    }

    /**
     * Get a list of all the tools being managed.  This will
     * be a flat list with no ToolGroups.
     *
     * @return The current list of tools
     */
    public List<Tool> getAllToolsFlattened() {
        List<Tool> toolList = new ArrayList<>();

        for (Catalog cat : catalogs) {
            List<ToolGroup> groups = cat.getAllGroupsFlattened();

            for (ToolGroup tg : groups) {
                List<Tool> kids = tg.getTools();
                toolList.addAll(kids);
            }
        }

        return toolList;
    }

    /**
     * Get a catalog by name.
     *
     * @param name The catalog name
     * @return The catalog or null if not found
     */
    public Catalog getCatalog(String name) {
        return catalogsByNameMap.get(name);
    }

    /**
     * Search all of the available catalogs to find the named tool. Assumes
     * that tools are uniquely named across all catalogs. If they are not, then
     * the first tool found will be returned. The definition of first is
     * undefined and may return different values each time.
     * <p>
     * <b>Warning:</b> Implementation could be extremely slow depending on
     * the nature of the catalogs created and needing to be searched.
     *
     * @param name
     * @return The tool or null if not found
     */
    public Tool findTool(String name) {
        for (Catalog cat : catalogs) {
            List<ToolGroup> groups = cat.getAllGroupsFlattened();
            for (ToolGroup tg : groups) {
                Tool tool = tg.getTool(name);

                if(tool != null)
                    return tool;
            }
        }

        return null;
    }

    /**
     * Debug method to print tools
     * @param desc
     * @param ident
     * @param list of ToolGroup children
     */
    public static void printTools(String desc, int ident, List<ToolGroupChild> list) {
        int len = list.size();

        indent(ident);

        if (ident == 0)
            System.out.println("Print Tools: " + desc + " len: " + len);


        for(int i=0; i < len; i++) {
            Object o = list.get(i);
            String name;

            if (o instanceof ToolGroup) {
                ToolGroup group = (ToolGroup)o;
                name = "GRP: " + group.getName();

                System.out.println(name);
                List<ToolGroupChild> children = group.getChildren();

                printTools("GRP: " + group.getName(), ident++, children);
            } else {
                name = ((ToolGroupChild)o).getName();

                indent(ident);
                System.out.println(name);
            }
        }
    }

    /**
     * Traverse a list of ToolGroups and Tools to generate
     * a flat list of Tools
     */
    private void getTools(List source, List<Tool> dest) {
        int len = source.size();

        for(int i=0; i < len; i++) {
            Object o = source.get(i);
            String name;

            if (o instanceof ToolGroup) {
                ToolGroup group = (ToolGroup)o;

                List<ToolGroupChild> children = group.getChildren();

                getTools(children, dest);
            } else {
                dest.add((Tool)o);
            }
        }
    }


    private static String indent(int level) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i < level; i++) {
            sb.append("   ");
        }

        return sb.toString();
    }

    /**
     * Notify listeners of catalog added.
     *
     * @param catalog The catalog added

     */
    private void fireCatalogAdded(Catalog catalog) {
        int len = listeners.size();

        for(int i=0; i < len; i++) {
            CatalogManagerListener l = listeners.get(i);

            l.catalogAdded(catalog);
        }
    }

    /**
     * Notify listeners of catalog removed.
     *
     * @param catalog The catalog removed
     */
    private void fireCatalogRemoved(Catalog catalog) {
        int len = listeners.size();

        for(int i=0; i < len; i++) {
            CatalogManagerListener l = listeners.get(i);

            l.catalogRemoved(catalog);
        }
    }

    /**
     * Add a CatalogManagerListener.  Duplicates are ignored.
     *
     * @param l The listener
     */
    public void addCatalogManagerListener(CatalogManagerListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove a CatalogManagerListener.
     *
     * @param l The listener
     */
    public void removeCatalogManagerListener(CatalogManagerListener l) {
        listeners.remove(l);
    }

}
