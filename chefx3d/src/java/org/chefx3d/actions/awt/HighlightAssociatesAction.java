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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

// Application specific imports
import org.chefx3d.model.*;
import org.chefx3d.tool.Tool;

/**
 * An action that can be used to delete the selected entity
 * from the model
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class HighlightAssociatesAction extends AbstractAction
    implements ModelListener {

    /** Title when the wrapper does not have children shown currently */
    private static final String SHOW_TITLE = "Highlight Associated Entities";

    /** Title when the wrapper has children shown currently */
    private static final String HIDE_TITLE = "Unighlight Associated Entities";

    /** The world model */
    private WorldModel model;

    /** The ID of the selected Entity in the model. A
    * value of -1 means that no Entity is selected. */
    private int entityID = -1;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     */
    public HighlightAssociatesAction(boolean standAlone, Icon icon, WorldModel model) {

        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, SHOW_TITLE);
        }

        this.model = model;
        this.model.addModelListener(HighlightAssociatesAction.this);

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_H, 0);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_H);

        putValue(SHORT_DESCRIPTION, SHOW_TITLE);

        setEnabled(false);
    }

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     * @param entityID
     */
    public HighlightAssociatesAction(boolean standAlone, Icon icon,
            WorldModel model, int entityID) {

        this(standAlone, icon, model);
        this.entityID = entityID;

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

//System.out.println("HighlightAssociatesAction.actionPerformed()");
//System.out.println("    selectedID: " + entityID);

        if (entityID == -1) {
            return;
        }

        Entity entity = model.getEntity(entityID);

        int[] associateIDs = new int[0];
        if (entity instanceof AssociatableEntity) {
            associateIDs = ((AssociatableEntity)entity).getAssociates();
        }

        List<Selection> list = new ArrayList<>(associateIDs.length + 1);

        int segmentID = -1;
        int vertexID = -1;
        if (entity instanceof SegmentableEntity) {
            segmentID = ((SegmentableEntity)entity).getSelectedSegmentID();
            vertexID = ((SegmentableEntity)entity).getSelectedVertexID();
        }

        // add the current entity to the list
        Selection selection =
            new Selection(entityID, segmentID, vertexID);

        list.add(selection);

        // add each associate to the list
        for (int i = 0; i < associateIDs.length; i++) {
            selection = new Selection(associateIDs[i], -1, -1);
            list.add(selection);
        }

        model.changeSelection(list);

    }

    //----------------------------------------------------------
    // Methods required by the ModelListener interface
    //----------------------------------------------------------

    /**
     * Ignored.
     */
    @Override
    public void entityAdded(boolean local, Entity entity){
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
        int removedID = entity.getEntityID();
        if(removedID == entityID) {
            entityID = -1;
        }
    }

    /**
     * An entity was selected. Enable this action as necessary.
     *
     * @param selection The list of selected entities. The last one is the
     * latest.
     */
    @Override
    public void selectionChanged(List<Selection> selection) {
        Selection select = selection.get(selection.size() - 1);
        entityID = select.getEntityID();
        if (entityID == -1) {
            // disable when no entities are selected
            setEnabled(false);
        } else {
            Entity entity = model.getEntity(entityID);
            if (entity.getType() == Tool.TYPE_WORLD) {
                // disable if the 'world' entity is selected
                entityID = -1;
                setEnabled(false);
            } else {
                // otherwise......
                setEnabled(true);
            }
        }
    }

    /*
     * Ignored.
     */
    @Override
    public void viewChanged(boolean local, double[] pos, float[] rot, float fov) {
    }

    /*
     * Ignored.
     */
    @Override
    public void masterChanged(boolean local, long viewID) {
    }

    /*
     * Ignored.
     */
    @Override
    public void modelReset(boolean local) {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

}
