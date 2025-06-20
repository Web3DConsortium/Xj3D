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

package org.chefx3d.property.awt;

// External imports
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

// Local imports
import org.chefx3d.model.WorldModel;

/**
 * An action that can be used to launch the costing panel in an external
 * window.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ViewCostReportAction extends AbstractAction implements WindowListener {

    /** The external report viewer */
    private JFrame externalReportViewer;

    /** The World model */
    private WorldModel model;

    /**
     * Create an instance of the action class.
     *
     * @param model The world model
     */
    public ViewCostReportAction(WorldModel model) {

        this.model = model;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_R);

        putValue(Action.NAME, "View Cost Report");
        putValue(SHORT_DESCRIPTION, "View Cost Report");

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
     * @param evt
     */
    @Override
    public void windowActivated(WindowEvent evt) {
    }

    /**
     * Ignored
     * @param evt
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
     * @param evt
     */
    @Override
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * Ignored
     * @param evt
     */
    @Override
    public void windowDeiconified(WindowEvent evt) {
    }

    /**
     * Ignored
     * @param evt
     */
    @Override
    public void windowIconified(WindowEvent evt) {
    }

    /**
     * When the window is opened, start everything up.
     * @param evt
     */
    @Override
    public void windowOpened(WindowEvent evt) {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
    public void show() {
        // Now define the external tree viewer
        externalReportViewer = new JFrame("Cost Report");
        externalReportViewer.setSize(300, 600);
        Container cpView = externalReportViewer.getContentPane();

        JPanel reportPanel = new CostingPanel(model);
        cpView.add(reportPanel, BorderLayout.CENTER);

        externalReportViewer.setVisible(true);
    }

    /**
     * Close this window.
     */
    public void close() {
        externalReportViewer.dispose();
    }
}
