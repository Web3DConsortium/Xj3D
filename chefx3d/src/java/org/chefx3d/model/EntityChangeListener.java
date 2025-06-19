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

/**
 * Notification of changes in an entities state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public interface EntityChangeListener {
    
    /**
     * A segment was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param startVertexID The starting vertexID
     * @param endVertexID The starting vertexID
     */
    void segmentAdded(boolean local, int entityID,
            int segmentID, int startVertexID, int endVertexID);

    /**
     * A segment was split.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param vertexID The splitting vertexID
     */
    void segmentSplit(boolean local, int entityID,
            int segmentID, int vertexID);

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The segment removed
     */
    void segmentRemoved(boolean local, int entityID,
            int segmentID);

    /**
     * A vertex was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param position The x position in world coordinates
     */
    void segmentVertexAdded(boolean local, int entityID,
            int vertexID, double[] position);

    /**
     * A vertex was updated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param propertySheet
     * @param propertyName The name of the property to change
     * @param newValue The updated value
     */
    void segmentVertexUpdated(boolean local, int entityID, int vertexID,
            String propertySheet, String propertyName, String newValue);

    /**
     * A vertex was moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param newPosition The new position in world coordinates
     */
    void segmentVertexMoved(boolean local, int entityID,
            int vertexID, double[] newPosition);

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     */
    void segmentVertexRemoved(boolean local, int entityID,
            int vertexID);

    /**
     * An entity has changed size.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param size The new size
     */
    void entitySizeChanged(boolean local, int entityID, float[] size);

    /**
     * An entity was associated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    void entityAssociated(boolean local, int parent, int child);

    /**
     * An entity was unassociated with another.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     */
    void entityUnassociated(boolean local, int parent, int child);

    /**
     * The entity moved.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D
     *        System).
     */
    void entityMoved(boolean local, int entityID, double[] position);

    /**
     * The entity was scaled.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    void entityScaled(boolean local, int entityID, float[] scale);

    /**
     * The entity was rotated.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID the id
     * @param rotation The rotation(axis + angle in radians)
     */
    void entityRotated(boolean local, int entityID, float[] rotation);

    /**
     * A property changed.
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The property that has changed
     * @param propertyValue The value being set.
     *
     * @deprecated As of ChefX3D 2.0 use {@link EntityPropertyListener} on each
     * Entity.
     */
    void propertyChanged(boolean local, int entityID,
            String propertySheet, String propertyName, Object propertyValue);
}
