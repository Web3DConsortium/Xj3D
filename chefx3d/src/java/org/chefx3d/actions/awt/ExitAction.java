/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2007
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

package org.chefx3d.actions.awt;

// Standard library imports
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/**
 * An action that can be used to exit the system.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ExitAction extends AbstractAction {

    /**
     * Create an instance of the action class.
     */
    public ExitAction() {
        super("Exit");

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                   KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_X);
        putValue(SHORT_DESCRIPTION, "Exit this app");
    }

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        System.exit(0);
    }
}
