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

package org.chefx3d.model;

// External Imports
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// Internal Imports
import org.chefx3d.tool.*;
import org.chefx3d.util.ConfigManager;
import org.chefx3d.util.DOMUtils;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A helper class to construct entities
 *
 * @author Russell Dodds
 * @version $Revision: 1.7 $
 */
public class EntityBuilder  {

    /** The name of the sheet */
    private final static String PARAM_SHEET = "ToolParams";

    /** The name of the defaults file, needs to be in the classpath */
    private String toolDefaults = "config/model/ToolParamDefaults.xml";

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The singleton class */
    private static EntityBuilder entityBuilder;

    EntityBuilder() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the singleton EntityBuilder.
     *
     * @return The EntityBuilder
     */
    public static EntityBuilder getEntityBuilder() {
        if (entityBuilder == null) {
            entityBuilder = new EntityBuilder();
        }

        return entityBuilder;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

    /**
     * Override the default tools file, must be in the
     * current classpath otherwise it will not be found.
     *
     * @param toolDefaults The name of the tool defaults file
     */
    private void setToolDefaults(String toolDefaults) {
        this.toolDefaults = toolDefaults;
    }

    /**
     * Create an Entity and issue the addEntity command
     *
     * @param model The model
     * @param entityID The entity ID
     * @param position The position in 3 dimensions
     * @param rotation The rotation in 3 dimensions
     * @param tool The tool used to create the entity
     * @return The new Entity
     */
    public Entity createEntity(WorldModel model, int entityID,
            double[] position, float[] rotation, Tool tool) {

        // Create the parameter sheets
        Map<String, Document> defaults = tool.getUserProperties();
        Map<String, Document> properties = new HashMap<>();

        // Clone all sheets defaults
        Iterator<Map.Entry<String, Document>> itr =
            defaults.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, Document> entry = itr.next();

            Document doc = entry.getValue();
            properties.put(entry.getKey(), (Document)doc.cloneNode(true));
        }

        // add the default parameter sheet
        properties.put(PARAM_SHEET, createParamSheet(tool, position, rotation));

        Entity newEntity;
        if (tool instanceof MultiSegmentTool) {

            newEntity = createMultiSegmentEntity(model, entityID, properties, tool);

        } else if (tool instanceof EntityGroupTool) {

            newEntity = createEntityGroupEntity(model, position,
                    rotation, properties, (EntityGroupTool)tool);

        } else {

            newEntity = createDefaultEntity(model, entityID, properties);

        }

        return newEntity;
    }

    /**
     * Create the parameter sheet and apply the tool values to it
     *
     * @return The updated sheet
     */
    private Document createParamSheet(Tool tool, double[] position,
            float[] rotation) {

        Element elem;
        Node node;
        Node parentNode;
        Document toolSheet = null;

        // add the parameter sheet
        try {

            DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                builderFactory.newDocumentBuilder();

            toolSheet = builder.parse(ConfigManager.getConfigFile(toolDefaults));

            elem = (Element) toolSheet.getElementsByTagName("Sheet").item(0);
            elem.setAttribute("name", PARAM_SHEET);

            elem = (Element) toolSheet.getElementsByTagName("Tool").item(0);

            elem.setAttribute("name", tool.getName());
            elem.setAttribute("category", tool.getCategory());
            elem.setAttribute("toolType",
                    String.valueOf(tool.getToolType()));
            elem.setAttribute("classificationLevel",
                    String.valueOf(tool.getClassificationLevel()));
            elem.setAttribute("constraint",
                    tool.getMultiplicityConstraint().toString());
            elem.setAttribute("fixedSize",
                    String.valueOf(tool.isFixedSize()));
            elem.setAttribute("helper",
                    String.valueOf(tool.isHelper()));
            elem.setAttribute("controller",
                    String.valueOf(tool.isController()));

//            elem = (Element) toolSheet.getElementsByTagName("Tool").item(0);

            node = toolSheet.getElementsByTagName("Description").item(0);
            node.setTextContent(tool.getDescription());

            elem = (Element) toolSheet.getElementsByTagName("Size").item(0);
            elem.setAttribute("sheet", tool.getSizeSheetName());
            elem.setAttribute("xExpr", tool.getXSizeExpression());
            elem.setAttribute("yExpr", tool.getYSizeExpression());
            elem.setAttribute("zExpr", tool.getZSizeExpression());

            elem = (Element) toolSheet.getElementsByTagName("Position").item(0);
            elem.setAttribute("x", String.valueOf(position[0]));
            elem.setAttribute("y", String.valueOf(position[1]));
            elem.setAttribute("z", String.valueOf(position[2]));

            elem = (Element) toolSheet.getElementsByTagName("Rotation").item(0);
            elem.setAttribute("x", String.valueOf(rotation[0]));
            elem.setAttribute("y", String.valueOf(rotation[1]));
            elem.setAttribute("z", String.valueOf(rotation[2]));
            elem.setAttribute("angle", String.valueOf(rotation[3]));

            elem = (Element) toolSheet.getElementsByTagName("Icons").item(0);
            elem.setAttribute("topDownUrl", tool.getIcon());
            elem.setAttribute("fixedAspect",
                    String.valueOf(tool.isFixedAspect()));

            String[] icons = tool.getInterfaceIcons();
            if (icons != null) {
                // create the fragment
                Document doc = DOMUtils.parseXML("<InterfaceIcon url='' />");
                elem = (Element) doc.getElementsByTagName("InterfaceIcon").item(0);

                // find the parent node to add to
                parentNode = toolSheet.getElementsByTagName("Icons").item(0);
                Element copyElem;

                for (String icon : icons) {
                    copyElem = (Element) elem.cloneNode(true);
                    copyElem.setAttribute("url", icon);
                    node = toolSheet.importNode(copyElem, true);
                    parentNode.appendChild(node);
                }

            }

            elem = (Element) toolSheet.getElementsByTagName("OutputMapping").item(0);
            elem.setAttribute("x3dUrl", tool.getURL()[0]);

            Map<String, String> styles = tool.getStylesheets();
            if (styles != null) {
                // create the fragment
                Document doc = DOMUtils.parseXML("<Mapping name='' url='' />");
                elem = (Element) doc.getElementsByTagName("Mapping").item(0);

                // find the parent node to add to
                parentNode = toolSheet.getElementsByTagName("OutputMapping").item(0);
                Element copyElem;

                Iterator<Map.Entry<String, String>> itr =
                    styles.entrySet().iterator();

                while (itr.hasNext()) {
                    Map.Entry<String, String> entry = itr.next();
                    copyElem = (Element) elem.cloneNode(true);
                    copyElem.setAttribute("name", entry.getKey());
                    copyElem.setAttribute("url", entry.getValue());

                    node = toolSheet.importNode(copyElem, true);
                    parentNode.appendChild(node);

                }

            }

            //DOMUtils.print(toolSheet);

        } catch(ParserConfigurationException | SAXException | IOException| DOMException e) {
            errorReporter.errorReport(e.getMessage(), e);
        }

        return toolSheet;
    }

    /**
     * Perform any tasks for a default Entity
     *
     * @param entity The entity
     */
    private Entity createDefaultEntity(WorldModel model, int entityID,
            Map<String, Document> properties) {

        // Create the default Entity
        return new DefaultEntity(entityID, PARAM_SHEET, properties);

    }

    /**
     * Perform any tasks for a MultiSegment Entity
     */
    private Entity createMultiSegmentEntity(WorldModel model, int entityID,
            Map<String, Document> properties, Tool tool) {

        String fixedLength;
        Document toolSheet = properties.get(PARAM_SHEET);

        Element elem = (Element) toolSheet.getElementsByTagName("MultiSegment").item(0);

        String[] params = tool.getToolParams();
        if (params != null && params.length > 0) {

            fixedLength = params[Tool.PARAM_SEGMENT_LENGTH];
            if (Float.parseFloat(fixedLength) > 0) {
                elem.setAttribute("length", fixedLength);
            } else {
                elem.setAttribute("length", "-1");
            }
        }

        elem.setAttribute("line", Boolean.toString(((MultiSegmentTool)tool).isLine()));

        // Create the default Entity
        DefaultEntity entity = new DefaultEntity(entityID, PARAM_SHEET, properties,
                tool.getSegmentDefaults(), tool.getVertexDefaults());

        // set the segmented Entity flag
        entity.setSegmentedEntity(true);

        return entity;
    }

    /**
     * Perform any tasks for an Entity with children
     *
     * @param model The model
     * @param entity The entity
     * @param position The position
     * @param rotation The rotation
     * @param tool The tool used to create the entity
     * @return An entity with children
     */
    private Entity createEntityGroupEntity(WorldModel model,
            double[] position, float[] rotation,
            Map<String, Document> properties, EntityGroupTool tool) {

        // Create the default Entity
        int entityID = model.issueEntityID();
        DefaultEntity entity = new DefaultEntity(entityID, PARAM_SHEET, properties);

        // create all the children
        createChildren(model, entity, position, rotation, tool);

        // update children properties
        entity.setUpdateChildren(true);

        return entity;
    }

    /**
     * Create all the children entities
     *
     * @param model The model
     * @param entity The entity
     * @param position The position
     * @param rotation The rotation
     * @param tool The tool used to create the entity
     */
    private void createChildren(WorldModel model, Entity entity,
            double[] position, float[] rotation, Tool tool) {

        int entityID;
        DefaultEntity childEntity;
        Tool childTool;

        //TODO: finish this
        if ((tool instanceof EntityGroupTool) &&
                (((EntityGroupTool)tool).hasChildren())) {

            List<Tool> children = ((EntityGroupTool)tool).getChildren();
            Iterator<Tool> itr = children.iterator();

            while (itr.hasNext()) {

                childTool = itr.next();

                // Create the parameter sheets
                Map<String, Document> defaults = childTool.getDefaults();
                Map<String, Document> properties = new HashMap<>();

                // Clone all sheets defaults
                Iterator<Map.Entry<String, Document>> itr1 =
                    defaults.entrySet().iterator();

                while (itr1.hasNext()) {
                    Map.Entry<String, Document> entry = itr1.next();

                    Document doc = entry.getValue();
                    properties.put(entry.getKey(), (Document)doc.cloneNode(true));
                }

                // add the default parameter sheet
                properties.put(PARAM_SHEET, createParamSheet(childTool, position, rotation));

                entityID = model.issueEntityID();
                childEntity = new DefaultEntity(entityID, PARAM_SHEET, properties);

                entity.addChild(childEntity);

                // create any children
                createChildren(model, childEntity, position, rotation, childTool);

            }
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

}
