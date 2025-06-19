/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property.awt;

// External Imports
//import com.zookitec.layout.*;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.List;
import org.w3c.dom.Document;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.tool.Tool;

/**
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 *
 */
public class CostingPanel extends JPanel {

    /** The world model */
    private WorldModel model;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * Constructor
     *
     * @param model
     */
    public CostingPanel(WorldModel model) {

        this.model = model;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        buildPropertyPanel();

    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Register an error reporter with the PlacementPanel instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Create the panel from the currently selected entity.
     *
     */
    private void buildPropertyPanel() {

        Entity[] entities;
        Entity entity;
        Vector<String> rowData;
        String propertyName;
        //String itemType;

        float capitolCosts = 0;
        float maintenanceCosts = 0;
        float tmpCapitolCost;
        float tmpYearlyCost;

        // Setup the panel
        setName("Costing");
        setLayout(new BorderLayout());

        // Define the table to use
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.addColumn("Entity");
        dataModel.addColumn("ID");
        dataModel.addColumn("Capitol");
        dataModel.addColumn("Maintenance");

        // Loop through model and display data
        entities = model.getModelData();

        for (Entity entitie : entities) {
            entity = entitie;
            if ((entity != null) && (entity.getType() != Tool.TYPE_WORLD)) {

                // check to make sure the costing sheet exists
                Document sheet = entity.getProperties("Cost");

                rowData = new Vector<>();
                rowData.add(entity.getName());
                rowData.add(Integer.toString(entity.getEntityID()));

                if (sheet != null) {

                    // get base cost
                    propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/Cost/@capitolBase";
                    tmpCapitolCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                    propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/Cost/@maintenanceBase";
                    tmpYearlyCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                    // if it is a Fence then do so more work
                    if (entity.isSegmentedEntity()) {

                        // get the user selected appearance
                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Fence/@postAppearance";
                        String postAppearance = entity.getProperties("SMAL", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Fence/@postTopAppearance";
                        String postTopAppearance = entity.getProperties("SMAL", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Fence/@panelAppearance";
                        String panelAppearance = entity.getProperties("SMAL", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Fence/@panelTopAppearance";
                        String panelTopAppearance = entity.getProperties("SMAL", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Fence/@panelRailAppearance";
                        String panelRailAppearance = entity.getProperties("SMAL", propertyName);

                        // translate ids to string values
                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/TypeMappings/Appearance[@id='" + postAppearance +"']/@value";
                        String postType = entity.getProperties("Cost", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/TypeMappings/Appearance[@id='" + postTopAppearance +"']/@value";
                        String postTopType = entity.getProperties("Cost", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/TypeMappings/Appearance[@id='" + panelAppearance +"']/@value";
                        String panelType = entity.getProperties("Cost", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/TypeMappings/Appearance[@id='" + panelTopAppearance +"']/@value";
                        String panelTopType = entity.getProperties("Cost", propertyName);

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/TypeMappings/Appearance[@id='" + panelRailAppearance +"']/@value";
                        String panelRailType = entity.getProperties("Cost", propertyName);

                        // get the capitol costs values
                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/Post/"+ postType + "/@capitolPerMeter";
                        float postCapitolCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/PostTop/"+ postTopType + "/@capitolPerMeter";
                        float postTopCapitolCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/Panel/"+ panelType + "/@capitolPerMeter";
                        float panelCapitolCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/PanelTop/"+ panelTopType + "/@capitolPerMeter";
                        float panelTopCapitolCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/PanelRail/"+ panelRailType + "/@capitolPerMeter";
                        float panelRailCapitolCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        // get the yearly costs values
                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/Post/"+ postType + "/@maintenancePerMeter";
                        float postYearlyCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/PostTop/"+ postTopType + "/@maintenancePerMeter";
                        float postTopYearlyCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/Panel/"+ panelType + "/@maintenancePerMeter";
                        float panelYearlyCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/PanelTop/"+ panelTopType + "/@maintenancePerMeter";
                        float panelTopYearlyCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        propertyName = "/ChefX3D/EntityParams/Sheet[@name='Cost']/EntityDefinition/PanelRail/"+ panelRailType + "/@maintenancePerMeter";
                        float panelRailYearlyCost = Float.valueOf(entity.getProperties("Cost", propertyName));

                        SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();
                        List<Segment> segmentList = segments.getSegments();
                        Segment segment;
                        int startVertexID;
                        int endVertexID;
                        SegmentVertex startVertex;
                        SegmentVertex endVertex;

                        int len = segmentList.size();

                        for (Iterator<Segment> j = segmentList.iterator(); j.hasNext();) {
                            segment = j.next();

                            startVertexID = segment.getStartIndex();
                            endVertexID = segment.getEndIndex();
                            startVertex = segments.getVertex(startVertexID);
                            endVertex = segments.getVertex(endVertexID);

                            // calculate post costs
                            propertyName = "/ChefX3D/VertexParams/Sheet[@name='Vertex']/VertexDefinition/Vertex/@postSize";
                            startVertex.getProperties("Vertex", propertyName);

                            String postSize = entity.getProperties("Vertex", propertyName);
                            float height = Float.valueOf(postSize.split(" ")[1]);

                            tmpCapitolCost += (postCapitolCost * height);
                            tmpYearlyCost += (postYearlyCost * height);

                            // calculate postTop costs
                            propertyName = "/ChefX3D/VertexParams/Sheet[@name='Vertex']/VertexDefinition/Vertex/@postTopSize";
                            startVertex.getProperties("Vertex", propertyName);

                            postSize = entity.getProperties("Vertex", propertyName);
                            height = Float.valueOf(postSize.split(" ")[1]);

                            tmpCapitolCost += (postTopCapitolCost * height);
                            tmpYearlyCost += (postTopYearlyCost * height);

                            double[] startPos = startVertex.getPosition();
                            double[] endPos = endVertex.getPosition();

                            double distance = Math.sqrt(
                                    ((endPos[0] - startPos[0]) * 2) +
                                            ((endPos[1] - startPos[1]) * 2) +
                                            ((endPos[2] - startPos[2]) * 2));

                            // panel costs
                            tmpCapitolCost += (panelCapitolCost * distance);
                            tmpYearlyCost += (panelYearlyCost * distance);

                            // panelTop costs
                            tmpCapitolCost += (panelTopCapitolCost * distance);
                            tmpYearlyCost += (panelTopYearlyCost * distance);

                            // panelRail costs
                            tmpCapitolCost += (panelRailCapitolCost * distance);
                            tmpYearlyCost += (panelRailYearlyCost * distance);
                        }
                    }

                    capitolCosts += tmpCapitolCost;
                    rowData.add(String.valueOf(tmpCapitolCost));

                    maintenanceCosts += tmpYearlyCost;
                    rowData.add(String.valueOf(tmpYearlyCost));

                } else {

                    rowData.add("?");
                    rowData.add("?");

                }

                dataModel.addRow(rowData);

            }
        }

        // the seperators
        rowData = new Vector<>();
        rowData.add("");
        rowData.add("");
        rowData.add("--------");
        rowData.add("--------");
        dataModel.addRow(rowData);

        // the totals
        rowData = new Vector<>();
        rowData.add("Totals:");
        rowData.add("");
        rowData.add(String.valueOf(capitolCosts));
        rowData.add(String.valueOf(maintenanceCosts));
        dataModel.addRow(rowData);

        // set preferred sizing and display table
        JTable table = new JTable(dataModel);
        table.getColumn("Entity").setPreferredWidth(150);
        table.getColumn("ID").setPreferredWidth(30);
        table.getColumn("Capitol").setPreferredWidth(60);
        table.getColumn("Maintenance").setPreferredWidth(60);
        table.setEnabled(false);

        JScrollPane scrollpane = new JScrollPane(table);

        add(scrollpane, BorderLayout.CENTER);

    }

}
