// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * JUnit 4 master level test suite
 * @author Terry Norbraten
 */
public class TestAllTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Run All JUnit Tests");

        suite.addTest(org.web3d.util.TestWeb3DUtilTestSuite.suite());
        suite.addTest(org.web3d.vrml.TestWeb3DVrmlTestSuite.suite());
        suite.addTest(org.web3d.x3d.jaxp.TestJaxpResolversTestSuite.suite());
        suite.addTest(org.xj3d.core.loading.TestContentLoaderTestSuite.suite());
        suite.addTest(xj3d.filter.TestXj3dFiltersTestSuite.suite());

        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run (suite());
    }
}