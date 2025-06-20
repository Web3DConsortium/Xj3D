/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes.proto;

// Standard imports
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.NRNodeFactory;

/**
 * A test case to check the functionality of the ProtoBuilder implementation.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TestProtoBuilder extends TestCase {

    /** The name of the root test proto we're creating */
    private static final String PROTO_NAME = "TestProto";

    /** The name of a test field we're creating */
    private static final String TEST_INT_FIELD = "intField";

    /** The type of the test field to be created */
    private static final String TEST_INT_TYPE = "SFInt32";

    /** The name of a test node field we're creating */
    private static final String TEST_NODE_FIELD = "nodeField";

    /** The type of the test node field to be created */
    private static final String TEST_NODE_TYPE = "SFNode";

    /** The ProtoBuilder instance we are testing */
    private ProtoBuilder builder;

    /** Node factory used for testing */
    private VRMLNodeFactory factory;

    /**
     * Create an instance of the test case for this particular test
     * name.
     *
     * @param name The name of the test method to be run
     */
    public TestProtoBuilder(String name) {
        super(name);
    }

    /**
     * Fetch the suite of tests for this test class to perform.
     *
     * @return A collection of all the tests to be run
     */
    public static Test suite() {
        return new TestSuite(TestProtoBuilder.class);
    }

    /**
     * Provide initialization of this test instance.
     */
    @Override
    public void setUp() {
        try {

            factory = NRNodeFactory.getNRNodeFactory();
            builder = new ProtoBuilder(factory);
            builder.setLoadRequirements(true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true);
        } catch(NullPointerException e) {
            fail(e.getMessage());
        }

        try {
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
        } catch(VRMLException e) {
            fail(e.getMessage());
        }
    }

    @Override
    public void tearDown() {
        factory = null;
        builder = null;
    }

    /**
     * Test an empty proto that does not contain any declarations or body.
     * Note that in the wild, this would be an illegal proto. All protos
     * require at least the one node in the body.
     */
    public void testSimpleProto() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
        } catch(VRMLException e) {
            fail(e.getMessage());
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);
        assertEquals("Proto name not set", PROTO_NAME, proto.getVRMLNodeName());

        // make sure we have the body group
        VRMLGroupingNodeType body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body group has children", 0, body.getChildrenSize());
    }

    /**
     * Test a proto declaration that contains fields but no body.
     */
    public void testProtoField() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_INT_TYPE,
                                   TEST_INT_FIELD,
                                   "42");

            builder.endProtoDecl();
        } catch(VRMLException e) {
            fail(e.getMessage());
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);

        // Check to see that we have a field with a value
        int index = proto.getFieldIndex(TEST_INT_FIELD);
        assertEquals("Proto missing fields", 2, proto.getFieldCount());
        assertTrue("Field not found", -1 != index);

        VRMLFieldDeclaration decl = proto.getFieldDeclaration(index);

        assertNotNull("Null field declaration", decl);

        assertEquals("Not correct access type",
                     FieldConstants.EXPOSEDFIELD,
                     decl.getAccessType());

        assertEquals("Not correct field type",
                     TEST_INT_TYPE,
                     decl.getFieldTypeString());
    }

    /**
     * Test a proto declaration of a field that contains a node. The node
     * is a simple declaration of a group node. Two tests are performed -
     * a group with no body and a group with a simple field.
     */
    public void testDeclNodeField() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoDecl();
        } catch(VRMLException e) {
            e.printStackTrace(System.err);
            fail(e.getClass() + " " + e.getMessage());
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);

        // Check to see that we have the field set
        int index = proto.getFieldIndex(TEST_NODE_FIELD);

        assertTrue("Field not found", -1 != index);
        assertEquals("Empty proto field count", 2, proto.getFieldCount());

        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            builder.startNode("Group", null);
            builder.startField("bboxCenter");
            builder.fieldValue("1 1 1");
            builder.endNode();
            builder.endProtoDecl();
        } catch(VRMLException e) {
            e.printStackTrace(System.err);
            fail(e.getClass() + " " + e.getMessage());
        }

        proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);

        // Check to see that we have the field set
        index = proto.getFieldIndex(TEST_NODE_FIELD);

        assertTrue("Field not found", -1 != index);

        assertEquals("Too many proto fields", 2, proto.getFieldCount());
    }

    /**
     * Test to check on the handling of nested node declarations in a
     * proto declaration. First test is a Group node with another group
     * node as a child. Second test has a Group node with a group and
     * shape as a child.
     */
    public void testDeclNestedNodeField() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            builder.startNode("Group", null);
            builder.startField("children");
            builder.startNode("Group", null);
            builder.endNode();
            builder.endField();
            builder.endNode();
            builder.endProtoDecl();
        } catch(VRMLException e) {
            e.printStackTrace(System.err);
            fail(e.getClass() + " " + e.getMessage());
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);

        // Check to see that we have the field set
        int index = proto.getFieldIndex(TEST_NODE_FIELD);

        assertTrue("Field not found", -1 != index);
        assertEquals("Empty proto field count", 2, proto.getFieldCount());

        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            builder.startNode("Group", null);
            builder.startField("bboxCenter");
            builder.fieldValue("1 1 1");
            builder.startField("children");
            builder.startNode("Group", null);
            builder.endNode();
            builder.startNode("Shape", null);
            builder.endNode();
            builder.endField();
            builder.endNode();
            builder.endProtoDecl();
        } catch(VRMLException e) {
            e.printStackTrace(System.err);
            fail(e.getClass() + " " + e.getMessage());
        }

        proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);

        // Check to see that we have the field set
        index = proto.getFieldIndex(TEST_NODE_FIELD);

        assertTrue("Field not found", -1 != index);

        assertEquals("Too many proto fields", 2, proto.getFieldCount());
    }

    /**
     * Test a proto declaration of a field that contains a node that has the
     * field value set to a USE. The USEd node is just a Group.
     */
    public void testDeclUseField() {

        // Build the USE map
        Map<String, VRMLNodeType> defs = new HashMap<>();
        defs.put("MY_GROUP", (VRMLNodeType) factory.createVRMLNode("Grouping", "Group", false));

        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            // This should fail as we haven't set the map yet
            builder.useDecl("MY_GROUP");
            fail("Failed to detect lack of def map");

        } catch(VRMLException e) {}

        // Now let's test with the map installed.
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.setGlobals(defs, null, null);
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);

            // This should fail as we haven't set the map yet
            builder.useDecl("MY_GROUP");
            builder.endProtoDecl();
        } catch(VRMLException e) {
            e.printStackTrace(System.err);
            fail(e.getClass() + " " + e.getMessage());
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();

        assertNotNull("Null proto reference", proto);

        // Check to see that we have the field set
        int index = proto.getFieldIndex(TEST_NODE_FIELD);

        assertTrue("Field not found", -1 != index);
        assertEquals("Empty proto field count", 2, proto.getFieldCount());
    }

    /**
     * Test a simple body of a proto that contains a single node. First test
     * has no field declarations. Second test includes a field declaration,
     * but there is no IS declarations. That is for another test. Third test
     * is changing the proto to be some other type than a group.
     */
    public void testSimpleBody() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // make sure we have the body group
        VRMLGroupingNodeType body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body group invalid children", 1, body.getChildrenSize());
        assertEquals("Wrong body type",
                     TypeConstants.GroupingNodeType,
                     proto.getPrimaryType());

        // Second test with the field decl in place
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_INT_TYPE,
                                   TEST_INT_FIELD,
                                   "42");
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            fail(e.getMessage());
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // Check to see that we have a field with a value
        assertEquals("Proto missing fields", 2, proto.getFieldCount());

        int index = proto.getFieldIndex(TEST_INT_FIELD);
        assertTrue("Field not found", -1 != index);

        // make sure we have the body group
        body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body group invalid children", 1, body.getChildrenSize());

        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Appearance", null);
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null appearance proto reference", proto);

        // make sure we have the body group
        body = proto.getBodyGroup();

        assertNotNull("Null app proto body", proto);
        assertEquals("Body group invalid children", 1, body.getChildrenSize());
        assertEquals("Wrong body type",
                     TypeConstants.AppearanceNodeType,
                     proto.getPrimaryType());

    }

    /**
     * Test of a body that contains multiple nodes. First test is simple
     * nesting children of the primary node type. The second test is to make
     * sure that multiple nodes at the root level work.
     */
    public void testMultiNodeBody() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.startField("children");
            builder.startNode("Group", null);
            builder.endNode();
            builder.endField();
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // make sure we have the body group
        VRMLGroupingNodeType body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body nested children", 1, body.getChildrenSize());

        // Second test with 2 nodes at root level
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body root children", 2, body.getChildrenSize());
    }

    /**
     * A test of nested proto declarations. The first test is a proto with no
     * fields and a single nested proto for the body. The second test is a
     * proto with nested proto that contains a simple field, followed by a test
     * with the field being a node.
     */
    public void testNestedProto() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.endProtoBody();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // make sure we have the body group
        VRMLGroupingNodeType body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body nested children", 0, body.getChildrenSize());

        // Second test with a field in the nested proto
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_INT_TYPE,
                                   TEST_INT_FIELD,
                                   "42");
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.endProtoBody();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // Check to see that we have a field with a value
        assertEquals("Proto root has fields", 1, proto.getFieldCount());

        body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body root children", 0, body.getChildrenSize());

        // Third test with a node field in the nested proto
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.endProtoBody();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // Check to see that we have a field with a value
        assertEquals("Proto root has fields", 1, proto.getFieldCount());

        body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body root children", 0, body.getChildrenSize());

        // Fourth test with a nested node field in the nested proto
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.protoFieldDecl(FieldConstants.EXPOSEDFIELD,
                                   TEST_NODE_TYPE,
                                   TEST_NODE_FIELD,
                                   null);
            builder.startNode("Group", null);
            builder.startField("children");
            builder.startNode("Group", null);
            builder.endNode();
            builder.endField();
            builder.endNode();
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.endProtoBody();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // Check to see that we have a field with a value
        assertEquals("Proto root has fields", 1, proto.getFieldCount());

        body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body root children", 0, body.getChildrenSize());
    }

    /**
     * Test for a proto body with nested protos not getting confused with
     * nodes in different body declarations
     */
    public void testNestedBody() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // make sure we have the body group
        VRMLGroupingNodeType body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body nested children", 0, body.getChildrenSize());

        // Second test has a nested proto, but a single node in the outside
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.endProtoBody();

            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("1. Null proto reference", proto);

        // Check to see that we have a field with a value
        assertEquals("1. Proto root has fields", 1, proto.getFieldCount());

        body = proto.getBodyGroup();

        assertNotNull("1. Null proto body", proto);
        assertEquals("1. Body root children", 1, body.getChildrenSize());

        // Third test has nodes in both protos, but more in the main body
        try{
            builder.reset();
            builder.startDocument("internal:///", null, "utf8", "#VRML", "V2.0", null);
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();

            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.endProtoDecl();

            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();

            builder.startNode("Appearance", null);
            builder.endNode();
            builder.startNode("Shape", null);
            builder.endNode();

            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("2. Null proto reference", proto);

        // Check to see that we have a field with a value
        assertEquals("2. Proto root has fields", 1, proto.getFieldCount());

        body = proto.getBodyGroup();

        assertNotNull("2. Null proto body", proto);
        assertEquals("2. Body root children", 2, body.getChildrenSize());
    }

    /**
     * Check the namespace handling of protos to make sure that we correctly
     * pick up nested proto definitions. First test makes sure that we can
     * create an internal proto and then declare an instance of it.
     */
    public void testProtoNamespace() {
        try{
            builder.startProtoDecl(PROTO_NAME);
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startProtoDecl(PROTO_NAME + "1");
            builder.endProtoDecl();
            builder.startProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.endProtoBody();
            builder.startNode("Group", null);
            builder.endNode();
            builder.startNode(PROTO_NAME + "1", null);
            builder.endNode();
            builder.endProtoBody();
        } catch(VRMLException e) {
            String msg = e.getMessage();

            if(msg == null) {
                msg = e.getClass().getName();
                e.printStackTrace(System.err);
            }

            fail(msg);
        }

        PrototypeDecl proto = (PrototypeDecl) builder.getPrototype();
        assertNotNull("Null proto reference", proto);

        // make sure we have the body group
        VRMLGroupingNodeType body = proto.getBodyGroup();

        assertNotNull("Null proto body", proto);
        assertEquals("Body nested children", 2, body.getChildrenSize());
    }

    /**
     * Main method to kick everything off with.
     * @param argv
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}

