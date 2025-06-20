/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.model;

// External Imports
import java.util.List;

// Internal Imports
import org.chefx3d.util.ErrorReporter;

/**
 * Stores all information that can authored.  Outward facing interface.
 *
 * @author Alan Hudson
 * @version $Revision: 1.23 $
 */
public interface WorldModel {
    /**
     * Add a listener for Model changes. Duplicates will be ignored.
     *
     * @param l The listener.
     */
    void addModelListener(ModelListener l);

    /**
     * Remove a listener for Model changes.
     *
     * @param l The listener.
     */
    void removeModelListener(ModelListener l);

    /**
     * Add a listener for Entity changes. Duplicates will be ignored.
     *
     * @param entity The entity
     * @param l The listener.
     */
    void addEntityChangeListener(Entity entity, EntityChangeListener l);

    /**
     * Remove a listener for Entity changes.
     *
     * @param entity The entity
     * @param l The listener.
     */
    void removeEntityChangeListener(Entity entity, EntityChangeListener l);

    /**
     * Add a listener for Property changes. Duplicates will be ignored.
     *
     * @param l The listener.
     */
    void addPropertyStructureListener(PropertyStructureListener l);

    /**
     * Remove a listener for Property changes.
     *
     * @param l The listener.
     */
    void removePropertyStructureListener(PropertyStructureListener l);

    /**
     * Apply a command against the model.
     *
     * @param command The command
     */
    void applyCommand(Command command);

    /**
     * Get a unique ID for an entity.
     *
     * @return The unique ID
     */
    int issueEntityID();

    /**
     * Get a unique ID for a transaction. A transaction is a set of transient
     * commands and the final real command. A transactionID only needs to be
     * unique for a short period of time. 0 is reserved as marking a
     * transactionless command.
     *
     * @return The ID
     */
    int issueTransactionID();

    /**
     * Update the selection list.
     *
     * @param selection The list of selected entities. The last one is the
     *        latest.
     */
    void changeSelection(List<Selection> selection);

    /**
     * Undo the last change.
     */
    void undo();

    /**
     * Returns true if there are any <code>Command</code>s to undo
     * @return
     */
    boolean canUndo();

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     * @return
     */
    String getUndoDescription();

    /**
     * Redo the last change.
     */
    void redo();

    /**
     * Returns true if there are any <code>Command</code>s to redo
     * @return
     */
    boolean canRedo();

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     * @return
     */
    String getRedoDescription();

    /**
     * Clear the model.
     *
     * @param local Is this a local change
     * @param listener to notify or null for all
     */
    void clear(boolean local, ModelListener listener);

    /**
     * Flush the undo history.
     */
    void clearHistory();

    /**
     * Reissue all events to catch a model listener up to the current state.
     *
     * @param l The model listener
     * @param ecl The entity change listener
     */
    void reissueEvents(ModelListener l, EntityChangeListener ecl);

    /**
     * Is this entity associated with another entity
     *
     * @param entityID The entityID
     * @return Whether it has any associations
     */
    boolean isEntityAssociated(int entityID);

    /**
     * Get an entity.
     *
     * @param entityID The ID of the entity
     * @return The entity
     */
    Entity getEntity(int entityID);

    /**
     * Get the currently selected entity.
     *
     * @return The entity or null not selected
     */
    Entity getSelectedEntity();

    /**
     * Get the entity that represents the location
     *
     * @return The entity or null if not found
     */
    Entity getLocationEntity();

    /**
     * Get the model data.
     *
     * @return Returns the current model data
     */
    Entity[] getModelData();

    /**
     * Sets the ErrorReporter to use to display messages
     *
     * @param reporter
     */
    void setErrorReporter(ErrorReporter reporter);

}
