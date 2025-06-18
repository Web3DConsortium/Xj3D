package org.web3d.vrml.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author <a href="mailto:tdnorbra@nps.edu">Terry D. Norbraten</a>
 */
public class TestURLChecker extends TestCase {

    public static final String OS = System.getProperty("os.name");
    public static final boolean IS_WIN = OS.toLowerCase().contains("win");

    public TestURLChecker(String name)
    {
        super(name);
    }

    public void testPrependFileScheme() {
        String expected = "file://server/dir/file.txt"; // Windows UNC Path
        String result = URLChecker.prependFileScheme(expected);
        assertEquals(expected, result);

        expected = null;
        result = URLChecker.prependFileScheme(null);
        assertEquals(expected, result);

        expected = "file:///Z:/dir/file.txt"; // Windows drive letter path
        result = URLChecker.prependFileScheme(expected);
        assertEquals(expected, result);

        expected = "file:///C:/dir/file.txt"; // Windows drive letter path
        result = URLChecker.prependFileScheme("file:/C:/dir/file.txt");
        assertEquals(expected, result);

        expected = "file:///dir/file.txt"; // Unix (absolute) path
        result = URLChecker.prependFileScheme(expected);
        assertEquals(expected, result);

        expected = "file:/moving_box.x3dv";
        result = URLChecker.prependFileScheme("/moving_box.x3dv"); // Unix path
        assertEquals(expected, result);

        expected = "https://moving_box.x3dv"; // http/https web path
        result = URLChecker.prependFileScheme(expected);
        assertEquals(expected, result);

        if (IS_WIN) {
            expected = "file:///C:/Users/terry/javaapis/org.web3d/Xj3D 2.3/parsetest/filter/AbsScale/square.x3dv";
            result = URLChecker.prependFileScheme("C:/Users/terry/javaapis/org.web3d/Xj3D 2.3/parsetest/filter/AbsScale/square.x3dv");
            assertEquals(expected, result);
        }

        expected = "file:///moving_box.x3dv";
        result = URLChecker.prependFileScheme("file:/moving_box.x3dv");
        assertEquals(expected, result);

        expected = "file:/some/parent/path/to/a/file/"; // append an ending '/'
        result = URLChecker.prependFileScheme("file:/some/parent/path/to/a/file");
        assertEquals(expected, result);
    }

    public static Test suite()
    {
        return new TestSuite(TestURLChecker.class);
    }

    public static void main (String[] args) {
        TestRunner.run (suite());
    }

}
