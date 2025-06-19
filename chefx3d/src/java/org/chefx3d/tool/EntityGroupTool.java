/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005
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

// Standard Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.chefx3d.PropertyPanelDescriptor;

/**
 * A entity grouping tool.
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
 public class EntityGroupTool extends Tool {

     /** list of children Tools */
     private List<Tool> children;

    /**
     * EntityGroup tool.
     *
     * @param isLine Is this segment tool restricted to be a line.
     * @param isController
     */
    public EntityGroupTool(String name, String topDownIcon, String[] interfaceIcons, boolean fixed,
            int toolType, String[] url, int classificationLevel,
            String description, PropertyPanelDescriptor[] entityPanels,
            PropertyPanelDescriptor[] segmentPanels, PropertyPanelDescriptor[] vertexPanels,
            HashMap stylesheets,
            String sizeSheet, String xExpr, String yExpr, String zExpr,
            String[] toolParams, MultiplicityConstraint constraint, String category,
            boolean isFixedSize, boolean isHelper, boolean isController) {

        super(name, topDownIcon, interfaceIcons, fixed,
           toolType, url, classificationLevel, description, entityPanels,
           segmentPanels, vertexPanels,
           stylesheets, sizeSheet, xExpr, yExpr, zExpr,
           toolParams, constraint, category,
           isFixedSize, isHelper, isController);

        children = new ArrayList<>();

    }

    // ---------------------------------------------------------------
    // Local Methods
    // ---------------------------------------------------------------

    /**
     * Add a child to the tool
     *
     * @param tool
     */
    public void addChild(Tool tool) {
        children.add(tool);
    }

    /**
     * Remove a child from the tool
     *
     * @param tool
     */
    public void removeChild(Tool tool) {
        children.remove(tool);
    }

    /**
     * Get the index of a child Tool
     *
     * @param tool
     * @return The index
     */
    public int getChildIndex(Tool tool) {
        return children.indexOf(tool);
    }

    /**
     * Get a Tool at the index, returns null if not found
     *
     * @param index The index
     * @return The Tool
     */
    public Tool getChildAt(int index) {
        if (children.size() > index) {
            return children.get(index);
        }
        return null;
    }

    /**
     * Get a list of all children of this Tool
     *
     * @return The list of children
     */
    public List<Tool> getChildren() {
        return children;
    }

    /**
     * Get the number of children of this Tool
     *
     * @return The number of children
     */
    public int getChildCount() {
       return children.size();
    }

    /**
     * Does this Tool have any children
     *
     * @return true if it has children, false otherwise
     */
    public boolean hasChildren() {
       return children.size() > 0;
    }

 }