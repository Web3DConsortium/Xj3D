/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import java.util.*;
import java.awt.*;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.MalformedURLException;
import javax.swing.*;
import org.j3d.aviatrix3d.output.graphics.BaseSurface;

import org.web3d.x3d.sai.*;

import org.xj3d.sai.Xj3DBrowser;

/**
 * A standalone X3D viewer. This uses Xj3D to view X3D files.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class StandaloneX3DViewer extends JFrame {

    /** version id */
    private static final long serialVersionUID = 1L;

    /** The title name of our viewer */
    private static final String TITLE = "Xj3D Viewer";

    /** The X3D browser */
    private ExternalBrowser x3dBrowser;

    /** The current scene */
    private X3DScene mainScene;

    /** The X3DComponent in use */
    private X3DComponent x3dComp;

    /** The content pane */
    private Container contentPane;

    /** Browser parameters */
    private Map<String, Object> requestedParameters;

    @SuppressWarnings("unchecked")
    public StandaloneX3DViewer() {
        super(TITLE);

        // Setup browser parameters
        requestedParameters = new HashMap<String, Object>();
        requestedParameters.put("Xj3D_FPSShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown", Boolean.FALSE);
        requestedParameters.put("Xj3D_LocationPosition", "top");
        requestedParameters.put("Xj3D_LocationReadOnly", Boolean.FALSE);
        requestedParameters.put("Xj3D_OpenButtonShown", Boolean.FALSE);
        requestedParameters.put("Xj3D_ReloadButtonShown", Boolean.FALSE);
        requestedParameters.put("Xj3D_ShowConsole", Boolean.FALSE);
        requestedParameters.put("Xj3D_StatusBarShown", Boolean.TRUE);

        init(requestedParameters);

        setSize(800, 600);
        enableEvents(WINDOW_EVENT_MASK);
        setVisible(true);

        ((BaseSurface)x3dComp.getImplementation()).initContext();
    }

    private void init(Map<String, Object> params) {

        // Create an SAI component
        x3dComp = BrowserFactory.createX3DComponent(params);

        // Add the component to the UI
        contentPane = getContentPane();
        contentPane.add((Component) x3dComp);

        // Get an external browser
        x3dBrowser = x3dComp.getBrowser();

        ((Xj3DBrowser) x3dBrowser).setMinimumFrameInterval(40);
    }

    /**
     * Return the X3D component in use.
     *
     * @return The component
     */
    public X3DComponent getX3DComponent() {
        return x3dComp;
    }

    /**
     * Load a new scene. This will replace the currently loaded scene.
     * @param strURL
     */
    public void load(String strURL) {
        String baseFileURL;

        try {
            baseFileURL = (new File(strURL)).toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            writeErr(ex.getMessage());

            return;
        }

        mainScene = x3dBrowser.createX3DFromURL(new String[] { baseFileURL });
        x3dBrowser.replaceWorld(mainScene);
    }

    //----------------------------------------------------------------------------
    /**
     * write a error message to console
     * @param aStr line to be written to console
     */
    private void writeErr(String aStr) {
        System.err.println(aStr);
    }

    /**
     * write a line to console
     * @param aStr line to be written to console
     */
    private void writeLn(String aStr) {
        System.out.println(aStr);
    }

    /**
     * write a line to console
     */
    private void writeLn() {
        writeLn("");
    }

    //---------------------------------------------------------------
    // Methods defined by Window
    //---------------------------------------------------------------

    @Override
    protected void processWindowEvent(WindowEvent e) {

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {

            // Call these instead of System.exit(0);
            getX3DComponent().shutdown();
            dispose();
            mainScene = null;
            x3dBrowser = null;
            x3dComp = null;
            requestedParameters.clear();
            requestedParameters = null;

        } else if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            writeLn("Shutting down " + TITLE);
        }

        // Pass along all other events
        super.processWindowEvent(e);

    }
}
