/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property;

// External Imports
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

// Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * Specification of a data editor and its parameters for a field.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public abstract class DataValidator
    implements Cloneable, UserDataHandler {

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;


    // ----------------------------------------------------------
    // Methods required by UserDataHandler interface
    // ----------------------------------------------------------

    @Override
    public void handle(short operation, String key, Object data, Node src,
            Node dst) {

        switch (operation) {
        case UserDataHandler.NODE_CLONED:
        case UserDataHandler.NODE_ADOPTED:
        case UserDataHandler.NODE_IMPORTED:
            try {

                //System.out.println("UserDataHandler.handle");
                //System.out.println("    key: " + key);


                DataValidator[] origValidator = (DataValidator[]) data;
                int len = origValidator.length;

                //System.out.println("    len: " + len);

                DataValidator[] newValidator = new DataValidator[len];

                for (int i = 0; i < len; i++) {
                    newValidator[i] = (DataValidator) origValidator[i]
                            .clone();
                }

                dst.setUserData(key, newValidator, this);

            } catch (CloneNotSupportedException e) {
                // shouldn't happen
                e.printStackTrace();
            }
            break;
        default:
            System.err.println("*** Unhandled type: " + operation);
        }
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Validate the value against a defined test
     *
     * @param value The value to check
     * @return True if check is valid.
     */
    public abstract boolean validate(String value);

    /**
     * What message should the user see if this fails
     *
     * @return The message
     */
    public abstract String getMessage();

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}