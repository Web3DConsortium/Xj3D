//
//   XmlUtil.java
//
//------------------------------------------------------------------------
//
//      Portions Copyright (c) 2000 SURVICE Engineering Company.
//      All Rights Reserved.
//      This file contains Original Code and/or Modifications of Original
//      Code as defined in and that are subject to the SURVICE Public
//      Source License (Version 1.3, dated March 12, 2002)
//
//      A copy of this license can be found in the doc directory
//------------------------------------------------------------------------
//
//      Developed by SURVICE Engineering Co. (www.survice.com)
//      April 2002
//
//      Authors:
//              Bob Parker
//------------------------------------------------------------------------
import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.xml.sax.SAXException;

import org.w3c.dom.*;

public class XmlUtil {
    public static final String XSD_ELEMENT = "xsd:element";
    public static final String XSD_COMPLEX_TYPE = "xsd:complexType";
    public static final String XSD_COMPLEX_CONTENT = "xsd:complexContent";
    public static final String XSD_EXTENSION = "xsd:extension";
    public static final String XSD_ATTRIBUTE = "xsd:attribute";
    public static final String XSD_ATTRIBUTE_GROUP = "xsd:attributeGroup";

    public static final String Header1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    public static String Header2 = "<!DOCTYPE X3D PUBLIC \"http://www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd\"\n                     \"file:///C:/www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd\">\n";

    public static DocumentBuilder getDocumentBuilder() {
        try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    //factory.setValidating(true);
	    //factory.setNamespaceAware(true);

	    // May need next line for compatibility with future
	    // versions of Xerces
	    //factory.setAttribute("http://apache.org/xml/features/validation/schema", true);

	    return factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace(System.err);
        }

	return null;
    }

    public static Document getDocument(File file) {
	DocumentBuilder builder = getDocumentBuilder();

	if (builder != null)
	    return getDocument(builder, file);

	return null;
    }

    public static Document getDocument(DocumentBuilder builder, File file) {
        try {
	    return builder.parse(file);
        } catch (SAXException sxe) {
	    Exception  x = sxe;
	    if (sxe.getException() != null)
		x = sxe.getException();
	    x.printStackTrace(System.err);
        } catch (IOException ioe) {
	    ioe.printStackTrace(System.err);
        }

	return null;
    }

    public static void export(Document document,
			      File file,
			      String styleSheet) {
	try {
	    TransformerFactory tFactory = TransformerFactory.newInstance();
	    Transformer transformer;
	    StreamSource styleSource;

	    //XXX I need to figure out how to compel the transformer
	    //    to include only references to included files and not
	    //    the contents thereof.

	    // Use a Transformer for output

	    if (styleSheet == null)
		transformer = tFactory.newTransformer();
	    else {
		styleSource = new StreamSource(new File(styleSheet));
		transformer = tFactory.newTransformer(styleSource);
	    }

	    DOMSource source = new DOMSource(document);
	    StreamResult result = new StreamResult(file);
	    //StreamResult result = new StreamResult(System.out);

	    String systemValue = (new File(document.getDoctype().getSystemId())).getName();
	    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
	    System.out.println("export: systemValue - " + systemValue);

	    transformer.transform(source, result);
	} catch (TransformerConfigurationException tce) {
	    // Error generated by the parser
	    System.out.println ("\n** Transformer Factory error");
	    System.out.println("   " + tce.getMessage() );

	    // Use the contained exception, if any
	    Throwable x = tce;
	    if (tce.getException() != null)
		x = tce.getException();
	    x.printStackTrace(System.err);

        } catch (TransformerException te) {
	    // Error generated by the parser
	    System.out.println ("\n** Transformation error");
	    System.out.println("   " + te.getMessage() );

	    // Use the contained exception, if any
	    Throwable x = te;
	    if (te.getException() != null)
		x = te.getException();
	    x.printStackTrace(System.err);
        }
    }

    public static void transformWriter(Document doc,
				       File file) {
	XmlUtil.export(doc, file, null);
    }

    public static void documentWriter(Document doc,
				      FileWriter fileWriter) {
	XmlUtil.documentWriter(doc, fileWriter, "", "\t");
    }

    public static void documentWriter(Document doc,
				      FileWriter fileWriter,
				      String currIndent) {
	XmlUtil.documentWriter(doc, fileWriter, currIndent, "\t");
    }

    // It's up to the application to perform the close on the fileWriter
    public static void documentWriter(Document doc,
				      FileWriter fileWriter,
				      String currIndent,
				      String addIndent) {
	try {
	    // The following two lines are bad. The version of
	    // Xerces installed doesn't have getEncoding and getVersion.
	    //
	    //System.out.println("Document Encoding - " + doc.getEncoding());
	    //System.out.println("Document Version - " + doc.getVersion());
	    System.out.println("Document Type - " + doc.getDoctype());

	    fileWriter.write(XmlUtil.Header1, 0, XmlUtil.Header1.length());
	    fileWriter.write(XmlUtil.Header2, 0, XmlUtil.Header2.length());
	} catch (IOException e) {
	    System.out.println("documentWriter: " + e.toString());
	}

	// The skipComments parameter is initially true in order to skip
	// the many comments coming from a DTD.
	XmlUtil.nodeWriter(doc, fileWriter, currIndent, addIndent, true);
    }

    public static void nodeWriter(Node node,
				  FileWriter fileWriter,
				  String currIndent,
				  String addIndent,
				  boolean skipComments) {
	NodeList children;
	StringBuilder s = new StringBuilder();

	children = node.getChildNodes();
	for (int i = 0; i < children.getLength(); ++i) {
	    Node child = children.item(i);
	    short type = child.getNodeType();

	    switch (type) {
	    case Node.ELEMENT_NODE:
		NamedNodeMap attrs = child.getAttributes();
		int n = child.getChildNodes().getLength();

		// indent
		s.append(currIndent);

		// print element name
		s.append("<");
		s.append(child.getNodeName());
		if (0 < attrs.getLength())
		    s.append(" ");

		// print attributes, if any here
		int len = attrs.getLength();
		for (int j = 0; j < len; ++j) {
		    Node attr = attrs.item(j);
		    s.append(attr.getNodeName());
		    s.append("=\"");

		    // replace double quotes with &quot;
		    StringTokenizer st = new StringTokenizer(attr.getNodeValue(), "\"", true);
		    while (st.hasMoreTokens()){
			String token = st.nextToken();

			if (token.length() == 1 && token.equals("\"")) {
			    s.append("&quot;");
			} else
			    s.append(token);
		    }

		    s.append("\"");

		    // append a space if not last one
		    if (j + 1 < len)
			s.append(" ");
		}

		if (n == 0) {
		    s.append("/>\n");

		    try {
			fileWriter.write(s.toString(), 0, s.length());
		    } catch (IOException e) {
			System.out.println("nodeWriter: " + e.toString());
		    }
		} else {
		    s.append(">\n");
		    try {
			fileWriter.write(s.toString(), 0, s.length());
		    } catch (IOException e) {
			System.out.println("nodeWriter: " + e.toString());
		    }

		    // recurse on children
		    XmlUtil.nodeWriter(child,
				       fileWriter,
				       currIndent + addIndent,
				       addIndent,
				       false);

		    s.delete(0, s.length());
		    s.append(currIndent);
		    s.append("</");
		    s.append(child.getNodeName());
		    s.append(">\n");

		    try {
			fileWriter.write(s.toString(), 0, s.length());
		    } catch (IOException e) {
			System.out.println("nodeWriter: " + e.toString());
		    }
		}

		// reset string buffer
		s.delete(0, s.length());
		break;
	    case Node.COMMENT_NODE:
		if (!skipComments && child.getNodeValue() != null) {
		    // indent
		    s.append(currIndent);
		    s.append("<!--");
		    s.append(child.getNodeValue());
		    s.append("-->\n");

		    try {
			fileWriter.write(s.toString(), 0, s.length());
		    } catch (IOException e) {
			System.out.println("nodeWriter: " + e.toString());
		    }


		    // reset string buffer
		    s.delete(0, s.length());
		}
		break;
	    case Node.CDATA_SECTION_NODE:
		// indent
		s.append(currIndent);
		s.append("<![CDATA[");
		s.append(child.getNodeValue());
		s.append("]]>\n");

		try {
		    fileWriter.write(s.toString(), 0, s.length());
		} catch (IOException e) {
		    System.out.println("nodeWriter: " + e.toString());
		}


		// reset string buffer
		s.delete(0, s.length());
		break;
	    case Node.DOCUMENT_NODE:
		System.out.println("Document Node - " +
				   child.getNodeName() +
				   ", " +
				   child.getNodeValue());
		break;
	    case Node.DOCUMENT_TYPE_NODE:
		System.out.println("Document Node Type - " +
				   child.getNodeName() +
				   ", " +
				   child.getNodeValue());
		break;
	    case Node.ENTITY_REFERENCE_NODE:
		System.out.println("Entity Reference Node - " +
				   child.getNodeName() +
				   ", " +
				   child.getNodeValue());
		break;
	    case Node.PROCESSING_INSTRUCTION_NODE:
		System.out.println("Processing Instruction Node - " +
				   child.getNodeName() +
				   ", " +
				   child.getNodeValue());
		break;
	    case Node.TEXT_NODE:
	    default:
		break;
	    }
	}
    }

    // Find a child of the root node
    // whose attr value matches val (brute force)
    public static Node findNode(Document document, String attr, String val) {
	NodeList children;
	Node root = document.getDocumentElement();

	if (attr == null || val == null || root == null)
	    return null;

	children = root.getChildNodes();
	for (int i = 0; i < children.getLength(); ++i) {
	    Node child = children.item(i);

	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		NamedNodeMap nodeMap = child.getAttributes();
		Node attr_node;

		if (nodeMap == null)
		    continue;

		if ((attr_node = nodeMap.getNamedItem(attr)) == null)
		    continue;

		if (attr_node.getNodeValue().equals(val)) {
		    // return node whose attribute matches val
		    return child;
		}
	    }
	}

	return null;
    }

    // get all XSD attributes for this node
    public static Vector getXSDAttributes(Document document, Node parent) {
	NodeList children;
	Vector attributes = new Vector();

	if (parent == null) {
	    attributes.trimToSize();
	    return attributes;
	}

	children = parent.getChildNodes();
	for (int i = 0; i < children.getLength(); ++i) {
	    Node child = children.item(i);

	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		NamedNodeMap nodeMap;

		if (child.getNodeName().equals(XSD_ATTRIBUTE)) {
		    Node xsd_name;

		    nodeMap = child.getAttributes();

		    // NO attributes
		    if (nodeMap == null)
			continue;

		    // NO name attribute
		    if ((xsd_name = nodeMap.getNamedItem("name")) == null)
			continue;

		    attributes.add(xsd_name.getNodeValue());
		} else {
		    // recurse on children
		    attributes.addAll(getXSDAttributes(document, child));

		    // follow extensions
		    if (child.getNodeName().equals(XSD_EXTENSION)) {
			Node xsd_base;

			nodeMap = child.getAttributes();

			// NO attributes
			if (nodeMap == null)
			    continue;

			// NO base attribute
			if ((xsd_base = nodeMap.getNamedItem("base")) == null)
			    continue;

			// recurse using extension base
			attributes.addAll(getXSDAttributes(document, findNode(document, "name", xsd_base.getNodeValue())));
		    } else {
			// follow references, if any

			Node xsd_ref;

			nodeMap = child.getAttributes();

			// NO attributes
			if (nodeMap == null)
			    continue;

			// NO ref attribute
			if ((xsd_ref = nodeMap.getNamedItem("ref")) == null)
			    continue;

			// recurse using ref
			attributes.addAll(getXSDAttributes(document, findNode(document, "name", xsd_ref.getNodeValue())));
		    }
		}
	    } else
		continue;
	}

	attributes.trimToSize();
	return attributes;
    }

    // Get the XSD element names of parent that are not abstract
    public static Vector getXSDElementNames(Document document, Node parent) {
	NodeList children;
	Vector elements = new Vector();

	if (parent == null) {
	    elements.trimToSize();
	    return elements;
	}

	children = parent.getChildNodes();
	for (int i = 0; i < children.getLength(); ++i) {
	    Node child = children.item(i);
	    String s = "";

	    if (child.getNodeType() == Node.ELEMENT_NODE &&
		child.getNodeName().equals(XSD_ELEMENT)) {
		NamedNodeMap nodeMap = child.getAttributes();
		Node xsd_name;
		Node xsd_abstract;

		// NO attributes or NO name attribute
		if (nodeMap == null ||
		    (xsd_name = nodeMap.getNamedItem("name")) == null)
		    continue;

		// skip if abstract attribute is true
		if ((xsd_abstract = nodeMap.getNamedItem("abstract")) != null &&
		    xsd_abstract.getNodeValue().equals("true"))
		    continue;

		/*
		  Enumeration attributes = getXSDAttributes(document, child).elements();
		  while (attributes.hasMoreElements())
		     s += ", " + attributes.nextElement();

		  elements.add(xsd_name.getNodeValue() + s);
		*/

		elements.add(xsd_name.getNodeValue());
	    }
	}

	elements.trimToSize();
	return elements;
    }

    /*
     * Returns a hash table keyed by element name. The values
     * are vectors of attributes names
     */
    public static Hashtable getXSDElemAttr(Document document, Node parent) {
	NodeList children;
	Hashtable attrHash = new Hashtable();

	if (parent == null) {
	    return attrHash;
	}

	children = parent.getChildNodes();
	for (int i = 0; i < children.getLength(); ++i) {
	    Node child = children.item(i);

	    if (child.getNodeType() == Node.ELEMENT_NODE &&
		child.getNodeName().equals(XSD_ELEMENT)) {
		NamedNodeMap nodeMap = child.getAttributes();
		Node xsd_name;
		Node xsd_abstract;

		// NO attributes or NO name attribute
		if (nodeMap == null ||
		    (xsd_name = nodeMap.getNamedItem("name")) == null)
		    continue;

		// skip if abstract attribute is true
		if ((xsd_abstract = nodeMap.getNamedItem("abstract")) != null &&
		    xsd_abstract.getNodeValue().equals("true"))
		    continue;

		attrHash.put(xsd_name.getNodeValue(),
			     getXSDAttributes(document, child));
	    }
	}

	return attrHash;
    }

    public static void printXSDElementNames(Document document,
					    Node parent) {
	Enumeration elements = getXSDElementNames(document, parent).elements();

	while (elements.hasMoreElements())
	    System.out.println(elements.nextElement());
    }

    public static void importSceneNode(Document fromDoc,
				       Document toDoc,
				       Node parent)
	               throws DOMException {

	Node importedScene;
	Node root = fromDoc.getDocumentElement();
	NodeList children = root.getChildNodes();

	for (int i = 0; i < children.getLength(); ++i) {
	    Node child = children.item(i);

	    // if we have a Scene element node, then import it
	    if (child.getNodeType() == Node.ELEMENT_NODE &&
		child.getNodeName().equals("Scene")) {
		// import Scene element into toDoc
		importedScene = toDoc.importNode(child, true);

		// create a Group element
		Element group = toDoc.createElement("Group");
		while(importedScene.hasChildNodes()) {
		    group.appendChild(importedScene.getFirstChild());
		}

		parent.appendChild(group);
	    }
	}
    }

    /*
    // get list of all DEF's in node
    public static Vector getDefs(Node parent) {
	if (parent.getNodeType() != Node.ELEMENT_NODE)
	    return null;

	Vector defList = new Vector();
	NamedNodeMap attrs = parent.getAttributes();
	NodeList children = parent.getChildNodes();
	Node child = attrs.getNamedItem("DEF");

	if (child != null) {
	    defList.add(child.getNodeValue());
	}

	for (int i = 0; i < children.getLength(); ++i) {
	    Vector tmpList;

	    child = children.item(i);
	    if ((tmpList = getDefs(child)) != null)
		defList.addAll(tmpList);
	}

	return defList;
    }
    */

    // Get list of DEF's in node that are of the appropriate type.
    // If types is null or of length 0, return all DEFs in parent.
    public static Vector getDefs(Node parent,
				 String types[]) {
	if (parent.getNodeType() != Node.ELEMENT_NODE)
	    return null;

	Vector defList = new Vector();
	NamedNodeMap attrs = parent.getAttributes();
	NodeList children = parent.getChildNodes();
	Node child = attrs.getNamedItem("DEF");

	//	System.out.println("getSensorDefs: " + parent.getNodeName());

	if (child != null &&
	    typeMatch(parent.getNodeName(), types))
	    defList.add(child.getNodeValue());

	for (int i = 0; i < children.getLength(); ++i) {
	    Vector tmpList;

	    child = children.item(i);
	    if ((tmpList = getDefs(child, types)) != null)
		defList.addAll(tmpList);
	}

	return defList;
    }

    public static boolean typeMatch(String type,
				    String types[]) {
	if (types == null || types.length == 0)
	    return false;

	for (int i = 0; i < types.length; ++i)
	    if (type.equals(types[i]))
		return true;

	return false;
    }
}
