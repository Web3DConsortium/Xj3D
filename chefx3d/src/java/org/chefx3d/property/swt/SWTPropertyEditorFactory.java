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

package org.chefx3d.property.swt;

// External Imports

// Internal Imports
import org.chefx3d.property.PropertyEditor;
import org.chefx3d.property.PropertyEditorFactory;
import org.chefx3d.model.WorldModel;

/**
 * Factory for creating SWT based Property Editors.
 *
 * @author Russell Dodds
 * @version $Revision: 1.7 $
 */
public class SWTPropertyEditorFactory implements PropertyEditorFactory {
    /**
     * Create a Property Editor.
     *
     * @param model The model
     * @param skipLevels Element levels to skip below the root.
     *
     * @return The editor
     */
    @Override
    public PropertyEditor createEditor(WorldModel model, int skipLevels) {
        return null;
    }

    /**
     * Create a Property Editor.
     *
     * @param model The model
     * @param vmanager The ViewManager
     * @param skipLevels Element levels to skip below the root.
     * @param showPlacement Present the placement properties tab
     *
     * @return The editor
     */
    @Override
    public PropertyEditor createMultiTabEditor(WorldModel model, int skipLevels) {
        return null;
    }
}
