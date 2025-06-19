package org.chefx3d.property.awt;

//External Imports
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.w3c.dom.*;
import com.zookitec.layout.*;
import org.chefx3d.AuthoringComponent;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.model.Entity;
import org.chefx3d.property.DataEditor;
import org.chefx3d.property.DataValidator;
import org.chefx3d.property.PanelItem;
import org.chefx3d.view.ViewManager;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

public class PropertyData implements EntityChangeListener {

    /** The world model */
    private WorldModel model;

    /** The list of property panels, sheetname = panel */
    private Map<String, JPanel> propertyPanels;

    /** List of editors for a single panel */
    private java.util.List<String> editorList;

    /** A Map to store Components for easy lookup later, name = component */
    private Map<String, PanelItem> editorComponents;

    /** The maximum number of cols */
    private int maxCol;

    /** The number of rows */
    private int numRows;

    /** The current entityID */
    private int currentEntityID;

    /** The current entity */
    private Entity entity;

    /** The number of element levels to skip */
    private int skipLevels;

    /** The ViewManager */
    private ViewManager vmanager;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /** List of panels to display */
    private String[] displayPanels;

    /** The default data editor to use */
    private Class<?> defaultEditor;

    /**
     * Get the named editable property sheet for an entity
     *
     * @param model
     * @param vmanager
     * @param skipLevels
     * @param entityID
     * @param displayPanels
     * @param defaultEditor
     */
    public PropertyData(WorldModel model, ViewManager vmanager, int skipLevels,
            int entityID, String[] displayPanels, Class<?> defaultEditor) {

        this.model = model;
        this.vmanager = vmanager;
        this.skipLevels = skipLevels;
        this.displayPanels = displayPanels;
        this.defaultEditor = defaultEditor;

        currentEntityID = entityID;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        entity = model.getEntity(entityID);

        propertyPanels = new HashMap<>();
        editorComponents = new HashMap<>();

        model.addEntityChangeListener(entity, PropertyData.this);

        createPropertyPanels();

    }

    // ----------------------------------------------------------
    // Methods required by EntityChangeListener
    // ----------------------------------------------------------

    /**
     * A property changed.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propSheet The property which changed
     * @param propName The property which changed
     * @param propValue The new value.
     */
    @Override
    public void propertyChanged(boolean local, int entityID, String propSheet,
            String propName, Object propValue) {

/*
System.out.println("PropertyData.propertyChanged()");
System.out.println("    entityID: " + entityID);
System.out.println("    propSheet: " + propSheet);
System.out.println("    propName: " + propName);
System.out.println("    propValue: " + propValue);
*/
        updatePropertyPanel(local, entityID, propSheet, propName, propValue);

    }

    /**
     * A segment was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param startVertexID The starting vertexID
     * @param endVertexID The starting vertexID
     */
    @Override
    public void segmentAdded(boolean local, int entityID,
            int segmentID, int startVertexID, int endVertexID) {

        // ignored
    }

    /**
     * A segment was split.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param vertexID The starting vertexID
     */
    @Override
    public void segmentSplit(boolean local, int entityID,
            int segmentID, int vertexID) {

        // ignored
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The segment removed
     */
    @Override
    public void segmentRemoved(boolean local, int entityID,
            int segmentID) {

        // ignored
    }

    /**
     * A vertex was added to an entity.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID,
            int vertexID, double[] position) {
        // ignore
    }

    /**
     * A vertex was updated.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param propSheet The property which changed
     * @param propName The property which changed
     * @param propValue The new value.
     */
    @Override
    public void segmentVertexUpdated(boolean local, int entityID,
            int vertexID, String propSheet, String propName,
            String propValue) {

        updatePropertyPanel(local, entityID, propSheet, propName, propValue);
    }

    /**
     * A vertex was moved.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     * @param position The position in world coordinates
     */
    @Override
    public void segmentVertexMoved(boolean local, int entityID,
            int vertexID, double[] position) {
        // ignore
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the view
     */
    @Override
    public void segmentVertexRemoved(boolean local, int entityID,
            int vertexID) {
        // ignore
    }

    /**
     * An entity has changed size.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param size The new size
     */
    @Override
    public void entitySizeChanged(boolean local, int entityID, float[] size) {
    }

    /**
     * An entity was associated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityAssociated(boolean local, int parent, int child) {
    }

    /**
     * An entity was unassociated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityUnassociated(boolean local, int parent, int child) {
        //propertyPanels = new HashMap<String, JPanel>();
        //editorComponents = new HashMap<String, PanelItem>();

        //System.out.println("PropertyData.entityUnassociated()");

        //createPropertyPanels();
    }

    /**
     * The entity moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    @Override
    public void entityMoved(boolean local, int entityID, double[] position) {
    }

    /**
     * The entity was scaled.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    @Override
    public void entityScaled(boolean local, int entityID, float[] scale) {
    }

    /**
     * The entity was rotated.
     *
     * @param local Was this action initiated from the local UI
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Return the property data in the required format
     * @return the property data in the required format
     */
    public Object getComponent() {

        if (propertyPanels.isEmpty()) {
            return null;
        } else {
            return propertyPanels;
        }

    }

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Update the property.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propName The property which changed
     * @param newValue The new value.
     * @param values The complete value tree. It is ok to retain a reference to
     *        this.
     */
    private void updatePropertyPanel(boolean local, int entityID,
            String propSheet, String propName, Object newValue) {

        if (editorComponents.containsKey(propName)) {

            PanelItem item = editorComponents.get(propName);
            Object comp = item.getComponent();

            if (comp instanceof DataEditor) {
                ((DataEditor) comp).setValue((String) newValue);
            }
        }
    }

    /**
     * Create the panel from the currently selected entity.
     */
    private void createPropertyPanels() {

        String propertyNameRoot;

        maxCol = 1;
        numRows = 0;

        if (entity == null) {
            propertyPanels.put("BLANK", new JPanel());
            return;
        }

        // get the properties for the entity.
        Map<String, Document> properties = entity.getProperties();

//System.out.println("    properties.size(): " + properties.size());

        // TODO: This seems dodgy, shouldn't they already be there?

        // add the properties for  the vertex, if necessary
        if (entity instanceof SegmentableEntity) {

            if (entity.isSegmentedEntity() && ((SegmentableEntity)entity).getSelectedVertexID() != -1) {
                SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();

                SegmentVertex vertex = segments.getVertex(((SegmentableEntity)entity).getSelectedVertexID());

                if (vertex != null) {
                    Map<String, Document> vertexProperties = vertex.getProperties();
                    if (vertexProperties != null) {
                        properties.putAll(vertexProperties);
                    }
                }
            }

            // add the properties for  the vertex, if necessary
            if (entity.isSegmentedEntity() && ((SegmentableEntity)entity).getSelectedSegmentID() != -1) {
                SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();

                Segment segment = segments.getSegment(((SegmentableEntity)entity).getSelectedSegmentID());

                if (segment != null) {
                    Map<String, Document> segmentProperties = segment.getProperties();
                    if (segmentProperties != null) {
                        properties.putAll(segmentProperties);
                    }
                }
            }
        }

        if (properties == null) {
            propertyPanels.put("BLANK", new JPanel());
            return;
        }

//System.out.println("Props: size: " + properties.size() + " props: " + properties);
        // iterate through each property sheet
        Iterator<Map.Entry<String, Document>> index = properties.entrySet()
                .iterator();
        boolean display;

        while (index.hasNext()) {
            editorList = new ArrayList<>();

            Map.Entry<String, Document> mapEntry = index.next();

            // get the key, value pairing
            String sheetName = mapEntry.getKey();
            Document sheetProperties = mapEntry.getValue();

//System.out.println("    sheetName: " + sheetName);
//org.chefx3d.util.DOMUtils.print(sheetProperties);

            // no property sheet provided
            if (sheetProperties == null) {
                errorReporter.errorReport("No DOM to create panel", new Exception());
                return;
            }

            // determine which sheets to display
            display = false;
            for (String displayPanel : displayPanels) {
                if (sheetName.equals(displayPanel)) {
                    display = true;
                    break;
                }
            }

            if (entity instanceof SegmentableEntity) {

                if (sheetName.equals("Vertex") && (!((SegmentableEntity)entity).isVertexSelected())) {
    //System.out.println("   ***Don't display vertex sheet");
//                    display = false;
                    break;
                }

                if (sheetName.equals("Segment") && (!((SegmentableEntity)entity).isSegmentSelected())) {
    //System.out.println("   ***Don't display segment sheet");
//                    display = false;
                    break;
                }
            }

            if (display) {
//System.out.println("   display");
                // lets start parsing the property sheet
                NodeList nodes = sheetProperties.getChildNodes();

                // get the first node to work with
                Node currentNode = nodes.item(0);

                propertyNameRoot = "/" + currentNode.getNodeName();

                // No properties to edit
                if (currentNode == null) {

                    continue;
                }

                // skip the number of levels specified at creation time
                if (skipLevels > 0) {

                    for (int i = 0; i < skipLevels; i++) {

                        nodes = currentNode.getChildNodes();

                        int j = 0;

                        while (j < nodes.getLength()) {
                            currentNode = nodes.item(j);

                            if (currentNode instanceof Element) {

                                propertyNameRoot += "/" + currentNode.getNodeName();

                                if (currentNode.getNodeName().equals("Sheet")) {
                                    propertyNameRoot += "[@name='" + sheetName + "']";
                                }
                                break;

                            }

                            j++;

                        }

                        // No properties to edit
//                        if (currentNode == null) {
//                            continue;
//                        }
                    }
                }

                // create the panel we are going to populate this sheet with
                JPanel propertyPanel = new JPanel();

                PropertyPanelLayout layout = new PropertyPanelLayout();
                propertyPanel.setLayout(layout);

                // get the list of nodes to process
                nodes = currentNode.getChildNodes();

                int len = nodes.getLength();
                if (len == 0) {
                    errorReporter.warningReport("Nothing to edit in property panel?", null);
                    propertyPanels.clear();
                    continue;
                }

                //System.out.println("*** xpath root: " + propertyNameRoot);

                for (int i = 0; i < len; i++) {
                    currentNode = nodes.item(i);

                    if (currentNode instanceof Element) {
                        addElement((Element) currentNode, 0, sheetName,
                                propertyNameRoot);
                    } else {

                    }
                }


                // REMOVE FOR PRODUCTION
                //printPanel(editorList);

                // propertyPanel.setPreferredSize(new Dimension(200, 2000));

                // PropertyPanelLayout PropertyPanelLayout = new
                // PropertyPanelLayout(editorComponents);
                // propertyPanel.setLayout(PropertyPanelLayout);
                propertyPanel.setName(sheetName);

                PanelItem item;
                ExplicitConstraints ec;

                Component previousComponent = null;
                Component currentComponent = null;

                int VGAP = 4;
                int HGAP = 16;
                int SPACER = 4;

                // i = 1
                for (int i = 0; i < editorList.size(); i++) {

                    item = editorComponents.get(editorList.get(i));

//System.out.println("ITEM: " + item.getType());

                    switch (item.getType()) {

                        case LABEL:

                            // Get the DataEditor
                            currentComponent = (Component) item.getComponent();

                            if (i == 0) {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(ContainerEF.left(propertyPanel).add(HGAP));
                                ec.setY(ContainerEF.top(propertyPanel).add(VGAP));
                            } else {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(MathEF.add(ContainerEF.left(propertyPanel),
                                        (HGAP * (item.getCol() + 1))));
                                ec.setY(MathEF.add(ComponentEF
                                        .bottom(previousComponent), VGAP));
                            }

                            // Add to the panel
                            propertyPanel.add(currentComponent, ec);

                            if (i + 1 < editorList.size()) {

                                item = editorComponents.get(editorList
                                        .get(i + 1));

//System.out.println(item.getComponent().getClass());
                                if (item.getType() == PanelItem.DataEditorType.DATAEDITOR) {

                                    // Get the DataEditor
                                    JPanel text = (JPanel) ((AuthoringComponent) item.getComponent())
                                            .getComponent();

                                    // Set the constraints
                                    ec = new ExplicitConstraints(text);
                                    ec.setX(MathEF.add(ComponentEF.right(currentComponent),
                                            SPACER));
                                    ec.setY(MathEF.add(ComponentEF.top(currentComponent),
                                            -2));
//System.out.println("name: " + ((DataEditor) item.getComponent()).getPropertyName());
//System.out.println("ExplicitConstraints set");

                                    // Add to the panel
                                    propertyPanel.add(text, ec);

//System.out.println("ExplicitConstraints set");
                                    currentComponent = text;

                                    i++;
                                } else if (item.getType() == PanelItem.DataEditorType.VECTORFIELD) {
                                    // Get the DataEditor
                                    JPanel text = (JPanel) ((AuthoringComponent) item.getComponent())
                                            .getComponent();

                                    // Set the constraints
                                    ec = new ExplicitConstraints(text);
                                    ec.setX(MathEF.add(ContainerEF.left(propertyPanel),
                                            (HGAP * (item.getCol() + 1))));
                                    ec.setY(MathEF.add(ComponentEF
                                            .bottom(currentComponent), VGAP));

                                    // Add to the panel
                                    propertyPanel.add(text, ec);

                                    currentComponent = text;

                                    i++;

                                }

                            }

                            break;

                        case DATAEDITOR:

                            // Get the DataEditor
                            currentComponent = (Component) ((AuthoringComponent) item
                                    .getComponent()).getComponent();

                            if (i == 0) {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(ContainerEF.left(propertyPanel).add(HGAP));
                                ec.setY(ContainerEF.top(propertyPanel).add(VGAP));
                            } else {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(MathEF.add(ContainerEF.left(propertyPanel),
                                        (HGAP * (item.getCol() + 1))));
                                ec.setY(MathEF.add(ComponentEF
                                        .bottom(previousComponent), VGAP));
                            }

                            // Add to the panel
                            propertyPanel.add(currentComponent, ec);

                            break;

                        case TEXTFIELD:

                            // Get the DataEditor
                            currentComponent = (Component) ((AuthoringComponent) item
                                    .getComponent()).getComponent();
                            currentComponent.setPreferredSize(new Dimension(
                                    ((DataEditor) item.getComponent()).getValue()
                                    .length(), 12));

                            if (i == 0) {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(ContainerEF.left(propertyPanel).add(HGAP));
                                ec.setY(ContainerEF.top(propertyPanel).add(VGAP));
                            } else {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(MathEF.add(ContainerEF.left(propertyPanel),
                                        (HGAP * (item.getCol() + 1))));
                                ec.setY(MathEF.add(ComponentEF
                                        .bottom(previousComponent), VGAP));
                            }

                            // Add to the panel
                            propertyPanel.add(currentComponent, ec);

                            break;

                        case VECTORFIELD:

                            // Get the DataEditor
                            currentComponent = (Component) ((AuthoringComponent) item
                                    .getComponent()).getComponent();

                            if (i == 0) {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(ContainerEF.left(propertyPanel).add(HGAP));
                                ec.setY(ContainerEF.top(propertyPanel).add(VGAP));
                            } else {
                                ec = new ExplicitConstraints(currentComponent);
                                ec.setX(MathEF.add(ContainerEF.left(propertyPanel),
                                        (HGAP * (item.getCol() + 1))));
                                ec.setY(MathEF.add(ComponentEF
                                        .bottom(previousComponent), VGAP));
                            }

                            // Add to the panel
                            propertyPanel.add(currentComponent, ec);

                            break;

                    }

                    if (item.getComponent() instanceof NullEditor) {
                        System.out.println("NULL EDITOR");
                    } else {
                        // remember the last component added
                        previousComponent = currentComponent;
                    }
                }

                propertyPanels.put(sheetName, propertyPanel);

            } else {
                //System.out.println("   don't display");
            }
        }
    }

    /**
     * Process an element from the property panel description.
     *
     * @param e The element
     * @param col The column
     * @param sheetName
     * @param propertyNameRoot
     */
    private boolean addElement(Element e, int col, String sheetName,
            String propertyNameRoot) {

        DataEditor[] dataEditors = (DataEditor[]) e.getUserData("DATA_EDITORS");
        DataValidator[] dataValidators = (DataValidator[]) e.getUserData("DATA_VALIDATORS");

//System.out.println("    sheetName: " + sheetName);


        String elementName = propertyNameRoot + "/" + e.getTagName();

        boolean hasEditor = false;
        boolean attHasEditor = false;
        boolean childHasEditor;

        String parentLabel = addLabel(sheetName, elementName, e.getTagName(), col, e);

//System.out.println("parentLabel: " + parentLabel);

        if (dataEditors != null) {
            for (int j = 0; j < dataEditors.length; j++) {
                if ((dataValidators != null) && (dataValidators[j] != null)) {
                    addDataEditor(sheetName, elementName, col + j, e,
                            dataEditors[j], dataValidators[j]);
                } else {
                    addDataEditor(sheetName, elementName, col + j, e,
                            dataEditors[j], null);
                }
            }
            hasEditor = true;
        }

//System.out.println("    self: " + hasEditor);

        NamedNodeMap atts = e.getAttributes();
        int nextCol = col + 1;
        Attr att;
        String labelName;

        for (int i = 0; i < atts.getLength(); i++) {

            att = (Attr) atts.item(i);

            dataEditors = (DataEditor[]) att.getUserData("DATA_EDITORS");
            dataValidators = (DataValidator[]) att.getUserData("DATA_VALIDATORS");

            labelName = addLabel(sheetName, elementName + "/@" + att.getName(), att
                    .getName(), nextCol, e);

//System.out.println("labelName: " + labelName);

            if (dataEditors != null) {

                for (int j = 0; j < dataEditors.length; j++) {

                    if (dataEditors[j] instanceof NullEditor) {
                        editorList.remove(labelName);
                        editorComponents.remove(labelName);
                    } else {

                        if ((dataValidators != null) && (dataValidators[j] != null)) {
                            addDataEditor(sheetName, elementName + "/@" + att.getName(),
                                    nextCol + 2 + j, e, dataEditors[j], dataValidators[j]);
                        } else {
                            addDataEditor(sheetName, elementName + "/@" + att.getName(),
                                    nextCol + 2 + j, e, dataEditors[j], null);
                        }

                        attHasEditor = true;
                    }

                }

            } else {

                try {

                    DataEditor editor = (DataEditor) defaultEditor.newInstance();
                    editor.setValue(att.getValue());

                    // if a null editor then remove the label
                    if (editor instanceof NullEditor) {
                        editorList.remove(labelName);
                        editorComponents.remove(labelName);
                    } else {
                        addDataEditor(sheetName, elementName + "/@" + att.getName(),
                                nextCol + 2, e, editor, null);
                        attHasEditor = true;
                    }

                } catch (IllegalAccessException | InstantiationException ex) {
                    System.err.println("Failed to find editor!");
                    ex.printStackTrace();
                }
            }

            numRows++;
        }

        // if any attributes have an editor then keep label
        if (attHasEditor) {
            hasEditor = true;
        }

//System.out.println("    attributes: " + attHasEditor);

        NodeList nodes = e.getChildNodes();
        Node n;

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n instanceof Element) {

                childHasEditor = addElement((Element) n, nextCol, sheetName, elementName);

//System.out.println("    children: " + childHasEditor);

                if (childHasEditor) {
                    hasEditor = true;
                }

            } else if (n instanceof Text) {

                dataEditors = (DataEditor[]) n.getUserData("DATA_EDITORS");
                dataValidators = (DataValidator[]) n.getUserData("DATA_VALIDATORS");

                if (dataEditors != null) {

                    for (int j = 0; j < dataEditors.length; j++) {

                        if (dataEditors[j] instanceof NullEditor) {
                        } else {

                            if ((dataValidators != null) && (dataValidators[j] != null)) {
                                addDataEditor(sheetName, elementName + "/" + n.getNodeName() + "/text()",
                                        nextCol + j, n, dataEditors[j], dataValidators[j]);
                            } else {
                                addDataEditor(sheetName, elementName + "/" + n.getNodeName() + "/text()",
                                        nextCol + j, n, dataEditors[j], null);
                            }

                            hasEditor = true;

                        }
                    }


                } else {

                    try {

                        String value = ((CharacterData) n).getData().trim();

                        DataEditor editor = (DataEditor) defaultEditor.newInstance();
                        editor.setValue(value);

                        if (!(editor instanceof NullEditor)) {
                            if (!value.equals("")) {
                                addDataEditor(sheetName,elementName + "/" + n.getNodeName()
                                    + "/text()", nextCol, n, editor, null);
                                hasEditor = true;
                            }
                        }

                    } catch (IllegalAccessException | InstantiationException | DOMException ex) {
                        System.err.println("Failed to find editor!");
                        ex.printStackTrace();
                    }
                }
            }
        }

        if (!hasEditor) {
            editorList.remove(parentLabel);
            editorComponents.remove(parentLabel);
        }

        return hasEditor;

    }

    /**
     * Process a label node from the property panel description.
     *
     * @param sheet
     * @param name
     * @param value
     * @param col
     * @param node
     *
     */
    private String addLabel(String sheet, String name, String value, int col,
            Node node) {

        String itemName = null;

        if (value.trim().length() > 0) {

            JLabel label = new JLabel(value);
            label.setName(name);
            label.setBorder(new EmptyBorder(0, 1, 0, 1));

            itemName = addPanelItem(PanelItem.DataEditorType.LABEL, "LABEL:" + name, col, node,
                    label);
        }

        return itemName;

    }

    /**
     * Process a text node from the property panel description.
     *
     * @param sheet
     * @param name
     * @param value
     * @param col
     * @param node
     *
     */
    private void addTextField(String sheet, String name, String value, int col,
            Node node, DataValidator validator) {

        if (value.trim().length() > 0) {

            TextFieldEditor text = new TextFieldEditor(value, null,
                    DataEditor.Types.ENTITY_PROPERTY);

            text.setErrorReporter(errorReporter);
            text.setField(node);
            text.setPropertyName(name);
            text.setEntityID(currentEntityID);
            text.setModel(model);
            text.setPropertySheet(sheet);
            text.setValidator(validator);

            addPanelItem(PanelItem.DataEditorType.DATAEDITOR, name, col, node, text);
        }
    }

    /**
     * Add en editing component
     *
     * @param sheet
     * @param name
     * @param col
     * @param node
     * @param editor
     * @param label
     */
    private void addDataEditor(String sheet, String name, int col, Node node,
            DataEditor component, DataValidator validator) {

        component.setErrorReporter(errorReporter);
        component.setField(node);
        component.setPropertyName(name);
        component.setEntityID(currentEntityID);
        component.setModel(model);
        component.setPropertySheet(sheet);
        component.setValidator(validator);

/*
System.out.println("PropertyData.addDataEditor()");
System.out.println("    name: " + component.getPropertyName());
System.out.println("    value: " + component.getValue());
*/

        if (component instanceof VectorFieldEditor) {
            addPanelItem(PanelItem.DataEditorType.VECTORFIELD, name, col, node, component);
        } else {
            addPanelItem(PanelItem.DataEditorType.DATAEDITOR, name, col, node, component);
        }
    }

    /**
     * Add an item to the panel list.
     *
     * @param type What sort of AWT/SWING element is this. Defined by TYPE_ in
     *        this class.
     * @param col What column to place the item.
     * @param node The node backing this item
     * @param value The initial value.
     * @param name The name of the editing component.
     */
    private String addPanelItem(PanelItem.DataEditorType type, String name, int col, Node node,
            Object component) {

        String newName = name;
        int index = 1;

        // increament columns as needed
        if (col + 1 > maxCol) {
            maxCol = col + 1;
        }

        // add the item to the lists for easy lookup and display
        if (editorList.contains(name)) {
            while (editorList.contains(newName)) {
                newName = name + "__" + index;

                index++;
            }

            if (type != PanelItem.DataEditorType.LABEL) {
                ((DataEditor)component).setPropertyName(newName);
                //System.out.println("Duplicate editor name found, using: " + newName);
            }


        }

        // create the panelitem to store data
        PanelItem item = new PanelItem(type, col, node, component);

        editorList.add(newName);
        editorComponents.put(newName, item);

        return newName;

    }

    /**
     * Parse the size XPaths. Find keywords to help limit when size recalcs are
     * done.
     *
     * @param sizeX The XPath expression for x size
     * @param sizeY The XPath expression for y size
     * @param sizeZ The XPath expression for z size
     * @return An HashSet of possible field changes that will trigger a recalc.
     *         Null means all. Zero length means all.
     */
    public HashSet parseSizeFields(String sizeX, String sizeY, String sizeZ) {
        return null;
    }

    /**
     * Print out a panel for debuging
     */
    private void printPanel(java.util.List<String> editorList) {

        PanelItem item;
        PanelItem.DataEditorType type;
        int col;
        String value;
        String[] propNames = new String[maxCol];
        Node n;

        System.out.println("Table items: " + editorList.size());

        for (int i = 0; i < editorList.size(); i++) {
            item = editorComponents.get(editorList.get(i));

            col = item.getCol();
            type = item.getType();
            n = item.getNode();

            System.out.print("Row: " + i + " ");
            switch (type) {
            case DATAEDITOR:
                DataEditor editor = (DataEditor) item.getComponent();
                System.out.println("    DATAEDITOR: " + editor);
                break;
            case LABEL:
                JLabel label1 = (JLabel) item.getComponent();
                value = label1.getText();
                System.out.print("  LABEL: " + value);

                if (i + 1 < editorList.size()) {
                    try {
                        item = editorComponents.get(editorList
                                .get(i + 1));
                    } catch (Exception e) {
                        System.out.println("PanelItem not found: " + i + " node: "
                                + n.hashCode());
                        continue;
                    }
                    type = item.getType();
                    if (type == PanelItem.DataEditorType.TEXTFIELD) {
                        JTextField text = (JTextField) item.getComponent();
                        value = text.getText();
                        System.out.println("    TEXTFIELD: " + value);
                        i++;
                    } else {
                        System.out.println(" no value?");
                    }
                }
                break;
            case TEXTFIELD:
                JTextField text1 = (JTextField) item.getComponent();
                value = text1.getText();
                System.out.println("    TEXTFIELD: " + value);
                break;
            }
        }

        System.out.flush();
    }

}
