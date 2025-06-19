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
import java.util.Map;
import org.chefx3d.util.ErrorReporter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An object representation of an Entity.
 *
 * All changes to this class are controlled by the model. All setters are
 * protected. Use applyCommand on the model to change the entities values.
 *
 * Selection and HighLight are not model parameters so they can be public here.
 *
 * @author Russell Dodds
 * @version $Revision: 1.64 $
 */
public interface Entity extends Comparable<Entity> {

    /**
     * Get this entityID
     *
     * @return The entityID
     */
    int getEntityID();

    /**
     * Set this entityID
     *
     * @param entityID
     */
    void setEntityID(int entityID);

    /**
     * Get the name
     * @return
     */
    String getName();

    /**
     * Get the category
     * @return
     */
    String getCategory();

    /**
     * Get the type
     * @return
     */
    int getType();

    /**
     * Get the classificationLevel this entity is assigned to
     * @return
     */
    int getClassificationLevel();

    /**
     * Get the constraint this entity is assigned to
     * @return
     */
    String getConstraint();

    /**
     * Get the fixedSize this entity is assigned to
     * @return
     */
    boolean isFixedSize();

    /**
     * Get the fixedSize this entity is assigned to
     * @return
     */
    boolean isHelper();

    /**
     * Get the fixedSize this entity is assigned to
     * @return
     */
    boolean isController();

    /**
     * Is this entity a line tool
     * @return
     */
    boolean isSegmentedEntity();

    /**
     * Get the description of this entity
     * @return
     */
    String getDescription();

    /**
     * Get the category this entity is assigned to
     * @return
     */
    String getIcon();

    /**
     * Get the fixedAspect this entity is assigned to
     * @return
     */
    boolean isFixedAspect();

    /**
     * Get the URL to the x3d file
     * @return
     */
    String getURL();

    /**
     * Get the parameters defined for this entity
     *
     * @return
     */
    Map<String, String> getParams();

    /**
     * Get the parameters defined for this entity
     *
     * @return
     */
    Map<String, String> getStyleSheets();

    /**
     * Set whether this entity is selected.
     *
     * @param selected Whether this entity is selected
     */
    void setSelected(boolean selected);

    /**
     * Get whether this entity is selected.
     *
     * @return Whether this entity is selected
     */
    boolean isSelected();

    /**
     * Set whether to highlight this entity.
     *
     * @param highlight Whether to highlight this entity
     */
    void setHighlighted(boolean highlight);

    /**
     * Get whether to highlight this entity.
     *
     * @return Whether to highlight this entity
     */
    boolean isHighlighted();

    /**
     * Set the properties of a sheet.
     *
     * @param propSheet The sheet name
     * @param node The properties
     */
    void setProperties(String propSheet, Document node);

    /**
     * Set all the properties sheets.
     * @param props
     */
    void setProperties(Map<String, Document> props);

    /**
     * Set a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @param propValue
     */
    void setProperties(String propSheet, String propName, String propValue);

    /**
     * Set a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @param propValue
     */
    void setProperty(String propSheet, String propName, Object propValue);

    /**
     * Add the specified property to the document.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     * @param propValue The property
     */
    void addProperty(String propSheet, String propName, Node propValue);

    /**
     * Add the specified property to the entity.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     * @param propValue The property
     */
    void addProperty(String propSheet, String propName, Object propValue);

    /**
     * Remove the specified property from the document.
     *
     * @param propSheet The sheet name
     * @param parentNode
     * @param propName The property name
     */
    void removeProperty(String propSheet, Node parentNode, String propName);

    /**
     * Remove the specified property from the entity.
     *
     * @param propSheet The sheet name
     * @param propName The property name
     */
    void removeProperty(String propSheet,  String propName);

    /**
     * Get a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @return propValue
     */
    String getProperties(String propSheet, String propName);

    /**
     * Get a specific property.
     *
     * @param propGroup The grouping name
     * @param propName The name of the property to set
     * @return propValue
     */
    Object getProperty(String propGroup, String propName);

    /**
     * Get a specific property.
     *
     * @param propSheet The sheet name
     * @param propName The name of the property to set
     * @return propValue
     */
    Node getPropertyNode(String propSheet, String propName);

    /**
     * Get the properties for a sheet.
     *
     * @param sheetName The sheet name
     * @return The properties
     */
    Document getProperties(String sheetName);

    /**
     * Get the properties for all sheets.
     *
     * @return The property map
     */
    Map<String, Document> getProperties();

    /**
     * Get the flag indicating if updates should
     * be applied to the children
     *
     * @return true/false
     */
    boolean getUpdateChildren();

    /**
     * Set the flag indicating if updates should
     * be applied to the children
     *
     * @param bool
     */
    void setUpdateChildren(boolean bool);

    /**
     * Add a child to the entity
     *
     * @param entity
     */
    void addChild(Entity entity);

    /**
     * Remove a child from the entity
     *
     * @param entity
     */
    void removeChild(Entity entity);

    /**
     * Add a child to the entity at a particular location
     *
     * @param index The index to add at
     * @param entity The child being added
     */
    void insertChildAt(int index, Entity entity);

    /**
     * Get the index of the entity
     *
     * @param entityID
     * @return the index
     */
    int getChildIndex(int entityID);

    /**
     * Get an Entity at the index, returns null if not found
     *
     * @param index The index
     * @return The entityID
     */
    Entity getChildAt(int index);

    /**
     * Get a list of all childrenIDs of this Entity
     *
     * @return The list of childrenIDs
     */
    int[] getChildrenIDs();

    /**
     * Get a list of all children of this Entity
     *
     * @return The list of children entities
     */
    List<Entity> getChildren();

    /**
     * Get the number of children of this Entity
     *
     * @return The number of children
     */
    int getChildCount();

    /**
     * Does this Entity have any children
     *
     * @return true if it has children, false otherwise
     */
    boolean hasChildren();

    /**
     * Each entity should notify listeners of selection changes
     *
     * @param listener
     */
    void addEntitySelectionListener(EntitySelectionListener listener);

    /**
     * Each entity should notify listeners of selection changes
     *
     * @param listener
     */
    void removeEntitySelectionListener(EntitySelectionListener listener);

    /**
     * Each entity should notify listeners of property changes
     *
     * @param listener
     */
    void addEntityPropertyListener(EntityPropertyListener listener);

    /**
     * Each entity should notify listeners of property changes
     *
     * @param listener
     */
    void removeEntityPropertyListener(EntityPropertyListener listener);

    /**
     * Each entity should notify listeners of children changes
     *
     * @param listener
     */
    void addEntityChildListener(EntityChildListener listener);

    /**
     * Each entity should notify listeners of children changes
     *
     * @param listener
     */
    void removeEntityChildListener(EntityChildListener listener);

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    void setErrorReporter(ErrorReporter reporter);

}