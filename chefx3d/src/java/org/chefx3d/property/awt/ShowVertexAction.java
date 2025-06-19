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

package org.chefx3d.property.awt;

// Standard library imports
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

// Application specific imports
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An action that can be used to toggle the vertex panel off/on
 * <p>
 * 
 * @author Russell Dodds
 * @version $Revision: 1.2 $
 */
public class ShowVertexAction extends AbstractAction {

    private TabMenuItems editorPanel;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * Create an instance of the action class.
     * 
     * @param editorPanel
     */
    public ShowVertexAction(TabMenuItems editorPanel) {

        super("Show Vertex Placement", null);

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        
        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_V);

        putValue(SHORT_DESCRIPTION, "Show Vertex Placement Tab");

        this.editorPanel = editorPanel;

    }

    // ----------------------------------------------------------
    // Methods required by the ActionListener interface
    // ----------------------------------------------------------

    /**
     * An action has been performed.
     * 
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

        if (editorPanel.isTabVisible(TabMenuItems.TYPE_VERTEX)) {

            editorPanel.hideTab(TabMenuItems.TYPE_VERTEX);

        } else {

            editorPanel.showTab(TabMenuItems.TYPE_VERTEX);

        }

    }

}
