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
public class IsNumberValidator extends DataValidator
    implements UserDataHandler {

    public static enum numberTypes {INTEGER, LONG, FLOAT, DOUBLE};

    /** The type to validate as */
    private numberTypes type;

    /** The message to return to the user */
    private String message;

    /**
     * Create a IsNumberValidator that will check that a
     * value is the type of number specified.
     *
     * @param type
     */
    public IsNumberValidator(numberTypes type) {
        this.type = type;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    // ----------------------------------------------------------
    // Methods required by the DataValidator
    // ----------------------------------------------------------

    @Override
    public boolean validate(String value) {

        switch(type) {
        case INTEGER:
            try {
                Integer.getInteger(value);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n The number provided [" + value + "] is not an Integer";
                return false;
            }
            break;
        case LONG:
            try {
                Long.getLong(value);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n The number provided [" + value + "] is not a Long";
                return false;
            }
            break;
        case FLOAT:
            try {
                Float.parseFloat(value);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n The number provided [" + value + "] is not a Float";
                return false;
            }
            break;
        case DOUBLE:
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                message = "Data Validation Error:\n The number provided [" + value + "] is not a Double";
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