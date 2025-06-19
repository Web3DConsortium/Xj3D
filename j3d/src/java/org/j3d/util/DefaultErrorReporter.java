/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
// none

// Local imports
// none

/**
 * An implementation of the ErrorReporter interface that just writes everything
 * to System.out.
 * <p>
 *
 * The default implementation to be used as convenience code for when the end
 * user has not supplied their own instance. By default, any class in this
 * repository that can be given an instance of the handler will use this
 * class, if none are given.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DefaultErrorReporter implements ErrorReporter
{

    /** Global singleton instance. */
    private static DefaultErrorReporter instance;

    /**
     * Creates a new, default instance of the reporter
     */
    public DefaultErrorReporter()
    {
    }

    /**
     * Fetch the common global instance of the reporter.
     *
     * @return The global instance
     */
    public static ErrorReporter getDefaultReporter()
    {
        if(instance == null)
            instance = new DefaultErrorReporter();

        return instance;
    }

    //-----------------------------------------------------------------------
    // Methods defined by ErrorReporter
    //-----------------------------------------------------------------------

    @Override
    public void partialReport(String msg)
    {
        System.out.print(msg);
    }

    @Override
    public void messageReport(String msg)
    {
        System.out.print("Message: ");
        System.out.println(msg);
    }

    @Override
    public void warningReport(String msg, Throwable th)
    {
        System.out.print("Warning: ");
        System.out.println(msg);

        if(th != null)
        {
            System.out.println("Contained message: ");
            System.out.println(th.getMessage());
            th.printStackTrace();
        }
    }

    @Override
    public void errorReport(String msg, Throwable th)
    {
        System.err.print("Error: ");
        System.err.println(msg);

        if(th != null)
        {
            System.err.println("Contained message: ");
            System.err.println(th.getMessage());
            th.printStackTrace();
        }
    }

    @Override
    public void fatalErrorReport(String msg, Throwable th)
    {
        System.err.print("Fatal Error: ");
        System.err.println(msg);

        if(th != null)
        {
            System.err.println("Contained message: ");
            System.err.println(th.getMessage());
            th.printStackTrace();
        }
    }
}
