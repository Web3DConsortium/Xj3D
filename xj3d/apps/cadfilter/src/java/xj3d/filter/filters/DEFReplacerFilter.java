/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package xj3d.filter.filters;

// External Imports
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

// Local Imports
import xj3d.filter.AbstractFilter;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;


/**
 * Search for nodes matching a DEF name table.  Replaces the content
 * with the X3D content specified in a file.
 *
 * Params supported:
 *
 *    -mappingFile foo.properties  -- A Java properties file that maps DEF names
 *                                    to files.
 *
 *     The X3D files must be a valid X3D file.  The content of the DEFed node
 *     in the source file will be replaced with the content of the same DEF name
 *     in the mapped file.
 *
 *  Any child DEF's that might match are ignored.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class DEFReplacerFilter extends AbstractFilter {

    /** Argument for the mapping file */
    private static final String MAPPING_FILE = "-mappingFile";

    /** The mapping file to use */
    private String mappingFile;

    /** Are we inside a mapped content, if so ignore */
    private boolean inMappedContent;

    /** Mapped name */
    private String mappedName;

    /** Container that keeps track of the defNames on the stack */
    private final Stack<String> nodes;

    /** DEF's to filename mapping */
    private final Map<String, String> defMapping;

    /**
     * Basic constructor.
     */
    public DEFReplacerFilter() {

        nodes = new Stack<>();
        inMappedContent = false;
        defMapping = new HashMap<>();

    }

    //----------------------------------------------------------
    // Methods overidding AbstractFilter
    //----------------------------------------------------------

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

        loadMapping(mappingFile);

        super.startDocument(uri,
                            url,
                            encoding,
                            type,
                            version,
                            comment);
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

        if (inMappedContent)
            return;

        super.importDecl(inline, exported, imported);
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

        if (inMappedContent)
            return;

        super.exportDecl(defName, exported);
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
        if (inMappedContent)
            return;

        super.endField();
    }

    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------

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
        if (inMappedContent)
            return;

        super.fieldValue(values);
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

        if (inMappedContent)
            return;

        super.fieldValue(value);
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

        if (inMappedContent)
            return;

        super.fieldValue(value, len);
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

        if (inMappedContent)
            return;

        super.fieldValue(value);
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

        if (inMappedContent)
            return;

        super.fieldValue(value, len);
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

        if (inMappedContent)
            return;

        super.fieldValue(value);
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

        if (inMappedContent)
            return;

        super.fieldValue(value, len);
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

        if (inMappedContent)
            return;

        super.fieldValue(value);
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

        if (inMappedContent)
            return;

        super.fieldValue(value, len);
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

        if (inMappedContent)
            return;

        super.fieldValue(value);
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

        if (inMappedContent)
            return;

        super.fieldValue(value, len);
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

        if (inMappedContent)
            return;

        super.fieldValue(value, len);
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

        if (inMappedContent)
            return;

        super.startProtoDecl(name);
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endProtoDecl() throws SAVException, VRMLException {
        if (inMappedContent)
            return;

        super.endProtoDecl();
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

        if (inMappedContent)
            return;

        super.protoFieldDecl(access, type, name, value);
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
        if (inMappedContent)
            return;

        super.protoIsDecl(fieldName);
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
        if (inMappedContent)
            return;

        super.startProtoBody();
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
        if (inMappedContent)
            return;

        super.endProtoBody();
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
        if (inMappedContent)
            return;

        super.startExternProtoDecl(name);
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endExternProtoDecl() throws SAVException, VRMLException {
        if (inMappedContent)
            return;

        super.endExternProtoDecl();
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
        if (inMappedContent)
            return;

        super.externProtoURI(values);
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
        if (inMappedContent)
            return;

        super.startScriptDecl();
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
        if (inMappedContent)
            return;

        super.endScriptDecl();
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

        if (inMappedContent)
            return;

        super.scriptFieldDecl(access, type, name, value);
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

        if (inMappedContent)
            return;

        super.routeDecl(srcNodeName,
                        srcFieldName,
                        destNodeName,
                        destFieldName);
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName) throws SAVException,
            VRMLException {

        if (inMappedContent)
            return;

        String filename = defMapping.get(defName);

        nodes.push(defName);

        if (filename != null) {
            // Load file and issue sav calls.
            mappedName = defName;
            filterDEF(new File(filename), defName);

            // Now suppress content
            inMappedContent = true;
        } else {
            super.startNode(name, defName);
        }
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
        if (inMappedContent)
            return;

        super.startField(name);
    }

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
        if (inMappedContent)
            return;

        super.fieldValue(value);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {
        if (inMappedContent)
            return;

        super.useDecl(defName);
    }

    /**
     * Notification of the end of a node declaration.
     * If boolean addMaterial is set to TRUE, then when a
     * Shape node ends we reset materialAdded, so that
     * future Shapes can also have Material nodes added
     * if necessary.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

        String nodeName = nodes.pop();

        if(nodeName != null && nodeName.equals(mappedName)) {
            inMappedContent = false;
            mappedName = null;
            return;
        }

        super.endNode();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Load the mapping file.
     *
     * @param filename The mapping file to load
     */
    public void loadMapping(final String filename) {
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                String user_dir = System.getProperty("user.dir");
                InputStream is;
                String file = user_dir + File.separator + filename;

                // Using File.separator does not work for defLoc, not sure why
                String defLoc = filename;

                try {
                    is = new FileInputStream(file);
                } catch(FileNotFoundException fnfe) {
                    // Fallback to default
                    is = ClassLoader.getSystemResourceAsStream(defLoc);
                }

                // Fallback for WebStart
                if (is == null) {
                    ClassLoader cl = DEFReplacerFilter.class.getClassLoader();
                    is = cl.getResourceAsStream(defLoc);
                }
                if (is == null) {
                    errorHandler.warningReport("No property file found in " +
                            defLoc, null);
                } else {
                    Properties props = new Properties();
                    try {
                        props.load(is);
                        is.close();
                    } catch(IOException ioe) {
                        errorHandler.warningReport(
                                "Error reading" + defLoc, null);
                    }

                    Set entries = props.entrySet();
                    Iterator itr = entries.iterator();

                    Map.Entry entry;
                    while(itr.hasNext()) {
                        entry = (Map.Entry) itr.next();
                        defMapping.put((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                return( null );
            });
        } catch ( PrivilegedActionException pae ) {
            errorHandler.errorReport( "Cannot load properties file: " + filename, pae );
        }

    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param filter The identifier of the filter type.
     * @param url The URL to open, or null if the input is specified by the file argument.
     * @param inFile The file to load, or null if the input is specified by the url argument.
     * @param out The output filename.
     * @param filter_args The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    private void filterDEF(File inFile, String defName) {
        AbstractFilter chooser_filter = new DEFChooserFilter(true, defName);

        chooser_filter.setContentHandler(getContentHandler());
        chooser_filter.setScriptHandler(getScriptHandler());
        chooser_filter.setProtoHandler(getProtoHandler());
        chooser_filter.setRouteHandler(getRouteHandler());

        VRMLParserFactory parserFactory;
        InputSource is = null;
        try {
            is = new InputSource(inFile);
            parserFactory = VRMLParserFactory.newVRMLParserFactory();
        } catch(FactoryConfigurationError | MalformedURLException ex) {
            errorHandler.errorReport("Failed to load factory: ", ex);
            try {
                // clean up...
                if (is != null)
                    is.close();
            } catch (IOException ioe) {}
            return;
        }

        VRMLReader reader = parserFactory.newVRMLReader();

        reader.setContentHandler(chooser_filter);
        reader.setRouteHandler(chooser_filter);
        reader.setScriptHandler(chooser_filter);
        reader.setProtoHandler(chooser_filter);
        reader.setErrorReporter(errorHandler);

        try {
            reader.parse(is);
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        } finally {
            try {
                // clean up...
                if (is != null)
                    is.close();
            } catch (IOException ioe) {}
        }
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(MAPPING_FILE)) {
                if (i + 1 >= args.length){

                    throw new IllegalArgumentException(
                        "Not enough args for DEFReplacerFilter.  " +
                        "Expecting one more to specify mapping file.");
                }
                mappingFile = args[i+1];
            }
        }
    }
}
