/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005
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
import com.zookitec.layout.*;
import java.awt.*;

/**
 * A custom layout for the property editor panels
 *
 * @author Russell Dodds
 * @version $Revision: 1.6 $
 *
 */
public class PropertyPanelLayout extends ExplicitLayout {

    /**
     * Determines the preferred size of the <code>target</code> container
     * using this layout manager, based on the components in the container.
     * <p>
     * Most applications do not call this method directly. This method is called
     * when a container calls its <code>getPreferredSize</code> method.
     *
     * @param target the container in which to do the layout.
     * @return the preferred dimensions to lay out the subcomponents of the
     *         specified container.
     * @see java.awt.Container
     * @see java.awt.BorderLayout#minimumLayoutSize
     * @see java.awt.Container#getPreferredSize()
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {

            Dimension dim = new Dimension(0, 0);

            Component[] components = target.getComponents();

            ExplicitConstraints constraints;
            int w, h;
            for (Component component : components) {
                // get the constraints for the component
                constraints = getConstraints(component);
                if (constraints != null) {
                    // set the component bounds based on the constraints
                    w = constraints.getWidthValue(this);
                    h = constraints.getHeightValue(this) - 5;

                    // h = components[i].getPreferredSize().height;

                    dim.width = Math.max(w, dim.width);
                    
                    dim.height += h;

                }
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }
    }

}
