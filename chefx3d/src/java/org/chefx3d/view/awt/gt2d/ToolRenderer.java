/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.view.awt.gt2d;

// External Imports
import java.awt.Graphics2D;

// Local imports
import org.chefx3d.util.ErrorReporter;

/**
 * Generic representation of rendering a entity on the GT2D view.
 * <p>
 *
 * Tool renderers are to assume that the entity location is the center of the
 * object to be rendered and width and height are total values, not distance
 * from the origin.
 *
 * @author Russell Dodds
 * @version $Revision: 1.5 $
 */
public interface ToolRenderer {

    /**
     * Draw the entity's graphics to the screen. Do not draw selection
     * highlights at this point in time. That will be done in the
     * {@link #drawSelection} method.
     *
     * @param g2d The graphics object used to draw
     * @param eWrapper The entity to draw
     */
    void draw(Graphics2D g2d, GTEntityWrapper eWrapper);

    /**
     * Draw the entity's selection representation to the screen.
     * Implementations may assume that the entity is checked for selection
     * state before this method is called, and only selected entities will be
     * drawn.
     *
     * @param g2d The graphics object used to draw
     * @param eWrapper The entity to draw
     */
    void drawSelection(Graphics2D g2d, GTEntityWrapper eWrapper);

    /**
     * Get the width of the tool to be drawn in pixels.
     *
     * @return int A non-negative size in pixels
     */
    int getWidth();

    /**
     * Get the height of the tool to be drawn in pixels.
     *
     * @return int A non-negative size in pixels
     */
    int getHeight();

    /**
     * Set an error reporter instance for any rendering/parsing information.
     * Setting a null value returns back to the default reporter.
     *
     * @param reporter The reporter instance to add
     */
    void setErrorReporter(ErrorReporter reporter);
}
