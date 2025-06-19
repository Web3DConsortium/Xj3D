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

//External Imports
import org.w3c.dom.*;
import java.util.*;

//Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.tool.Tool;

/**
 * The base implementation for all WorldModels.  Inward facing interface.
 *
 * @author Alan Hudson
 * @version $Revision: 1.67 $
 */
abstract class BaseWorldModel implements WorldModel {

    /** The list of Model Listeners */
    protected List<ModelListener> modelListeners;

    /** The list of PropertyStructureListeners */
    protected List<PropertyStructureListener> propertyListeners;

    /**
     * The list of Entity Listeners. Eventually move to Entity itself, but need
     * to consider how to deal with local flag and reissue events logic.
     */
    protected Map<Entity, List<EntityChangeListener>> entityListeners;

    /** The current tool */
    protected Tool currentTool;

    /** Entity indexed by entityID */
    protected List<Entity> entities;

    /** How many parent associations does this entityID have */
    private Map<Integer, Integer> associatedCount;

    /** The last entityID as a Integer */
    private Integer lastEntityID;

    /** The selectedEntity */
    private Entity selectedEntity;

    /** The current master pos */
    private double[] masterPos;

    /** The current master orientation */
    private float[] masterRot;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** Default constructor for BaseWorldModel */
    public BaseWorldModel() {

        entities = new ArrayList<>();
        associatedCount = new HashMap<>();

        modelListeners = new ArrayList<>();
        propertyListeners = new ArrayList<>();
        entityListeners = new HashMap<>();

        masterPos = new double[3];
        masterRot = new float[4];

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }

    // ----------------------------------------------------------
    // Methods implementing WorldModel
    // ----------------------------------------------------------

    /**
     * Add a listener for Entity changes. Duplicates will be ignored.
     *
     * @param entity The entity
     * @param l The listener.
     */
    @Override
    public void addEntityChangeListener(Entity entity, EntityChangeListener l) {
        List<EntityChangeListener> listeners = entityListeners.get(entity);

        if (listeners == null) {
            listeners = new ArrayList<>(2);
            listeners.add(l);

            entityListeners.put(entity, listeners);
        } else {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    /**
     * Remove a listener for Entity changes.
     *
     * @param entity The entity
     * @param l The listener.
     */
    @Override
    public void removeEntityChangeListener(Entity entity, EntityChangeListener l) {
        List<EntityChangeListener> listeners = entityListeners.get(entity);

        if (listeners != null) {
            listeners.remove(l);
        }
    }

    /**
     * Add a listener for Model changes. Duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addModelListener(ModelListener l) {
        if (!modelListeners.contains(l)) {
            modelListeners.add(l);
        }
    }

    /**
     * Remove a listener for Model changes.
     *
     * @param l The listener.
     */
    @Override
    public void removeModelListener(ModelListener l) {
        modelListeners.remove(l);
    }

    /**
     * Add a listener for Property changes. Duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addPropertyStructureListener(PropertyStructureListener l) {
        if (!propertyListeners.contains(l)) {
            propertyListeners.add(l);
        }
    }

    /**
     * Remove a listener for Property changes.
     *
     * @param l The listener.
     */
    @Override
    public void removePropertyStructureListener(PropertyStructureListener l) {
        propertyListeners.remove(l);
    }

    /**
     * Get a unique ID for an entity.
     *
     * @return The unique ID
     */
    @Override
    public synchronized int issueEntityID() {
        Collections.sort(entities);
        return entities.get(entities.size() - 1).getEntityID() + 1;
    }

    /**
     * Get a unique ID for a transaction. A transaction is a set of transient
     * commands and the final real command. A transactionID only needs to be
     * unique for a short period of time. 0 is reserved as marking a
     * transactionless command.
     *
     * @return The ID
     */
    @Override
    public int issueTransactionID() {
        // Generate a likely uniqueID. A failure will just result in some
        // transient events being lost

        int ret_val = 0;

        while (ret_val == 0) {
            ret_val = (int) (Math.random() * Integer.MAX_VALUE);
        }

        return ret_val;
    }

    /**
     * Update the selection list.
     *
     * @param selection The list of selected entities. The last one is the
     *        latest.
     */
    @Override
    public void changeSelection(List<Selection> selection) {
        Selection select = selection.get(selection.size() - 1);
        if (select.getEntityID() == -1) {
            selectedEntity = null;

        } else {

            for (Entity entity : entities) {

                if (entity.getEntityID() == select.getEntityID())
                    selectedEntity = entity;
            }

            if (selectedEntity != null) {

//System.out.println("Model changeSel: " + selectedEntity.getEntityID() + " segID: " + select.getSegmentID() + " vID: " + select.getVertexID());

                if (selectedEntity instanceof SegmentableEntity) {
                    ((SegmentableEntity)selectedEntity).setSelectedSegmentID(select.getSegmentID());
                    ((SegmentableEntity)selectedEntity).setSelectedVertexID(select.getVertexID());
                }
            }
        }

        Iterator<ModelListener> i = modelListeners.iterator();
        while(i.hasNext()) {
            ModelListener l = i.next();
            l.selectionChanged(selection);
        }
    }

    /**
     * ReIssue all events to catch a model listener up to the current state.
     *
     * @param listener The model listener
     * @param ecl The entity change listener
     */
    @Override
    public void reissueEvents(ModelListener l, EntityChangeListener ecl) {

        List<SegmentVertex> vertices;
        SegmentVertex vertex;
        int[] associations;
        int ix = 0;

        for (Entity entity : entities) {

            if (entity == null) {
                continue;
            }

            addEntity(false, entity, l);

            if (entity instanceof SegmentableEntity) {
                vertices = ((SegmentableEntity) entity).getSegmentSequence().getVertices();

                if (vertices != null) {
                    int cnt = 0;
                    for (Iterator<SegmentVertex> j = vertices.iterator(); j.hasNext();) {
                        vertex = j.next();
                        addSegmentVertex(false, ix, cnt++, vertex.getPosition(), ecl);
                    }
                }
            }

            if (entity instanceof AssociatableEntity) {
                associations = ((AssociatableEntity) entity).getAssociates();

                if (associations != null) {
                    for (int j = 0; j < associations.length; j++) {
                        associateEntities(false, ix, associations[j], ecl);
                    }
                }
            }
            ix++;
        }
    }

    /**
     * Is this entity associated with another entity
     *
     * @param entityID The entityID
     * @return Whether it has any associations
     */
    @Override
    public boolean isEntityAssociated(int entityID) {
        if (lastEntityID == null || (lastEntityID != entityID)) {
            lastEntityID = entityID;
        }

        Integer cnt = associatedCount.get(lastEntityID);

        return cnt != null && cnt > 0;
    }

    // TODO: Remove this method
    /**
     * Get the named property sheet for this entity.
     *
     * @param entityID The selected entity
     * @param propertySheet The requested sheet
     * @return The Map of data sheet name = property values
     */
    public Map<String, Document> getEntityProperties(int entityID,
            String propertySheet) {

        Entity td = null;
        if (entityID != -1) {

            for (Entity entity : entities) {
                if (entity.getEntityID() == entityID)
                    td = entity;
            }

            if (td == null) {

                errorReporter.errorReport("Can't find properties for entity: "
                        + entityID, new Exception());

                return null;

            }

            if (propertySheet == null) {

                return td.getProperties();

            } else {

                // get the properties
                Document doc = td.getProperties(propertySheet);

                // add result to HashMap
                Map<String, Document> properties = new HashMap<>();
                properties.put(propertySheet, doc);

                return properties;
            }

        } else {
            return null;
        }

    }

    /**
     * Clear the model.
     *
     * @param local Was this action initiated from the local UI
     * @param listener The model listener to inform or null for all
     */
    @Override
    public void clear(boolean local, ModelListener listener) {

        for (Entity entity : entities) {
            if (entity != null) {

                if (listener == null) {
                    Iterator<ModelListener> j = modelListeners.iterator();
                    while(j.hasNext()) {
                        ModelListener l = j.next();
                        l.entityRemoved(local, entity);
                    }
                } else {
                    listener.entityRemoved(local, entity);
                }
            }
        }

        if (!entities.isEmpty()) {
            entities.clear();
        }

        // now send notification of the reset
        if (listener == null) {
            Iterator<ModelListener> i = modelListeners.iterator();
            while(i.hasNext()) {
                ModelListener l = i.next();
                l.modelReset(local);
            }
        } else {
            listener.modelReset(local);
        }
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

    /**
     * A property changed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The entity which changed
     * @param propSheet The sheet which changed
     * @param propName The property which changed. A blank property name means
     *        the whole tree changed.
     * @param newValue The new value.
     * @param listener The model listener or null for all
     * @deprecated As of ChefX3D 2.0 use {@link EntityPropertyListener} on each
     * Entity.
     */
    protected void changeProperty(boolean local, int entityID,
            String propSheet, String propName, Object newValue,
            EntityChangeListener listener) {

/*
System.out.println("BaseWorldModel.changeProperty()");
System.out.println("    entityID: " + entityID);
System.out.println("    propSheet: " + propSheet);
System.out.println("    propName: " + propName);
System.out.println("    newValue: " + newValue);
*/

        // Get the Entity for the selected entity
        Entity entity = null;
        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        // Grab the size of the entity, so we can check if is has changed
        float[] originalSize = new float[3];
        if ((entity.getType() != Tool.TYPE_WORLD) &&
            (entity instanceof PositionableEntity)) {

            ((PositionableEntity)entity).getSize(originalSize);

        }

        // Now set the individual property passed
        entity.setProperty(propSheet, propName, newValue);

        // Grab the size of the entity, so we can check if is has changed
        float[] newSize = new float[3];
        if ((entity.getType() != Tool.TYPE_WORLD) &&
            (entity instanceof PositionableEntity)) {

            ((PositionableEntity)entity).getSize(newSize);

        }

        // If the size has changed then also send a change size command
        if (listener == null) {
            List<EntityChangeListener> ecl = entityListeners.get(entity);

            if (ecl != null) {
                Iterator<EntityChangeListener> i = ecl.iterator();
                while(i.hasNext()) {
                    EntityChangeListener l = i.next();
                    l.propertyChanged(local, entityID, propSheet, propName,
                            newValue);
                    if ((originalSize[0] != newSize[0]) || (originalSize[1] != newSize[1])
                            || (originalSize[2] != newSize[2])) {
                        l.entitySizeChanged(local, entityID, newSize);
                    }
                }
            }
        } else {
            listener.propertyChanged(local, entityID, propSheet, propName,
                    newValue);
            if ((originalSize[0] != newSize[0]) || (originalSize[1] != newSize[1])
                    || (originalSize[2] != newSize[2])) {

                listener.entitySizeChanged(local, entityID, newSize);
            }

        }
    }

    /**
     * Add a property to an entity. This can be called with a batch of changes,
     * but it must be called at least once anytime properties are added to the
     * model.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The entity which changed
     * @param propSheet The sheet which changed
     * @param values The complete value tree.
     * @param listener The model listener or null for all
     */
    protected void addProperty(boolean local, int entityID, String propSheet,
            String propName, Node propValue, ModelListener listener) {

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                e.addProperty(propSheet, propName, propValue);
        }

        Iterator<PropertyStructureListener> i = propertyListeners.iterator();
        while(i.hasNext()) {
            PropertyStructureListener l = i.next();
            l.propertyAdded(local, entityID, propSheet, propName, propValue);
        }
    }

    /**
     * Associate a two entities with a parent/child relationship.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     * @param listener The model listener or null for all
     */
    public void associateEntities(boolean local, int parent, int child,
            EntityChangeListener listener) {

        Entity parentEntity = null;
        Entity childEntity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == parent)
                parentEntity = e;
        }

        for (Entity e : entities) {
            if (e.getEntityID() == parent)
                childEntity = e;
        }

        if ((parentEntity instanceof AssociatableEntity) &&
            (childEntity instanceof AssociatableEntity)) {

            if (parentEntity == null) {
                errorReporter.errorReport("Can't find parent for associateEntities: "
                        + parent, new Exception());
                return;
            }

            if (childEntity == null) {
                errorReporter.errorReport("Can't find child for associateEntities: "
                        + child, new Exception());
                return;
            }

            ((AssociatableEntity)parentEntity).addAssociation(child);
            ((AssociatableEntity)childEntity).addParentAssociation(parent);

            Integer ci = child;

            Integer cnt = associatedCount.get(ci);
            if (cnt == null) {
                cnt = 1;
                associatedCount.put(ci, cnt);
            } else {
                cnt = cnt + 1;
                associatedCount.put(ci, cnt);
            }

    /*
    System.out.println("BaseWorldModel.associateEntities()");
    System.out.println("child: " + ci);
    System.out.println("parent: " + parent);
    System.out.println("count: " + associatedCount.get(ci));
    */
            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(parentEntity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.entityAssociated(local, parent, child);
                    }
                }
            } else {
                listener.entityAssociated(local, parent, child);
            }
        }
    }

    /**
     * Remove an Association between two entities.
     *
     * @param local Was this action initiated from the local UI
     * @param parent The parent entityID
     * @param child The child entityID
     * @param listener The model listener or null for all
     */
    public void unassociateEntities(boolean local, int parent, int child,
            EntityChangeListener listener) {

        Entity parentEntity = null;
        Entity childEntity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == parent)
                parentEntity = e;
        }

        for (Entity e : entities) {
            if (e.getEntityID() == parent)
                childEntity = e;
        }

        if ((parentEntity instanceof AssociatableEntity) &&
            (childEntity instanceof AssociatableEntity)) {

            if (parentEntity == null) {
                errorReporter.errorReport("Can't find parent for associateEntities: "
                        + parent, new Exception());
                return;
            }

            if (childEntity == null) {
                errorReporter.errorReport("Can't find child for associateEntities: "
                        + child, new Exception());
                return;
            }

            errorReporter.messageReport("unassociateEntities() not supported at this time.");

            ((AssociatableEntity)parentEntity).removeAssociation(child);
            ((AssociatableEntity)childEntity).removeParentAssociation(parent);

            Integer ci = child;

            Integer cnt = associatedCount.get(ci);
            if (cnt == null) {
                errorReporter.errorReport("Unassociated something with no ref counts",
                        new Exception());
            } else {
                int newCount = cnt - 1;

                if (newCount < 0) {
                    associatedCount.remove(ci);
                } else {
                    cnt = cnt - 1;
                    associatedCount.put(ci, cnt);
                }
            }

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(parentEntity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.entityUnassociated(local, parent, child);
                    }
                }
            } else {
                listener.entityUnassociated(local, parent, child);
            }
        }
    }

    /**
     * An entity was added.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The entity to add
     * @param mlistener The model listener or null for all
     */
    protected void addEntity(boolean local, Entity entity, ModelListener mlistener) {

        entities.add(entity);

        if (mlistener == null) {
            Iterator<ModelListener> i = modelListeners.iterator();
            while(i.hasNext()) {
                ModelListener l = i.next();
                l.entityAdded(local, entity);
            }
        } else {
            mlistener.entityAdded(local, entity);
        }
    }

    /**
     * A vertex was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param pos The x position in world coordinates
     * @param listener The model listener to inform or null for all
     */
    protected void addSegmentVertex(boolean local, int entityID, int vertexID,
            double[] pos, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).addSegmentVertex(vertexID, pos);

            if (listener == null) {
                List<EntityChangeListener> ecl =
                    entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentVertexAdded(local, entityID, vertexID, pos);
                    }
                }
            } else {
                listener.segmentVertexAdded(local, entityID, vertexID, pos);
            }
        }
    }

    /**
     * Remove a segment vertex.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertextId The unique segmentID assigned by the view
     * @param listener The model listener to inform or null for all
     */
    protected void removeSegmentVertex(boolean local, int entityID,
            int vertexID, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).removeSegmentVertex(vertexID);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentVertexRemoved(local, entityID, vertexID);
                    }
                }
            } else {
                listener.segmentVertexRemoved(local, entityID, vertexID);
            }
        }
    }

    /**
     * A segment was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param pos The x position in world coordinates
     * @param listener The model listener to inform or null for all
     */
    protected void addSegment(boolean local, int entityID, int segmentID,
        int startVertexID, int endVertexID, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).addSegment(segmentID, startVertexID, endVertexID);

            if (listener == null) {
                List<EntityChangeListener> ecl =
                    entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentAdded(local, entityID, segmentID, startVertexID, endVertexID);
                    }
                }
            } else {
                listener.segmentAdded(local, entityID, segmentID, startVertexID, endVertexID);
            }
        }
    }

    /**
     * Split a segment with the provided vertex.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The segment to split
     * @param vertexID The vertex to use for the mid point
     * @param listener The model listener to inform or null for all
     */
    protected void splitSegment(boolean local, int entityID, int segmentID,
        int vertexID, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).splitSegment(segmentID, vertexID);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentSplit(local, entityID, segmentID, vertexID);
                    }
                }
            } else {
                listener.segmentSplit(local, entityID, segmentID, vertexID);
            }
        }
    }

    /**
     * Remove a segment.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertextId The unique segmentID assigned by the view
     * @param listener The model listener to inform or null for all
     */
    protected void removeSegment(boolean local, int entityID,
            int vertextId, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).removeSegmentVertex(vertextId);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentVertexRemoved(local, entityID, vertextId);
                    }
                }
            } else {
                listener.segmentVertexRemoved(local, entityID, vertextId);
            }
        }
    }

    /**
     * Move a segment vertex.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The entity
     * @param vertexID The vertex
     * @param newPosition The new position
     * @param origPosition The original position
     * @param listener The model listener to inform or null for all
     */
    protected void moveSegmentVertex(boolean local, int entityID, int vertexID,
            double[] newPosition, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).moveSegmentVertex(vertexID, newPosition);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentVertexMoved(local, entityID, vertexID, newPosition);
                    }
                }
            } else {
                listener.segmentVertexMoved(local, entityID, vertexID, newPosition);
            }
        }
    }

    /**
     * Update a segment vertex.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The entity
     * @param vertexID The vertex
     * @param propertySheet The sheet to save to
     * @param propertyName The property to change
     * @param newValue The value to store
     * @param listener The model listener to inform or null for all
     */
    protected void updateSegmentVertex(boolean local, int entityID, int vertexID,
            String propertySheet, String propertyName, String newValue, EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof SegmentableEntity) {

            ((SegmentableEntity)entity).updateSegmentVertex(vertexID, propertySheet, propertyName, newValue);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.segmentVertexUpdated(local, entityID, vertexID, propertySheet, propertyName, newValue);
                    }
                }
            } else {
                listener.segmentVertexUpdated(local, entityID, vertexID, propertySheet, propertyName, newValue);
            }
        }
    }

    /**
     * Remove an entity
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param listener The model listener to inform or null for all
     */
    protected void removeEntity(boolean local, Entity entity,
            ModelListener listener) {

        if (entity != null) {

            // check all entities for associations
            int[] associates;

            for (Entity check : entities) {
                if ((check != null) &&
                        (check instanceof AssociatableEntity) ){

                    associates = ((AssociatableEntity)check).getAssociates();

                    for (int j = 0; j < associates.length; j++) {

                        if (associates[j] == entity.getEntityID()) {

                            /*
                            System.out.println("BaseWorldModel.removeEntity()");
                            System.out.println("child, associates[j]: " + associates[j]);
                            System.out.println("child, entity.getEntityID(): " + entity.getEntityID());
                            System.out.println("parent: " + check.getEntityID());
                            */

                            //unassociateEntities(local, check.getEntityID(), entity.getEntityID(), null);
                        }
                    }
                }
            }

            // now, remove the entity
            entities.remove(entity);

            if (listener == null) {
                Iterator<ModelListener> i = modelListeners.iterator();
                while(i.hasNext()) {
                    ModelListener l = i.next();
                    l.entityRemoved(local, entity);
                }
            } else {
                listener.entityRemoved(local, entity);
            }
        }
    }

    /**
     * Move an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The entity
     * @param position The new position
     * @param listener The model listener to inform or null for all
     */
    protected void moveEntity(boolean local, int entityID, double[] position,
            EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof PositionableEntity) {

            ((PositionableEntity)entity).setPosition(position);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.entityMoved(local, entityID, position);
                    }
                }
            } else {
                listener.entityMoved(local, entityID, position);
            }
        }
    }

    /**
     * Change the rotation of an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The entity
     * @param rotation The new rotation
     * @param listener The model listener to inform or null for all
     */
    protected void rotateEntity(boolean local, int entityID, float[] rotation,
            EntityChangeListener listener) {

        Entity entity = null;

        for (Entity e : entities) {
            if (e.getEntityID() == entityID)
                entity = e;
        }

        if (entity instanceof PositionableEntity) {

            ((PositionableEntity)entity).setRotation(rotation);

            if (listener == null) {
                List<EntityChangeListener> ecl = entityListeners.get(entity);

                if (ecl != null) {
                    Iterator<EntityChangeListener> i = ecl.iterator();
                    while(i.hasNext()) {
                        EntityChangeListener l = i.next();
                        l.entityRotated(local, entityID, rotation);
                    }
                }
            } else {
                listener.entityRotated(local, entityID, rotation);
            }
        }
    }

    /**
     * User view information changed.
     *
     * @param local Was this action initiated from the local UI
     * @param pos The position of the user
     * @param rot The orientation of the user
     * @param fov The field of view changed(X3D Semantics)
     * @param listener The model listener to inform or null for all
     */
    public void setViewParams(boolean local, double[] pos, float[] rot,
            float fov, ModelListener listener) {
        masterPos[0] = pos[0];
        masterPos[1] = pos[1];
        masterPos[2] = pos[2];

        masterRot[0] = rot[0];
        masterRot[1] = rot[1];
        masterRot[2] = rot[2];
        masterRot[3] = rot[3];

        if (listener == null) {
            Iterator<ModelListener> i = modelListeners.iterator();
            while(i.hasNext()) {
                ModelListener l = i.next();
                l.viewChanged(local, pos, rot, fov);
            }
        } else {
            listener.viewChanged(local, pos, rot, fov);
        }
    }

    /**
     * The master view has changed.
     *
     * @param local Was this action initiated from the local UI
     * @param view The view which is master
     * @param listener The model listener to inform or null for all
     */
    public void setMaster(boolean local, long viewID, ModelListener listener) {
        if (listener == null) {
            Iterator<ModelListener> i = modelListeners.iterator();
            while(i.hasNext()) {
                ModelListener l = i.next();
                l.masterChanged(local, viewID);
            }
        } else {
            listener.masterChanged(local, viewID);
        }
    }

    /**
     * Get the model data.
     *
     * @return Returns the current model data
     */
    @Override
    public Entity[] getModelData() {
        return entities.toArray(new Entity[entities.size()]);
    }

    /**
     * Get an entity.
     *
     * @param entityID The ID of the entity
     * @return The entity or null if not found
     */
    @Override
    public Entity getEntity(int entityID) {

        Entity ret_val = null;

        if (entityID >= 0) {
            for (Entity e : entities) {
                if (e.getEntityID() == entityID) {
                    ret_val = e;
                }
            }

        }
        return ret_val;

    }

    /**
     * Get the entity that represents the location
     *
     * @return The entity or null if not found
     */
    @Override
    public Entity getLocationEntity() {

        for (Entity entity : entities) {
            if (entity != null) {

                if (entity.getType() == Tool.TYPE_WORLD) {
                    return entity;
                }

            }
        }

        return null;

    }

    /**
     * Get the currently selected entity.
     *
     * @return The entity or null not selected
     */
    @Override
    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    /**
     * Set the model data.
     */
    protected void setModelData(Entity[] data) {

        // initialize the local entity storage array with the new data,
        // NOTE: this presumes that the model has been cleared, otherwise
        // the potential exists for a mix of data from different sources
        entities.addAll(Arrays.asList(data));

        reissueEvents(null, null);
    }

    /**
     * Parse the size XPaths. Find keywords to help limit when size recalcs are
     * done.
     *
     * @param sizeX The XPath expression for x size
     * @param sizeY The XPath expression for y size
     * @param sizeZ The XPath expression for z size
     * @return An HashSet of possible field changes that will trigger a recalc.
     *         Null means all. Zero length means all.
     */
    protected Set parseSizeFields(String sizeX, String sizeY, String sizeZ) {
        return null;
    }

    /**
     * Clone a property map. All its children nodes will be deeply cloned as
     * well.
     *
     * @param propMap The map to clone
     */
    private Map<String, Document> cloneProperties(Map propMap) {
        Map<String, Document> ret_val = new HashMap<>();

        Iterator itr = propMap.entrySet().iterator();
        Map.Entry entry;
        Document doc;

        while (itr.hasNext()) {
            entry = (Map.Entry) itr.next();

            doc = (Document) entry.getValue();

            ret_val.put((String) entry.getKey(),
                        (Document) doc.cloneNode(true));
        }

        return ret_val;
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
