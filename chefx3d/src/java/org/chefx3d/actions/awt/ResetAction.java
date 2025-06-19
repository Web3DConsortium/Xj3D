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

//Standard library imports
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.*;

// Application specific imports
import org.chefx3d.model.*;

/**
 * An action that can be used to delete the selected entity
 * from the model
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.1 $
 */
public class ResetAction extends AbstractAction
    implements ModelListener {

    /** The world model */
    private WorldModel model;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     */
    public ResetAction(boolean standAlone, Icon icon, WorldModel model) {
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Delete All");
        }

        this.model = model;
        this.model.addModelListener(ResetAction.this);

        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        //putValue(ACCELERATOR_KEY, acc_key);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));

        putValue(SHORT_DESCRIPTION, "Delete All");

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

        CommandUtils util = new CommandUtils(model);
        util.resetModel(true, null);

        setEnabled(false);
    }

    //----------------------------------------------------------
    // Methods required by the ModelListener interface
    //----------------------------------------------------------

    /**
     * Ignored.
     */
    @Override
    public void entityAdded(boolean local, Entity entity){
        setEnabled(true);
    }

    /**
     * An entity was removed. If the removed Entity was selected,
     * disable the copy function until a new Entity is selected.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The entity being removed from the view
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {

        Entity[] entities = model.getModelData();
        Entity check;

        setEnabled(false);
        for (Entity entitie : entities) {
            check = entitie;
            if (check != null) {
                setEnabled(true);
                break;
            }
        }

     }

    /**
     * Ignored.
     */
    @Override
    public void selectionChanged(List<Selection> selection) {
    }

    /**
     * Ignored.
     */
    @Override
    public void viewChanged(boolean local, double[] pos, float[] rot, float fov) {
    }

    /**
     * Ignored.
     */
    @Override
    public void masterChanged(boolean local, long viewID) {
    }

    /**
     * Ignored.
     */
    @Override
    public void modelReset(boolean local) {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

}
