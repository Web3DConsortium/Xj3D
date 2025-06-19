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

package org.chefx3d.model;

// External Imports

// Internal Imports
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.XPathEvaluator;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A single vertex.
 *
 * @author Russell Dodds
 * @version $Revision: 1.6 $
 */
public class SegmentVertex {

    /** A vertexId */
    private int vertexID;

    /** A position in World Coordinates */
    private double[] position;

    /** A rotation in World Coordinates */
    private float[] rotation;

    /** The properties of the vertex, sheet = properties */
    private Map<String, Document> properties;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Create a new vertex using user properties
     *
     * @param vertexID The vertexID
     * @param position The world coordinate system loction
     * @param rotation The rotation of the pole, not used currently
     * @param props The property sheet map (sheet -> document)
     */
    public SegmentVertex(int vertexID, double[] position,
            float[] rotation, Map<String, Document> props) {

        this.vertexID= vertexID;

        this.position = new double[3];
        this.position[0] = position[0];
        this.position[1] = position[1];
        this.position[2] = position[2];

        this.rotation = new float[4];
        this.rotation[0] = rotation[0];
        this.rotation[1] = rotation[1];
        this.rotation[2] = rotation[2];
        this.rotation[3] = rotation[3];

        this.properties = new HashMap<>(props);

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }

    /**
     * Get the vertexID
     *
     * @return
     */
    public int getVertexID() {
        return vertexID;
    }

    /**
     * Set the vertexID
     *
     * @param vertexID
     */
    public void setVertexID(int vertexID) {
        this.vertexID = vertexID;
    }

    /**
     * Get the world coordinate position
     *
     * @return position
     */
    public double[] getPosition() {
        return position;
    }

    /**
     * Set the world coordinate position
     *
     * @param position
     */
    public void setPosition(double[] position) {
        this.position = position;
    }

    /**
     * Get the rotation of the vertex
     *
     * @return rotation
     */
    public float[] getRotation() {
        return rotation;
    }

    /**
     * Set the rotation of the vertex
     *
     * @param rotation
     */
    public void setRotation(float[] rotation) {
        this.rotation[0] = rotation[0];
        this.rotation[1] = rotation[1];
        this.rotation[2] = rotation[2];
        this.rotation[3] = rotation[3];
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
     * Set a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @param propValue property value
     */
    protected void setProperties(String propSheet, String propName,
            String propValue) {

        Document doc = properties.get(propSheet);
        Node node;

        try {

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
            if (node instanceof Text) {
                ((CharacterData) node).setData(propValue);
            } else if (node instanceof Attr) {
                ((Attr) node).setValue(propValue);
            }

        } catch (NumberFormatException | XPathExpressionException | DOMException ex) {
            errorReporter.errorReport("Could not find property to set.", ex);
        }

    }

    /**
     * Get the properties for a sheet.
     *
     * @param sheetName The sheet name
     * @return The properties
     */
    public Document getProperties(String sheetName) {
        return properties.get(sheetName);
    }

    /**
     * Set the properties of a sheet.
     *
     * @param propSheet The sheet name
     * @param node The properties
     */
    protected void setProperties(String propSheet, Document node) {
        properties.put(propSheet, node);
    }

    /**
     * Get the properties for all sheets.
     *
     * @return The property map
     */
    public Map<String, Document> getProperties() {
        return properties;
    }

    /**
     * Set all the properties sheets.
     *
     * @param props
     */
    protected void setProperties(Map<String, Document> props) {
        properties = new HashMap<>(props);
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
