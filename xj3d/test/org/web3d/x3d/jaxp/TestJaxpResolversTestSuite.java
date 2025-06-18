package org.web3d.x3d.jaxp;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 *
 * @author terry
 */
public class TestJaxpResolversTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("JAXP Entity Resolver Tests");

        suite.addTest(TestEntityResolver.suite());

        return suite;
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

}