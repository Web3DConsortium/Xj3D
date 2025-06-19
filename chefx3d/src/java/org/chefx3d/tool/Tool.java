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

// External Imports
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

// Local imports
import org.chefx3d.PropertyPanelDescriptor;
import org.chefx3d.util.XPathEvaluator;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * Describes a tool.
 *
 * @author Alan Hudson
 * @version $Revision: 1.29 $
 */
public class Tool
    implements ToolGroupChild, Comparable<Tool> {

    public static final int TYPE_MODEL = 0;

    public static final int TYPE_WORLD = 1;

    public static final int TYPE_MULTI_SEGMENT = 2;

    //public static final int TYPE_DELETE_SELECTED_ENTITY = 4;

    // MultiSegment Params. -1 for unrestricted. > 0 for fixed length
    public static int PARAM_SEGMENT_LENGTH = 0;

    /** A map of the constants to ints */
    private final static Map<String, Integer> constantsMap;

    /** A map of the ints to constants */
    private final static Map<Integer, String> stringMap;

    private String name;

    private String[] url;

    private String topDownIcon;

    private String[] interfaceIcons;

    private int classificationLevel;

    private String description;

    private int toolType;

    /** Designate the tool to be a fixed size */
    private boolean isFixedSize;

    /** Designate the tool to be a helper */
    private boolean isHelper;

    /** Designate the tool to be a controller */
    private boolean isController;

    /** How is the multiplicity constrained */
    private MultiplicityConstraint multiplictyConstraint;

    /** What tool category is this. */
    private String category;

    /** Default values for a sheet, keyed by the sheet name */
    /** All property sheet names for entity, segment, vertex must be unique */
    private Map<String, Document> defaults;

    /** Default values for a vertex, keyed by the sheet name */
    private Map<String, Document> vertexDefaults;

    /** Default values for a segment, keyed by the sheet name */
    private Map<String, Document> segmentDefaults;

    /** User modified properties input from a PropertyPanel */
    private Map<String, Document> userProperties;

    private Map<String, String> stylesheets;

    /** The sheet to use for size expressions */
    private String sizeExprSheet;

    // XPath expression for x size
    private String xSizeExpr;

    // XPath expression for y size
    private String ySizeExpr;

    // XPath expression for z size
    private String zSizeExpr;

    /** Tool specific parameters */
    private String[] toolParams;

    /** Is the aspect ratio of the icon fixed */
    private boolean iconFixedAspect;

    /** The parent of this tool */
    private ToolGroupChild toolParent;

    static {
        constantsMap = new HashMap<>();
        constantsMap.put("TYPE_MODEL", TYPE_MODEL);
        constantsMap.put("TYPE_WORLD", TYPE_WORLD);
        constantsMap.put("TYPE_MULTI_SEGMENT_CREATE", TYPE_MULTI_SEGMENT);
        //constantsMap.put("TYPE_DELETE_SELECTED_ENTITY", new Integer(
        //        TYPE_DELETE_SELECTED_ENTITY));
        stringMap = new HashMap<>();
        stringMap.put(TYPE_MODEL, "TYPE_MODEL");
        stringMap.put(TYPE_WORLD, "TYPE_WORLD");
        stringMap.put(TYPE_MULTI_SEGMENT,
                "TYPE_MULTI_SEGMENT_CREATE");
        //stringMap.put(new Integer(TYPE_DELETE_SELECTED_ENTITY),
        //        "TYPE_DELETE_SELECTED_ENTITY");
    }

    public Tool(String name, String topDownIcon, String[] interfaceIcons, boolean fixed,
            int toolType, String[] url, int classificationLevel,
            String description, PropertyPanelDescriptor[] entityPanels,
            PropertyPanelDescriptor[] segmentPanels, PropertyPanelDescriptor[] vertexPanels,
            Map<String, String> stylesheets,
            String sizeSheet, String xExpr, String yExpr, String zExpr,
            String[] toolParams, MultiplicityConstraint constraint, String category
            ) {

            this(name, topDownIcon, interfaceIcons, fixed,
               toolType, url, classificationLevel, description, entityPanels,
               segmentPanels, vertexPanels,
               stylesheets, sizeSheet, xExpr, yExpr, zExpr,
               toolParams, constraint, category,
               false, false, false);
    }

    public Tool(String name, String topDownIcon, String[] interfaceIcons, boolean fixed,
            int toolType, String[] url, int classificationLevel,
            String description, PropertyPanelDescriptor[] entityPanels,
            PropertyPanelDescriptor[] segmentPanels, PropertyPanelDescriptor[] vertexPanels,
            Map<String, String> stylesheets,
            String sizeSheet, String xExpr, String yExpr, String zExpr,
            String[] toolParams, MultiplicityConstraint constraint, String category,
            boolean isFixedSize, boolean isHelper, boolean isController) {

        this.name = name;
        this.topDownIcon = topDownIcon;
        iconFixedAspect = fixed;

        if (interfaceIcons != null) {
            //System.out.println("interfaceIcons.length: " + interfaceIcons.length);

            this.interfaceIcons = new String[interfaceIcons.length];
            System.arraycopy(interfaceIcons, 0, this.interfaceIcons, 0, interfaceIcons.length);
        }

        if (url != null)  {

            this.url = new String[url.length];
            System.arraycopy(url, 0, this.url, 0, url.length);

        }

        this.classificationLevel = classificationLevel;
        this.description = description;
        this.toolType = toolType;

        defaults = new HashMap<>();
        vertexDefaults = new HashMap<>();
        segmentDefaults = new HashMap<>();
        userProperties = new HashMap<>();

        if (entityPanels != null) {
            for (PropertyPanelDescriptor entityPanel : entityPanels) {
                defaults.put(entityPanel.getName(), entityPanel.getDefaults());
            }
        }

        if (segmentPanels != null) {
            for (PropertyPanelDescriptor segmentPanel : segmentPanels) {
                segmentDefaults.put(segmentPanel.getName(), segmentPanel.getDefaults());
            }
        }

        if (vertexPanels != null) {
            for (PropertyPanelDescriptor vertexPanel : vertexPanels) {
                vertexDefaults.put(vertexPanel.getName(), vertexPanel.getDefaults());
            }
        }

        this.stylesheets = stylesheets;

        if (xExpr == null || xExpr.length() < 1) {
            System.err.println("Invalid xExpression for: " + name);
        }

        sizeExprSheet = sizeSheet;

        Document doc = defaults.get(sizeExprSheet);

        if (doc == null) {
            System.err.println("Error: sizeExprSheet does not exist");
        }

        this.xSizeExpr = xExpr;
        this.ySizeExpr = yExpr;
        this.zSizeExpr = zExpr;
        this.toolParams = toolParams;
        this.category = category;
        this.multiplictyConstraint = constraint;

        if (multiplictyConstraint == null) {
            multiplictyConstraint = MultiplicityConstraint.NO_REQUIREMENT;
        }

        this.isFixedSize = isFixedSize;
        this.isHelper = isHelper;
        this.isController = isController;

    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Calculate the hashcode for this object.
     *
     */
    @Override
    public int hashCode() {
        // TODO: Not a very good hash
        return name.hashCode();
    }

    /**
     * override of Objects equals test
     * @param o
     */
    @Override
    public boolean equals(Object o) {

        if (o instanceof Tool) {
            Tool check = (Tool)o;
            if (name.equals(check.name)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------
    // Methods defined by Comparable<Tool>
    //----------------------------------------------------------

    /**
     * Return compare based on string ordering
     * @param t
     */
    @Override
    public int compareTo(Tool t) {

        Tool check = t;
        return name.compareTo(check.name);

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
     * Convert a string toolType into an int.
     *
     * @param val The string value
     * @return The class int or -1 if not found
     */
    public static int typeLookup(String val) {
        Integer ival = constantsMap.get(val);
        if (ival != null)
            return ival;
        else
            return -1;
    }

    /**
     * Map a type to a type string. Null if not known.
     *
     * @param type The type int
     * @return The type string
     */
    public static String mapType(int type) {
        Integer ival = type;

        return stringMap.get(ival);
    }

    /**
     * Get the tool type. Defined in this class as TYPE_*
     *
     * @return The type
     */
    public int getToolType() {
        return toolType;
    }

    /**
     * Get the tool type as string.
     *
     * @return The tooltype string constant
     */
    public String getToolTypeString() {
        return stringMap.get(toolType);
    }

    /**
     * Get the URL's to use for this tool.
     *
     * @return The list of urls
     */
    public String[] getURL() {
        return url;
    }

    /**
     * Get the top down icon for this tool
     *
     * @return The icon
     */
    public String getIcon() {
        return topDownIcon;
    }

    /**
     * Get the top down icon for this tool
     *
     * @param icon
     */
    public void setIcon(String icon) {
        topDownIcon = icon;
    }

    /**
     * Is the aspect ratio of the icon fixed.
     *
     * @return TRUE if its fixed.
     */
    public boolean isFixedAspect() {
        return iconFixedAspect;
    }

    /**
     * Get the isHelper flag
     *
     * @return
     */
    public boolean isHelper() {
        return isHelper;
    }

    /**
     * Get the isController flag
     *
     * @return
     */
    public boolean isController() {
        return isController;
    }

    /**
     * Get the isFixedSize flag
     *
     * @return
     */
    public boolean isFixedSize() {
        return isFixedSize;
    }

    /**
     * Set the isScaleable flag
     *
     * @param bool
     */
    public void setFixedSize(boolean bool) {
        isFixedSize = bool;
    }

    /**
     * Get the interfaceIcon. If no icon is found at the index provided
     * then return the topDownIcon by default
     *
     * @param index
     * @return The icon
     */
    public String getInterfaceIcon(int index) {

        if (interfaceIcons == null) {
            return topDownIcon;
        } else if (interfaceIcons[index] == null) {
            return topDownIcon;
        } else {
            return interfaceIcons[index];
        }
    }

    /**
     * Get the interfaceIcons.
     *
     * @return The icons
     */
    public String[] getInterfaceIcons() {
        return interfaceIcons;
    }

    /**
     * Get the classification level of this tool.
     *
     * @return The classification level
     */
    public int getClassificationLevel() {
        return classificationLevel;
    }

    /**
     * Get the string describing the tool.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the defaults for all sheets.
     *
     * @return The defaults
     */
    public Map<String, Document> getDefaults() {
        return defaults;
    }

    /**
     * Get the defaults for all vertex sheets.
     *
     * @return The vertexDefaults
     */
    public Map<String, Document> getVertexDefaults() {
        return vertexDefaults;
    }

    /**
     * Get the defaults for all segment sheets.
     *
     * @return The segmentDefaults
     */
    public Map<String, Document> getSegmentDefaults() {
        return segmentDefaults;
    }

    public String[] getToolParams() {
        return toolParams;
    }

    public Map<String, String> getStylesheets() {
        return stylesheets;
    }

    /**
     * Get the default x size of this tool.
     *
     * @return The x size
     */
    public double getXSize() {
        Document doc = defaults.get(sizeExprSheet);

        if (doc == null) {
            System.err.println("No defaults for: " + sizeExprSheet);
        }

        if (xSizeExpr.startsWith("/ChefX3D/EntityParams")) {
            return XPathEvaluator.getValue(xSizeExpr, doc);
        } else {
            return Double.valueOf(xSizeExpr);
        }
    }

    /**
     * Get the default y size of this tool.
     *
     * @return The y size
     */
    public double getYSize() {
        Document doc = defaults.get(sizeExprSheet);

        if (doc == null) {
            System.err.println("No defaults for: " + sizeExprSheet);
        }

        if (ySizeExpr.startsWith("/ChefX3D/EntityParams")) {
            return XPathEvaluator.getValue(ySizeExpr, doc);
        } else {
            return Double.valueOf(xSizeExpr);
        }

    }

    /**
     * Get the default z size of this tool.
     *
     * @return The z size
     */
    public double getZSize() {
        Document doc = defaults.get(sizeExprSheet);

        if (doc == null) {
            System.err.println("No defaults for: " + sizeExprSheet);
        }

        if (zSizeExpr.startsWith("/ChefX3D/EntityParams")) {
            return XPathEvaluator.getValue(zSizeExpr, doc);
        } else {
            return Double.valueOf(zSizeExpr);
        }

    }

    /**
     * Get the property sheet name for size expressions.
     *
     * @return The property sheet name
     */
    public String getSizeSheetName() {
        return sizeExprSheet;
    }

    /**
     * Get the XPATH expression for x size.
     *
     * @return The expression
     */
    public String getXSizeExpression() {
        return xSizeExpr;
    }

    /**
     * Get the XPATH expression for y size.
     *
     * @return The expression
     */
    public String getYSizeExpression() {
        return ySizeExpr;
    }

    /**
     * Get the XPATH expression for z size.
     *
     * @return The expression
     */
    public String getZSizeExpression() {
        return zSizeExpr;
    }

    /**
     * Get the multiplicity constraint.
     *
     * @return The constraint
     */
     public MultiplicityConstraint getMultiplicityConstraint() {
        return multiplictyConstraint;
     }

    /**
     * Get the category of this tool.  Used for constraint checking.
     *
     * @return The category
     */
     public String getCategory() {
        return category;
     }

     /**
      * Set a specific property. NOT USED
      *
      * @param propSheet The sheet name
      * @param propName The name of the property to set
      * @param propValue The value to set
      */
     public void setProperties(String propSheet, String propName,
         String propValue) {

         Document doc = defaults.get(propSheet);
         Node node;

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

         } catch (NumberFormatException | XPathExpressionException | DOMException ex) {
             System.err.println("Could not find property to set.\n" + ex);
         }

     }

    /**
     * Get a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @return propValue
     */
    public String getProperties(String propSheet, String propName) {
         return XPathEvaluator.getString(propName, getProperties(propSheet));
    }

    /**
     * Get the properties for a sheet.
     *
     * @param sheetName The sheet name
     * @return The properties
     */
    public Document getProperties(String sheetName) {
        return defaults.get(sheetName);
    }

    /**
     * A toString method to pretty print.
     */
    @Override
    public String toString() {
        return "Tool: hc: " + hashCode() + " name: " + name
                + " numStyleSheets: " + stylesheets.size();
    }

    /**
     * Set the tool parent to be this new object. Null clears the reference.
     * Package private because only ToolGroup should be calling this.
     */
    void setParent(ToolGroupChild parent) {
        toolParent = parent;
    }

    /**
     * Make sure we return user modified properties input from a PropertyPanel
     * @return user modified properties input from a PropertyPanel
     */
    public Map<String, Document> getUserProperties() {
        if (!userProperties.isEmpty())
            return userProperties;
        else
            return getDefaults();
    }

    /**
     * @param userProperties the userProperties to set
     */
    public void setUserProperties(Map<String, Document> userProperties) {
        this.userProperties = userProperties;
    }

}
