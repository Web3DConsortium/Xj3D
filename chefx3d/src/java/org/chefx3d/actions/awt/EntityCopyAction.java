/*
Copyright (c) 2007 Yumetech.  All rights reserved.

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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import java.util.List;

// Application specific imports
import org.chefx3d.model.Entity;
import org.chefx3d.model.DefaultEntity;
import org.chefx3d.model.ModelListener;
import org.chefx3d.model.Selection;
import org.chefx3d.model.WorldModel;

import org.chefx3d.tool.Tool;

/**
 * An action that can be used to create a copy of a selected Entity from the
 * model.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class EntityCopyAction extends AbstractAction implements
        EntityCopyBuffer, ModelListener {

    /**
     * The world model
     */
    private WorldModel model;

    /**
     * The entity to duplicate
     */
    private Entity entityToCopy;

    /**
     * The ID of the selected Entity in the model. A value of -1 means that no
     * Entity is selected.
     */
    private int selectedID = -1;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     */
    public EntityCopyAction(boolean standAlone, Icon icon, WorldModel model) {
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Copy");
        }

        this.model = model;
        this.model.addModelListener(EntityCopyAction.this);

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);

        putValue(SHORT_DESCRIPTION, "Copy");

        setEnabled(false);
    }

	//----------------------------------------------------------
    // Methods required by the ActionListener interface
    //----------------------------------------------------------
    /**
     * An action has been performed. Copy the selected entity.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (selectedID != -1) {
            entityToCopy = model.getEntity(selectedID);
        }
    }

	//----------------------------------------------------------
    // Methods required by the EntityCopyBuffer interface
    //----------------------------------------------------------
    /**
     * Return whether the buffer contains an Entity.
     *
     * @return true if the buffer contains an Entity, false otherwise.
     */
    @Override
    public boolean hasEntity() {
        return (entityToCopy != null);
    }

    /**
     * Return a copy of the Entity in the buffer. The returned Entity will have
     * a new unique ID, but will otherwise be identical to the buffered Entity.
     *
     * @return The Entity that has been copied. If no Entity is in the buffer,
     * null is returned.
     */
    @Override
    public Entity getEntity() {
        Entity duplicate = null;
        if (entityToCopy != null) {
            int entityID = model.issueEntityID();
            duplicate = ((DefaultEntity) entityToCopy).clone(entityID);
        }
        return (duplicate);
    }

    /**
     * Set an Entity into the buffer.
     *
     * @param entity The Entity to place in the buffer. If null, the buffer is
     * cleared.
     */
    @Override
    public void setEntity(Entity entity) {
        entityToCopy = entity;
    }

	//----------------------------------------------------------
    // Methods required by the ModelListener interface
    //----------------------------------------------------------
    /**
     * Ignored.
     */
    @Override
    public void entityAdded(boolean local, Entity entity) {
    }

    /**
     * An entity was removed. If the removed Entity was selected, disable the
     * copy function until a new Entity is selected.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The entity being removed from the view
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {
        int removedID = entity.getEntityID();
        if (removedID == selectedID) {
            selectedID = -1;
        }
        if (entity == entityToCopy) {
            entityToCopy = null;
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
        selectedID = select.getEntityID();
        if (selectedID == -1) {
            // disable when no entities are selected
            setEnabled(false);
        } else {
            Entity entity = model.getEntity(selectedID);
            if (entity.getType() == Tool.TYPE_WORLD) {
                // disable if the 'world' entity is selected
                selectedID = -1;
                setEnabled(false);
            } else {
                // otherwise......
                setEnabled(true);
            }
        }
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

}
