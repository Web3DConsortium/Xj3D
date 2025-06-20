/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.export;

// External imports
import java.io.*;
import java.util.*;
import org.j3d.util.ErrorReporter;

// Local imports
import org.web3d.util.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;
import org.web3d.vrml.nodes.proto.AbstractProto;

import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.DefaultNodeFactory;

/**
 * VRML encoding Exporter.
 * <p>
 * The pretty printing on this is pretty bad.  Routes are pushed to the end of the
 * execution space, spacing is off and comments are lost.  In the future this may
 * be fixed, but its not a trivial problem.
 * <p>
 * <b>WARNING</b>: Do not change the location of a push or pop unless you really
 * test the results.
 * <p>
 * This class must track when a MFField ends.  The rules for this are fairly complex.
 * A field starts either on a startField, protoFieldDecl or scriptFieldDecl.
 * it ends on another startField/scriptfield/protofield, an endProtoDecl, endScriptDecl
 * or endNode which had at least one field decl and an IS decl and a fieldValue call.  yesh.
 * <p>
 * startField pushes these stacks:
 * <ul>
 * <li>inFieldType</li>
 * <li>currentField</li>
 * <li>nodeCnt -- all fields, only MFNode fieldDecl</li>
 * </ul>
 * on the end of the field it pops these stacks.
 * <p>
 * startNode pushes these stacks:
 * <ul>
 * <li>fieldCnt</li>
 * </ul>
 *
 * @author Jonathon Hubbard
 * @version $Revision: 1.7 $
 */
public class VrmlExporter extends Exporter
    implements BinaryContentHandler {

    /** How many spaces should we indent per level */
    private static final int INDENT_SIZE = 4;

    /** Field Default values, mapped by fieldType to value(String) */
    private static final Map<String, String> fieldDefaults;

    /** The output stream */
    private PrintWriter pw;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Field Parser */
    private VRMLFieldReader fieldParser;

    /** Node or Abstract Proto */
    private SimpleStack currentNode;
    private SimpleStack currentField;

    /** What type of field are we in, 0=MFNode, 1=SFNode, (not nodes)2=SF*, 3=MF* */
    private IntStack inFieldType;

    /** A count of the number of fields processed for this node */
    private IntStack fieldCnt;

    /** A count of the number of nodes processed for this MFNode */
    private IntStack nodeCnt;

    /** The current indent level */
    private int ilevel;

    /** String representation of n indent spaces */
    private String istring;

    /** Are we upgrading from VRML to X3D */
    private boolean upgrading;

    /** A mapping of node name to node.  Stores only one instance of each node. */
    private Map<String, VRMLNode> nodeMap;

    /** The mapping of proto names (key) to node instances (value) */
    private Map<String, PrototypeDecl> protoMap;

    /** The mapping of externproto names (key) to node instances (value) */
    private Map<String, ExternalPrototypeDecl>  externProtoMap;

    /** The working stack of currently defined PROTO declarations */
    private SimpleStack protoDeclStack;

    /** The working stack of proto decls maps */
    private SimpleStack protoMapStack;

    /** Copy of the current working prototype definition */
    private AbstractProto currentProto;

    /** Copy of the current working script definition */
    private VRMLScriptNodeType currentScript;

    /** Was the last token a field, for IS nodeCnt popping */
    private boolean lastStartField;

    /** Was the last token an IS */
    private boolean lastIS;

    /** Did we get a field value for the last field */
    private boolean gotFieldValue;

    /** List to hold ROUTES to print after a node ends */
    private StringBuilder routeBuff;

    private boolean ignoreNode;

    /** Do we need to drop a default for SFNode values */
    private boolean needSFNodeDefault;

    /** Mapping of node name to def string */
    private org.j3d.util.HashSet<String> defTable;

    /** Contains the current clash name to non-duplicate name mapping */
    private Map<String, String> defRemapTable;

    /** How many times have we seen the given DEF name already? */
    private Map<String, Integer> defNumTable;

    /**
     * FLag tracking whether we have started processing, to protect against
     * multithreaded use of this class.
     */
    private boolean alreadyStarted;

    /** Should long strings be split */
    private boolean splitLong;

    /** Maximum string length before splitting */
    private int MAX_STRING = 80;

    /**
     * Static constructor to populate the field default values
     */
    static {
        fieldDefaults = new HashMap<>();
        fieldDefaults.put("MFString","[]");
        fieldDefaults.put("MFFloat","[]");
        fieldDefaults.put("MFInt32","[]");
        fieldDefaults.put("MFDouble","[]");
        fieldDefaults.put("MFTime","[]");
        fieldDefaults.put("MFVec2f","[]");
        fieldDefaults.put("MFVec2d","[]");
        fieldDefaults.put("MFVec3f","[]");
        fieldDefaults.put("MFVec3d","[]");
        fieldDefaults.put("MFImage","[]");
        fieldDefaults.put("MFLong","[]");
        fieldDefaults.put("MFBool","[]");
        fieldDefaults.put("MFRotation","[]");
        fieldDefaults.put("MFColor","[]");
        fieldDefaults.put("MFColorRGBA","[]");

        fieldDefaults.put("SFString","\"\"");
        fieldDefaults.put("SFBool","FALSE");
        //fieldDefaults.put("SFNode","NULL");
        fieldDefaults.put("SFFloat","0");
        fieldDefaults.put("SFInt32","0");
        fieldDefaults.put("SFDouble","0");
        fieldDefaults.put("SFTime","-1");
        fieldDefaults.put("SFVec2f","0 0");
        fieldDefaults.put("SFVec2d","0 0");
        fieldDefaults.put("SFVec3f","0 0 0");
        fieldDefaults.put("SFVec3d","0 0");
        fieldDefaults.put("SFImage","0 0 0");
        fieldDefaults.put("SFLong","0");
        fieldDefaults.put("SFRotation","0 0 1 0");
        fieldDefaults.put("SFColor","0 0 0");
        fieldDefaults.put("SFColorRGBA","0 0 0 0");
    }

    /**
     * Public Constructor.
     *
     * @param os The stream to send output to.
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param reporter The error reporter to write messages to
     */
    public VrmlExporter(OutputStream os,
                        int major,
                        int minor,
                        ErrorReporter reporter
                        ) {

        this(os, major, minor, reporter, false);
    }

    /**
     * Public Constructor.
     *
     * @param os The stream to send output to.
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param reporter The error reporter to write messages to
     * @param split Whether to split long lines
     */
    public VrmlExporter(OutputStream os,
                        int major,
                        int minor,
                        ErrorReporter reporter,
                        boolean split) {

        super(major, minor, reporter);
        pw = new PrintWriter(os,false);

        currentNode = new SimpleStack();
        currentField = new SimpleStack();
        fieldCnt = new IntStack();
        nodeCnt = new IntStack();
        inFieldType = new IntStack();
        protoDeclStack = new SimpleStack();
        protoMapStack = new SimpleStack();

        nodeFactory = DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
        nodeFactory.setSpecVersion(major, minor);
        nodeFactory.setProfile("Immersive");

        FieldParserFactory fac =
            FieldParserFactory.getFieldParserFactory();

        fieldParser = fac.newFieldParser(major, minor);

        ilevel=0;
        upgrading = false;
        lastStartField=false;
        lastIS = false;
        gotFieldValue=true;
        alreadyStarted=false;
        routeBuff = new StringBuilder();
        StringBuilder buff = new StringBuilder();
        for(int i=0; i < INDENT_SIZE; i++) {
            buff.append(' ');
        }

        istring = buff.toString();
        nodeMap = new HashMap<>(50);
        protoMap = new HashMap<>();
        externProtoMap = new HashMap<>();

        needSFNodeDefault = false;

        defTable = new org.j3d.util.HashSet<>();
        defRemapTable = new HashMap<>();
        defNumTable = new HashMap<>();

        splitLong = split;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <code>startDocument()</code> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    @Override
    public void setDocumentLocator(DocumentLocator loc) {
    }


    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <code>#</code> start. The type string contains the representation of
     * the first few characters of the file after the #.
     * <p>
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

        throws SAVException, VRMLException{

        if(!alreadyStarted){
            pw.println("#VRML V2.0 utf8 \n");
            alreadyStarted = true;
        }
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
        throws SAVException, VRMLException{

        // Profiles don't exist in VRML97
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void componentDecl(String componentName)
        throws SAVException, VRMLException{

        // Components don't exist in VRML97
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
        throws SAVException, VRMLException{

        // Preserve META information as comments
        pw.print("# META: ");
        pw.print(key);
        pw.print(" = ");
        pw.println(value);
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
        throws SAVException, VRMLException{

        // Import doesn't exist in VRML97
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
        throws SAVException, VRMLException{

        // Export doesn't exist in VRML97
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {
        if (routeBuff.length() > 0) {
            pw.print(routeBuff.toString());
            routeBuff.setLength(0);
        }

        pw.flush();
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
    public void startNode(String name, String defName)
        throws SAVException, VRMLException{
        VRMLNode node;
        lastStartField = false;

        needSFNodeDefault = false;

        if (!inFieldType.isEmpty()) {
            int type = inFieldType.peek();

            int nCnt = nodeCnt.pop();
            if (type==0 && nCnt == 0)
                pw.println("[");
            nodeCnt.push(++nCnt);
        }
        lastIS=false;
        node = nodeMap.get(name);

        if (node == null) {
            if(protoMap.containsKey(name) || externProtoMap.containsKey(name)) {
                PrototypeDecl proto_def = protoMap.get(name);

                if(proto_def == null) {
                    ExternalPrototypeDecl eproto_def =
                        externProtoMap.get(name);
                    currentNode.push(eproto_def);
                } else {
                    currentNode.push(proto_def);
                }
            } else {
                node = nodeFactory.createVRMLNode(name, false);
                nodeMap.put(name, node);
                currentNode.push(node);
            }
        } else {
            currentNode.push(node);
        }

        fieldCnt.push(0);
        ilevel++;

        if (name.equals("Text")) {
            ignoreNode = true;
            return;
        } else {
            ignoreNode = false;
        }

        if (defName != null) {

            if (defTable.contains(defName)) {
                Integer num = defNumTable.get(defName);

                if (num != null)
                    num = num + 1;
                else
                    num = 1;

                defNumTable.put(defName, num);
                String newName = defName + num;
                errorReporter.warningReport("Duplicate DEF detected, renamed to: " +
                                      newName, null);
                defRemapTable.put(defName, newName);
                defName = newName;
            } else
                defTable.add(defName);

            pw.print("DEF ");
            pw.print(defName);
            pw.print(" ");
        }

        pw.print(name);
        pw.println(" {");
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode()
        throws SAVException, VRMLException{

        ilevel--;
        currentNode.pop();

        int cnt = fieldCnt.pop();

        if (cnt > 0 && !lastIS && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (type==0) {
                    indent();
                    ilevel--;
                    if (nCnt == 0 && !lastIS)
                        pw.print("[");

                    pw.println("]");
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                    pw.println("[]");
                }
            }
        }

        indent();

        if (!ignoreNode)
            pw.println("}");

        lastIS=false;
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startField(String name)
        throws SAVException, VRMLException{

        if (ignoreNode)
            return;

        lastStartField=true;
        int cnt = fieldCnt.pop();

        if (cnt > 0 && !lastIS && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (type==0) {
                    indent();
                    ilevel--;
                    if (nCnt == 0 && !lastIS)
                        pw.print("[");
                    pw.println("]");
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                    pw.println("[]");
                }
            }
        }
        fieldCnt.push(++cnt);
        lastIS=false;
        gotFieldValue=false;

        if(name.equals("IndexedTriangleSet") ||
           name.equals("IndexedTriangleStripSet") ||
           name.equals("IndexedTriangleFanSet")) {
            errorReporter.messageReport("Invalid Node Name: " + name);
        }

        indent();
        pw.print(name);
        pw.print(" ");
        Object node = currentNode.peek();

        int idx;
        if (node instanceof VRMLNodeType) {
            idx = ((VRMLNode)node).getFieldIndex(name);
        } else if (node instanceof PrototypeDecl) {
            idx = ((VRMLNodeTemplate)node).getFieldIndex(name);
        } else {
            idx = ((VRMLNodeTemplate)node).getFieldIndex(name);
        }
        currentField.push(idx);

        if (isMFNode()) {
            inFieldType.push(0);
            pw.println();
            ilevel++;
            indent();
        } else if (isSFNode()) {
            inFieldType.push(1);
        } else if (isSFField()) {
            inFieldType.push(2);
        } else {
            inFieldType.push(3);
        }
        nodeCnt.push(0);
    }


    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <code>startField()</code> or <code>endNode()</code>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName)
        throws SAVException, VRMLException{

        if (ignoreNode)
            return;

        int nCnt = nodeCnt.pop();

        if (nCnt == 0 && isMFNode()) {
            pw.print("[");
        }
        nodeCnt.push(++nCnt);

        String newName = defRemapTable.get(defName);
        if (newName != null)
            defName = newName;

        pw.print("USE ");
        pw.println(defName);
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endField() throws SAVException, VRMLException {
        // Ingored, handled by startField, endNode, endProtoDecl, endScriptDecl
    }

    //----------------------------------------------------------
    // Methods defined by StringContentHandler
    //----------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <code>startField()</code> or <code>endNode()</code>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String value)
        throws SAVException, VRMLException{

        if (ignoreNode)
            return;

        gotFieldValue=true;
        if (isMFField()) {
           pw.print("[");
        }

        if (value != null)
            if (isField(FieldConstants.SFBOOL)) {
                pw.print(value.toUpperCase());
            } else if (isField(FieldConstants.SFSTRING) ||
                       isField(FieldConstants.MFSTRING)) {
                boolean addwrap;
                if (value.charAt(0) != '\"') {
                    addwrap = true;
                    pw.print("\"");
                } else {
                    addwrap = false;
                }

                if (splitLong && value.length() > MAX_STRING) {
                    split(pw, value, MAX_STRING);
                } else {
                    pw.print(value);
                }

                if (addwrap)
                    pw.print("\"");
            } else {
                pw.print(value);
            }
        if (isMFField()) {
           //indent();
           pw.print("]");
        }
        pw.println();
        currentField.pop();
        nodeCnt.pop();
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] values)
        throws SAVException, VRMLException{

        if (ignoreNode)
            return;

        gotFieldValue=true;
        if (isMFField()) {
           pw.print("[");
        }

        boolean addwrap = false;
        for (String value : values) {
            if (isField(FieldConstants.SFSTRING) ||
                    isField(FieldConstants.MFSTRING)) {
                if (value.charAt(0) != '\"') {
                    addwrap = true;
                    pw.print("\"");
                } else {
                    addwrap = false;
                }
            }
            if (splitLong && value.length() > MAX_STRING) {
                split(pw, value, MAX_STRING);
            } else {
                pw.print(value);
            }
            if (addwrap)
                pw.print("\"");
            pw.print(" ");
        }

        pw.println("]");
        currentField.pop();
        nodeCnt.pop();
    }

    //----------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //----------------------------------------------------------

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
        gotFieldValue=true;
        if(value){
            pw.println("TRUE");
        }else{
            pw.println("FALSE");
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

         if (value == null  || value.length==0)
             return;

         gotFieldValue=true;
         pw.print("[");

         for(int j=0; j < len; j++) {
             if (j == len -1) {
                 if(value[j]) {
                     pw.print("TRUE");
                 } else {
                     pw.print("FALSE");
                 }
             } else {
                 if(value[j]) {
                     pw.print("TRUE,");
                 } else {
                     pw.print("FALSE,");
                 }
             }
         }
         pw.println("]");
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

        gotFieldValue=true;
        pw.println(value);
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

        if (value == null  || value.length==0)
            return;

        gotFieldValue=true;
        boolean ismf = isMFField();

        if (ismf)
            pw.print("[");

        if (len > 0) {
            int span = value.length / len;
            int smax = MAX_STRING * 6;  // pick some average string length

            int idx = 0;
            for(int j=0; j < len; j++) {
                for(int k=0; k < span; k++) {
                    pw.print(value[idx++]);
                    if (k != span - 1)
                        pw.print(" ");
                }
                if (j != len -1) {
                    pw.print(" ");
                }

                if (splitLong && j > 0 && ((j * span) % smax == 0)) {
                    pw.println();
                }
            }
        }

        if (ismf)
            pw.println("]");
        else
            pw.println();
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

        gotFieldValue=true;
        pw.println(value);
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

        if (value == null || value.length==0)
            return;


        gotFieldValue=true;
        pw.print("[");
        if (len > 0) {
            for(int j=0; j < len; j++) {

                if(j==len - 1){
                    pw.print(value[j]);
                }else{
                    pw.print(value[j]);
                    pw.print(" ");
                }
            }
        }
        pw.println("]");

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

        gotFieldValue=true;
        pw.println(value);
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

        if (value == null || value.length==0)
            return;

        gotFieldValue=true;
        boolean ismf = isMFField();

        if (ismf)
            pw.print("[");

        if (len > 0) {
            int span = value.length / len;
            int idx = 0;
            for(int j=0; j < len; j++) {
                for(int k=0; k < span; k++) {
                    pw.print(value[idx++]);
                    if (k != span - 1)
                        pw.print(" ");
                }
                if (j != len -1) {
                    pw.print(" ");
                }
            }
        }

        if (ismf)
            pw.println("]");
        else
            pw.println();
    }

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

        gotFieldValue=true;
        pw.println(value);
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

        if (value == null || value.length==0)
            return;


        gotFieldValue=true;
        pw.print("[");
        if (len > 0) {
            int smax = MAX_STRING * 4;  // pick some average string length
            for(int j=0; j < len - 1; j++) {
                pw.print(value[j]);

                if (splitLong && j > 0 && (j % smax == 0)) {
                    pw.println();
                } else {
                    pw.print(" ");
                }
            }

            pw.print(value[len - 1]);
        }
        pw.println("]");
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


        if (value == null || value.length==0)
            return;

        gotFieldValue=true;
        pw.print("[");


        if (len > 0) {
            int span = value.length / len;
            int idx = 0;
            for(int j=0; j < len - 1; j++) {
                pw.print("\"");
                pw.print(value[j]);
                pw.print("\",");
            }

            pw.print("\"");
            pw.print(value[len - 1]);
            pw.print("\"");

            pw.println("]");
        }
    }

    //-----------------------------------------------------------------------
    // Methods defined by RouteHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNode The name of the DEF of the source node
     * @param srcField The name of the field to route values from
     * @param destNode The name of the DEF of the destination node
     * @param destField The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void routeDecl(String srcNode,
                          String srcField,
                          String destNode,
                          String destField)
        throws SAVException, VRMLException{

        routeBuff.append("ROUTE ");
        routeBuff.append(srcNode);
        routeBuff.append(".");
        routeBuff.append(srcField);
        routeBuff.append(" TO ");
        routeBuff.append(destNode);
        routeBuff.append(".");
        routeBuff.append(destField);
        routeBuff.append("\n");
    }

    //----------------------------------------------------------
    // ScriptHandler methods
    //----------------------------------------------------------

    /**
     * Notification of the start of a script declaration. All calls between
     * now and the corresponding {@link #endScriptDecl} call belong to this
     * script node. This method will be called <i>after</i> the ContentHandler
     * <code>startNode()</code> method call. All DEF information is contained
     * in that method call and this just signifies the start of script
     * processing so that we know to treat the field parsing callbacks a
     * little differently.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startScriptDecl() throws SAVException, VRMLException {
        currentScript = (VRMLScriptNodeType)currentNode.peek();
        fieldCnt.push(0);
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <code>endNode()</code> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endScriptDecl() throws SAVException, VRMLException {
        int cnt = fieldCnt.pop();

        // TODO: I think this isEmpty check will fail for nested
        if (cnt > 0 && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (type==0) {
                    if (nCnt == 0 && !lastIS)
                        pw.print("[");

                    pw.println("]");
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                    pw.println("[]");
                }
            }
        }
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <code>url</code>, <code>mustEvaluate</code> and
     * <code>directOutput</code> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     * <p>
     * If the current parsing is in a proto and the field "value" is defined
     * with an IS statement then the value returned here is null. There will
     * be a subsequent call to the ProtoHandlers <code>protoIsDecl()</code>
     * method with the name of the field included.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException{

        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        currentScript.appendField(field);

        printFieldDecl(access, type, name, value);
    }

    //----------------------------------------------------------
    // ProtoHandler methods
    //----------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startProtoDecl(String name)
        throws SAVException, VRMLException{

        if (routeBuff.length() > 0) {
            pw.print(routeBuff.toString());
            routeBuff.setLength(0);
        }

        indent();
        ilevel++;
        pw.print("PROTO ");
        pw.print(name);
        pw.println(" [");

        PrototypeDecl proto = new PrototypeDecl(name,
                                                majorVersion,
                                                minorVersion,
                                                null);

        protoMap.put(name, proto);
        protoDeclStack.push(proto);

        currentProto = proto;
        fieldCnt.push(0);
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #startProtoBody()} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endProtoDecl()
        throws SAVException, VRMLException{

        int cnt = fieldCnt.pop();

        if (needSFNodeDefault) {
            needSFNodeDefault = false;
            pw.print("NULL\n");
        }

        if (cnt > 0 && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (type==0) {
                    if (nCnt == 0 && !lastIS)
                        pw.print("[");

                    pw.println("]");
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                    pw.println("[]");
                }
            }
        }

        ilevel--;
        indent();
        pw.println("]");
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
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException{

        if (needSFNodeDefault) {
            needSFNodeDefault = false;
            pw.print("NULL\n");
        }

        printFieldDecl(access, type, name, value);
        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        currentProto.appendField(field);
    }

    /**
     * Notification of a field value uses an IS statement.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void protoIsDecl(String fieldName)
        throws SAVException, VRMLException{

        int type = inFieldType.pop();

        needSFNodeDefault = false;

        if (lastStartField || type<2) {
            nodeCnt.pop();
            currentField.pop();
        }

        lastIS = true;
        pw.print("IS ");
        pw.println(fieldName);
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scene graph information.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startProtoBody() throws SAVException, VRMLException{

        indent();
        pw.println("{");
        ilevel++;

        protoMapStack.push(protoMap);

        protoMap = new HashMap<>();
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked") // cast from a home rolled Stack derivative
    public void endProtoBody() throws SAVException, VRMLException{
        if (routeBuff.length() > 0) {
            pw.print(routeBuff.toString());
            routeBuff.setLength(0);
        }

        ilevel--;
        indent();
        pw.println("}");

        if(protoMapStack.size() > 0) {

            // Now replace the data structures for this level
            protoMap = (Map<String, PrototypeDecl>)protoMapStack.pop();
        } else {
            protoMap = new HashMap<>();
        }
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startExternProtoDecl(String name)
        throws SAVException, VRMLException{

        indent();
        pw.print("EXTERNPROTO ");
        pw.print(name);
        pw.println(" [");
        ilevel++;

        ExternalPrototypeDecl proto = new ExternalPrototypeDecl(name,
                                                                majorVersion,
                                                                minorVersion,
                                                                null);

        // by spec, a new proto will trash the previous definition. Do it now.
        externProtoMap.put(name, proto);
        currentProto = proto;
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #externProtoURI} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endExternProtoDecl()
        throws SAVException, VRMLException{

        indent();
        pw.print("] ");
        ilevel--;
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list as an array of URI strings. The calling application is required to
     * interpret the incoming string. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
        pw.print("\"");

        // Not sure multiple URLs is valid?
        for (String value : values) {
            pw.print(value);
        }
        pw.println("\"\n");
    }

    //-----------------------------------------------------------------------
    //Local Methods
    //-----------------------------------------------------------------------

    /**
     * Indent to the current level.
     */
    private void indent() {
        for(int i=0; i < ilevel; i++) {
            pw.print(istring);
        }
    }

    /**
     * Print a field declaration.
     */
    private void printFieldDecl(int access,
                                String type,
                                String name,
                                Object value) {
        int typeNum;

        int cnt = fieldCnt.pop();

        if (cnt > 0 && !lastIS && !inFieldType.isEmpty()) {
            typeNum = inFieldType.pop();
            if (typeNum != 2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (typeNum == 0) {
                    indent();
                    ilevel--;
                    if (nCnt == 0 && !lastIS)
                        pw.print("[");

                    pw.println("]");
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                    pw.println("[]");
                }
            }
        }
        fieldCnt.push(++cnt);

        indent();
        switch(access) {
            case FieldConstants.FIELD:
                pw.print("field ");
                break;
            case FieldConstants.EXPOSEDFIELD:
                pw.print("exposedField ");
                break;
            case FieldConstants.EVENTIN:
                pw.print("eventIn ");
                break;
            case FieldConstants.EVENTOUT:
                pw.print("eventOut ");
                break;
            default:
                errorReporter.errorReport("Unknown field type in VrmlExporter: " +
                                    access, null);
        }

        pw.print(type);
        pw.print(" ");
        pw.print(name);
        pw.print(" ");

        switch (type) {
            case "MFNode":
                typeNum=0;
                break;
            case "SFNode":
                typeNum=1;
                break;
            default:
                typeNum=2;
                break;
        }

        inFieldType.push(typeNum);
        if (typeNum!=2) {
            nodeCnt.push(0);
            // TODO:Do we need a valid value here?
            currentField.push(0);
        }

        if (value instanceof String) {
            pw.print("\"");
            pw.print(value);
            pw.print("\"");
        } else if (value instanceof String[]) {
            pw.print("[");
            String[] strArray = (String[])value;
            for (String strArray1 : strArray) {
                pw.print("\"");
                pw.print(strArray1);
                pw.print("\" ");
            }
            pw.print("]");
        } else {
            if (access == FieldConstants.FIELD ||
                access == FieldConstants.EXPOSEDFIELD) {
                // Need to drop a default value by field type
                // TODO: Change to a hashmap
                String val = fieldDefaults.get(type);
                if (val != null) {
                    pw.print(val);
                } else {
                    if (type.equals("SFNode")) {
                        needSFNodeDefault = true;
                    }
                }
            }
        }
/*
        if ((access == FieldConstants.FIELD || access == FieldConstants.EXPOSEDFIELD) && type.startsWith("MF") && typeNum != 0 && value == null) {
            pw.print("[]");
        }
*/
        pw.println();
        lastIS=false;
        gotFieldValue=true;
    }

    /**
     * Is the current field a MFField?
     */
    private boolean isMFField() {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek());
        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNode)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        } else {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        }

        if (decl == null) {
            errorReporter.warningReport("No decl for: " + node + " idx: " + idx, null);
        }

        switch(decl.getFieldType()) {
            case FieldConstants.MFINT32:
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFFLOAT:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFVEC2F:
            case FieldConstants.MFIMAGE:
            case FieldConstants.MFSTRING:
            case FieldConstants.MFNODE:
                return true;
        }

        return false;
    }

    /**
     * Is the current field a MFNode?
     */
    private boolean isMFNode() {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek());

        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNode)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        } else {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        }

        return (decl.getFieldType() == FieldConstants.MFNODE);
    }

    /**
     * Is the current field a SFNode?
     */
    private boolean isSFNode() {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek());

        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNode)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        } else {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        }

        return (decl.getFieldType() == FieldConstants.SFNODE);
    }

    /**
     * Is the current field a simple SF* field, ie not SFNode?
     */
    private boolean isSFField() {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek());

        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNode)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        } else {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        }

        switch(decl.getFieldType()) {
            case FieldConstants.SFINT32:
            case FieldConstants.SFCOLOR:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFFLOAT:
            case FieldConstants.SFROTATION:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFVEC2F:
            case FieldConstants.SFIMAGE:
            case FieldConstants.SFSTRING:
                return true;
        }
        return false;
    }

    /**
     * Is the current field of the type requested.
     *
     * @param const The FieldConstants constant
     */
    private boolean isField(int fconst) {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek());

        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNode)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        } else {
            decl = ((VRMLNodeTemplate)node).getFieldDeclaration(idx);
        }

        return (fconst == decl.getFieldType());
    }

    /**
     * Split a long string into pieces.  Split along VRML whitespace characters.
     *
     * @param pw Output writer
     * @param val The string value
     * @param max The max length
     */
    private void split(PrintWriter pw, String val, int max) {
        int len = val.length();

        int lineLen = 0;

        for(int i=0; i < len; i++) {
            char c = val.charAt(i);
            lineLen++;

            if (c == '\n') {
                lineLen = 0;
            }

            if (lineLen >= max) {
                switch(c) {
                    case ' ':
                    case '\t':
                    case ',':
                        pw.append('\n');
                        lineLen = 0;
                        break;
                    default:
                        pw.append(c);
                }

            } else {
                pw.append(c);
            }
        }
    }
}
