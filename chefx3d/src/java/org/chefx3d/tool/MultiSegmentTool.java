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
import java.util.Map;

// Application specific imports
import org.chefx3d.PropertyPanelDescriptor;

/**
 * A multi segment tool.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
 public class MultiSegmentTool extends Tool {

    /** Is this a line tool */
    private boolean lineTool;

    /**
     * MultiSegment tool.
     *
     * @param isLine Is this segment tool restricted to be a line.
     * @param isController
     */
    public MultiSegmentTool(String name, String topDownIcon, String[] interfaceIcons, boolean fixed,
            int toolType, String[] url, int classificationLevel,
            String description, PropertyPanelDescriptor[] entityPanels,
            PropertyPanelDescriptor[] segmentPanels, PropertyPanelDescriptor[] vertexPanels,
            Map<String, String> stylesheets,
            String sizeSheet, String xExpr, String yExpr, String zExpr,
            String[] toolParams, MultiplicityConstraint constraint, String category,
            boolean isFixedSize, boolean isHelper, boolean isController, boolean isLine) {

            super(name, topDownIcon, interfaceIcons, fixed,
               toolType, url, classificationLevel, description, entityPanels,
               segmentPanels, vertexPanels,
               stylesheets, sizeSheet, xExpr, yExpr, zExpr,
               toolParams, constraint, category,
               isFixedSize, isHelper, isController);

            lineTool = isLine;
    }

    /**
     * Is the tool a line tool or does it allow multiple paths.
     *
     * @return True if its restricted to a single line
     */
    public boolean isLine() {
        return lineTool;
    }
 }