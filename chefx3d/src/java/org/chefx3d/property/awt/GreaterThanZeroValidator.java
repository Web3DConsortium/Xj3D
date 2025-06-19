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
public class GreaterThanZeroValidator extends DataValidator
    implements UserDataHandler {

    public static enum numberTypes {INTEGER, LONG, FLOAT, DOUBLE};

    /** The type to validate as */
    private numberTypes type;

    /** The value to check */
    private double checkValue;

    /** The message to return to the user */
    private String message;

    /**
     * Create a GreaterThanZeroValidator that will check that a
     * value is greater than zero but less then the max value
     * of the type specified.
     *
     * @param type
     */
    public GreaterThanZeroValidator(numberTypes type) {
        this.type = type;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    // ----------------------------------------------------------
    // Methods required by the DataValidator
    // ----------------------------------------------------------
    @Override
    public boolean validate(String value) {

        try {
            checkValue = Double.parseDouble(value);

            switch(type) {
            case INTEGER:
                if (checkValue <= Integer.MAX_VALUE && checkValue > 0) {
                    return true;
                } else {
                    message = "Data Validation Error:\n The number provided [" + value +
                        "] is not greater then zero and less than the maximum size for and Integer.";
                    return false;
                }
            case LONG:
                if (checkValue <= Long.MAX_VALUE && checkValue > 0) {
                    return true;
                } else {
                    message = "Data Validation Error:\n The number provided [" + value +
                        "] is not greater then zero and less than the maximum size for a Long.";
                    return false;
                }
            case FLOAT:
                if (checkValue <= Float.MAX_VALUE && checkValue > 0) {
                    return true;
                } else {
                    message = "Data Validation Error:\n The number provided [" + value +
                        "] is not greater then zero and less than the maximum size for a Float.";
                    return false;
                }
            case DOUBLE:
                if (checkValue <= Double.MAX_VALUE && checkValue > 0) {
                    return true;
                } else {
                    message = "Data Validation Error:\n The number provided [" + value +
                        "] is not greater then zero and less than the maximum size for a Double.";
                    return false;
                }
            }

        } catch (NumberFormatException nfe) {
            message = "Data Validation Error:\n The data provided [" + value + "] must be a number.";
            return false;
        }

        return true;

    }

    /**
     * The message the user will see if failed
     * @return
     */
    @Override
    public String getMessage() {
        return message;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

}