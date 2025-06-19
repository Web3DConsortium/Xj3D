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

import javax.swing.*;

// Application specific imports
import org.chefx3d.model.*;

/**
 * An action that can be used to redo the last modification to
 * the model.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class RedoAction extends AbstractAction
    implements CommandListener {

    /** The Command Controller */
    private CommandController controller;

    /** Is this standalone or in a menu */
    private boolean standAlone;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param controller The controller managing commands
     */
    public RedoAction(boolean standAlone, Icon icon, CommandController controller) {

        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Redo");
        }

        this.controller = controller;
        this.controller.addCommandHistoryListener(RedoAction.this);

        this.standAlone = standAlone;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                                                   KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_R);

        putValue(SHORT_DESCRIPTION, "Redo");

        setEnabled(false);
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
        controller.redo();
    }

    //----------------------------------------------------------
    // Methods required by the CommandListener interface
    //----------------------------------------------------------

    /**
     * A command was successfully executed
     */
    @Override
    public void commandExecuted() {
        updateRedoMenu();
    }

    /**
     * A command was successfully undone
     */
    @Override
    public void commandUndone() {
        updateRedoMenu();
    }

    /**
     * A command was successfully redone
     */
    @Override
    public void commandRedone() {
        updateRedoMenu();
    }

    /**
     * The command stack was cleared
     */
    @Override
    public void commandCleared() {
        updateRedoMenu();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Enable redo menu when here is something in the command
     * history to redo.  Otherwise disable.  Set the description
     * as appropriate.
     */
    private void updateRedoMenu() {

        if (controller.canRedo()) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }

        if (!standAlone) {
            if (controller.canRedo()) {
                putValue(Action.NAME, "Redo " + controller.getRedoDescription());
            } else {
                putValue(Action.NAME, "Redo");
            }
        }

    }
}
