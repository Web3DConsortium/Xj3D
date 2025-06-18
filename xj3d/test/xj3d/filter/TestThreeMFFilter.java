/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
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

// External Imports

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.textui.TestRunner;

// Internal Imports
// None

/**
 * Test ThreeMF Filter
 *
 * @author Alan Hudson
 * @version $Revision: 1.0 $
 */
public class TestThreeMFFilter extends BaseTestFilter {

    /**
     * The filter argument name
     */
    public static final String FILTER_NAME = "Identity";

    /**
     * The directory containing the files to pass through the filter
     */
    public static final String PARSETEST = "parsetest/filter/threemf/";

    /**
     * The file types from the directory to use
     */
    public static final String[] EXTENSIONS = new String[]{".3mf"};

    /**
     * Creates a test suite consisting of all the methods that start with
     * "test".
     * @return
     */
    public static Test suite() {
        return new TestSuite(TestThreeMFFilter.class);
    }

    /**
     * Base test
     * <p>
     * This does nothing more than pass the test file set through the filter.
     * Whether the filter does what it's supposed to other than 'not crash', is
     * not determined.
     */
    public void testBasic() {

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        ExtFilter extFileFilter;
        File sourceDir;
        File[] sourceFiles;
        String file;
        int err_code;
        for (String ext : EXTENSIONS) {
            extFileFilter = new ExtFilter(ext);
            sourceDir = new File(PARSETEST);
            sourceFiles = sourceDir.listFiles(extFileFilter);
            if (sourceFiles == null) continue;
            for (File sourceFile : sourceFiles) {
                file = sourceFile.getPath();
                System.out.printf("Testing file: %s\n", file);
                try {
                    err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                            null, validArgs);

                    assertEquals(FILTER_NAME + ": Error code not 0: " + file, 0, err_code);

                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    fail(FILTER_NAME + ": IOError in parsing: " + file);
                }
            }
        }
    }

    /**
     * TODO: No real clue what this is testing.  Underlying geometry is a box so test for 1 shape
     */
    public void testChapter2_1_PartsRelationships() {
        String file = "MUSTPASS_Chapter2.1_PartsRelationships.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("Shape EQ 6");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * TODO: No real clue what this is testing.  Underlying geometry is a box so test for 1 shape
     */
    public void testChapter2_2_PartNaming() {
        String file = "MUSTPASS_Chapter2.2_PartNaming.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("Shape EQ 6");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * TODO: No real clue what this is testing.  Underlying geometry is a box so test for 1 shape
     */
    public void testChapter2_3a_IgnorableMarkup() {
        String file = "MUSTPASS_Chapter2.3a_IgnorableMarkup.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("Shape EQ 1");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * Test that a build can contain multiple items with transforms.
     */
    public void _testMUSTPASS_Chapter3_2c_MultipleItemsTransform() {
        String file = "MUSTPASS_Chapter3.2c_MultipleItemsTransform.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("Shape EQ 2");
        validArgs.add("-countNode2");
        validArgs.add("Transform EQ 3");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * Test that the unit of measure is followed.  Object's bounds should be in 10mm range.
     */
    public void testChapter3_4_3a_MustNotOutputNonReferencedObjects() {
        String file = "MUSTPASS_Chapter3.4.3a_MustNotOutputNonReferencedObjects.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-compareBounds");
        validArgs.add("LT 0.011 0.011 0.011");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.GlobalBoundsFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }


    /**
     * Tests that an object speced in the resource section but not referenced in the build section isn't output.  We
     * test this by making sure the number of shapes is exactly 1.
     */
    public void testMustNotOutputNonReferencedObjects() {
        String file = "MUSTPASS_Chapter3.4.3a_MustNotOutputNonReferencedObjects.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("Shape EQ 6");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * Test that the unit of measure is followed.  Object's bounds should be in 10mm range.
     */
    public void testComponents() {
        String file = "MUSTPASS_Chapter4.2_Components.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("MatrixTransform EQ 2");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * Start of volume slice testing, prototype currently
     * NOTE: volslice.3mf has errors (tdn) 9/22/24
     */
//    public void testVolSlice() {
//
//        String file = "volslice.3mf";
//
//        List<String> filters = new ArrayList<>();
//        filters.add(FILTER_NAME);
//
//        List<String> args = new ArrayList<>();
//
//        List<String> validArgs = new ArrayList<>();
//
//        String f = PARSETEST + File.separator + file;
//
//        validArgs.add("-countNode1");
//        validArgs.add("Shape EQ 1");
//
//        int err_code = 0;
//
//        try {
//            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
//                "xj3d.filter.CountFilterValidator", validArgs);
//        } catch (IOException ioe) {
//            ioe.printStackTrace(System.err);
//            fail(FILTER_NAME + ": IOError in parsing: " + f);
//        }
//        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
//    }

    /**
     * Test transformed Item work
     */
    public void testItemScaledCube() {
        String file = "ItemTransform.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);
        filters.add("Triangulation");
        filters.add("Index");
        filters.add("FlattenTransform");

        List<String> args = new ArrayList<>();
        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        int err_code = 0;

        validArgs.add("-compareBounds");
        validArgs.add("EQ 0.08 0.040 0.008");

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.GlobalBoundsFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * Test that a maximal resourceID works
     */
    public void testMaximalResourceID() {
        String file = "MaxResourceID.3mf";

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        String f = PARSETEST + File.separator + file;

        validArgs.add("-countNode1");
        validArgs.add("Shape EQ 1");

        int err_code = 0;

        try {
            err_code = executeFilter(filters, args, f, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            fail(FILTER_NAME + ": IOError in parsing: " + f);
        }
        assertEquals(FILTER_NAME + ": Error code not 0: " + f, 0, err_code);
    }

    /**
     * Main method to kick everything off with.
     * @param argv
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }
}
