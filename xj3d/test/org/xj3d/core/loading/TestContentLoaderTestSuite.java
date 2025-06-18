package org.xj3d.core.loading;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Tests

/**
 * Top level test suite for the core loading package
 * @author Terry Norbraten
 * @version
 */
public class TestContentLoaderTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Content Loader Tests");

        suite.addTest(TestContentLoader.suite());

        return suite;
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
}