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
import java.util.*;

import org.w3c.dom.*;
import javax.xml.xpath.*;
import javax.xml.xpath.XPathFactory;

// Internal Imports
import org.chefx3d.util.*;
import static org.chefx3d.util.DOMUtils.parseXML;
import static org.chefx3d.util.DefaultErrorReporter.getDefaultReporter;
import static org.chefx3d.util.XPathEvaluator.getNode;
import static org.chefx3d.util.XPathEvaluator.getString;
import static org.chefx3d.util.XPathEvaluator.getValue;

/**
 * An object representation of an entity.
 *
 * All changes to this class are controlled by the model. All setters are
 * protected. Use applyCommand on the model to change the entities values.
 *
 * Selection and HighLight are not model parameters so they can be public here.
 * *
 * @author Russell Dodds
 * @version $Revision: 1.11 $
 */
public class DefaultEntity
    implements Cloneable, PositionableEntity, AssociatableEntity, SegmentableEntity, Comparable<Entity> {

    /** The entityID */
    private int entityID;

    /** The properties of the entity, sheet = properties */
    private Map<String, Document> properties;

    /** The segmentProperties of the entity, sheet = properties */
    private Map<String, Document> segmentProperties;

    /** The vertexProperties of the entity, sheet = properties */
    private Map<String, Document> vertexProperties;

    /**
     * Cached properties extracted from the DOM. The key is the XPath
     * expression used to fetch the items from the DOM and the value
     * is what was extracted. This is used for performance due to the
     * horribly slow XPATH lookup times.
     */
    private Map<String, String> cachedProperties;

    /** Same as above but for the DOM Nodes instead */
    private Map<String, Node> cachedPropertyNodes;

    /** A list of lines that form a multi_segment tool */
    private SegmentSequence segments;

    /** Flag indicating this entity is a segment entity */
    private boolean segmentedEntity;

    /** Is this entity selected */
    private boolean selected;

    /** Is this entity highlighted */
    private boolean highlighted;

    /** The currently selected vertex */
    private int selectedVertexID;

    /** The currently highlited vertex */
    private int highlightedVertexID;

    /** The currently selected segment */
    private int selectedSegmentID;

    /** The currently highlited segment */
    private int highlightedSegmentID;

    /** The tools associated associates */
    private ArrayList<Integer> associates;

    /** The list of entities this is associated to */
    private ArrayList<Integer> associateParents;

    /** list of children entities */
    private ArrayList<Entity> children;

    /** Quick access to linear form of associates */
    private int[] associatesCache;

    /** Quick access to linear form of associateParents */
    private int[] associateParentsCache;

    /** Quick access to linear form of associateParents */
    private int[] childrenCache;

    /** Is the cache dirty */
    private boolean associatesCacheDirty;

    /** Is the cache dirty */
    private boolean associateParentsCacheDirty;

    /** Is the cache dirty */
    private boolean childrenCacheDirty;

    /** Is the cache dirty */
    private boolean updateChildren;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The parameter sheet to use */
    private String paramSheet;

    /** The list of EntityPropertyListeners */
    private List<EntityPropertyListener> propertyListeners;

    /** The list of EntityChildListeners */
    private List<EntityChildListener> childrenListeners;

    /** The list of EntitySelectionListeners */
    private List<EntitySelectionListener> selectionListeners;

    /** The list of EntityPositionListeners */
    private List<EntityPositionListener> positionListeners;

    /**
     * The constructor. The entity properties will be copied from the tool
     * defaults.
     *
     * @param entityID The entity ID
     * @param paramSheet The name of the sheet that contains the params
     * @param properties The properties of an entity
     */
    public DefaultEntity(int entityID, String paramSheet,
            Map<String, Document> properties) {

        init(entityID, paramSheet, properties, null, null);

    }

    /**
     * The constructor. The entity properties will be copied from the tool
     * defaults.
     *
     * @param entityID The entity ID
     * @param paramSheet
     * @param properties
     * @param segmentProperties
     * @param vertexProperties
     */
    public DefaultEntity(int entityID, String paramSheet,
            Map<String, Document> properties,
            Map<String, Document> segmentProperties,
            Map<String, Document> vertexProperties) {

        init(entityID, paramSheet, properties, segmentProperties, vertexProperties);

    }

    // ---------------------------------------------------------------
    // Methods defined by Object
    // ---------------------------------------------------------------

    /**
     * Create a copy of the Entity
     * @param clonedID
     * @return
     */
    public DefaultEntity clone(int clonedID) {

        // Create the new copy
        DefaultEntity clonedEntity = new DefaultEntity(clonedID, paramSheet,
                properties, segmentProperties, vertexProperties);

        // copy all the other data over
        clonedEntity.children = (ArrayList<Entity>)children.clone();
        clonedEntity.associates = (ArrayList<Integer>)associates.clone();
        clonedEntity.associateParents = (ArrayList<Integer>)associateParents.clone();
        clonedEntity.segments = segments;
        clonedEntity.selectedVertexID = selectedVertexID;
        clonedEntity.selectedSegmentID = selectedSegmentID;
        clonedEntity.segmentedEntity = segmentedEntity;

        return(clonedEntity);
    }

    /**
     * Compare the given details to this one to see if they are equal. Equality
     * is defined as pointing to the same clipPlane source, with the same
     * transformation value.
     *
     * @param o The object to compare against
     * @return true if these represent identical objects
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultEntity))
            return false;

        Entity e = (Entity) o;

        return e.getEntityID() == entityID;
    }

    /**
     * Calculate the hashcode for this object.
     *
     */
    @Override
    public int hashCode() {
        // TODO: Not a very good hash
        return entityID;
    }

    // ---------------------------------------------------------------
    // Local Methods
    // ---------------------------------------------------------------

    /**
     * Common initialization code.
     */
    private void init(int entityID, String paramSheet,
            Map<String, Document> properties,
            Map<String, Document> segmentProperties,
            Map<String, Document> vertexProperties) {

        this.entityID = entityID;
        this.paramSheet = paramSheet;

        children = new ArrayList<>();
        childrenCacheDirty = false;
        childrenCache = new int[0];

        associates = new ArrayList<>();
        associatesCacheDirty = false;
        associatesCache = new int[0];

        associateParents = new ArrayList<>();
        associateParentsCacheDirty = false;
        associateParentsCache = new int[0];

        propertyListeners = new ArrayList<>();
        childrenListeners = new ArrayList<>();
        selectionListeners = new ArrayList<>();
        positionListeners = new ArrayList<>();

        // TODO: deal with this
        segments =
            new SegmentSequence(segmentProperties, vertexProperties);

        errorReporter = getDefaultReporter();

        selected = false;
        highlighted = false;
        highlightedVertexID = -1;
        highlightedSegmentID = -1;

        selectedVertexID = -1;
        selectedSegmentID = -1;

        cachedProperties = new HashMap<>();
        cachedPropertyNodes  = new HashMap<>();

        // clone all properties
        this.properties = new HashMap<>();
        Iterator<Map.Entry<String, Document>> itr =
            properties.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, Document> entry = itr.next();
            this.properties.put(entry.getKey(),
                    (Document) entry.getValue().cloneNode(true));
        }

    }

    // ---------------------------------------------------------------
    // Methods defined by Entity
    // ---------------------------------------------------------------

    /**
     * Get this entityID
     *
     * @return The entityID
     */
    @Override
    public int getEntityID() {
        return entityID;
    }

    /**
     * DO NOT USE - Use Commands
     * Set this entityID
     *
     * @param entityID
     */
    @Override
    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    /**
     * Get the name
     */
    @Override
    public String getName() {

        return getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@name");

    }

    /**
     * Get the category
     */
    @Override
    public String getCategory() {

        return getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@category");

    }

    /**
     * Get the type
     */
    @Override
    public int getType() {

        return Integer.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@toolType"));

    }

    /**
     * Get the classificationLevel this entity is assigned to
     */
    @Override
    public int getClassificationLevel() {

        return Integer.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@classificationLevel"));

    }

    /**
     * Get the constraint this entity is assigned to
     */
    @Override
    public String getConstraint() {

        return getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@constraint");

    }

    /**
     * Get the fixedSize this entity is assigned to
     */
    @Override
    public boolean isFixedSize() {

        return Boolean.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@fixedSize"));

    }

    /**
     * Get the fixedSize this entity is assigned to
     */
    @Override
    public boolean isHelper() {

        return Boolean.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@helper"));

    }

    /**
     * Get the fixedSize this entity is assigned to
     */
    @Override
    public boolean isController() {

        return Boolean.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@controller"));

    }

    /**
     * Get the description of this entity
     */
    @Override
    public String getDescription() {

        Node description = getPropertyNode(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Description");

        return description.getTextContent();

    }

    /**
     * Get the category this entity is assigned to
     */
    @Override
    public String getIcon() {

        return getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Icons/@topDownUrl");

    }

    /**
     * Get the fixedAspect this entity is assigned to
     */
    @Override
    public boolean isFixedAspect() {

        return Boolean.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Icons/@fixedAspect"));

    }

    /**
     * Get the URL to the x3d file
     */
    @Override
    public String getURL() {

        return getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/OutputMapping/@x3dUrl");

    }

    /**
     * Set whether this entity is selected.
     *
     * @param selected Whether to highlight this entity
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Get whether this entity is selected.
     *
     * @return Whether this entity is selected
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set whether to highlight this entity.
     *
     * @param highlight Whether to highlight this entity
     */
    @Override
    public void setHighlighted(boolean highlight) {
        highlighted = highlight;
    }

    /**
     * Get whether to highlight this entity.
     *
     * @return Whether to highlight this entity
     */
    @Override
    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Get the parameters defined for this entity
     *
     * @return
     */
    @Override
    public Map<String, String> getParams() {

        Map<String, String> parameterMap = new HashMap<>();

        Element child;
        Node parameters = getPropertyNode(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Parameters");

        NodeList nodes = parameters.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            child = (Element) nodes.item(i);

            parameterMap.put(
                    child.getAttribute("name"),
                    child.getAttribute("value"));
        }

        return parameterMap;

    }

    /**
     * Get the parameters defined for this entity
     *
     * @return
     */
    @Override
    public Map<String, String> getStyleSheets() {

        Map<String, String> stylesMap = new HashMap<>();

        Element child;
        Node styleSheets = getPropertyNode(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/OutputMapping");

        NodeList nodes = styleSheets.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            child = (Element) nodes.item(i);

            stylesMap.put(
                    child.getAttribute("name"),
                    child.getAttribute("url"));
        }

        return stylesMap;

    }

    /**
     * DO NOT USE - Use Commands
     * Set the properties of a sheet.
     *
     * @param propSheet The sheet name
     * @param node The properties
     */
    @Override
    public void setProperties(String propSheet, Document node) {
        properties.put(propSheet, node);
        cachedProperties.clear();

        if (updateChildren) {

            Entity child;
            Iterator<Entity> itr = children.iterator();
            while (itr.hasNext()) {
                child = itr.next();
                child.setProperties(propSheet, node);
            }
        }
    }

    /**
     * DO NOT USE - Use Commands
     * Set all the properties sheets.
     *
     * @param props The properties
     */
    @Override
    public void setProperties(Map<String, Document> props) {
        properties = new HashMap<>(props);
        cachedProperties.clear();

        if (updateChildren) {

            Entity child;
            Iterator<Entity> itr = children.iterator();
            while (itr.hasNext()) {
                child = itr.next();
                child.setProperties(props);
            }
        }
    }

    /**
     * DO NOT USE - Use Commands
     * Set a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @param propValue The property value
     */
    @Override
    public void setProperties(String propSheet, String propName,
        String propValue) {

        Document doc = properties.get(propSheet);

        if (doc == null) {return;}

        Node node;

        cachedProperties.put(propName, propValue);

        try {

            //
            XPath xpath = XPathFactory.newInstance().newXPath();

            // Determine the XPath to use
            if (propName.contains("__")) {

                int index = propName.indexOf("__");
                int num = Integer.valueOf(propName.substring(index + 2,
                    propName.length()));
                String exp = propName.substring(0, index);

                // find the required nodelist in the document
                NodeList nodes = (NodeList) xpath.evaluate(exp, doc,
                    XPathConstants.NODESET);

                // now get the single node at the index
                node = nodes.item(num);

            } else {

                node = (Node) xpath
                    .evaluate(propName, doc, XPathConstants.NODE);

            }

            // update the xml

            //System.out.println("    node type: " + node.getNodeType() + " element: " + Node.ELEMENT_NODE + " text: " + Node.TEXT_NODE);

            if (node == null) {return;}

            if (node instanceof Text) {
                //System.out.println("    text data: " + propValue);
                ((CharacterData) node).setData(propValue);
            } else if (node instanceof Attr) {
                ((Attr) node).setValue(propValue);
            } else if (node instanceof Element) {
                Node n = node.getFirstChild();
                if (n instanceof Text) {
                    //System.out.println("    text data: " + propValue);
                    ((CharacterData) n).setData(propValue);
                } else {
                    Text text = doc.createTextNode(propValue);
                    node.appendChild(text);
                    //System.out.println("added new textNode to " + node);
                }
            }

            if (updateChildren) {

                Entity child;
                Iterator<Entity> itr = children.iterator();
                while (itr.hasNext()) {
                    child = itr.next();
                    child.setProperties(propSheet, propName, propValue);
                }

            }

            Iterator<EntityPropertyListener> i = propertyListeners.iterator();
            while(i.hasNext()) {
                EntityPropertyListener l = i.next();
                l.propertyUpdated(entityID, propSheet, node.toString());
            }


        } catch (NumberFormatException | XPathExpressionException | DOMException ex) {
            errorReporter.errorReport("Could not find property to set.", ex);
        }

    }

    /**
     * DO NOT USE - Use Commands
     * Set a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @param propValue The property value
     */
    @Override
    public void setProperty(String propSheet, String propName,
        Object propValue) {

        setProperties(propSheet, propName, (String) propValue);

    }

    /**
     * DO NOT USE - Use Commands
     * Add the specified property to the document.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     * @param propValue The property
     */
    @Override
    public void addProperty(String propSheet, String propName, Node propValue) {
        Document doc = properties.get(propSheet);

        Node node;
        Node importNode;

        cachedPropertyNodes.put(propName, propValue);

//System.out.println("Entity.addProperty()");
//System.out.println("    propSheet: " + propSheet);
//System.out.println("    propName: " + propName);

        try {

            XPath xpath = XPathFactory.newInstance().newXPath();

            // Determine the XPath to use
            if (propName.contains("__")) {

                int index = propName.indexOf("__");
                int num = Integer.valueOf(propName.substring(index + 2,
                    propName.length()));
                String exp = propName.substring(0, index);

                // System.out.println(" num: " + num);
                // System.out.println(" exp: " + exp);

                // find the required nodelist in the document
                NodeList nodes = (NodeList) xpath.evaluate(exp, doc,
                    XPathConstants.NODESET);

                // now get the single node at the index
                node = nodes.item(num);

            } else {

                node =  (Node) xpath
                    .evaluate(propName, doc, XPathConstants.NODE);

            }

            // save userData
            NamedNodeMap savedAttributes = null;

            if (propValue instanceof Document)
                savedAttributes = ((Document)propValue).getDocumentElement().getAttributes();
            else if (propValue instanceof Element)
                savedAttributes = propValue.getAttributes();

            Map<String, Object[]> attributeMap = new HashMap<>();

            String attributeName;
            Object[] editors;

            attributeMap.put("Node", (Object[])propValue.getUserData("DATA_EDITORS"));

            if (savedAttributes != null) {
                for (int i = 0; i < savedAttributes.getLength(); i++) {
                    attributeName = savedAttributes.item(i).getNodeName();
                    editors = (Object[]) savedAttributes.item(i).getUserData("DATA_EDITORS");

                    attributeMap.put(attributeName, editors);
                }
            }

            // Import the DOM fragment into the document
            if (propValue instanceof Document) {
                importNode = doc.importNode(((Document) propValue)
                    .getDocumentElement(), true);
            } else {
                importNode = doc.importNode(propValue, true);
            }

            // reapply userData
            NamedNodeMap newAttributes = importNode.getAttributes();

            importNode.setUserData("DATA_EDITORS", attributeMap.get("Node"), null);

            if (newAttributes != null) {
                for (int j = 0; j < newAttributes.getLength(); j++) {
                    attributeName = newAttributes.item(j).getNodeName();
                    if (attributeMap.containsKey(attributeName)) {
                        newAttributes.item(j).setUserData("DATA_EDITORS", attributeMap.get(attributeName), null);
                    }
                }
            }

            if (importNode instanceof Attr) {
                if (node instanceof Element) {
                    importNode.setUserData("DATA_EDITORS", propValue.getUserData("DATA_EDITORS"), null);
                    ((Element)node).setAttributeNode((Attr)importNode);
                } else {
                    errorReporter.errorReport("Cannot add an attribute to a non Element", null);
                }
            } else {
                // now tell it to be the child of the required node
                node.appendChild(importNode);
            }
        } catch (NumberFormatException | XPathExpressionException | DOMException ex) {
            errorReporter.errorReport("Could not find parent property to add to.", ex);
        }

    }

    /**
     * DO NOT USE - Use Commands
     * Add the specified property to the entity.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     * @param propValue The property
     */
    @Override
    public void addProperty(String propSheet, String propName, Object propValue) {
        // not implemented
    }

    /**
     * DO NOT USE - Use Commands
     * Remove the specified property from the document.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     */
    @Override
    public void removeProperty(String propSheet, Node parentNode, String propName) {


        Node node;
        Document doc = properties.get(propSheet);

//System.out.println("Entity.removeProperty()");
//System.out.println("    propSheet: " + propSheet);
//System.out.println("    propName: " + propName);

        try {

            XPath xpath = XPathFactory.newInstance().newXPath();

            // Determine the XPath to use
            if (propName.contains("__")) {

                int index = propName.indexOf("__");
                int num = Integer.valueOf(propName.substring(index + 2,
                    propName.length()));
                String exp = propName.substring(0, index);

                // System.out.println(" num: " + num);
                // System.out.println(" exp: " + exp);

                // find the required nodelist in the document
                NodeList nodes = (NodeList) xpath.evaluate(exp, doc,
                    XPathConstants.NODESET);

                // now get the single node at the index
                node = nodes.item(num);

            } else {

                node =  (Node) xpath
                    .evaluate(propName, doc, XPathConstants.NODE);

            }

            // Remove the node
            if (node instanceof Attr) {
                ((Element)parentNode).removeAttribute(((Attr)node).getName());
            } else {
                parentNode.removeChild(node);
            }

            cachedPropertyNodes.remove(propName);

        } catch (NumberFormatException | XPathExpressionException | DOMException ex) {
            errorReporter.errorReport("Could not remove the property.", ex);
        }

    }

    /**
     * DO NOT USE - Use Commands
     * Remove the specified property from the entity.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     */
    @Override
    public void removeProperty(String propSheet, String propName) {
//      not implemented
    }

    /**
     * Get a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @return propValue
     */
    @Override
    public String getProperties(String propSheet, String propName) {

        String prop = cachedProperties.get(propName);
        if(prop == null) {
            prop = getString(propName, getProperties(propSheet));
            cachedProperties.put(propName, prop);
        }

        return prop;
    }

    /**
     * Get a specific property.
     *
     * @param propGroup The grouping name
     * @param propName The name of the property to set
     * @return propValue
     */
    @Override
    public Object getProperty(String propGroup, String propName) {
        // not implemented
        return null;
    }

    /**
     * Get a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @return propValue
     */
    @Override
    public Node getPropertyNode(String propSheet, String propName) {

        Node prop = cachedPropertyNodes.get(propName);
        if(prop == null) {
            prop = getNode(propName, true, getProperties(propSheet));
            cachedPropertyNodes.put(propName, prop);
        }

        return prop;
    }

    /**
     * Get the properties for a sheet.
     *
     * @param sheetName The sheet name
     * @return propValue
     */
    @Override
    public Document getProperties(String sheetName) {
        return properties.get(sheetName);
    }

    /**
     * Get the properties for all sheets.
     *
     * @return The properties
     */
    @Override
    public Map<String, Document> getProperties() {
        return properties;
    }

    /**
     * Add a child to the entity
     *
     * @param entity
     */
    @Override
    public void addChild(Entity entity) {

        if (!children.contains(entity)) {

            // add the entityID to the internal data structure
            children.add(entity);
            childrenCacheDirty = true;

            // add the entityID to the persistent data storage
            // create the fragment
            Document doc = parseXML("<Entity id='" + entity.getEntityID() + "' />");
            Element elem = (Element) doc.getElementsByTagName("Entity").item(0);

            addProperty(paramSheet,
                    "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                    "/EntityDefinition/Tool/Children",
                    elem);

        }

    }

    /**
     * Remove a child from the entity
     *
     * @param entity
     */
    @Override
    public void removeChild(Entity entity) {

        if (children.contains(entity)) {
            // remove the entityID from the internal data structure
            children.remove(entity);
            childrenCacheDirty = true;

            // remove the entityID from the persistent data storage
            Document toolSheet = properties.get(paramSheet);
            Node parentNode = toolSheet.getElementsByTagName("Children").item(0);
            String propertyName =
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Children/Entity[@id='" + entity.getEntityID() + "']";

            removeProperty(paramSheet, parentNode, propertyName);

        }

    }

    /**
     * Add a child to the entity at a particular location
     *
     * @param index The index to add at
     * @param entity The child being added
     */
    @Override
    public void insertChildAt(int index, Entity entity) {

        if (!children.contains(entity)) {

            // add the entityID to the internal data structure
            if (index >= children.size()) {
                children.add(entity);
            } else if (index < 0) {
                children.add(0, entity);
            } else {
                children.add(index, entity);
            }
            childrenCacheDirty = true;

            // add the entityID to the persistent data storage
            // create the fragment
            Document doc = parseXML("<Entity id='" + entity.getEntityID() + "' />");
            Element elem = (Element) doc.getElementsByTagName("Entity").item(0);

            addProperty(paramSheet,
                    "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                    "/EntityDefinition/Tool/Children",
                    elem);

        }

    }

    /**
     * Get the index of the entity
     *
     * @param entityID
     * @return the index
     */
    @Override
    public int getChildIndex(int entityID) {
        return children.indexOf(entityID);
    }

    /**
     * Get an Entity at the index, returns null if not found
     *
     * @param index The index
     * @return The entityID
     */
    @Override
    public Entity getChildAt(int index) {
        if (children.size() > index) {
            return children.get(index);
        }
        return null;
    }

    /**
     * Get a list of all childrenIDs of this Entity
     *
     * @return The list of childrenIDs
     */
    @Override
    public int[] getChildrenIDs() {

        if (!childrenCacheDirty)
            return childrenCache;

        int[] ret_val = new int[children.size()];

        int cnt = 0;

        for (Iterator<Entity> itr = children.iterator(); itr.hasNext();) {
            ret_val[cnt++] = itr.next().getEntityID();
        }

        childrenCache = ret_val;
        childrenCacheDirty = false;
        return ret_val;

    }

    /**
     * Get a list of all children of this Entity
     *
     * @return The list of children entities
     */
    @Override
    public ArrayList<Entity> getChildren() {
        return children;
    }

    /**
     * Get the number of children of this Entity
     *
     * @return The number of children
     */
    @Override
    public int getChildCount() {
       return children.size();
    }

    /**
     * Does this Entity have any children
     *
     * @return true if it has children, false otherwise
     */
    @Override
    public boolean hasChildren() {
       return children.size() > 0;
    }

    /**
     * Each entity should notify listeners of property changes
     *
     * @param listener
     */
    @Override
    public void addEntityPropertyListener(EntityPropertyListener listener) {
        if (!propertyListeners.contains(listener)) {
            propertyListeners.add(listener);
        }
    }

    /**
     * Each entity should notify listeners of property changes
     *
     * @param listener
     */
    @Override
    public void removeEntityPropertyListener(EntityPropertyListener listener) {
        propertyListeners.remove(listener);
    }

    /**
     * Each entity should notify listeners of children changes
     *
     * @param listener
     */
    @Override
    public void addEntityChildListener(EntityChildListener listener) {
        if (!childrenListeners.contains(listener)) {
            childrenListeners.add(listener);
        }
    }

    /**
     * Each entity should notify listeners of children changes
     *
     * @param listener
     */
    @Override
    public void removeEntityChildListener(EntityChildListener listener) {
        childrenListeners.remove(listener);
    }

    /**
     * Each entity should notify listeners of selection changes
     *
     * @param listener
     */
    @Override
    public void addEntitySelectionListener(EntitySelectionListener listener) {
        if (!selectionListeners.contains(listener)) {
            selectionListeners.add(listener);
        }
    }

    /**
     * Each entity should notify listeners of selection changes
     *
     * @param listener
     */
    @Override
    public void removeEntitySelectionListener(EntitySelectionListener listener) {
        selectionListeners.remove(listener);
    }

    //----------------------------------------------------------
    // Methods for AssociatableEntity
    //----------------------------------------------------------

    /**
     * DO NOT USE - Use Commands
     * Associate an entity with this one.
     *
     * @param entityID The entityID
     */
    @Override
    public void addAssociation(int entityID) {

        if (!associates.contains(entityID)) {

            // add the entityID to the internal data structure
            associates.add(entityID);
            associatesCacheDirty = true;

            // add the entityID to the persistent data storage
            // create the fragment
            Document doc = parseXML("<Entity id='" + entityID + "' />");
            Element elem = (Element) doc.getElementsByTagName("Entity").item(0);

            addProperty(paramSheet,
                    "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                    "/EntityDefinition/Tool/Associates",
                    elem);

        }

    }

    /**
     * Retain the parent list of associations.
     *
     * @param entityID The entityID
     */
    @Override
    public void addParentAssociation(int entityID) {

        if (!associateParents.contains(entityID)) {

            // add the entityID to the internal data structure
            associateParents.add(entityID);
            associateParentsCacheDirty = true;

            // add the entityID to the persistent data storage
            // create the fragment
            Document doc = parseXML("<Entity id='" + entityID + "' />");
            Element elem = (Element) doc.getElementsByTagName("Entity").item(0);

            addProperty(paramSheet,
                    "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                    "/EntityDefinition/Tool/ParentAssociates",
                    elem);

        }

    }

    /**
     * DO NOT USE - Use Commands
     * Remove a child association.
     *
     * @param entityID the child's entityID
     */
    @Override
    public void removeAssociation(int entityID) {

        Integer id = entityID;
        if (associates.contains(id)) {
            // remove the entityID from the internal data structure
            associates.remove(id);
            associatesCacheDirty = true;

            // remove the entityID from the persistent data storage
            Document toolSheet = properties.get(paramSheet);
            Node parentNode = toolSheet.getElementsByTagName("Associates").item(0);
            String propertyName =
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Associates/Entity[@id='" + entityID + "']";

            removeProperty(paramSheet, parentNode, propertyName);

        }

    }

    /**
     * Remove the parent from the list of associations.
     *
     * @param entityID The entityID
     */
    @Override
    public void removeParentAssociation(int entityID) {

        Integer id = entityID;
        if (associateParents.contains(id)) {

            associateParents.remove(id);
            associateParentsCacheDirty = true;

            // remove the entityID from the persistent data storage
            Document toolSheet = properties.get(paramSheet);
            Node parentNode = toolSheet.getElementsByTagName("Associates").item(0);
            String propertyName =
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Associates/Entity[@id='" + entityID + "']";

            removeProperty(paramSheet, parentNode, propertyName);

        }

    }

    /**
     * Get the associated entities.
     *
     * @return The associates
     */
    @Override
    public int[] getAssociates() {
        if (!associatesCacheDirty)
            return associatesCache;

        int[] ret_val = new int[associates.size()];

        int cnt = 0;

        for (Iterator<Integer> itr = associates.iterator(); itr.hasNext();) {
            ret_val[cnt++] = itr.next();
        }

        associatesCache = ret_val;
        associatesCacheDirty = false;
        return ret_val;
    }

    /**
     * Get the associated parent entities.
     *
     * @return The associates
     */
    @Override
    public int[] getParentAssociates() {
        if (!associateParentsCacheDirty)
            return associateParentsCache;

        int[] ret_val = new int[associateParents.size()];

        int cnt = 0;

        for (Iterator<Integer> itr = associateParents.iterator(); itr.hasNext();) {
            ret_val[cnt++] = itr.next();
        }

        associateParentsCache = ret_val;
        associateParentsCacheDirty = false;
        return ret_val;
    }

    /**
     * Get the flag indicating if updates should
     * be applied to the children
     *
     * @return true/false
     */
    @Override
    public boolean getUpdateChildren() {

        return Boolean.getBoolean(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@updateChildren"));

    }

    /**
     * Set the flag indicating if updates should
     * be applied to the children
     *
     * @param bool
     */
    @Override
    public void setUpdateChildren(boolean bool) {

        // set a local varibale for quick access
        updateChildren = bool;

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/@updateChildren",
                String.valueOf(bool));

    }

    //----------------------------------------------------------
    // Methods for MultiSegmentEntity
    //----------------------------------------------------------

    @Override
    public boolean isLine() {

        return Boolean.valueOf(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/MultiSegment/@line"));

    }

    @Override
    public boolean isFixedLength() {

        float length = Float.parseFloat(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/MultiSegment/@length"));

        return length > 0;

    }

    /*
     * DO NOT USE - Use Commands
     * Add a segment to this tool.
     */
    @Override
    public void addSegment(int segmentID, int startVertexID, int endVertexID) {
        segments.addSegment(segmentID, startVertexID, endVertexID);
    }

    /*
     * DO NOT USE - Use Commands
     * Split a segment with the provided vertex.
     */
    @Override
    public void splitSegment(int segmentID, int vertexID) {
        segments.splitSegment(segmentID, vertexID);
    }

    /*
     * DO NOT USE - Use Commands
     * Remove a segment from this tool.
     */
    @Override
    public void removeSegment(int segmentID) {
        // TODO: do we need to notify the model?

        if (selectedSegmentID == segmentID)
            selectedSegmentID = -1;

        if (highlightedSegmentID == segmentID)
            highlightedSegmentID = -1;

        segments.removeSegment(segmentID);
    }

    /*
     * DO NOT USE - Use Commands
     * Add a segment vertex to this tool.
     */
    @Override
    public void addSegmentVertex(int vertexID, double[] pos) {
        segments.addVertex(vertexID, pos);
    }

    /*
     * DO NOT USE - Use Commands
     * Move a vertex of this SegmentSequence.
     */
    @Override
    public void moveSegmentVertex(int vertexID, double[] pos) {
        selectedVertexID = segments.moveVertex(vertexID, pos);
    }

    /*
     * DO NOT USE - Use Commands
     * Update a vertex of this SegmentSequence.
     */
    @Override
    public void updateSegmentVertex(int vertexID, String paramSheet,
        String propertyName, String newValue) {
        segments.updateVertex(vertexID, paramSheet, propertyName, newValue);
    }

    /*
     * DO NOT USE - Use Commands
     * Remove a segment vertex from this tool.
     */
    @Override
    public void removeSegmentVertex(int vertexID) {
        if (selectedVertexID == vertexID)
            selectedVertexID = -1;

        if (highlightedVertexID == vertexID)
            highlightedVertexID = -1;

        if (selectedSegmentID > -1) {
            Segment segment = segments.getSegment(selectedSegmentID);
            if (segment.getStartIndex() == vertexID || segment.getEndIndex() == vertexID)
                selectedSegmentID = -1;
        }

        segments.removeVertex(vertexID);
    }

    @Override
    public boolean isSegmentedEntity() {
        return segmentedEntity;
    }

    @Override
    public void setSegmentedEntity(boolean isSegment) {
        segmentedEntity = isSegment;
    }

    @Override
    public void setSelectedVertexID(int vertexID) {
        selectedVertexID = vertexID;
    }

    @Override
    public int getSelectedVertexID() {
        return selectedVertexID;
    }

    @Override
    public void setSelectedSegmentID(int segmentID) {
        selectedSegmentID = segmentID;
    }

    @Override
    public int getSelectedSegmentID() {
        return selectedSegmentID;
    }

    @Override
    public void setHighlightedVertexID(int vertexID) {
        highlightedVertexID = vertexID;
    }

    @Override
    public int getHighlightedVertexID() {
        return highlightedVertexID;
    }

    @Override
    public SegmentVertex getSelectedVertex() {
        if ((segments != null) && (segments.getLength() > 0)) {
            return segments.getVertex(selectedVertexID);
        } else {
            return null;
        }
    }

    @Override
    public void setHighlightedSegmentID(int segmentID) {
        highlightedSegmentID = segmentID;
    }

    @Override
    public int getHighlightedSegmentID() {
        return highlightedSegmentID;
    }

    @Override
    public Segment getSelectedSegment() {
        if ((segments != null) && (segments.getLength() > 0)) {
            return segments.getSegment(selectedSegmentID);
        } else {
            return null;
        }
    }

    @Override
    public double[] getSelectedVertexPosition() {
        return segments.getVertexPosition(selectedVertexID);
    }

    @Override
    public boolean isVertexSelected() {

        return selectedVertexID > -1;

    }

    @Override
    public boolean isSegmentSelected() {

        return selectedSegmentID > -1;

    }

    @Override
    public SegmentSequence getSegmentSequence() {
        return segments;
    }

    // ---------------------------------------------------------------
    // Methods defined by PositionableEntity
    // ---------------------------------------------------------------

    /**
     * Get the position of the entity.
     *
     * @param pos The position
     */
    @Override
    public void getPosition(double[] pos) {

        pos[0] = Double.parseDouble(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Position/@x"));

        pos[1] = Double.parseDouble(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Position/@y"));

        pos[2] = Double.parseDouble(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Position/@z"));

    }

    /**
     * DO NOT USE - Use Commands
     * Set the current position of the entity
     *
     * @param pos
     */
    @Override
    public void setPosition(double[] pos) {

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Position/@x",
                String.valueOf(pos[0]));

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Position/@y",
                String.valueOf(pos[1]));

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Position/@z",
                String.valueOf(pos[2]));

        if (updateChildren) {

            Entity child;
            Iterator<Entity> itr = children.iterator();
            while (itr.hasNext()) {
                child = itr.next();
                if (child instanceof PositionableEntity) {
                    ((PositionableEntity)child).setPosition(pos);
                }
            }
        }
    }

    /**
     * Get the current rotation of the entity
     *
     * @param rot The rotation
     */
    @Override
    public void getRotation(float[] rot) {

        rot[0] = Float.parseFloat(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@x"));

        rot[1] = Float.parseFloat(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@y"));

        rot[2] = Float.parseFloat(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@z"));

        rot[3] = Float.parseFloat(getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@angle"));

    }

    /**
     * DO NOT USE - Use Commands
     * Set the current rotation of the entity
     *
     * @param rot
     */
    @Override
    public void setRotation(float[] rot) {

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@x",
                String.valueOf(rot[0]));

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@y",
                String.valueOf(rot[1]));

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@z",
                String.valueOf(rot[2]));

        setProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Rotation/@angle",
                String.valueOf(rot[3]));

        if (updateChildren) {

            Entity child;
            Iterator<Entity> itr = children.iterator();
            while (itr.hasNext()) {
                child = itr.next();
                if (child instanceof PositionableEntity) {
                    ((PositionableEntity)child).setRotation(rot);
                }
            }
        }
    }

    /**
     * Get the size of this entity.
     *
     * @param size The array to place the size values
     */
    @Override
    public void getSize(float[] size) {

        String expr;
        String sizeSheet =
            getProperties(paramSheet,
                    "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                    "/EntityDefinition/Tool/Size/@sheet");
        Document sizeDoc = properties.get(sizeSheet);

        expr = getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Size/@xExpr");
        size[0] = (float) getValue(expr, sizeDoc);

        expr = getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Size/@yExpr");
        size[1] = (float) getValue(expr, sizeDoc);

        expr = getProperties(paramSheet,
                "/ChefX3D/EntityParams/Sheet[@name='" + paramSheet + "']" +
                "/EntityDefinition/Tool/Size/@zExpr");
        size[2] = (float) getValue(expr, sizeDoc);

    }

    /**
     * Compare positioning of this Entity with the provide Entity.
     *
     * @param compare The Entity to compare to
     * @return true if same location, false otherwise
     */
    @Override
    public boolean samePosition(Entity compare) {


        // check the entity positions
        double[] position = new double[3];
        getPosition(position);

        double[] comparePosition = new double[3];
        if (compare instanceof PositionableEntity) {
            ((PositionableEntity)compare).getPosition(comparePosition);
        } else {
            return false;
        }

        if ((position[0] != comparePosition[0]) ||
            (position[1] != comparePosition[1]) ||
            (position[2] != comparePosition[2])) {
            return false;
        }

        // check the vertex positions
        boolean check = false;
        if (compare instanceof SegmentableEntity) {
            check = compare.isSegmentedEntity();
        }

        if (isSegmentedEntity() && check) {

            SegmentSequence compareSegments = ((SegmentableEntity)compare).getSegmentSequence();
            if (segments.getLength() != compareSegments.getLength()) {
                return false;
            }

            List<SegmentVertex> vertices =  segments.getVertices();
            List<SegmentVertex> compareVertices =  compareSegments.getVertices();

            for (int i = 0; i < vertices.size(); i++) {

                if ((vertices.get(i).getPosition()[0] != compareVertices.get(i).getPosition()[0]) ||
                    (vertices.get(i).getPosition()[1] != compareVertices.get(i).getPosition()[1]) ||
                    (vertices.get(i).getPosition()[2] != compareVertices.get(i).getPosition()[2])) {
                    return false;
                }
            }
        }

        return true;

    }

    /**
     * Add a listener for positioning changes.
     *
     * @param listener The listener to add
     */
    @Override
    public void addEntityPositionListener(EntityPositionListener listener) {
        if ((listener != null) && !positionListeners.contains(listener)) {
            positionListeners.add(listener);
        }
    }

    /**
     * Remove a listener for positioning changes.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeEntityPositionListener(EntityPositionListener listener) {
        if (listener != null) {
            positionListeners.remove(listener);
        }
    }

    //----------------------------------------------------------
    // Utility Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = getDefaultReporter();
    }

    @Override
    public int compareTo(Entity o) {
        return entityID - o.getEntityID();
    }

}
