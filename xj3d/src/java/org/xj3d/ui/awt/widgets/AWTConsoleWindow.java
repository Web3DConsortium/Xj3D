/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// Local imports
import org.web3d.util.Xj3dVersionInformation;
import org.web3d.vrml.sav.ErrorHandler;
import org.web3d.vrml.sav.DocumentLocator;

/**
 * A window that can act as console for error messages from the application.
 * <p>
 *
 * The window will print error messages for all the error levels and only
 * throw an exception for the fatalError.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class AWTConsoleWindow extends Frame implements ActionListener, ErrorHandler
{
    /** The textfield to put the error information in */
    private TextArea errorField;

    /** Clear button to remove stuff from window */
    private Button clearButton;

    /** Issues button to launch issues list */
    private Button issuesButton;

    /** DocumentLocator used for printing out line/column information */
    private DocumentLocator docLocator;

    /**
     * Create an instance of the console window.
     */
    public AWTConsoleWindow()
    {
        super("Xj3D Console");

        setLayout(new BorderLayout());

        errorField = new TextArea(20, 60);

        add(errorField, BorderLayout.CENTER);

        Panel p1 = new Panel(new FlowLayout());
        add(p1, BorderLayout.SOUTH);

        clearButton = new Button("Clear");
        clearButton.addActionListener(AWTConsoleWindow.this);
        p1.add(clearButton);

        issuesButton = new Button("Issues list");
        issuesButton.addActionListener(AWTConsoleWindow.this);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            p1.add(issuesButton);
        else System.out.println("Unable to support issues button, this installation does not support Java Desktop");

        setSize(800, 600); // wide enough for typical paths
        setLocation(80, 80);

        messageReport("Xj3D Xj3dVersionInformation: " + Xj3dVersionInformation.XJ3D_VERSION);
    }

    //----------------------------------------------------------
    // Methods defined by ErrorHandler
    //----------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <code>startDocument()</code> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    @Override
    public void setDocumentLocator(DocumentLocator loc) {
        docLocator = loc;
    }

    //----------------------------------------------------------
    // Methods defined by ErrorReporter
    //----------------------------------------------------------

    /**
     * Notification of an partial message from the system. When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    @Override
    public void partialReport(String msg) {
        errorField.append(msg);
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    @Override
    public final void messageReport(String msg) {
        StringBuilder buf = new StringBuilder(msg);
        buf.append('\n');

        errorField.append(buf.toString());
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    @Override
    public void warningReport(String msg, Throwable e) {
        StringBuilder buf = new StringBuilder("Warning: ");

        if(docLocator != null) {
            buf.append(" in X3D scene, Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String e_msg = e.getMessage();

            if(e_msg != null || e_msg.length() != 0) {
                buf.append(e.getMessage());
                buf.append('\n');
            } else
                buf.append("No message provided\n");
            }

        errorField.append(buf.toString());
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to add a route to a non-existent node or the
     * use of a node that the system cannot find the definition of.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    @Override
    public void errorReport(String msg, Throwable e) {
        StringBuilder buf = new StringBuilder("Error: ");

        if(docLocator != null) {
            buf.append(" in X3D scene, Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String e_msg = e.getMessage();

            if(e_msg != null || e_msg.length() != 0) {
                buf.append(e.getMessage());
                buf.append('\n');
            } else
                buf.append("No message provided\n");
            }

        errorField.append(buf.toString());
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you receive this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    @Override
    public void fatalErrorReport(String msg, Throwable e) {
        StringBuilder buf = new StringBuilder("Fatal Error: ");

        if(docLocator != null) {
            buf.append(" in X3D scene, Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String e_msg = e.getMessage();

            if(e_msg != null || e_msg.length() != 0) {
                buf.append(e.getMessage());
                buf.append('\n');
            } else
                buf.append("No message provided\n");
            }

        errorField.append(buf.toString());
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    /**
     * Process an action event that came from the user interface.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();

        if      (src == clearButton)
        {
            errorField.setText("");
        }
        else if (src == issuesButton)
        {
            String issuesUrl = "https://gitlab.nps.edu/Savage/xj3d/issues";
            // https://stackoverflow.com/questions/5226212/how-to-open-url-in-default-webbrowser-using-java
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            {
                try
                {
                    URI issuesURI = new URI(issuesUrl);
                    Desktop.getDesktop().browse(issuesURI);
                }
                catch (IOException | URISyntaxException e)
                {
                    System.err.println("Unable to connect to " + issuesUrl);
                }
            }
        }
    }
    }
