/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import com.jogamp.opengl.*;
import com.jogamp.opengl.GLProfile;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

// Local imports
// None

/**
 * A sample chooser for selecting the right number of multisamples.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class SampleChooser {

    /** Message when we can't find a matching format for the capabilities */
    private static final String NO_PIXEL_FORMATS_MSG =
        "WARNING: antialiasing will be disabled because none of the " +
        "available pixel formats had it to offer";

    /** Message the caller didn't request antialiasing */
    private static final String NO_AA_REQUEST_MSG =
        "WARNING: antialiasing will be disabled because the " +
        "DefaultGLCapabilitiesChooser didn't supply it";

    /** The number of samples we've discovered */
    private int maxSamples = -1;

    private boolean anyHaveSampleBuffers = false;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Construct a new, default instance of this class.
     */
    SampleChooser() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        GLDrawableFactory fac = GLDrawableFactory.getFactory(GLProfile.getDefault());

        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setSampleBuffers(true);

        // Set a max to compare with reality
        caps.setNumSamples(16);

        GLAutoDrawable ad = fac.createDummyAutoDrawable(fac.getDefaultDevice(), true, caps, new DefaultGLCapabilitiesChooser());
        ad.display();
        caps = (GLCapabilities) ad.getChosenGLCapabilities();

        if (caps != null) {
            if (caps.getNumSamples() > maxSamples) {
                maxSamples = caps.getNumSamples();
            }
            anyHaveSampleBuffers = caps.getSampleBuffers();
        }

        if (maxSamples == -1) {
            errorReporter.messageReport(NO_PIXEL_FORMATS_MSG);
        } else {
            if (!anyHaveSampleBuffers) {
                errorReporter.messageReport(NO_AA_REQUEST_MSG);
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Ask for the number of samples detected. This is only valid after having
     * been run. Otherwise, it will return a value of -1.
     */
    int getMaxSamples() {
        return maxSamples;
    }
}

