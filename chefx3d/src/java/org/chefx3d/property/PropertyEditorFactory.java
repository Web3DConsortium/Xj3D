/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property;

// External Imports

// Internal Imports
import org.chefx3d.model.WorldModel;

/**
 * Factory for creating Property Editors.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public interface PropertyEditorFactory {

    /**
     * Create a Property Editor.
     *
     * @param model The model
     * @param skipLevels Element levels to skip below the root.
     *
     * @return The editor
     */
    PropertyEditor createEditor(WorldModel model, int skipLevels);

    /**
     * Create a Property Editor.
     *
     * @param model The model
     * @param skipLevels Element levels to skip below the root.
     *
     * @return The editor
     */
    PropertyEditor createMultiTabEditor(WorldModel model, int skipLevels);

}
