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

package org.chefx3d.property.awt;

// External Imports

// Internal Imports
import org.chefx3d.property.PropertyEditor;
import org.chefx3d.property.PropertyEditorFactory;
import org.chefx3d.model.WorldModel;

/**
 * Factory for creating AWT Property Editors.
 *
 * @author Russell Dodds
 * @version $Revision: 1.8 $
 */
public class AWTPropertyEditorFactory implements PropertyEditorFactory {

    @Override
    public PropertyEditor createEditor(WorldModel model, int skipLevels) {

        return new DefaultPropertyEditor(model, skipLevels);

    }

    @Override
    public PropertyEditor createMultiTabEditor(WorldModel model,
            int skipLevels) {

        return new MultiTabPropertyEditor(model, skipLevels);

    }

}
