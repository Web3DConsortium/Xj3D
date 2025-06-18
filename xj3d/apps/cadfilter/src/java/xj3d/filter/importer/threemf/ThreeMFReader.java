/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

package xj3d.filter.importer.threemf;

import org.web3d.util.SimpleStack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * Read a 3mf file from an XML source
 *
 * @author Alan Hudson
 */
public class ThreeMFReader extends XMLReaderAdapter {
    private static final boolean DEBUG = false;

    /** Stack of Elements being processed */
    private SimpleStack stack;

    /** Factory for the data binding classes */
    private ThreeMFElementFactory cef;

    /** The root element of the document */
    private ThreeMFElement root;

    /**
     * Constructor
     * @throws org.xml.sax.SAXException
     */
    public ThreeMFReader() throws SAXException {
    }

    /**
     * Return the results of the parse
     *
     * @return The results of the parse
     */
    public Object getResult() {
        return(root);
    }

    /**
     * @see org.xml.sax.helpers.XMLReaderAdapter#startDocument
     */
    @Override
    public void startDocument() {
        stack = new SimpleStack();
        cef = new ThreeMFElementFactory();
    }

    /**
     * @see org.xml.sax.helpers.XMLReaderAdapter#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) {
        if (DEBUG) System.out.printf("SE: ln:%s qn:%s\n",localName,qName);
        ThreeMFElement parent = null;
        if (!stack.isEmpty()) 
            parent = (ThreeMFElement) stack.peek();

        // Handle special cases to avoid garbage on
        switch (qName) {
            case "vertex":
                if (parent.getElementID() == ThreeMFElementFactory.VERTICES) {
                    ((Vertices) parent).addVertex(atts);
                } else throw new IllegalArgumentException("Invalid location for vertex");
                break;
            case "triangle":
                if (parent.getElementID() == ThreeMFElementFactory.TRIANGLES) {
                    ((Triangles)parent).addTriangle(atts);
                } else throw new IllegalArgumentException("Invalid location for triangle");
                break;
            case "base":
                if (parent.getElementID() == ThreeMFElementFactory.BASE_MATERIALS) {
                    ((BaseMaterials)parent).addColor(atts);
                } else throw new IllegalArgumentException("Invalid location for triangle");
                break;
            case "m:color":
                if (parent.getElementID() == ThreeMFElementFactory.COLOR_GROUP) {
                    ((ColorGroup)parent).addColor(atts);
                } else throw new IllegalArgumentException("Invalid location for color");
                break;
            case "m:tex2coord":
                if (parent.getElementID() == ThreeMFElementFactory.TEXTURE_2D_GROUP) {
                    ((Texture2dGroup)parent).addCoord(atts);
                } else throw new IllegalArgumentException("Invalid location tex2coord");
                break;
            default:
                ThreeMFElement te = cef.getElement(parent, qName, atts);
                if (parent != null && te != parent && te != null) {
                    parent.addElement(te);
                }   if (root == null) root = te;
                stack.push(te);
                break;
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLReaderAdapter#characters
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        /*
        if (isAppendable) {
            int current_length = text.length();
            if ((current_length + length) > LOW_WATER) {
                int remainder = 0;
                for (int i = current_length - 1; i >= 0; i--) {
                    if (Character.isWhitespace(text.charAt(i))) {
                        break;
                    } else {
                        remainder++;
                    }
                }
                int transfer_length = current_length - remainder;
                Appendable ce = (Appendable)stack.peek();
                ce.appendTextContent(text.substring(0, transfer_length));
                text.delete(0, transfer_length);
            }
            text.append(ch, start, length);
        } else {
            text.append(ch, start, length);
        }
        */
    }

    /**
     * @see org.xml.sax.helpers.XMLReaderAdapter#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "vertex":
                break;
            case "triangle":
                break;
            case "base":
                break;
            case "m:color":
                break;
            case "m:tex2coord":
                break;
            default:
                ThreeMFElement ce = (ThreeMFElement) stack.pop();
                break;
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLReaderAdapter#endDocument
     */
    @Override
    public void endDocument() {
        stack = null;
        cef = null;
    }

}
