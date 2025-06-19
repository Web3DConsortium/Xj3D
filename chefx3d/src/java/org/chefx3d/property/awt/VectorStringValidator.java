/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property.awt;

// External Imports
import org.w3c.dom.UserDataHandler;

// Internal Imports
import org.chefx3d.property.DataValidator;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An editor that add's properties to the tree.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class VectorStringValidator extends DataValidator
    implements UserDataHandler {

    public static enum numberTypes {INTEGER, LONG, FLOAT, DOUBLE};

    /** The type to validate as */
    private numberTypes type;

    /** The message to return to the user */
    private String message;

    /**
     * Create a VectorStringValidator that will check that a
     * vector string "x y z" are all valid numeric types
     *
     * @param type
     */
    public VectorStringValidator(numberTypes type) {
        this.type = type;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    // ----------------------------------------------------------
    // Methods required by the DataValidator
    // ----------------------------------------------------------

    @Override
    public boolean validate(String value) {

        String[] parts = value.split(" ");
        if (parts.length != 3) {
            message = "Data Validation Error:\n The data provided [" + value +
            "] does not contain 3 numbers separated by a space.";
            return false;
        }

        switch(type) {
        case INTEGER:
            try {
                Integer.getInteger(parts[0]);
                Integer.getInteger(parts[1]);
                Integer.getInteger(parts[2]);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n One or more of the the numbers provided [" + value + "] are not Integers";
                return false;
            }
            break;
        case LONG:
            try {
                Long.getLong(parts[0]);
                Long.getLong(parts[1]);
                Long.getLong(parts[2]);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n One or more of the the numbers provided [" + value + "] are not Longs";
                return false;
            }
            break;
        case FLOAT:
            try {
                Float.parseFloat(parts[0]);
                Float.parseFloat(parts[1]);
                Float.parseFloat(parts[2]);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n One or more of the the numbers provided [" + value + "] are not Floats";
                return false;
            }
            break;
        case DOUBLE:
            try {
                Double.parseDouble(parts[0]);
                Double.parseDouble(parts[1]);
                Double.parseDouble(parts[2]);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n One or more of the the numbers provided [" + value + "] are not Doubles";
                return false;
            }
            break;
        }

        return true;

    }

    @Override
    public String getMessage() {
        return message;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

}