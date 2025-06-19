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
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

// Application specific imports
import org.chefx3d.model.AddAssociationCommand;
import org.chefx3d.model.AddEntityCommand;
import org.chefx3d.model.Entity;
import org.chefx3d.model.AssociatableEntity;
import org.chefx3d.model.WorldModel;

import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * An action that can be used place a copy of an Entity into
 * the model.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class EntityPasteAction extends AbstractAction implements MenuListener {

    /**
     * The world model
     */
    private WorldModel model;

    /**
     * The source of the Entity to paste
     */
    private EntityCopyBuffer buffer;

    /**
     * The ErrorReporter for messages
     */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     * @param buffer The source of the Entity to paste.
     * @param menu The menu that this action is an item of.
     */
    public EntityPasteAction(boolean standAlone, Icon icon, WorldModel model,
            EntityCopyBuffer buffer, JMenu menu) {

        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Paste");
        }

        this.model = model;
        this.buffer = buffer;

        if (menu != null) {
            menu.addMenuListener(EntityPasteAction.this);
        }

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_V);

        putValue(SHORT_DESCRIPTION, "Paste");

        setEnabled(false);
    }

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param model The world model
     * @param buffer The source of the Entity to paste.
     */
    public EntityPasteAction(boolean standAlone, Icon icon, WorldModel model,
            EntityCopyBuffer buffer) {

        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Paste");
        }

        this.model = model;
        this.buffer = buffer;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_V);

        putValue(SHORT_DESCRIPTION, "Paste");

        setEnabled(true);
    }

	//----------------------------------------------------------
    // Methods required by the ActionListener interface
    //----------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (buffer.hasEntity()) {
            setEnabled(true);

            if (errorReporter == null) {
                errorReporter = DefaultErrorReporter.getDefaultReporter();
            }
            Entity entity = buffer.getEntity();
            AddEntityCommand cmd = new AddEntityCommand(model, entity);
            cmd.setErrorReporter(errorReporter);
            model.applyCommand(cmd);

            if (entity instanceof AssociatableEntity) {

                int[] assocID = ((AssociatableEntity) entity).getAssociates();
                for (int i = 0; i < assocID.length; i++) {
                    int entityID = entity.getEntityID();
                    AddAssociationCommand acmd
                            = new AddAssociationCommand(model, entityID, assocID[i]);
                    acmd.setErrorReporter(errorReporter);
                    model.applyCommand(acmd);
                }

            }

        } else {
            setEnabled(false);
        }
    }

	//----------------------------------------------------------
    // Methods required by the MenuListener interface
    //----------------------------------------------------------
    @Override
    public void menuCanceled(MenuEvent evt) {
    }

    @Override
    public void menuDeselected(MenuEvent evt) {
        setEnabled(true);
    }

    /**
     * Invoked when a menu is selected. Enable this item if an Entity is
     * available in the copy buffer.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void menuSelected(MenuEvent evt) {
        if (buffer.hasEntity()) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
