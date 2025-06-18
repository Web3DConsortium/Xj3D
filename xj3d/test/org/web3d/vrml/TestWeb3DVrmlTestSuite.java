package org.web3d.vrml;

// External Tests
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Tests
import org.web3d.vrml.export.compressors.*;
import org.web3d.vrml.nodes.proto.*;
import org.web3d.vrml.parser.*;
import org.web3d.vrml.parser.vrml97.*;
import org.web3d.vrml.util.*;

/**
 * Top level test suite for the Web3D VRML package
 * @author <a href="mailto:tdnorbra@nps.edu">Terry D. Norbraten</a>
 */
public class TestWeb3DVrmlTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Web3D VRML Tests");

//        suite.addTest(BinaryTester.suite()); <- Needs works
        suite.addTest(TestCompressionTools.suite());
        suite.addTest(TestProtoBuilder.suite());
        suite.addTest(TestVRML97Reader.suite());
        suite.addTest(TestVRML97FieldParser.suite());
        suite.addTest(TestURLChecker.suite());

        return suite;
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

}
