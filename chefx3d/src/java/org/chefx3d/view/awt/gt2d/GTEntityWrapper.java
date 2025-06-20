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
import java.awt.*;

import javax.swing.JComponent;
import org.geotools.geometry.jts.ReferencedEnvelope;
import java.awt.geom.AffineTransform;

import java.awt.image.ImageObserver;

// Internal Imports
import org.chefx3d.model.Entity;

import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A holder of entity data for the GeoTools backed View.
 *
 * TODO: This is an AWT specific class. It will need a SWT counterpart.
 *
 * @author Rex Melton
 * @version $Revision: 1.10 $
 */
public class GTEntityWrapper implements ImageObserver {

    /** The entity that this object is wrapping */
    private Entity entity;

    private int width;

    private int height;

    private int screenX;

    private int screenY;

    private int screenCenterX;

    private int screenCenterY;

    private float scaleX;

    private float scaleY;

    private AffineTransform xform;

    private int headingDegrees;

    private float headingRadians;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * entity position in world coordinates, maintained for transient operations
     */
    private double[] worldPosition;

    private ReferencedEnvelope mapArea;

    private JComponent mapPanel;

    /** Flag holding the current selection state of this entity */
    private boolean selected;

    /** Flag holding the current show children state of this entity */
    private boolean childrenShown;

    /**
     * Flag to say if this is a fixed size object or variable size. Fixed
     * sized objects always have the same pixel dimensions on screen.
     */
    private final boolean fixedSize;

    /**
     *
     * @param entity
     * @param width
     * @param height
     * @param screenX
     * @param screenY
     * @param scaleX
     * @param scaleY
     * @param mapArea
     * @param mapPanel
     * @param selected
     * @param fixedSize true if this is to be constant size on screen
     */
    public GTEntityWrapper(Entity entity, int width, int height, int screenX, int screenY,
            float scaleX, float scaleY, ReferencedEnvelope mapArea, JComponent mapPanel,
            boolean selected, boolean fixedSize) {

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        this.entity = entity;
        this.width = width;
        this.height = height;

        this.screenX = screenX;
        this.screenY = screenY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.mapArea = mapArea;
        this.mapPanel = mapPanel;
        this.selected = selected;
        this.fixedSize = fixedSize;

        screenCenterX = (int) (width / 2 * scaleX);
        screenCenterY = (int) (height / 2 * scaleY);

        xform = new AffineTransform();

        worldPosition = new double[3];
        childrenShown = false;
    }

    //----------------------------------------------------------
    // Methods defined by ImageObserver
    //----------------------------------------------------------

    /**
     * Check the images as they load.
     *
     * @param img The image to check status.
     * @param x The horizontal position.
     * @param y The vertical position.
     * @param width The image width.
     * @param height The image height.
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {

        if (width == -1) {
            return true;
        } else {
            updateTransform();
            return false;
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the entityID.
     *
     * @return The entityID
     */
    public int getEntityID() {
        return entity.getEntityID();
    }

    /**
     * Get the entity.
     *
     * @return The entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Get the translation
     *
     * @return
     */
    public AffineTransform getXform() {
        return xform;
    }

    /**
     * See if this is a fixed size wrapper or can vary with zoom level.
     *
     * @return true if this is fixed in size
     */
    public boolean isFixedSize() {
        return fixedSize;
    }

    /**
     * Check to see if this entity (and thus it's wrapper), is currently
     * selected.
     *
     * @return true if this is currently selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Changed the selected state of this wrapper based on the entity's state
     *
     * @param bool The selection state
     */
    public void setSelected(boolean bool) {
        selected = bool;
    }

    /**
     * Set the flag that says the children of the entity that this wrapper
     * holds should be shown on screen.
     *
     * @param state true to make the children be shown on screen
     */
    public void setChildrenShown(boolean state) {
        childrenShown = state;
    }

    /**
     * Get the status of whether children should be shown on screen. By default
     * they are not shown on screen.
     *
     * @return true to have the children be shown on screen
     */
    public boolean getChildrenShown() {
        return childrenShown;
    }

    /**
     * Change the screen position of this entity in pixel coordinates.
     * Note that changing the screen position requires explicit recalculation
     * of the transform.
     *
     * @param x The center position in pixels
     * @param y The center position in pixels
     */
    public void setScreenPosition(int x, int y) {
        screenX = x;
        screenY = y;

        screenCenterX = (int) (width / 2 * scaleX);
        screenCenterY = (int) (height / 2 * scaleY);
    }

    /**
     * Return the position of the entity in screen coordinates in the argument
     * array.
     *
     * @param position The array to initialize with the entity position in
     *        screen coordinates.
     */
    public void getScreenPosition(int[] position) {
        position[0] = screenX;
        position[1] = screenY;
    }

    /**
     * Set the position of the entity in world coordinates, used for transient
     * ops.
     *
     * @param position The new position of the entity in world coordinates.
     */
    public void setWorldPosition(double[] position) {
        worldPosition[0] = position[0];
        worldPosition[1] = position[1];
        worldPosition[2] = position[2];
    }

    /**
     * Return the position of the entity in world coordinates in the argument
     * array.
     *
     * @param position The array tio initialize with the entity position in
     *        world coordinates.
     */
    public void getWorldPosition(double[] position) {
        position[0] = worldPosition[0];
        position[1] = worldPosition[1];
        position[2] = worldPosition[2];
    }

    /**
     * Set the heading. Once this has been called, an explicit update of the
     * transform must be made.
     *
     * @param angle The rotation in degrees
     */
    public void setHeading(int angle) {
        headingDegrees = angle;
        headingRadians = angle / 180.0f * (float) Math.PI;
    }

    /**
     * Return the heading.
     *
     * @return The rotation in degrees
     */
    public int getHeading() {
        return headingDegrees;
    }

    /**
     * Return the heading.
     *
     * @return The rotation in radians
     */
    public float getHeadingRadians() {
        return headingRadians;
    }

    /**
     * Set the scale. Once this has been called, an explicit update of the
     * transform must be made.
     *
     * @param x The x scale factor
     * @param y The y scale factor
     */
    public void setScale(float x, float y) {
        scaleX = x;
        scaleY = y;

        int newScreenCenterX = (int) (width * 0.5f * scaleX);
        int newScreenCenterY = (int) (height * 0.5f * scaleY);

        screenX += (screenCenterX - newScreenCenterX);
        screenY += (screenCenterY - newScreenCenterY);

        screenCenterX = newScreenCenterX;
        screenCenterY = newScreenCenterY;
    }

    /**
     * Get the scale.
     *
     * @param x The x scale factor
     * @param y The y scale factor
     */
    public void getScale(float x, float y) {
        x = scaleX;
        y = scaleY;
    }

    /**
     * Gets the current scaled width of the 2D icon being displayed
     *
     * @return size in pixels
     */
    public int getIconWidth() {
        if (fixedSize) {
            return width;
        }

        return Math.round(width * scaleX);
    }

    /**
     * Gets the current scaled height of the 2D icon being displayed
     *
     * @return size in pixels
     */
    public int getIconHeight() {
        if (fixedSize) {
            return height;
        }
        return Math.round(height * scaleY);
    }

    /**
     * Convert mouse coordinates into world coordinates.
     *
     * TODO: This depends on iconCenterX which per tool.  Needs to be passed in.
     *
     * @param panelX The panel x coordinate
     * @param panelY The panel y coordinate
     * @param position The world position in meters
     */
    public void convertScreenPosToWorldPos( int panelX, int panelY, double[] position ) {

        // the map extent
        double mapWidth = mapArea.getWidth( );
        double mapHeight = mapArea.getHeight( );

        Rectangle panelBounds = mapPanel.getBounds( );

        // the dimensions of the panel
        double panelWidth = panelBounds.getWidth( );
        double panelHeight = panelBounds.getHeight( );

        // translate the mouse position to map coordinates
        double x = ( panelX * mapWidth / panelWidth ) + mapArea.getMinX( );
        double y = ( panelY * mapHeight / panelHeight ) + mapArea.getMinY( );

        position[0] = x;
        position[1] = 0;
        position[2] = y;
    }

    /**
     * Convert world coordinates in meters to panel pixel location.
     *
     * TODO: This depends on iconCenterX which per tool.  Needs to be passed in.
     *
     * @param position World coordinates
     * @param pixel Mouse coordinates
     */
    public void convertWorldPosToScreenPos( double[] position, int[] pixel ) {

        // mapArea seems to be null a lot
        if (mapArea == null)
            return;

        // the map extent
        double mapWidth = mapArea.getWidth( );
        double mapHeight = mapArea.getHeight( );

        Rectangle panelBounds = mapPanel.getBounds( );

        // the dimensions of the panel
        double panelWidth = panelBounds.getWidth( );
        double panelHeight = panelBounds.getHeight( );

        // convert world coordinates to panel coordinates
        int x = (int)Math.round( ( ( position[0] - mapArea.getMinX( ) ) * panelWidth ) / mapWidth );
        int y = (int)Math.round( ( ( position[2] + mapArea.getMaxY( ) ) * panelHeight ) / mapHeight );

        pixel[0] = x;
        pixel[1] = y;
    }

    public void setMapArea(ReferencedEnvelope mapArea) {
        this.mapArea = mapArea;
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Update the transform based on current parameters.
     */
    public void updateTransform() {

        float heading;

        //TODO: what if this is not along the Y-axis?
        // need to project the rotation into 2D space
        heading = -headingRadians;

        xform.setToIdentity();

        if (fixedSize) {

// TODO: Need to fix up the fixed size code.
//            scaledImageX = screenX - (float) (width / 2);
//            scaledImageY = screenY - (float) (height / 2);
            float scaledImageX = screenX ;
            float scaledImageY = screenY;

            xform.translate(scaledImageX, scaledImageY);
            xform.rotate(heading);
            xform.scale(1, 1);
        } else {
            xform.translate(screenX, screenY);
            xform.rotate(heading);
            xform.scale(scaleX, scaleY);
        }
    }
}
