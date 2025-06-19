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
public class InRangeValidator extends DataValidator
    implements UserDataHandler {

    /** The start value */
    private double start;

    /** The end value */
    private double end;

    /** The value to check */
    private double checkValue;

    /** The message to return to the user */
    private String message;

    /**
     * Create a InRangeValidator that will check that a
     * value is within a specified range.
     *
     * @param start
     * @param end
     */
    public InRangeValidator(double start, double end) {
        this.start = start;
        this.end = end;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    // ----------------------------------------------------------
    // Methods required by the DataValidator
    // ----------------------------------------------------------
    @Override
    public boolean validate(String value) {

        try {
            checkValue = Double.parseDouble(value);

            if (checkValue <= end && checkValue >= start) {
                return true;
            } else {
                message = "Data Validation Error:\n The number provided [" + value +
                    "] does not fall in the valid range [" + start + " -> " + end + "].";
                return false;
            }

        } catch (NumberFormatException nfe) {
            message = "Data Validation Error:\n The data provided [" + value + "] must be a number.";
            return false;
        }

    }

    @Override
    public String getMessage() {
        return message;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

}