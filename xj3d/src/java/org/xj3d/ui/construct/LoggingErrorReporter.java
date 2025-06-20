/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.construct;

// External imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.UnknownHostException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.util.Xj3dVersionInformation;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.sav.VRMLParseException;

/**
 * An implementation of the ErrorReporter interface that writes all reports
 * to a log file.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class LoggingErrorReporter implements ErrorReporter {

    /** Messaging strings */
    private static final String MESSAGE = "Message: ";
    private static final String WARNING = "Warning: ";
    private static final String ERROR = "Error: ";
    private static final String FATAL_ERROR = "Fatal Error: ";
    private static final String LINE = " Line: ";
    private static final String COLUMN = " Column: ";
    private static final char NL = '\n';

    /** The set of exceptions to ignore the stack trace for */
    private Set<Class<?>> ignoredExceptionTypes;

    /** The output file writer */
    private BufferedWriter writer;

    /** Should messages be logged */
    private boolean logMessage = true;

    /** Should warnings be logged */
    private boolean logWarning = true;

    /** Should errors be logged */
    private boolean logError = true;

    /** Should fatal errors be logged */
    private boolean logFatalError = true;

    /** Reusable output message buffer */
    private StringBuffer buf;

    /**
     * Constructor. Create a new instance of the reporter
     * with all reporting levels enabled.
     *
     * @param log_file The file to log to.
     */
    public LoggingErrorReporter( File log_file ) {
        this( log_file, true, true, true, true );
    }

    /**
     * Constructor
     *
     * @param log_file The file to log to.
     * @param logMessage Set whether messages should be logged
     * @param logWarning Set whether warnings should be logged
     * @param logError Set whether errors should be logged
     * @param logFatalError Set whether fatal errors should be logged
     */
    public LoggingErrorReporter(
            File log_file,
            boolean logMessage,
            boolean logWarning,
            boolean logError,
        boolean logFatalError ) {

        try {
            writer = new BufferedWriter( new FileWriter( log_file, true ) );
        } catch ( IOException ioe ) {
            System.err.println( "LoggingErrorReporter: could not create log file: "+ log_file );
        }

        buf = new StringBuffer();

        setReportingLevels( logMessage, logWarning, logError, logFatalError );

        messageReport(new Date().toString());
        messageReport("Xj3D Xj3dVersionInformation: " + Xj3dVersionInformation.XJ3D_VERSION);

        ignoredExceptionTypes = new HashSet<>();
        ignoredExceptionTypes.add(InvalidFieldException.class);
        ignoredExceptionTypes.add(InvalidFieldFormatException.class);
        ignoredExceptionTypes.add(InvalidFieldValueException.class);
        ignoredExceptionTypes.add(FileNotFoundException.class);
        ignoredExceptionTypes.add(IOException.class);
        ignoredExceptionTypes.add(UnknownHostException.class);
        ignoredExceptionTypes.add(IllegalArgumentException.class);
        ignoredExceptionTypes.add(InvalidFieldConnectionException.class);
        ignoredExceptionTypes.add(VRMLParseException.class);
        ignoredExceptionTypes.add(UnsupportedComponentException.class);
        ignoredExceptionTypes.add(UnsupportedNodeException.class);
        ignoredExceptionTypes.add(UnsupportedProfileException.class);
        ignoredExceptionTypes.add(ClassNotFoundException.class);
    }

    //---------------------------------------------------------------
    // Methods defined by ErrorReporter
    //---------------------------------------------------------------

    @Override
    public void partialReport(String msg) {
        if ( writer != null ) {
            try {
                writer.write( msg, 0, msg.length( ) );
                writer.flush( );
            } catch ( IOException ioe ) {
            }
        }
    }

    @Override
    public final void messageReport(String msg) {
        if ( ( writer != null ) && logMessage ) {
            try {
                writer.write( MESSAGE, 0, MESSAGE.length( ) );
                writer.write( msg, 0, msg.length( ) );
                writer.newLine( );
                writer.flush( );
            } catch ( IOException ioe ) {
            }
        }
    }

    @Override
    public void warningReport(String msg, Throwable e) {
        if ( ( writer != null ) && logWarning ) {
            buf.append(WARNING);
            if(e instanceof VRMLParseException) {
                buf.append(LINE);
                buf.append(((VRMLParseException)e).getLineNumber());
                buf.append(COLUMN);
                buf.append(((VRMLParseException)e).getColumnNumber());
                buf.append(NL);
            } else if(e instanceof InvalidFieldFormatException) {
                buf.append(LINE);
                buf.append(((InvalidFieldFormatException)e).getLineNumber());
                buf.append(COLUMN);
                buf.append(((InvalidFieldFormatException)e).getColumnNumber());
                buf.append(NL);
            }

            if(msg != null) {
                buf.append(msg);
                buf.append(NL);
            }

            if(e != null) {
                String txt = e.getMessage();
                if(txt == null)
                    txt = e.getClass().getName();

                buf.append(txt);
                buf.append(NL);

                if(!ignoredExceptionTypes.contains(e.getClass())) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    buf.append(sw.toString());
                }
            }
            try {
                String output = buf.toString( );
                writer.write( output, 0, output.length( ) );
                writer.newLine( );
                writer.flush( );
            } catch ( IOException ioe ) {
            }

            // Clear the buffer
            buf.setLength(0);
        }
    }

    @Override
    public void errorReport(String msg, Throwable e) {
        if ( ( writer != null ) && logError ) {
            buf.append(ERROR);
            if(e instanceof VRMLParseException) {
                buf.append(LINE);
                buf.append(((VRMLParseException)e).getLineNumber());
                buf.append(COLUMN);
                buf.append(((VRMLParseException)e).getColumnNumber());
                buf.append(NL);
            } else if(e instanceof InvalidFieldFormatException) {
                buf.append(LINE);
                buf.append(((InvalidFieldFormatException)e).getLineNumber());
                buf.append(COLUMN);
                buf.append(((InvalidFieldFormatException)e).getColumnNumber());
                buf.append(NL);
            }

            if(msg != null) {
                buf.append(msg);
                buf.append(NL);
            }

            if(e != null) {
                String txt = e.getMessage();
                if(txt == null)
                    txt = e.getClass().getName();

                buf.append(txt);
                buf.append(NL);

                if(!ignoredExceptionTypes.contains(e.getClass())) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    buf.append(sw.toString());
                }
            }
            try {
                String output = buf.toString( );
                writer.write( output, 0, output.length( ) );
                writer.newLine( );
                writer.flush( );
            } catch ( IOException ioe ) {
            }

            // Clear the buffer
            buf.setLength(0);
        }
    }

    @Override
    public void fatalErrorReport(String msg, Throwable e) {
        if ( ( writer != null ) && logFatalError ) {
            buf.append(FATAL_ERROR);
            if(e instanceof VRMLParseException) {
                buf.append(LINE);
                buf.append(((VRMLParseException)e).getLineNumber());
                buf.append(COLUMN);
                buf.append(((VRMLParseException)e).getColumnNumber());
                buf.append(NL);
            } else if(e instanceof InvalidFieldFormatException) {
                buf.append(LINE);
                buf.append(((InvalidFieldFormatException)e).getLineNumber());
                buf.append(COLUMN);
                buf.append(((InvalidFieldFormatException)e).getColumnNumber());
                buf.append(NL);
            }

            if(msg != null) {
                buf.append(msg);
                buf.append(NL);
            }

            if(e != null) {
                String txt = e.getMessage();
                if(txt == null)
                    txt = e.getClass().getName();

                buf.append(txt);
                buf.append(NL);

                if(!ignoredExceptionTypes.contains(e.getClass())) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    buf.append(sw.toString());
                }
            }
            try {
                String output = buf.toString( );
                writer.write( output, 0, output.length( ) );
                writer.newLine( );
                writer.flush( );
            } catch ( IOException ioe ) {
            }

            // Clear the buffer
            buf.setLength(0);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /** Close the log file */
    public void close( ) {
        if ( writer != null ) {
            try {
                writer.close( );
            } catch ( IOException ioe ) {
            }
        }
    }

    /**
     * Configure the reporting levels
     *
     * @param logMessage Set whether messages should be logged
     * @param logWarning Set whether warnings should be logged
     * @param logError Set whether errors should be logged
     * @param logFatalError Set whether fatal errors should be logged
     */
    public final void setReportingLevels(
            boolean logMessage,
            boolean logWarning,
            boolean logError,
        boolean logFatalError ) {

        this.logMessage = logMessage;
        this.logWarning = logWarning;
        this.logError = logError;
        this.logFatalError = logFatalError;
    }
}
