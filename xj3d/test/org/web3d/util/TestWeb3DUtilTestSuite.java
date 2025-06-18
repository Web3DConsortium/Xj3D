package org.web3d.util;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

// Internal Tests
import org.web3d.util.spatial.*;

/**
 * Top level test suite for the Web3D Util package
 * @author <a href="mailto:tdnorbra@nps.edu">Terry D. Norbraten</a>
 */
public class TestWeb3DUtilTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Web3D Util Tests");

        suite.addTest(DoubleToStringTest.suite());
        suite.addTest(IntHashSetTest.suite());
        suite.addTest(TestArrayUtils.suite());
        suite.addTest(GridTrianglePartitionTest.suite());

        return suite;
    }

    public static void main (String[] args) {
        TestRunner.run (suite());
    }

}
