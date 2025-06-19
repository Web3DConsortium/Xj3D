/*
Copyright (c) 2005-2006 Yumetech.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.MovesInstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package org.chefx3d.actions.awt;

// Standard library imports
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

// Application specific imports
import org.chefx3d.model.*;
import org.chefx3d.view.*;
import org.chefx3d.view.awt.AWTViewFactory;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

/**
 * An action that can be used to launch the 3D viewer.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class View3DAction extends AbstractAction implements WindowListener {

    /** The world model */
    protected WorldModel model;

    /** The view manager */
    protected ViewManager viewManager;

    /** The view */
    protected ViewX3D view;

    /** The external viewer */
    protected JFrame externalViewer;

    /** The device to display on */
    protected GraphicsDevice device;

    /** The intialWorld to load */
    protected String intialWorld;

    /** The image directory */
    protected String imageDirectory;

    /** The display name for the frame */
    protected String title;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     * @param vmanager The view manager
     * @param device The device to display on
     * @param intialWorld The intialWorld to load
     * @param imageDirectory The image directory
     * @param title The display name for the frame
     */
    public View3DAction(boolean standAlone, Icon icon, WorldModel model,
        ViewManager vmanager, GraphicsDevice device, String intialWorld,
        String imageDirectory, String title) {

        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "View3D");
        }

        this.model = model;
        viewManager = vmanager;
        this.device = device;
        this.intialWorld = intialWorld; //  build/catalog/InitialWorld.x3dv
        this.imageDirectory = imageDirectory; //  build/images
        this.title = title; //  Savage Studio - Perspective View

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_V);

        putValue(SHORT_DESCRIPTION, "View 3D");
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener interface
    //----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        show();
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     * @param evt a WindowEvent
     */
    @Override
    public void windowActivated(WindowEvent evt) {
    }

    /**
     * Ignored
     * @param evt a WindowEvent
     */
    @Override
    public void windowClosed(WindowEvent evt) {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowClosing(WindowEvent evt) {
        close();
    }

    /**
     * Ignored
     * @param evt a WindowEvent
     */
    @Override
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * Ignored
     * @param evt a WindowEvent
     */
    @Override
    public void windowDeiconified(WindowEvent evt) {
    }

    /**
     * Ignored
     * @param evt a WindowEvent
     */
    @Override
    public void windowIconified(WindowEvent evt) {
    }

    /**
     * When the window is opened, start everything up.
     * @param evt a WindowEvent
     */
    @Override
    public void windowOpened(WindowEvent evt) {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
    
    public void show() {

        if (view == null) {
            Map<String, String> params = new HashMap<>();
            params.put(ViewFactory.PARAM_INITIAL_WORLD, intialWorld);
            params.put(ViewFactory.PARAM_IMAGES_DIRECTORY, imageDirectory);
            view = (ViewX3D) (new AWTViewFactory()).createView(model, ViewFactory.PERSPECTIVE_X3D_VIEW, params);

            externalViewer = new JFrame(title, device.getDefaultConfiguration());

            externalViewer.setSize(1_024,768);
            Container cp = externalViewer.getContentPane();
            cp.add((Component)view.getComponent(), BorderLayout.CENTER);
            externalViewer.setVisible(true);

            ((GraphicsOutputDevice)view.getX3DComponent().getImplementation()).createGLContext(); // ?? was initContext();

            externalViewer.addWindowListener(this);

            viewManager.addView(view);
            // TODO: Need to have the view catchup with the model
        } else {
            externalViewer.setVisible(true);
        }
    }

    /**
     * Close this window.
     */
    public void close() {
        if (view != null) {
            view.shutdown();

            viewManager.removeView(view);
            view = null;
            externalViewer.setVisible(false);
            externalViewer = null;
        }
    }

    /**
     * Return the view.
     *
     * @return the view. If not instantiated, null is returned.
     */
    public ViewX3D getView() {
        return(view);
    }
}
