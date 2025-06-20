/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package xj3d.filter;

// External imports
// None

// Local imports
import org.web3d.util.I18nUtils;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.UnsupportedNodeException;
import org.web3d.vrml.sav.*;

import org.web3d.parser.DefaultFieldParserFactory;
import org.web3d.util.SimpleStack;
import org.web3d.util.SimpleStackLogged;
import org.j3d.util.ErrorReporter;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.FieldConstants;

/**
 * Base of all filters that can be implemented in the filter chain.
 *
 * This class is designed to always have its methods called by overriding
 * classes.  If you do not wish the sav calls to be passed onto the
 * downstream filters then you should call suppressCalls.  This insures
 * that all the stacks are properly maintained.
 *
 * <p>
 * Takes SAV stream and issues SAV Streams after some transformation.
 * <p>
 *
 * The default implementation of all the methods act as pure pass-throughs.
 * Each implementation may override the methods that are necessary for its
 * function.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class BaseFilter extends AbstractFilter {

    private static final String INVALID_FIELD_VALUE_MSG_PROP =
            "xj3d.filter.FieldValueHandler.invalidFieldValue";

    /** Should we suppress calls to the underlying content handler */
    protected boolean suppressCalls;

    /** The node marker at the scene root */
    protected NodeMarker sceneMarker;

    protected boolean insideInvalidField = false;

    /**
     * Construct a default instance of the field handler
     */
    protected BaseFilter() {
        this(false);
    }

    /**
     * Constructor
     *
     * @param debug Should we run in debug mode
     */
    protected BaseFilter(boolean debug) {
        if (debug) {
            nodeStack = new SimpleStackLogged("Node");
        } else {
            nodeStack = new SimpleStack();
        }

        suppressCalls = false;

        lastErrorCode = FilterExitCodes.SUCCESS;
    }

    /**
     * Set the filter into debug mode.  This must be called before
     * any filtering occurs.
     *
     * @param debug True to debug
     */
    public void setDebug(boolean debug) {
        if (debug) {
            nodeStack = new SimpleStackLogged("Node");
        } else {
            if (!(nodeStack instanceof SimpleStack)) {
                nodeStack = new SimpleStack();
            }
        }
    }

    /**
     * Should calls to the underlying content handler be suppressed.
     * Useful for buffering operations to reuse stack logic while
     * maintaining control of content handler calls.
     *
     * @param val The new value
     */
    protected void suppressCalls(boolean val) {
        suppressCalls = val;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <CODE>startDocument()</CODE> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The documentLocator instance to use
     */
    @Override
    public void setDocumentLocator(DocumentLocator loc) {
        documentLocator = loc;
    }

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        majorVersion = 3;
        minorVersion = 0;

        if(type.charAt(1) == 'V') {
            // we're in VRML model either 97 or 1.0.
            // Look at the 6th character to see the version number
            // ie "VRML V1.0" or "VRML V2.0"
            boolean is_20 = (version.charAt(1) == '2');

            if(is_20) {
                majorVersion = 2;
            }

        } else {
            // Parse the number string looking for the version minor number.
            int dot_index = version.indexOf('.');
            String minor_num = version.substring(dot_index + 1);

            // Should this look for a badly formatted number here or just
            // assume the parsing beforehad has correctly identified something
            // already dodgy?
            minorVersion = Integer.parseInt(minor_num);
        }

        DefaultFieldParserFactory fieldParserFactory =
            new DefaultFieldParserFactory();
        fieldReader =
            fieldParserFactory.newFieldParser(majorVersion, minorVersion);

        fieldHandler = new FieldValueHandler(fieldReader, contentHandler);
        fieldHandler.setParsingType(parsingType);

        // Model the rootNode as a Group
        sceneMarker = new NodeMarker("Scene", "children", FieldConstants.MFNODE);
        nodeStack.push(sceneMarker);

        if(contentHandler != null && !suppressCalls)
            contentHandler.startDocument(uri,
                                         url,
                                         encoding,
                                         type,
                                         version,
                                         comment);
    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {

        if(contentHandler != null && !suppressCalls)
            contentHandler.profileDecl(profileName);
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentInfo The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void componentDecl(String componentInfo)
        throws SAVException, VRMLException {

        if(contentHandler != null && !suppressCalls)
            contentHandler.componentDecl(componentInfo);
    }

    /**
     * A META declaration has been found in the code. There may be zero
     * or more meta declarations in the file, appearing just after the
     * component declaration. Each meta declaration has a key and value
     * strings. No information is to be implied from this. It is for extra
     * data only.
     *
     * @param key The value of the key string
     * @param value The value of the value string
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void metaDecl(String key, String value)
        throws SAVException, VRMLException {

        if(contentHandler != null && !suppressCalls)
            contentHandler.metaDecl(key, value);
    }

    /**
     * An IMPORT declaration has been found in the document. All three
     * parameters will always be provided, regardless of whether the AS keyword
     * has been used or not. The parser implementation will automatically set
     * the local import name as needed.
     *
     * @param inline The name of the inline DEF nodes
     * @param exported The exported name from the inlined file
     * @param imported The local name to use for the exported name
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void importDecl(String inline, String exported, String imported)
        throws SAVException, VRMLException {

        if(contentHandler != null && !suppressCalls)
            contentHandler.importDecl(inline, exported, imported);
    }

    /**
     * An EXPORT declaration has been found in the document. Both parameters
     * will always be provided regardless of whether the AS keyword has been
     * used. The parser implementation will automatically set the exported
     * name as needed.
     *
     * @param defName The DEF name of the nodes to be exported
     * @param exported The name to be exported as
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void exportDecl(String defName, String exported)
        throws SAVException, VRMLException {

        if(contentHandler != null && !suppressCalls)
            contentHandler.exportDecl(defName, exported);
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {
        if(contentHandler != null && !suppressCalls)
            contentHandler.endDocument();

        if (nodeStack.isEmpty()) {

            System.out.println("Node stack incorrect at end document: sceneMarker is missing");

        } else {

            NodeMarker marker = (NodeMarker)nodeStack.peek();
            if (marker != sceneMarker) {
                System.out.println("Node stack incorrect at end document: remaining elements:");
                int num = nodeStack.size();
                while (!nodeStack.isEmpty()) {
                    System.out.println(nodeStack.pop());
                }
            }
        }
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        NodeMarker marker = new NodeMarker(name);
        nodeStack.push(marker);

        if(contentHandler != null && !suppressCalls && name != null) {
            contentHandler.startNode(name, defName);
        }
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

        NodeMarker marker = (NodeMarker)nodeStack.pop();
        NodeMarker parent = (NodeMarker)nodeStack.peek();
        if (parent.fieldType == FieldConstants.SFNODE) {
            parent.clearFieldData();
        }

        if(contentHandler != null && !suppressCalls)
            contentHandler.endNode();
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        try {
            int fieldType = fieldHandler.getFieldType(marker.nodeName, name);
            marker.setFieldData(name, fieldType);
        } catch (UnsupportedNodeException une) {
        	String[] msg_args = new String[] {marker.nodeName};

            if (parsingType == ParsingType.TOLERANT) {
                insideInvalidField = true;
                I18nUtils.printMsg("xj3d.filter.BaseFilter.unsupportedNode", I18nUtils.EXT_MSG, msg_args);
                return;
            }
            if (nodeStack instanceof SimpleStackLogged) {
                I18nUtils.printMsg("xj3d.filter.BaseFilter.unsupportedNode", I18nUtils.CRIT_MSG, msg_args);
                System.out.println(((SimpleStackLogged)nodeStack).toStringHistory());
            } else {
            	I18nUtils.printMsg("xj3d.filter.BaseFilter.unsupportedNode", I18nUtils.CRIT_MSG, msg_args);
            }

            throw une;
        } catch (InvalidFieldException ife) {
        	String[] msg_args = new String[] {marker.nodeName + "." + name};

            if (parsingType == ParsingType.TOLERANT) {
                insideInvalidField = true;
                I18nUtils.printMsg("xj3d.filter.BaseFilter.invalidFieldName", I18nUtils.EXT_MSG, msg_args);
                return;
            }
            if (nodeStack instanceof SimpleStackLogged) {
                I18nUtils.printMsg("xj3d.filter.BaseFilter.invalidFieldName", I18nUtils.CRIT_MSG, msg_args);
                System.out.println(((SimpleStackLogged)nodeStack).toStringHistory());
            } else {
                I18nUtils.printMsg("xj3d.filter.BaseFilter.invalidFieldName", I18nUtils.CRIT_MSG, msg_args);
            }

            throw ife;
        } catch (VRMLException ife) {
        	String[] msg_args = new String[] {marker.nodeName + "." + name, "Unknown"};

            if (parsingType == ParsingType.TOLERANT) {
                insideInvalidField = true;
                I18nUtils.printMsg("xj3d.filter.FieldValueHandler.invalidFieldValue", I18nUtils.EXT_MSG, msg_args);
                return;
            }
            if (nodeStack instanceof SimpleStackLogged) {
                I18nUtils.printMsg("xj3d.filter.FieldValueHandler.invalidFieldValue", I18nUtils.CRIT_MSG, msg_args);
                System.out.println(((SimpleStackLogged)nodeStack).toStringHistory());
            } else {
                I18nUtils.printMsg("xj3d.filter.FieldValueHandler.invalidFieldValue", I18nUtils.CRIT_MSG, msg_args);
            }

            throw ife;
        }
        if(contentHandler != null && !suppressCalls)
            contentHandler.startField(name);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        NodeMarker marker = (NodeMarker)nodeStack.peek();
        if (marker.fieldType == FieldConstants.SFNODE) {
            // Only clear field data on SFNODE,
            // MFNode is handled in endField
            marker.clearFieldData();
        }

        if(contentHandler != null && !suppressCalls)
            contentHandler.useDecl(defName);
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endField() throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        NodeMarker parent = (NodeMarker)nodeStack.peek();
        if (parent.fieldType == FieldConstants.MFNODE) {
            parent.clearFieldData();
        }

        if(contentHandler != null && !suppressCalls)
            contentHandler.endField();
    }

    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String value) throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();

            try {
                fieldHandler.setFieldValue(
                    marker.nodeName,
                    marker.fieldName,
                    value);
            } catch(VRMLException ve) {
                if (parsingType != ParsingType.TOLERANT) {
                    throw ve;
                }

                I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP,I18nUtils.EXT_MSG,
                        new String[]{marker.nodeName + "." + marker.fieldName, value});
            }
            marker.clearFieldData();

        }
    }

    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String[] values) throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                values);
            marker.clearFieldData();
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int[] value, int len)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value,
                len);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean[] value, int len)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value,
                len);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float[] value, int len)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value,
                len);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long[] value, int len)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value,
                len);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double[] value, int len)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value,
                len);
            marker.clearFieldData();
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] value, int len)
        throws SAVException, VRMLException {

        if (insideInvalidField) {
            insideInvalidField = false;
            return;
        }

        if (contentHandler != null && !suppressCalls) {
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            fieldHandler.setFieldValue(
                marker.nodeName,
                marker.fieldName,
                value,
                len);
            marker.clearFieldData();
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ProtoHandler
    //---------------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startProtoDecl(String name) throws SAVException, VRMLException {

        if(protoHandler != null)
            protoHandler.startProtoDecl(name);
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endProtoDecl() throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.endProtoDecl();
    }

    /**
     * Notification of a proto's field declaration. This is used for both
     * external and ordinary protos. Externprotos don't allow the declaration
     * of a value for the field. In this case, the parameter value will be
     * null.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field. Null if not allowed.
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException {

        if(protoHandler != null)
            protoHandler.protoFieldDecl(access, type, name, value);
    }

    /**
     * Notification of a field value uses an IS statement. If we are running
     * in VRML97 mode, this will throw an exception if the field access types
     * do not match.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void protoIsDecl(String fieldName) throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.protoIsDecl(fieldName);
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startProtoBody() throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.startProtoBody();
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endProtoBody() throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.endProtoBody();
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startExternProtoDecl(String name) throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.startExternProtoDecl(name);
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endExternProtoDecl() throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.endExternProtoDecl();
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list of URIs. The calling application is required to interpret the
     * incoming strings. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
        if(protoHandler != null)
            protoHandler.externProtoURI(values);
    }

    //---------------------------------------------------------------
    // Methods defined by ScriptHandler
    //---------------------------------------------------------------

    /**
     * Notification of the start of a script declaration. All calls between
     * now and the corresponding {@link #endScriptDecl} call belong to this
     * script node. This method will be called <I>after</I> the ContentHandler
     * <CODE>startNode()</CODE> method call. All DEF information is contained
     * in that method call and this just signifies the start of script
     * processing so that we know to treat the field parsing callbacks a
     * little differently.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startScriptDecl() throws SAVException, VRMLException {
        if(scriptHandler != null)
            scriptHandler.startScriptDecl();
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <CODE>endNode()</CODE> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endScriptDecl() throws SAVException, VRMLException {
        if(scriptHandler != null)
            scriptHandler.endScriptDecl();
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <CODE>url</CODE>, <CODE>mustEvaluate</CODE> and
     * <CODE>directOutput</CODE> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException {

        if(scriptHandler != null)
            scriptHandler.scriptFieldDecl(access, type, name, value);
    }

    //---------------------------------------------------------------
    // Methods defined by RouteHandler
    //---------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNodeName The name of the DEF of the source node
     * @param srcFieldName The name of the field to route values from
     * @param destNodeName The name of the DEF of the destination node
     * @param destFieldName The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void routeDecl(String srcNodeName,
                          String srcFieldName,
                          String destNodeName,
                          String destFieldName)
        throws SAVException, VRMLException {

        if ((routeHandler != null) && !suppressCalls) {
            routeHandler.routeDecl(
                srcNodeName,
                srcFieldName,
                destNodeName,
                destFieldName);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the currently set {@link ScriptHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set script handler.
     */
    @Override
    public ScriptHandler getScriptHandler() {
        return scriptHandler;
    }

    /**
     * Set the script handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param sh The script handler instance to use
     */
    @Override
    public void setScriptHandler(ScriptHandler sh) {
        scriptHandler = sh;
    }

    /**
     * Get the currently set {@link ProtoHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set proto handler.
     */
    @Override
    public ProtoHandler getProtoHandler() {
        return protoHandler;
    }

    /**
     * Set the proto handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ph The proto handler instance to use
     */
    @Override
    public void setProtoHandler(ProtoHandler ph) {
        protoHandler = ph;
    }

    /**
     * Get the currently set {@link ContentHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set node handler.
     */
    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Set the node handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param nh The node handler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler nh) {
        contentHandler = nh;
    }

    /**
     * Get the currently set {@link RouteHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set route handler.
     */
    @Override
    public RouteHandler getRouteHandler() {
        return routeHandler;
    }

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param rh The route handler instance to use
     */
    @Override
    public void setRouteHandler(RouteHandler rh) {
        routeHandler = rh;
    }

    /**
     * Get the currently set {@link ErrorHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set error handler.
     */
    @Override
    public ErrorReporter getErrorReporter() {
        return errorHandler;
    }

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorReporter(ErrorReporter eh) {
        errorHandler = eh;
    }

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] arg) {
    }

    /**
     * Return the last error code generated by this filter.
     * 0 if everything is working as intended
     * @return
     */
    @Override
    public int getLastErrorCode() {
        return lastErrorCode;
    }
}
