/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2006-2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there"s a problem you get to fix it.
 *
 ****************************************************************************/

// External Imports
import java.util.*;

import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

// Local imports
import org.chefx3d.property.awt.*;

import org.chefx3d.PropertyPanelDescriptor;
import org.chefx3d.model.WorldModelFactory;
import org.chefx3d.property.DataEditor;
import org.chefx3d.property.DataValidator;
import org.chefx3d.catalog.CatalogManager;
import org.chefx3d.catalog.Catalog;
import org.chefx3d.tool.MultiSegmentTool;
import org.chefx3d.tool.MultiplicityConstraint;
import org.chefx3d.tool.Tool;
import org.chefx3d.tool.ToolGroup;
import org.chefx3d.tool.ToolGroupChild;
import org.chefx3d.util.DOMUtils;

/**
 * A simple example of how to use ChefX3D
 *
 * @author Alan Hudson
 * @version
 */
public class NetworkedExample extends BaseExample {

    /** The content path */
    private static final String cpath = "catalog/";

    /** The image path */
    private static final String ipath = "images/";

    public NetworkedExample(String server, String username, String password) {
        super(WorldModelFactory.NETWORK_XMPP, server, "mercury",
                username, password);    }

    /**
     * Create ChefX3D tools.
     */
    protected void createTools() {
        ArrayList<ToolGroup> tools = new ArrayList<ToolGroup>();
        ArrayList<ToolGroupChild> chapters = null;
        Document properties = null;
        HashMap<String, String> styles = new HashMap<String, String>();
        Tool tool;
        ToolGroup td;
        String[] toolParams = null;
        PropertyPanelDescriptor[] propertyPanels;
        PropertyPanelDescriptor[] segmentPanels;
        PropertyPanelDescriptor[] vertexPanels;
        DataEditor editor;
        DataValidator validator;
        Element n;
        Attr att;
        NodeList nlist;

        // Locations Menu
        // Grid World
        chapters = new ArrayList<ToolGroupChild>();
        properties = DOMUtils.parseXML("" +
                "<ChefX3D>" +
                "   <EntityParams>" +
                "       <Sheet name='SMAL'>" +
                "           <EntityDefinition>" +
                "               <Grid timeOfDay='0' foo='bar' />" +
                "               <Description>This is a grid</Description>" +
                "           </EntityDefinition>" +
                "       </Sheet>" +
                "   </EntityParams>" +
                "</ChefX3D>");

        nlist = properties.getElementsByTagName("Grid");

        String tooltip = "Use time of day is it?";
        String[] validValues = new String[] {"Noon", "Dusk" , "Night", "Dawn"};

        editor = new ComboBoxEditor(tooltip, validValues,
                validValues[0], DataEditor.Types.ENTITY_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("timeOfDay");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        att = n.getAttributeNode("foo");
        att.setValue(String.valueOf("test"));
        editor = new NullEditor();
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        Node node = properties.getElementsByTagName("Description").item(0);
        int rows = 5;
        node = node.getFirstChild();
        editor = new TextAreaEditor(node.getTextContent(), rows);
        node.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        propertyPanels = new PropertyPanelDescriptor[1];
        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);

        styles.put("x3d", cpath + "common/x3d_default_world.xslt");

        String[] interfaceIcons = new String[] {ipath + "Grid16x16.png",
                ipath + "Grid32x32.png", ipath + "Grid64x64.png"};

        tool = new Tool("Grid", ipath + "Grid.png", interfaceIcons, false, Tool.TYPE_WORLD,
                new String[] { cpath + "Locations/Grid/Grid.x3dv" }, Classification.FREE, "Grid",
                propertyPanels, null, null, styles, "SMAL", "16", "0.1", "16", toolParams,
                MultiplicityConstraint.SINGLETON, "World", false, false, false);

        chapters.add(tool);

        td = new ToolGroup("Locations", chapters);
        tools.add(td);

        // Primitives Menu
        // Box
        chapters = new ArrayList<ToolGroupChild>();
        propertyPanels = new PropertyPanelDescriptor[2];

        properties = DOMUtils
                .parseXML(""
                        + "<ChefX3D>"
                        + " <EntityParams>"
                        + "     <Sheet name='SMAL'>"
                        + "         <EntityDefinition>"
                        + "             <Box x='2' y='2' z='2' />"
                        + "         </EntityDefinition>"
                        + "     </Sheet>"
                        + " </EntityParams>"
                        + "</ChefX3D>");

        nlist = properties.getElementsByTagName("Box");

        validator = new InRangeValidator(0.00000001, Float.MAX_VALUE);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("x");
        att.setUserData("DATA_VALIDATORS", new DataValidator[] {validator}, validator);

        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);

        properties = DOMUtils
        .parseXML(""
                + "<ChefX3D>"
                + " <EntityParams>"
                + "     <Sheet name='Cost'>"
                + "         <EntityDefinition>"
                + "             <Cost capitolBase='2' maintenanceBase='0.5' />"
                + "         </EntityDefinition>"
                + "     </Sheet>"
                + " </EntityParams>"
                + "</ChefX3D>");

        propertyPanels[1] = new PropertyPanelDescriptor("Cost", null, properties);

        styles = new HashMap<String, String>();
        styles.put("x3d", cpath + "Primitives/Box/Box.xslt");
        styles.put("x3d_view", cpath + "Primitives/Box/Box_view.xslt");
        toolParams = null;

        interfaceIcons = new String[] {ipath + "Box16x16.png", ipath + "Box32x32.png",
                ipath + "/Box64x64.png"};

        tool = new Tool("Box", ipath + "Box.png", interfaceIcons, false, Tool.TYPE_MODEL,
                new String[] { cpath + "Primitives/Box/Box.x3d" }, Classification.FREE, "Box",
                propertyPanels, null, null, styles, "SMAL", "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Box/@x",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Box/@y", "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Box/@z",
                toolParams,MultiplicityConstraint.NO_REQUIREMENT, "Model", false, false, false);

        chapters.add(tool);

        // Cone
        properties = DOMUtils
                .parseXML("<ChefX3D><EntityParams><Sheet name='SMAL'><EntityDefinition><Cone bottom='true' bottomRadius='1' height='2' side='true' solid='true' /></EntityDefinition></Sheet></EntityParams></ChefX3D>");

        nlist = properties.getElementsByTagName("Cone");
        n = (Element) nlist.item(0);

        editor = new CheckBoxEditor(true, DataEditor.Types.ENTITY_PROPERTY);

        att = n.getAttributeNode("bottom");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);
        att = n.getAttributeNode("side");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);
        att = n.getAttributeNode("solid");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        propertyPanels = new PropertyPanelDescriptor[1];
        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);

        styles = new HashMap<String, String>();
        styles.put("x3d", cpath + "Primitives/Cone/Cone.xslt");
        styles.put("x3d_view", cpath + "Primitives/Cone/Cone_view.xslt");
        toolParams = null;

        interfaceIcons = new String[] {ipath + "Cone16x16.png", ipath + "Cone32x32.png",
                ipath + "Cone64x64.png"};

        tool = new Tool("Cone", ipath + "Cone.png", interfaceIcons, false,
                Tool.TYPE_MODEL, new String[] { cpath
                        + "Primitives/Cone/Cone.x3d" }, Classification.FREE, "Cone", propertyPanels,
                null, null, styles, "SMAL",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cone/@bottomRadius * 2",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cone/@height",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cone/@bottomRadius * 2", toolParams,
                MultiplicityConstraint.NO_REQUIREMENT, "Model", false, false, false);

        chapters.add(tool);

        // Cylinder
        properties = DOMUtils
                .parseXML("<ChefX3D><EntityParams><Sheet name='SMAL'><EntityDefinition><Cylinder bottom='true' height='2' radius='1' side='true' solid='true' top='true' /></EntityDefinition></Sheet></EntityParams></ChefX3D>");

        nlist = properties.getElementsByTagName("Cylinder");
        n = (Element) nlist.item(0);

        editor = new CheckBoxEditor(true, DataEditor.Types.ENTITY_PROPERTY);

        att = n.getAttributeNode("bottom");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);
        att = n.getAttributeNode("side");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);
        att = n.getAttributeNode("solid");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);
        att = n.getAttributeNode("top");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);


        propertyPanels = new PropertyPanelDescriptor[1];
        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);
        styles = new HashMap<String, String>();
        styles.put("x3d", cpath + "Primitives/Cylinder/Cylinder.xslt");
        styles
                .put("x3d_view", cpath
                        + "Primitives/Cylinder/Cylinder_view.xslt");
        toolParams = null;

        interfaceIcons = new String[] {ipath + "Cylinder16x16.png",
                ipath + "Cylinder32x32.png", ipath + "Cylinder64x64.png"};

        tool = new Tool("Cylinder", ipath + "Cylinder.png", interfaceIcons, false,
                Tool.TYPE_MODEL, new String[] { cpath
                        + "Primitives/Cylinder/Cylinder.x3d" }, Classification.FREE, "Cylinder",
                        propertyPanels, null, null, styles, "SMAL",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cylinder/@radius * 2",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cylinder/@height",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cylinder/@radius * 2", toolParams,
                MultiplicityConstraint.NO_REQUIREMENT, "Model", false, false, false);

        chapters.add(tool);

        // Sphere
        properties = DOMUtils
                .parseXML("<ChefX3D><EntityParams><Sheet name='SMAL'><EntityDefinition><Sphere radius='1' /></EntityDefinition></Sheet></EntityParams></ChefX3D>");
        propertyPanels = new PropertyPanelDescriptor[1];
        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);
        styles = new HashMap<String, String>();
        styles.put("x3d", cpath + "Primitives/Sphere/Sphere.xslt");
        styles.put("x3d_view", cpath + "Primitives/Sphere/Sphere_view.xslt");
        toolParams = null;

        interfaceIcons = new String[] {ipath + "Sphere16x16.png",
                ipath + "Sphere32x32.png", ipath + "Sphere64x64.png"};

        tool = new Tool("Sphere", ipath + "Sphere.png", interfaceIcons, false,
                Tool.TYPE_MODEL, new String[] { cpath
                        + "Primitives/Sphere/Sphere.x3d" }, Classification.FREE, "Sphere",
                        propertyPanels, null, null, styles, "SMAL",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Sphere/@radius * 2",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Sphere/@radius * 2",
                "/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Sphere/@radius * 2", toolParams,
                MultiplicityConstraint.NO_REQUIREMENT, "Model", false, false, false);

        chapters.add(tool);

        td = new ToolGroup("Primitives", chapters);
        td.setTool(tool);

        tools.add(td);

        // Barriers Menu
        // Fence
        chapters = new ArrayList<ToolGroupChild>();

        properties = DOMUtils
            .parseXML("" +
                "<ChefX3D>" +
                "   <EntityParams>" +
                "       <Sheet name='SMAL'>" +
                "           <EntityDefinition>" +
                "               <Fence height='2' />" +
                "           </EntityDefinition>" +
                "       </Sheet>" +
                "   </EntityParams>" +
                "</ChefX3D>");
        propertyPanels = new PropertyPanelDescriptor[1];
        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);

        properties = DOMUtils
            .parseXML("" +
                "<ChefX3D>" +
                "   <SegmentParams>" +
                "       <Sheet name='Segment'>" +
                "           <SegmentDefinition>" +
                "               <FenceSegment span='true'/>" +
                "           </SegmentDefinition>" +
                "       </Sheet>" +
                "   </SegmentParams>" +
                "</ChefX3D>");

        nlist = properties.getElementsByTagName("FenceSegment");
        n = (Element) nlist.item(0);

        editor = new CheckBoxEditor(true, DataEditor.Types.VERTEX_PROPERTY);

        att = n.getAttributeNode("span");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        segmentPanels = new PropertyPanelDescriptor[1];
        segmentPanels[0] = new PropertyPanelDescriptor("Segment", null, properties);

        properties = DOMUtils
            .parseXML("" +
                "<ChefX3D>" +
                "   <VertexParams>" +
                "       <Sheet name='Vertex'>" +
                "           <VertexDefinition>" +
                "               <FenceVertex cornerType='Curled' />" +
                "           </VertexDefinition>" +
                "       </Sheet>" +
                "   </VertexParams>" +
                "</ChefX3D>");

        nlist = properties.getElementsByTagName("FenceVertex");

        tooltip = "Use what type of corner transition?";
        validValues = new String[] {"Curled"};

        editor = new ComboBoxEditor(tooltip, validValues,
                validValues[0], DataEditor.Types.VERTEX_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("cornerType");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        vertexPanels = new PropertyPanelDescriptor[1];
        vertexPanels[0] = new PropertyPanelDescriptor("Vertex", null, properties);

        styles = new HashMap<String, String>();
        styles.put("x3d", cpath + "Barriers/FloatingBarriers/FloatingBarrier.xslt");
        styles.put("x3d_global", cpath + "Barriers/FloatingBarriers/FloatingBarrier_global.xslt");
        styles.put("x3d_view", cpath + "Barriers/FloatingBarriers/FloatingBarrier_view.xslt");

        toolParams = new String[1];
        toolParams[0] = "15.3"; // Segment length is unrestricted if set to 0

        interfaceIcons = new String[] {ipath + "Segment16x16.png",
                ipath + "Segment32x32.png", ipath + "Segment64x64.png"};

        tool = new MultiSegmentTool("New Floating Barrier", ipath + "Segment.png",
                interfaceIcons, false,
                Tool.TYPE_MULTI_SEGMENT, new String[] { cpath
                        + "Barriers/FloatingBarriers/FloatingBarrier.x3dv" }, Classification.FREE, "FloatingBarrier", propertyPanels,
                null, vertexPanels, styles, "SMAL", "0.3","2", "0.3", toolParams,
                MultiplicityConstraint.NO_REQUIREMENT, "Barrier", false, false, false, true);

        chapters.add(tool);

        // Create the new land fence
        tool = buildLandTool();

        chapters.add(tool);

        td = new ToolGroup("Barrier", chapters);
        tools.add(td);

        tools.add(createWaypointTools());

        tools.add(createAStarTools());

        CatalogManager cmanager = CatalogManager.getCatalogManager();
        Catalog catalog = new Catalog("Sample", 1, 0);
        cmanager.addCatalog(catalog);
        catalog.addTools(tools);
    }

    /**
     * Create tools for authoring land fences
     */
    private Tool buildLandTool() {

        Document properties = null;
        HashMap<String, String> styles = new HashMap<String, String>();
        Tool tool;
        String[] toolParams = null;
        PropertyPanelDescriptor[] propertyPanels;
        PropertyPanelDescriptor[] segmentPanels;
        PropertyPanelDescriptor[] vertexPanels;
        NodeList nlist;
        String tooltip;
        String[] comboBoxValidValues;
        DataEditor editor;
        Element n;
        Attr att;

        // Land Fence
        properties = DOMUtils
            .parseXML("<ChefX3D>"
                    + "   <EntityParams>"
                    + "       <Sheet name='SMAL'>"
                    + "           <EntityDefinition>"
                    + "               <Fence stepped='false' panelAppearance='0' panelTopAppearance='4' panelRailAppearance='3' postAppearance='2' postTopAppearance='2' />"
                    + "               <Panel panelHeight='2' panelTopHeight='0.3' />"
                    + "           </EntityDefinition>"
                    + "       </Sheet>"
                    + "   </EntityParams>"
                    + "</ChefX3D>");

        // Define the stepped DataEditor
        nlist = properties.getElementsByTagName("Fence");

        editor = new CheckBoxEditor(false, DataEditor.Types.ENTITY_PROPERTY);
        editor.setEnabled(false);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("stepped");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the panelAppearance DataEditor
        nlist = properties.getElementsByTagName("Fence");

        tooltip = "Use what type of fence panel?";
        comboBoxValidValues = new String[] {"chainLink"};

        // setup mapping of text to ID
        HashMap<String, String> panelAppearanceMapping = new HashMap<String, String>(1);
        panelAppearanceMapping.put("chainLink", "0");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, panelAppearanceMapping,
                "0", DataEditor.Types.ENTITY_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("panelAppearance");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the panelTopAppearance DataEditor
        nlist = properties.getElementsByTagName("Fence");

        tooltip = "Use what type of top section panel?";
        comboBoxValidValues = new String[] {"barbedWire"};

        // setup mapping of text to ID
        HashMap<String, String> panelTopAppearanceMapping = new HashMap<String, String>(1);
        panelTopAppearanceMapping.put("barbedWire", "4");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, panelTopAppearanceMapping,
                "4", DataEditor.Types.ENTITY_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("panelTopAppearance");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the panelRailAppearance DataEditor
        nlist = properties.getElementsByTagName("Fence");

        tooltip = "Use what type of rail?";
        comboBoxValidValues = new String[] {"none"};

        // setup mapping of text to ID
        HashMap<String, String> panelRailAppearanceMapping = new HashMap<String, String>(1);
        panelRailAppearanceMapping.put("none", "3");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, panelRailAppearanceMapping,
                "3", DataEditor.Types.ENTITY_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("panelRailAppearance");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the postAppearance DataEditor
        nlist = properties.getElementsByTagName("Fence");

        tooltip = "Use what color post?";
        comboBoxValidValues = new String[] {"black", "none"};

        // setup mapping of text to ID
        HashMap<String, String> postAppearanceMapping = new HashMap<String, String>(2);
        postAppearanceMapping.put("black", "2");
        postAppearanceMapping.put("none", "3");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, postAppearanceMapping,
                "2", DataEditor.Types.ENTITY_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postAppearance");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the postTopAppearance DataEditor
        nlist = properties.getElementsByTagName("Fence");

        tooltip = "Use what color top section post?";
        comboBoxValidValues = new String[] {"black", "none"};

        // setup mapping of text to ID
        HashMap<String, String> postTopAppearanceMapping = new HashMap<String, String>(2);
        postTopAppearanceMapping.put("black", "2");
        postTopAppearanceMapping.put("none", "3");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, postTopAppearanceMapping,
                "2", DataEditor.Types.ENTITY_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postTopAppearance");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        propertyPanels = new PropertyPanelDescriptor[2];
        propertyPanels[0] = new PropertyPanelDescriptor("SMAL", null, properties);

        // Land Fence
        properties = DOMUtils
            .parseXML("<ChefX3D>" +
                    "   <EntityParams>" +
                    "       <Sheet name='Cost'>" +
                    "           <EntityDefinition>" +
                    "               <Cost capitolBase='2' maintenanceBase='2' />" +
                    "               <Panel>" +
                    "                   <chainLink capitolPerMeter='1' maintenancePerMeter='0.1' />" +
                    "               </Panel>" +
                    "               <PanelTop>" +
                    "                   <barbedWire capitolPerMeter='1' maintenancePerMeter='0.1' />" +
                    "               </PanelTop>" +
                    "               <PanelRail>" +
                    "                   <none capitolPerMeter='0' maintenancePerMeter='0' />" +
                    "               </PanelRail>" +
                    "               <Post>" +
                    "                   <black capitolPerMeter='1' maintenancePerMeter='0.1' />" +
                    "                   <silver capitolPerMeter='1' maintenancePerMeter='0.1' />" +
                    "               </Post>" +
                    "               <PostTop>" +
                    "                   <black capitolPerMeter='1' maintenancePerMeter='0.1' />" +
                    "                   <silver capitolPerMeter='1' maintenancePerMeter='0.1' />" +
                    "               </PostTop>" +
                    "           </EntityDefinition>" +
                    "           <TypeMappings>" +
                    "               <Appearance id='0' value='chainLink' />" +
                    "               <Appearance id='1' value='silver' />" +
                    "               <Appearance id='2' value='black' />" +
                    "               <Appearance id='3' value='none' />" +
                    "               <Appearance id='4' value='barbedWire' />" +
                    "           </TypeMappings>" +
                    "       </Sheet>" +
                    "   </EntityParams>" +
                    "</ChefX3D>");

        propertyPanels[1] = new PropertyPanelDescriptor("Cost", null, properties);


        properties = DOMUtils
            .parseXML("<ChefX3D>" +
                    "   <SegmentParams>" +
                    "       <Sheet name='SMAL'>" +
                    "           <SegmentDefinition>" +
                    "               <Panel panelHeight='2.0' panelTopHeight='0.3' panelOverhang='0' panelRailCount='2' panelRailHeight='0.075' panelRailType='0' panelRaisedHeight='0' panelSpan='true'/>" +
                    "           </SegmentDefinition>" +
                    "       </Sheet>" +
                    "   </SegmentParams>" +
                    "</ChefX3D>");

        // Define the panelRailType DataEditor
        nlist = properties.getElementsByTagName("Panel");

        tooltip = "Use what type of rail?";
        comboBoxValidValues = new String[] {"square"};

        // setup mapping of text to ID
        HashMap<String, String> panelRailTypeMapping = new HashMap<String, String>(1);
        panelRailTypeMapping.put("square", "0");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, panelRailTypeMapping,
                "0", DataEditor.Types.SEGMENT_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("panelRailType");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        segmentPanels = new PropertyPanelDescriptor[1];
        segmentPanels[0] = new PropertyPanelDescriptor("Segment", null, properties);

        properties = DOMUtils
            .parseXML("" +
                "<ChefX3D>" +
                "   <VertexParams>" +
                "       <Sheet name='Vertex'>" +
                "           <VertexDefinition>" +
                "               <Vertex postBraced='false' postTopType='0' postType='0' postSize='0.1 2 0.1' postTopSize='0.1 0.3 0.1'/>" +
                "           </VertexDefinition>" +
                "       </Sheet>" +
                "   </VertexParams>" +
                "</ChefX3D>");

        // Define the postBraced DataEditor
        nlist = properties.getElementsByTagName("Vertex");

        editor = new CheckBoxEditor(false, DataEditor.Types.VERTEX_PROPERTY);
        editor.setEnabled(false);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postBraced");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the postType DataEditor
        nlist = properties.getElementsByTagName("Vertex");

        tooltip = "Use what type of post?";
        comboBoxValidValues = new String[] {"square"};

        // setup mapping of text to ID
        HashMap<String, String> postTypeMapping = new HashMap<String, String>(1);
        postTypeMapping.put("square", "0");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, postTypeMapping,
                "0", DataEditor.Types.VERTEX_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postType");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the postCapType DataEditor
        nlist = properties.getElementsByTagName("Vertex");

        tooltip = "Use what type of post top?";
        comboBoxValidValues = new String[] {"squareAngled"};

        // setup mapping of text to ID
        HashMap<String, String> postTopTypeMapping = new HashMap<String, String>(1);
        postTopTypeMapping.put("squareAngled", "0");

        editor = new ComboBoxEditor(tooltip, comboBoxValidValues, postTopTypeMapping,
                "0", DataEditor.Types.VERTEX_PROPERTY);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postTopType");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the postSize DataEditor
        nlist = properties.getElementsByTagName("Vertex");

        editor = new VectorFieldEditor("0.1 2 0.1", DataEditor.Types.VERTEX_PROPERTY);
        editor.setEnabled(true);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postSize");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        // Define the postTopSize DataEditor
        nlist = properties.getElementsByTagName("Vertex");

        editor = new VectorFieldEditor("0.1 0.3 0.1", DataEditor.Types.VERTEX_PROPERTY);
        editor.setEnabled(true);

        n = (Element) nlist.item(0);
        att = n.getAttributeNode("postTopSize");
        att.setUserData("DATA_EDITORS", new DataEditor[] {editor}, editor);

        vertexPanels = new PropertyPanelDescriptor[1];
        vertexPanels[0] = new PropertyPanelDescriptor("Vertex", null, properties);

        styles = new HashMap<String, String>();
        styles.put("x3d", cpath + "Barriers/Fence/Fence.xslt");
        styles.put("x3d_global", cpath + "Barriers/Fence/Fence_global.xslt");
        styles.put("x3d_view", cpath + "Barriers/Fence/Fence_view.xslt");

        toolParams = new String[1];
        toolParams[0] = "0"; // Segment length is unrestricted if set to 0

        String[] interfaceIcons = new String[] {ipath + "Segment16x16.png",
                ipath + "Segment32x32.png", ipath + "Segment64x64.png"};

        tool = new MultiSegmentTool("New Land Fence", ipath + "Segment.png", interfaceIcons, false,
                Tool.TYPE_MULTI_SEGMENT, new String[] { cpath
                        + "Barriers/Fence/FencePrototype.x3dv" }, Classification.FREE, "New Land Fence", propertyPanels,
                null, vertexPanels, styles, "SMAL", "0.3", "0.3", "0.3", toolParams,
                MultiplicityConstraint.NO_REQUIREMENT, "Barrier", false, false, false, true);

        return tool;
    }


    /**
     * Create tools for authoring waypoints
     */
    private ToolGroup createWaypointTools() {
        ToolGroup root;
        ArrayList<ToolGroupChild> tools = new ArrayList<ToolGroupChild>();
        ArrayList<ToolGroupChild> chapters = null;
        Document properties = null;
        HashMap<String, String> styles = new HashMap<String, String>();
        Tool tool = null;
        ToolGroup td;
        String[] toolParams = null;
        PropertyPanelDescriptor[] panels;
        PropertyPanelDescriptor[] segmentPanels;
        PropertyPanelDescriptor[] vertexPanels;
        NodeList nlist;
        String tooltip;
        String[] comboBoxValidValues;
        DataEditor editor;
        Element n;
        Attr att;

        chapters = new ArrayList<ToolGroupChild>();
        properties = DOMUtils.parseXML("<ChefX3D><EntityParams><Sheet name='SMAL'><EntityDefinition><Waypoint name='' speed='0'/></EntityDefinition></Sheet></EntityParams></ChefX3D>");

        panels = new PropertyPanelDescriptor[1];
        panels[0] = new PropertyPanelDescriptor("SMAL",null, properties);

        properties = DOMUtils.parseXML("<ChefX3D><SegmentParams><Sheet name='Segment'><SegmentDefinition><WaypointSegment speed='0'/></SegmentDefinition></Sheet></SegmentParams></ChefX3D>");
        segmentPanels = new PropertyPanelDescriptor[1];
        segmentPanels[0] = new PropertyPanelDescriptor("Segment",null, properties);

        properties = DOMUtils
            .parseXML("<ChefX3D><VertexParams><Sheet name='Vertex'><VertexDefinition><WaypointVertex speed='0' /></VertexDefinition></Sheet></VertexParams></ChefX3D>");

        vertexPanels = new PropertyPanelDescriptor[1];
        vertexPanels[0] = new PropertyPanelDescriptor("Vertex",null, properties);

        String[] interfaceIcons = new String[] {ipath + "Segment16x16.png",
                ipath + "Segment32x32.png", ipath + "Segment64x64.png"};

        try {
            styles = new HashMap<String, String>();
            styles.put("x3d", cpath + "Waypoint/Linear/WaypointLinear.xslt");
            styles.put("x3d_global", cpath + "Waypoint/WaypointLinear_global.xslt");
            styles.put("x3d_view", cpath + "Waypoint/Linear/WaypointLinear_view.xslt");
            styles.put("viskit", cpath + "Waypoint/Linear/WaypointLinear_viskit.xslt");
            toolParams = new String[1];
            toolParams[Tool.PARAM_SEGMENT_LENGTH] = "0";

            tool = new MultiSegmentTool("Waypoint", ipath + "Segment.png", interfaceIcons,
                      false, Tool.TYPE_MULTI_SEGMENT,
                      new String[] {"No3DRep.x3d"},
                      0,"WaypointLinear", panels, segmentPanels, vertexPanels, styles, "SMAL","0.3", "0.3", "0.3",
                      toolParams, MultiplicityConstraint.NO_REQUIREMENT, "Waypoint", false, true, false, true);
        } catch(Exception e) {
            e.printStackTrace();

            return null;
        }

        chapters.add(tool);

        td = new ToolGroup("Linear", chapters);
        tools.add(td);

        root = new ToolGroup("Waypoint", tools);

        return root;
    }

    /**
     * Create tools for authoring a star paths
     */
    private ToolGroup createAStarTools() {
        ToolGroup root;
        ArrayList<ToolGroup> tools = new ArrayList<ToolGroup>();
        ArrayList<ToolGroupChild> chapters = null;
        Document properties = null;
        HashMap<String, String> styles = new HashMap<String, String>();
        Tool tool = null;
        ToolGroup td;
        String[] toolParams = null;
        PropertyPanelDescriptor[] panels;
        PropertyPanelDescriptor[] segmentPanels;
        PropertyPanelDescriptor[] vertexPanels;
        NodeList nlist;
        String tooltip;
        String[] comboBoxValidValues;
        DataEditor editor;
        Element n;
        Attr att;

        chapters = new ArrayList<ToolGroupChild>();
        properties = DOMUtils.parseXML("<ChefX3D><EntityParams><Sheet name='SMAL'><EntityDefinition><AStar name=''/></EntityDefinition></Sheet></EntityParams></ChefX3D>");

        panels = new PropertyPanelDescriptor[1];
        panels[0] = new PropertyPanelDescriptor("SMAL",null, properties);

        properties = DOMUtils.parseXML("<ChefX3D><SegmentParams><Sheet name='Segment'><SegmentDefinition><AStarSegment speed='0' reversible='TRUE'/></SegmentDefinition></Sheet></SegmentParams></ChefX3D>");
        segmentPanels = new PropertyPanelDescriptor[1];
        segmentPanels[0] = new PropertyPanelDescriptor("Segment",null, properties);

        properties = DOMUtils
            .parseXML("<ChefX3D><VertexParams><Sheet name='Vertex'><VertexDefinition><AStarVertex speed='0'/></VertexDefinition></Sheet></VertexParams></ChefX3D>");

        vertexPanels = new PropertyPanelDescriptor[1];
        vertexPanels[0] = new PropertyPanelDescriptor("Vertex",null, properties);

        String[] interfaceIcons = new String[] {ipath + "Segment16x16.png",
                ipath + "Segment32x32.png", ipath + "Segment64x64.png"};

        try {
            styles = new HashMap<String, String>();
            styles.put("x3d", cpath + "AStar/AStar.xslt");
            styles.put("x3d_global", cpath + "AStar/AStar_global.xslt");
            styles.put("x3d_view", cpath + "AStar/AStar_view.xslt");
            toolParams = new String[1];
            toolParams[Tool.PARAM_SEGMENT_LENGTH] = "0";

            tool = new MultiSegmentTool("AStar", ipath + "Segment.png", interfaceIcons,
                      false, Tool.TYPE_MULTI_SEGMENT,
                      new String[] {"No3DRep.x3d"},
                      0,"AStar", panels, segmentPanels, vertexPanels, styles, "SMAL","0.3", "0.3", "0.3",
                      toolParams, MultiplicityConstraint.NO_REQUIREMENT, "AStar", false, true, false, false);
        } catch(Exception e) {
            e.printStackTrace();

            return null;
        }

        chapters.add(tool);
/*
        td = new ToolGroup("Linear", chapters);
        tools.add(td);
*/
        root = new ToolGroup("AStar", chapters);

        return root;
    }

    public static void main(final String args[]) {
        if (args.length < 2) {
            System.out.println("Usage: server username password");
            return;
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Set System L&F
                    UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
                } catch (UnsupportedLookAndFeelException e) {
                    // handle exception
                } catch (ClassNotFoundException e) {
                   // handle exception
                } catch (InstantiationException e) {
                   // handle exception
                } catch (IllegalAccessException e) {
                   // handle exception
                }
		        NetworkedExample example = new NetworkedExample(args[0], args[1], args[2]);
            }
        });

    }
}
