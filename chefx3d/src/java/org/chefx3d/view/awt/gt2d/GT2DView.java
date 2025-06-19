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

// External imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.Float.parseFloat;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import static java.util.Collections.unmodifiableList;

import javax.swing.*;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.*;

// Explicitly listed due to clash with java.util.List
import org.chefx3d.actions.awt.DeleteAction;
import org.chefx3d.actions.awt.HighlightAssociatesAction;


// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.model.EntityBuilder;

import org.chefx3d.tool.Tool;
import static org.chefx3d.tool.Tool.mapType;
import org.chefx3d.toolbar.ToolBarManager;
import static org.chefx3d.toolbar.ToolBarManager.getToolBarManager;
import static org.chefx3d.util.DefaultErrorReporter.getDefaultReporter;
import org.chefx3d.util.ErrorReporter;
import static org.chefx3d.util.ImageLoader.loadImage;
import static org.chefx3d.util.PropertyTools.fetchSystemProperty;
import org.chefx3d.view.View;
import static org.chefx3d.view.ViewManager.getViewManager;
import static org.chefx3d.view.awt.gt2d.GT2DUtils.getScale;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.gce.imagepyramid.ImagePyramidReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.measure.CoordinateFormat;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Element;

/**
 * A View which is backed by a Geotools referenced image.
 *
 * @author Rex Melton
 * @version $Revision: 1.47 $
 */
public class GT2DView extends JPanel
    implements View, ModelListener, EntityChangeListener, EntityPropertyListener,
        MouseMotionListener, MouseListener, MouseWheelListener, KeyListener,
        ActionListener, ChangeListener, Runnable {

    private static enum MouseMode {NONE, NAVIGATION, PLACEMENT, SELECTION, ASSOCIATE};

    /** The initial nav control location within the map panel */
    private static final Point NAV_CONTROL_LOCATION = new Point(8, 8);

    /**
     * The distance in pixels away from a entity center that will result in the
     * entity being selected.
     */
    private static final int SELECTION_RADIUS = 15;

    /**
     * The distance in pixels away from a vertex that will result in the vertex
     * being picked.
     */
    private static final int VERTEX_PICK_RADIUS = 10;

    /**
     * The distance in pixels away from the segment that will result in that
     * segment being considered picked.
     */
    private static final int SEGMENT_PICK_DISTANCE = 6;

    /** MouseWheel sensitivity for rotation changes.  0 = No Change, multiplier */
    private static final int MOUSEWHEEL_STEPUP = 10;

    /** The minimum size an icon can be in pixels */
    private static final int ICON_MINIMUM = 12;

    /**
     * The minimum time to pass on a zoom call before we recalculate all
     * the entity state and size. If we get another update in less than this
     * delay time, we do not recalculate anything. Time is in milliseconds.
     */
    private static final int ZOOM_UPDATE_CHECK_DELAY = 300;

    /** Property name for association image */
    private static final String ASSOCIATION_2D_IMAGE_PROPERTY = "ASSOCIATION2D.image";

    /** Default fly button image */
    private static final String DEFAULT_ASSOCIATION_IMAGE = "images/2d/associateIcon.png";

    /** Default open hand cursor image file */
    private static final String DEFAULT_OPEN_HAND_CURSOR_IMAGE = "images/2d/openHandCursor.png";

    /** Default closed hand cursor image file */
    private static final String DEFAULT_CLOSED_HAND_CURSOR_IMAGE = "images/2d/closedHandCursor.png";

    /** An empty list used for clearing selections */
    private static final List<Entity> EMPTY_ENTITY_LIST =
        unmodifiableList(new ArrayList<Entity>());

    /** current state for mouse clicks */
    private MouseMode currentMode;

    /** previous state for mouse clicks */
    private MouseMode previousMode;

    // Debug variables
    private boolean showIconCenter = false;

    /** The world model */
    private WorldModel model;

    /** A map of image url to cached Image */
    private Map<String, Image> imageMap;

    /** The current tool image */
    private Image toolImage;

    /** The current tool segment image */
    private Image toolSegmentImage;

    /** The current tool */
    private Tool currentTool;

    /** The current tools width in pixels */
    private int imgWidth;

    /** The current tools length in pixels */
    private int imgHeight;

    /** Current mouseX */
    private int mouseX;

    /** Current mouseY */
    private int mouseY;

    /**
     * The time that the last zoom updated. Value is extracted from
     * System.currentTimeMillis.
     */
    private long lastZoomUpdateTime;

    /** The entity ID  to display(GTEntityWrapper) */
    private Map<Integer, GTEntityWrapper> entityWrapperMap;

    /** The entity ID to entity proper */
    private Map<Integer, Entity> entityMap;

    /** A list of all the entity wrappers used for fast listing of all items */
    private List<GTEntityWrapper> wrapperList;

    /** The tool transform */
    private AffineTransform toolTransform;

    /** The blank cursor */
    private Cursor blankCursor;

    /** The associate cursor */
    private Cursor associateCursor;

    /** Open hand cursor, the default navigate mode cursor. */
    private Cursor openHandCursor;

    /** Closed hand cursor, used when doing a pan drag operation */
    private Cursor closedHandCursor;

    /** Cross hair cursor, used when doing a select area operation */
    private Cursor crossHairCursor;

    /** Are we in associate mode */
    private boolean associateMode;

    /** The valid types for the next associate */
    private String[] validTools;

    /** Are we inside the Map area */
    private boolean insideMap;

    /** The current entities that are selected according to the model */
    private List<Entity> selectedEntities;

    /** Is a drag of an entity ongoing */
    private boolean entityDragging;

    /** Is a rotation of an entity ongoing */
    private boolean entityRotating;

    /** If x is a factor of y then swap */
    private boolean swap;

    /** The current icon scaleX */
    private float iconScaleX;

    /** The current icon scaleY */
    private float iconScaleY;

    /** The current icon centerX */
    private int iconCenterX;

    /** The current icon centerY */
    private int iconCenterY;

    /** The directory to load images from */
    private String imgDir;

    /** Is the shift key active currently */
    private boolean shiftActive;

    /** Are we in a fixed segment authoring mode */
    //private boolean fixedMode;

    /** Should we ignore a drag motion.  Ignores till next mouse release */
    private boolean ignoreDrag;

    /** What is the fixed segment length */
    private float segmentLength;

    /** Scratch coordinate - the last segment position in screen space */
    private int[] lastSegmentPosition;

    /** Are we in a multi-segment operation */
    private boolean multiSegmentOp;

    /** The current highlighted vertexID */
    //private int highlightedVertexID;

    /** The image size */
    private Dimension imageSize;

    /** What is the active button */
    private int activeButton;

    /** How are helper objects displayed */
    private int helperMode;

    /** A scratch screen position */
    private int[] screenPos;

    /** Scratch position */
    private double[] tmpPos;

    /** A scratch rotation */
    private float[] tmpRot;

    /** A scratch scale */
    private float[] tmpScale;

    /** A scratch center in pixels */
    private int[] tmpCenter;

    /** The unique viewID */
    private long viewID;

    /** The starting position of entity for transient actions */
    private double[] startPos;

    /** The starting rotation of the entity */
    private float[] startRot;

    /** The current transactionID for transient commands.  Assume only one can be active. */
    private int transactionID;

    /** Are we in a transient command */
    private boolean inTransient;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The ToolBarManager */
    private ToolBarManager toolBarManager;

    //////////////////////////////////////////////////////////////////////////////

    /** The base map image context to render */
    private MapContext baseContext;

    /** The area (bounds) of the map to draw. The map coordinate boundaries
    *  used by the renderer */
    private ReferencedEnvelope mapArea;

    /** The last map area drawn. */
    private ReferencedEnvelope oldMapArea;

    /** The referencing system in use */
    private CoordinateReferenceSystem crs;

    /** Constant defining how far to zoom in or out per level */
    private double zoomFactor;

    /** The current zoom level */
    private int zoomLevel;

    /** The current scale of the map view. Typically meters per pixel */
    private double mapScale;

    /** The location of the mouse at the last press or drag event, used for the
     *  initial condition for a pan controlled by a mouse drag and for moving
     *  entities */
    private Point lastMousePoint;

    /** Formatter of map position coordinates */
    private CoordinateFormat coordFormat;

    /** Position coordinate used by formatter */
    private DirectPosition2D position;

    /** Factory for producing grid coverage of entities layer */
    private GridCoverageFactory gcf;

    /** The style type used for coverage image layers */
    private Style rasterStyle;

    /** The panel containing the rendered map */
    private ImagePanel mapPanel;

    /** UI controls to switch modes between pick & place and navigation modes */
    private JToolBar toolBar;

    /** Group for the mode switch buttons */
    private ButtonGroup modeGroup;

    /** Control to enable the pick & place mode */
    private JToggleButton pickAndPlaceButton;

    /** Flag indicating that pick & place mode is active */
    //private boolean pickAndPlaceIsActive;

    /** Control to enable the navigation mode */
    private JToggleButton navigateButton;

    /** Flag indicating that navigation mode is active */
    //private boolean navigateIsActive;

    /** Group for the navigate switch buttons */
    private ButtonGroup navigateGroup;

    /** Control to enable the bounding function */
    private JToggleButton boundButton;

    /** Flag indicating that select area function is active */
    private boolean boundIsActive;

    /** Flag indicating that a select area drag is in progress */
    private boolean boundInProgress;

    /** Rectangle containing the bound function area */
    private Rectangle boundRectangle;

    /** Control to enable pan navigation mode */
    private JToggleButton panButton;

    /** Flag indicating that the pan navigation function is active */
    private boolean panIsActive;

    /** Flag indicating that a pan drag is in progress */
    private boolean panInProgress;

    /** The status display of map coordinates */
    private JTextField statusField;

    /** The status display of map scale */
    private JTextField scaleField;

    /** Progress bar for loading images */
    private JProgressBar progressBar;

    /** The pan/zoom control */
    private GTPanZoomControl navControl;

    /** Flag indicating that the current mouse position is over the pan/zoom control */
    private boolean isOverNavControl;

    /** The location identifier string parameter for the image reader initialization thread. */
    private String url_string;

    /** GT Utility class instance */
    private GT2DUtils gtUtils;

    /** Flag indicating that the map renderer and context are ready */
    private boolean mapIsAvailable;

    /** Flag indicating a location has been set */
    private boolean locationSelected;

    /** a map of entities to renderers */
    private EntityRendererMapper entityRendererMapper;

    /** Was there a mousePress/Release event before the click this "frame" */
    private boolean inMousePressed;

    ///////////////////////////////////
    // Inner classes
    ///////////////////////////////////

    /**
     * An action inner class that is used to show and hide the children being
     * rendered for a given entity.
     */
    class ShowChildrenAction extends AbstractAction {
        /** Title when the wrapper does not have children shown currently */
        private static final String SHOW_TITLE = "Show children";

        /** Title when the wrapper has children shown currently */
        private static final String HIDE_TITLE = "Hide children";

        /** The wrapper that this will work on */
        private GTEntityWrapper entityWrapper;

        /**
         * Construct a new action that handles the given wrapper and
         * talks to the map panel
         */
        ShowChildrenAction(GTEntityWrapper eWrapper, ImagePanel map) {
            super(eWrapper.getChildrenShown() ? HIDE_TITLE : SHOW_TITLE);

            entityWrapper = eWrapper;

            // Only enable this if there are children to show
            Entity entity = eWrapper.getEntity();
            setEnabled(entity.hasChildren());
        }

        //---------------------------------------------------------
        // Method defined by ActionListener
        //---------------------------------------------------------

        /**
         * Process the mouse click action on this menu item.
         *
         * @param evt The event that caused this method to be called
         */
        @Override
        public void actionPerformed(ActionEvent evt) {
            entityWrapper.setChildrenShown(!entityWrapper.getChildrenShown());
            mapPanel.entityUpdateRequired();
        }
    }

    /**
     * Image handler panel used to render the specific information of the map.
     */
    class ImagePanel extends JComponent {

        /**
         * The GeoTools map renderer. Renders each layer to a common coordinate
         * reference frame
         */
        private GTRenderer renderer;

        /** The size of the view panel last time we drew */
        private Rectangle previousPanelBounds;

        /** Flag to say that this object must be restarted */
        private boolean reset;

        /**
         * The base image of the map. Only updated when the coverage
         * area changes or that the bounds of the window changes.
         */
        private BufferedImage baseImage;

        /**
         * Composited version of the image that has the base image plus
         * all of the entities rendered on it. It does not contain the
         * current tool icon image.
         */
        private BufferedImage entityImage;

        /**
         * Flag to indicate that the coverage area has been recalculated
         * and a new image needs to be fetched from GeoTools.
         */
        private boolean coverageChanged;

        /**
         * One or more entities have changed and so need to be redrawn. In
         * this case we need to clear the base image and re-render everything
         * again in the next frame, rather than just blitting the current
         * composited entityImage.
         */
        private boolean entitiesChanged;

        /**
         * Construct a default instance of this class.
         */
        ImagePanel() {
            setBackground(Color.GRAY);
            coverageChanged = true;
            entitiesChanged = true;

            // The renderer for all the map layers
            renderer = new StreamingRenderer();

            // feel the need for speed
            RenderingHints hints =
                new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                                   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
            hints.put(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
            hints.put(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_DISABLE);

            renderer.setJava2DHints(hints);
        }

        //----------------------------------------------------------
        // Methods defined by JComponent
        //----------------------------------------------------------

        /**
         * Standard override of the update method to prevent clearing of
         * of the underlying area since we are going to be covering the entire
         * window area.
         */
        @Override
        public void update(Graphics g) {
            paint(g);
        }

        /**
         * Change the bounds of the panel.
         *
         * @param x The new x location of this component
         * @param y The new y location of this component
         * @param width The new width of the component
         * @param height The new height of the component
         */
        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);

            updateBufferImages();
            updateMapArea();
            updateEntityScaleAndZoom();
        }

        /**
         * Change the size of this component.
         *
         * @param width The new width of the component
         * @param height The new height of the component
         */
        @Override
        public void setSize(int width, int height) {
            setSize(new Dimension(width, height));
        }

        /**
         * Change the size of this component.
         *
         * @param width The new width of the component
         * @param height The new height of the component
         */
        @Override
        public void setSize(Dimension d) {
            super.setSize(d);

            updateBufferImages();
            updateMapArea();
            updateEntityScaleAndZoom();
        }

        /**
         * Canvas painting
         *
         * @param g canvas graphics object
         */
        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);

            if (!mapIsAvailable)
                return;

            if (coverageChanged) {
                // render the new coverage image
                Rectangle panelBounds = getBounds();
                panelBounds.x = 0;
                panelBounds.y = 0;

                Graphics2D ig = baseImage.createGraphics();
                ig.setColor(Color.GRAY);
                ig.fillRect(0, 0, panelBounds.width, panelBounds.height);

                renderer.setContext(baseContext);

                // Something in this call goes null occasionally, or if you
                // crop down too far causing the crop bounds go empty
                try {
                    renderer.paint(ig, panelBounds, mapArea);
                } catch (NullPointerException | IllegalArgumentException e) {
                    return;
                }
            }

            if(entitiesChanged || coverageChanged) {
                Graphics2D eg = entityImage.createGraphics();
                AffineTransform defaultTransform = eg.getTransform();

                eg.drawImage(baseImage, 0, 0, null);

                if (helperMode != View.HELPER_NONE) {
                    for (GTEntityWrapper eWrapper : wrapperList) {
                        if(!eWrapper.getEntity().isHelper()) {
                            continue;
                        }

                        // translate eg
                        eg.setTransform(eWrapper.getXform());

                        // render object
                        ToolRenderer toolRenderer =
                                entityRendererMapper.getRenderer(eWrapper.getEntity());
                        toolRenderer.draw(eg, eWrapper);
                    }
                }

                // Not really needed, but this is a fail-safe just in case
                // something in the above loop goes completely haywire.
                eg.setTransform(defaultTransform);

                for (GTEntityWrapper eWrapper : wrapperList) {
                    if (eWrapper.getEntity().isHelper()) {
                        continue;
                    }

//                    if (eWrapper == null) {
//                        continue;
//                    }

                    // translate eg
                    eg.setTransform(eWrapper.getXform());

                    // render object
                    ToolRenderer toolRenderer =
                            entityRendererMapper.getRenderer(eWrapper.getEntity());
                    toolRenderer.draw(eg, eWrapper);
                }

                // Not really needed, but this is a fail-safe just in case
                // something in the above loop goes completely haywire.
                eg.setTransform(defaultTransform);

                entitiesChanged = false;
                coverageChanged = false;
            }

            // Now draw everything to the main window.
            Graphics2D g2d = (Graphics2D)g;
            g2d.drawImage(entityImage, 0, 0, null);

            AffineTransform defaultTransform = g2d.getTransform();

            for (Entity entity : selectedEntities) {
                GTEntityWrapper eWrapper =
                        entityWrapperMap.get(entity.getEntityID());


                if (eWrapper == null) {
                    continue;
                }

                g2d.setTransform(defaultTransform);
                g2d.transform(eWrapper.getXform());

                // render object
                ToolRenderer toolRenderer =
                        entityRendererMapper.getRenderer(eWrapper.getEntity());
                toolRenderer.drawSelection(g2d, eWrapper);
            }

            // Restore any transforms applied above
            g2d.setTransform(defaultTransform);

            // Draw the tool image/icon next if we have one set.
            if (insideMap && toolImage != null) {
                g2d.drawImage(toolImage, toolTransform, null);

                if (showIconCenter) {
                    g2d.setColor(Color.red);
                    g2d.drawRect(mouseX + iconCenterX, mouseY + iconCenterY,3,3);
                }
            }

            if (boundInProgress) {
                // an area selection is in progress,
                // draw the bounding rectangle
                g.setColor(Color.RED);
                int width = boundRectangle.width;
                int height = boundRectangle.height;
                int x = boundRectangle.x;
                int y = boundRectangle.y;
                if (width < 0) {
                    x += width;
                    width = (-width);
                }
                if (height < 0) {
                    y += height;
                    height = (-height);
                }
                g.drawRect(x, y, width, height);

            }

            if (currentMode == MouseMode.NAVIGATION || currentMode == MouseMode.SELECTION) {
                // navigate mode is active, paint the
                // control on top of the map area
                navControl.paintComponent(g2d);
            }
        }

        //----------------------------------------------------------
        // Local Methods
        //----------------------------------------------------------

        /**
         * Method to force the panel to reset all the zoom and may coverage
         * handlin.
         */
        void reset() {
            reset = true;
        }

        /**
         * Inform the panel that one or more entities have been updated and
         * need to be repainted.
         */
        void entityUpdateRequired() {
            entitiesChanged = true;
            repaint();
        }

        /**
         * Inform the panel that the map area (coverage) has been updated and
         * needs to be repainted.
         */
        void coverageUpdateRequired() {
            coverageChanged = true;
            repaint();
        }

        /**
         * Update the information about the entities and bounds.
         */
        void updateMapArea() {

            if (!mapIsAvailable)
                return;

            // rem: MUST clear the panel bounds x & y as they are a measure
            // of the relative position of this panel to it's parent - and
            // this will FU the renderer if not zero'ed
            Rectangle panelBounds = getBounds();
            panelBounds.x = 0;
            panelBounds.y = 0;

            // check to see if the coverage image must be regenerated
            boolean coverageChange = false;

            if (!panelBounds.equals(previousPanelBounds) || reset) {

                // the viewer size has changed
                coverageChange = true;
                reset = false;

                if ((previousPanelBounds == null)) {

                    // establish the initial conditions on loading a location
                    previousPanelBounds = panelBounds;

                    // the dimensions of the panel
                    double panelWidth = panelBounds.getWidth();
                    double panelHeight = panelBounds.getHeight();

                    // the map extent
                    double mapWidth = mapArea.getWidth();
                    double mapHeight = mapArea.getHeight();

                    // calculate the new scale
                    double scaleX = panelWidth / mapWidth;
                    double scaleY = panelHeight / mapHeight;

                    // use the smaller scale
                    if (scaleX < scaleY) {
                        mapScale = 1.0 / scaleX;
                    } else {
                        mapScale = 1.0 / scaleY;
                    }

                    // the difference in width and height of the new extent divided by 2
                    double deltaX2 = ((panelWidth * mapScale) - mapWidth) * 0.5;
                    double deltaY2 = ((panelHeight * mapScale) - mapHeight) * 0.5;

                    // the new region of the map to display
                    mapArea = new ReferencedEnvelope(
                        mapArea.getMinX() - deltaX2,
                        mapArea.getMaxX() + deltaX2,
                        mapArea.getMinY() - deltaY2,
                        mapArea.getMaxY() + deltaY2,
                        crs);

                } else {
                    coverageChanged = true;
                    mapArea = gtUtils.rescaleMapArea(
                        mapArea,
                        previousPanelBounds,
                        panelBounds);
                    mapScale = getScale(mapArea, panelBounds);
                    previousPanelBounds = panelBounds;
                }
            }

            // check if the map extent changed
            if (!mapArea.equals(oldMapArea)) {

                // a pan or zoom has occured
                if (!coverageChanged)
                    mapScale = getScale(mapArea, panelBounds);

                coverageChanged = true;
                oldMapArea = mapArea;
            }
        }

        /**
         * The window has changed size, so re-size the buffer images to the
         * new size.
         */
        private void updateBufferImages() {
            Rectangle panelBounds = getBounds();

            baseImage = new BufferedImage(
                panelBounds.width,
                panelBounds.height,
                BufferedImage.TYPE_INT_ARGB);
            entityImage = new BufferedImage(
                panelBounds.width,
                panelBounds.height,
                BufferedImage.TYPE_INT_ARGB);
        }
    } // End of MapPanel inner class


    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor
     * @param model
     * @param imageDir
     */
    public GT2DView(WorldModel model, String imageDir) {
        errorReporter = getDefaultReporter();

        setImageDirectory(imageDir);

        zoomFactor = 1.5;
        mapScale = 1.0;

        this.model = model;
        helperMode = View.HELPER_SELECTED;
        model.addModelListener(GT2DView.this);

        entityMap = new HashMap<>();
        entityWrapperMap = new HashMap<>();
        selectedEntities = new ArrayList<>();
        wrapperList = new ArrayList<>();

        toolTransform = new AffineTransform();

        imageMap = new HashMap<>();

        Toolkit tk = getDefaultToolkit();

        String imgName = fetchSystemProperty(ASSOCIATION_2D_IMAGE_PROPERTY, DEFAULT_ASSOCIATION_IMAGE);
        Image img = loadImage(imgName);
        associateCursor = tk.createCustomCursor(img, new Point(), null);

        img = new ImageIcon("blankCursor").getImage();
        blankCursor = tk.createCustomCursor(img, new Point(), null);

        img = tk.createImage(getSystemResource(DEFAULT_OPEN_HAND_CURSOR_IMAGE));
        openHandCursor = tk.createCustomCursor(img, new Point(6, 4), null);

        img = tk.createImage(getSystemResource(DEFAULT_CLOSED_HAND_CURSOR_IMAGE));
        closedHandCursor = tk.createCustomCursor(img, new Point(7, 7), null);

        crossHairCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        toolBarManager = getToolBarManager();

        position = new DirectPosition2D();
        gcf = new GridCoverageFactory();

        initUI();

        tmpPos = new double[3];
        startPos = new double[3];
        entityRotating = false;
        startRot = new float[4];
        tmpRot = new float[4];
        tmpScale = new float[2];
        tmpCenter = new int[2];
        swap = false;
        iconScaleX = 0.25f;
        iconScaleY = 0.25f;
        multiSegmentOp = false;
        screenPos = new int[3];
        lastSegmentPosition = new int[3];
        viewID = (long) (random() * Long.MAX_VALUE);
        imageSize = new Dimension(512,512);
        inTransient = false;
        locationSelected = false;
        inMousePressed = false;

        mapIsAvailable = false;

        // default the MouseMode to an uninitialized state
        currentMode = MouseMode.NONE;
        previousMode = MouseMode.NONE;

        toolTransform.scale(iconScaleX,iconScaleY);

        getViewManager().addView(GT2DView.this);

        entityRendererMapper = new DefaultEntityRendererMapper();
    }

    /**
     * Set the mappings between entities and renderers. A null value clears
     * the current mapper instance and returns to the default mapping.
     *
     * @param mapper The mapper instance to use or null
     */
    public void setEntityRendererMapper(EntityRendererMapper mapper) {
        entityRendererMapper = mapper;

        if(entityRendererMapper == null)
            entityRendererMapper = new DefaultEntityRendererMapper();
    }

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    @Override
    public void setTool(Tool tool) {

        if (tool == null) {
            return;
        }

        int type = tool.getToolType();

//System.out.println("Got tool: " + tool + "(" + type + ")");
        if (type == Tool.TYPE_WORLD) {

            setMode(MouseMode.NAVIGATION, true);

            locationSelected = true;

            return;

        } else {

            // check to make sure work has been added, this is in case the
            // file was loaded.
            if (!locationSelected) {
                Entity[] entities = model.getModelData();
                for (Entity entitie : entities) {
                    if ((entitie != null) && (entitie.getType() == Tool.TYPE_WORLD)) {
                        locationSelected = true;
                        break;
                    }
                }
            }

            if (locationSelected) {

                getViewManager().disableAssociateMode();

                multiSegmentOp = type == Tool.TYPE_MULTI_SEGMENT;
                setMode(MouseMode.PLACEMENT, true);
            } else {
                showMessageDialog(this,
                    "You cannot add a new item until a Location has been selected.",
                    "Add Item Action",
                    JOptionPane.ERROR_MESSAGE);

                mapPanel.repaint();
                return;
            }
        }

        toolImage = getImage(tool.getIcon());
        imgWidth = toolImage.getWidth(null);
        imgHeight = toolImage.getHeight(null);

        float toolWidth = (float) tool.getXSize();
        float toolLength = (float) tool.getZSize();

        if (imgWidth < 0 || imgHeight < 0) {
            errorReporter.messageReport("Error processing icon: " + tool.getIcon());
        }

        calcScaleFactor(tool.isFixedAspect(), imgWidth, imgHeight, toolWidth, toolLength, tmpScale, tmpCenter);
        iconScaleX = tmpScale[0];
        iconScaleY = tmpScale[1];

        iconCenterX = tmpCenter[0];
        iconCenterY = tmpCenter[1];

        currentTool = tool;

        // Reset the tool
        if (!currentTool.getUserProperties().isEmpty())
            currentTool.setUserProperties(currentTool.getDefaults());

        toolSegmentImage = getImage(tool.getIcon());

    }

    /**
     * Go into associate mode.  The next selection in any view
     * will issue a selection event and do nothing else.
     */
    @Override
    public void enableAssociateMode(String[] validTools) {
        // Change to selection state
        //System.out.println("GT2DView.enableAssociateMode()");
        setMode(MouseMode.ASSOCIATE, false);
        this.validTools = validTools;

        highlightTools(validTools, true);
    }

    /**
     * Exit associate mode.
     */
    @Override
    public void disableAssociateMode() {
        setMode(MouseMode.SELECTION, true);
        associateMode = false;
        ignoreDrag = false;

        highlightTools(null, false);
    }

    /**
     * Set how helper objects are displayed.
     *
     * @param mode The mode
     */
    @Override
    public void setHelperDisplayMode(int mode) {
        helperMode = mode;

        mapPanel.repaint();
    }

    /**
     * Get the viewID.  This shall be unique per view on all systems.
     *
     * @return The unique view ID
     */
    @Override
    public long getViewID() {
        return viewID;
    }

    @Override
    public Dimension getMinimumSize() {
        return imageSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return imageSize;
    }

    /**
     * Set the physical dimensions of the image in meters.
     *
     * @param width The width
     * @param length The length
     */
    public void setPhysicalSize(float width, float length) {
        // ? redundant information ?, images are referenced from a starting
        // point and a scale factor per image unit.
    }

    //----------------------------------------------------------
    // Methods required by View
    //----------------------------------------------------------

    /**
     * Set the location.
     *
     * @param url_string The url of the map imagery.
     */
    public void setLocation(String url_string) {

        // disable the ui while the load is in process
        mapIsAvailable = false;

        setIsLoading(true);
        pickAndPlaceButton.setEnabled(false);
        navigateButton.setEnabled(false);
        boundButton.setEnabled(false);
        panButton.setEnabled(false);

        // force a recalculation of map scaling in the paint routine
        mapPanel.reset();

        // start the loader thread
        this.url_string = url_string;
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Control of the view has changed.
     *
     * @param newMode The new mode for this view
     */
    @Override
    public void controlChanged(int newMode) {
    }

    /**
     * Get the rendering component.
     *
     * @return The rendering component
     */
    @Override
    public JComponent getComponent() {
        return this;
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
            errorReporter = getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods required by ModelListener
    //----------------------------------------------------------

    /**
     * An entity was added.
     *
     * @param local Was this action initiated from the local UI
     * @param entity The entity
     */
    @Override
    public void entityAdded(boolean local, Entity entity) {

//System.out.println("GT2DView.entityAdded(" + local +"," + entity.getTool().getName() + ")");

        int entityID = entity.getEntityID();
        double[] pos = new double[] {0 ,0, 0};
        float[] rotation = new float[] {0, 0, 0 ,0};

        if (entity instanceof PositionableEntity) {
            ((PositionableEntity)entity).getPosition(pos);
            ((PositionableEntity)entity).getRotation(rotation);
        }

        GTEntityWrapper eWrapper = entityWrapperMap.get(entityID);

        // Ignore dups as they are expected in networked environments
        if (eWrapper != null) {
            return;
        }

        if (entity.getType() == Tool.TYPE_WORLD) {
            String rel_url_string = entity.getURL();

            int pos1 = rel_url_string.lastIndexOf("/");
            int pos2 = rel_url_string.lastIndexOf(".x3d");
            String baseName;

            if (pos2 > 0) {
                if (pos1 < 0)
                    pos1 = 0;

                // pos1+1 to get rid of last "/"
                baseName = rel_url_string.substring(pos1+1,pos2);
            } else {
                baseName = rel_url_string;
            }

            entityMap.clear();
            entityWrapperMap.clear();
            wrapperList.clear();

            /////////////////////////////////////////////////////////////////
            // determine whether we've been pointed to a directory,
            // which is assumed to be the source for an image pyramid.
            // if not, then we presume that the source is a single image
            // with a predetermined file extension

            url_string = imgDir + baseName;
            URL url = null;
            try {
                File dir = new File(getProperty("user.dir"));
                URL dirURL = dir.toURI().toURL();
                url = new URL(dirURL, url_string);
            } catch (MalformedURLException mue) {
                errorReporter.errorReport("Bad URL!", mue);
            }
            if (url == null) {
                return;
            }
            URI uri = null;
            try {
                uri = url.toURI();
            } catch (URISyntaxException urise) {
                errorReporter.errorReport("Bad URL!", urise);
            }
            if (uri == null) {
                return;
            }
            File target = new File(uri);
            if (target.exists() && target.isDirectory()) {
                // if a directory - assume it contains a pyramid
            } else {
                // otherwise - assume that the target is a single image
                url_string = url_string + ".png";
            }

            setLocation(url_string);
            /////////////////////////////////////////////////////////////////

            entityWrapperMap.put(entity.getEntityID(), null);
            entityMap.put(entity.getEntityID(), entity);

            resetState();
        } else {

            // Don't overwrite the wrapper if we already have one for this instance.
            // We should never get here because the entityAdded call should only
            // give us new unique instances, but we just want to be careful.
            if(entityWrapperMap.containsKey(entity.getEntityID()))
                return;


            ToolRenderer tImage = entityRendererMapper.getRenderer(entity);

            imgWidth = tImage.getWidth();
            imgHeight = tImage.getHeight();

            float[] size = new float[3];
            if (entity instanceof PositionableEntity) {
                ((PositionableEntity)entity).getSize(size);
            }

            float toolWidth = size[0];
            float toolLength = size[2];

            calcScaleFactor(entity.isFixedAspect(),
                imgWidth,
                imgHeight,
                toolWidth,
                toolLength,
                tmpScale,
                tmpCenter);

            // TODO: This will cause problems for networked events but needed for now
            iconCenterX = tmpCenter[0];
            iconCenterY = tmpCenter[1];

            // Set it up with default scale and position right now because it will
            // be corrected by updateEntityScaleAndZoom() shortly.
            GTEntityWrapper wrapper = new GTEntityWrapper(
                entity,
                imgWidth,
                imgHeight,
                0,
                0,
                1,
                1,
                mapArea,
                mapPanel,
                false,
                entity.isFixedSize());

            wrapper.setErrorReporter(errorReporter);
            wrapper.setWorldPosition(pos);

            tmpRot[0] = rotation[0];
            tmpRot[1] = rotation[1];
            tmpRot[2] = rotation[2];
            tmpRot[3] = rotation[3];

            adjustRotation(false, tmpRot);

            int angle = (int)round(tmpRot[3] * 180.0f / Math.PI);

            wrapper.setHeading(angle);

            entityWrapperMap.put(entity.getEntityID(), wrapper);
            entityMap.put(entity.getEntityID(), entity);
            wrapperList.add(wrapper);

            mapPanel.updateMapArea();
            updateEntityScaleAndZoom(wrapper);

            model.addEntityChangeListener(entity, this);
            entity.addEntityPropertyListener(this);
            mapPanel.entityUpdateRequired();
        }
    }

    /**
     * A property changed.  NO LONGER USED
     *
     * @param local Was this a local change
     * @param entityID The entity which changed
     * @param propertySheet The sheet that holds the property
     * @param propertyName The property that has changed
     * @param propertyValue The value being set.
     */
    @Override
    public void propertyChanged(boolean local, int entityID, String propertySheet,
        String propertyName, Object propertyValue) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * An entity has changed size.
     *
     * @param entityID The unique entityID assigned by the view
     * @param size The new size in meters
     */
    @Override
    public void entitySizeChanged(boolean local, int entityID, float[] size) {

        //System.out.println("GT2DView.entitySizeChanged()");
        //System.out.println("    entityID: " + entityID);
        //System.out.println("    size x: " + size[0]);
        //System.out.println("    size y: " + size[1]);
        //System.out.println("    size z: " + size[2]);

        GTEntityWrapper eWrapper = entityWrapperMap.get(entityID);
        Entity entity = eWrapper.getEntity();

        if (entity.getType() == Tool.TYPE_WORLD) {
            // TODO: ignore.  What should we do?
            return;
        }

        ToolRenderer tImage =
            entityRendererMapper.getRenderer(eWrapper.getEntity());
        imgWidth = tImage.getWidth();
        imgHeight = tImage.getHeight();
        float toolWidth = size[0];
        float toolLength = size[2];

        calcScaleFactor(entity.isFixedAspect(),
                        imgWidth,
                        imgHeight,
                        toolWidth,
                        toolLength,
                        tmpScale,
                        tmpCenter);

        iconCenterX = tmpCenter[0];
        iconCenterY = tmpCenter[1];

        // TODO: the center moving logic is causing issues with the matching 3D view.
        //errorReporter.messageReport("Got size change, new scale: " + tmpScale[0] + " " + tmpScale[1]);
        eWrapper.setScale(tmpScale[0], tmpScale[1]);
        eWrapper.updateTransform();

        mapPanel.entityUpdateRequired();
    }


    /**
     * An entity was associated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityAssociated(boolean local, int parent, int child) {
        associateMode = false;
        mapPanel.setCursor(null);

        highlightTools(null, false);
    }

    /**
     * An entity was unassociated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityUnassociated(boolean local, int parent, int child) {
        associateMode = false;

        highlightTools(null, false);
        mapPanel.setCursor(null);
    }

    /**
     * An entity was removed.
     *
     * @param entity The id
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {

        if (entity == null)
            return;

        GTEntityWrapper wrapper =
            entityWrapperMap.remove(entity.getEntityID());
        wrapperList.remove(wrapper);

        entityMap.remove(entity.getEntityID());
        mapPanel.entityUpdateRequired();
    }

    /**
     * A segment was added to the sequence.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param startVertexID The starting vertexID
     * @param endVertexID The starting vertexID
     */
    @Override
    public void segmentAdded(boolean local, int entityID,
            int segmentID, int startVertexID, int endVertexID) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * A segment was split.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param vertexID The splitting vertexID
     */
    @Override
    public void segmentSplit(boolean local, int entityID,
            int segmentID, int vertexID) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * A vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The segment removed
     */
    @Override
    public void segmentRemoved(boolean local, int entityID,
            int segmentID) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * A segment vertex was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param position The x position in world coordinates
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID, int vertexID,
        double[] position) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * A segment vertex was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param propertyValue
     */
    @Override
    public void segmentVertexUpdated(boolean local, int entityID, int vertexID,
            String propertySheet, String propertyName, String propertyValue) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * A segment vertex was added to an entity.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     * @param position The x position in world coordinates
     */
    @Override
    public void segmentVertexMoved(boolean local, int entityID, int vertexID,
        double[] position) {

        mapPanel.entityUpdateRequired();
    }

    /**
     * A segment vertex was removed.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique segmentID assigned by the view
     */
    @Override
    public void segmentVertexRemoved(boolean local, int entityID, int vertexID) {
        mapPanel.entityUpdateRequired();
    }

    /**
     * The entity moved.
     *
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D System).
     */
    @Override
    public void entityMoved(boolean local, int entityID, double[] position) {
        // Ignore networked events if we are modifying the item
        Entity entity = entityMap.get(entityID);

        if (!local && entityDragging && entity != null)
            return;

        GTEntityWrapper eWrapper = entityWrapperMap.get(entityID);
        convertWorldPosToScreenPos(position, screenPos);

        eWrapper.setWorldPosition(position);
        eWrapper.setScreenPosition(screenPos[0], screenPos[1]);
        eWrapper.updateTransform();

        mapPanel.entityUpdateRequired();
    }

    /**
     * The entity was scaled.
     *
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    @Override
    public void entityScaled(boolean local, int entityID, float[] scale) {
        mapPanel.entityUpdateRequired();
    }

    /**
     * The entity was rotated.
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
        GTEntityWrapper wrapper = entityWrapperMap.get(entityID);

        // TODO: What to do about full on rotations?

        tmpRot[0] = rotation[0];
        tmpRot[1] = rotation[1];
        tmpRot[2] = rotation[2];
        tmpRot[3] = rotation[3];

        //adjustRotation(true, tmpRot);

        int angle = (int)round(tmpRot[3] * 180.0f / Math.PI);

        wrapper.setHeading(angle);
        wrapper.updateTransform();
        mapPanel.entityUpdateRequired();
    }

    /**
     * The entity was selected.
     *
     * @param selection The list of selected entities.  The last one is the latest.
     */
    @Override
    public void selectionChanged(List<Selection> selection) {

        if (associateMode) {
            return;
        }

        for (Selection e : selection) {

            GTEntityWrapper wrapper = entityWrapperMap.get(e.getEntityID());

            // Wrapper will be null if this is the Location that is selected.
            if (wrapper != null)
                wrapper.setSelected(false);
        }

        selectedEntities.clear();

        if (!selection.isEmpty()) {
            // end the rotation action if there is one
            checkForRotation();
            mapPanel.setCursor(null);

            for (Selection s : selection) {
                int id = s.getEntityID();
                GTEntityWrapper wrapper = entityWrapperMap.get(id);

                // Wrapper will be null if this is the Location that is selected.
                if (wrapper !=  null) {
                    wrapper.setSelected(true);
                    Entity entity = entityMap.get(id);

                    selectedEntities.add(entity);
                }
            }
        }

        mapPanel.entityUpdateRequired();
    }

    /**
     * User view information changed.
     *
     * @param pos The position of the user
     * @param rot The orientation of the user
     * @param fov The field of view changed(X3D Semantics)
     */
    @Override
    public void viewChanged(boolean local, double[] pos, float[] rot, float fov) {
        // ignore
    }

    /**
     * The master view has changed.
     *
     * @param viewID The view which is master
     */
    @Override
    public void masterChanged(boolean local, long viewID) {
        // ignore
    }

    /**
     * The model has been reset.
     *
     * @param local Was this action initiated from the local UI
     */
    @Override
    public void modelReset(boolean local) {
        // clear the location panel
        mapIsAvailable = false;
        locationSelected = false;

        // disable nav buttons
        pickAndPlaceButton.setEnabled(false);
        navigateButton.setEnabled(false);
        panButton.setEnabled(false);
        boundButton.setEnabled(false);

        // reset state variables
        resetState();
        resetNavigateState();

        mapPanel.repaint();
    }

    //---------------------------------------------------------
    // Method defined by ActionListener
    //---------------------------------------------------------

    /**
     * UI event handlers
     * @param ae
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == pickAndPlaceButton) {
            if (previousMode == MouseMode.PLACEMENT) {

                if (currentTool != null) {
                    toolImage = getImage(currentTool.getIcon());
                    imgWidth = toolImage.getWidth(null);
                    imgHeight = toolImage.getHeight(null);
                    float toolWidth = (float) currentTool.getXSize();
                    float toolLength = (float) currentTool.getZSize();

                    //errorReporter.messageReport("Tool selected: " + imgWidth + " " + imgHeight);
                    if (imgWidth < 0 || imgHeight < 0) {
                        errorReporter.messageReport("Error processing icon: " + currentTool.getIcon());
                    }

                    calcScaleFactor(currentTool.isFixedAspect(), imgWidth, imgHeight, toolWidth, toolLength, tmpScale, tmpCenter);
                    iconScaleX = tmpScale[0];
                    iconScaleY = tmpScale[1];

                    iconCenterX = tmpCenter[0];
                    iconCenterY = tmpCenter[1];
                }

                setMode(MouseMode.PLACEMENT, false);
            } else if (previousMode == MouseMode.ASSOCIATE) {
                setMode(MouseMode.ASSOCIATE, false);
            } else {
                setMode(MouseMode.SELECTION, true);
            }
        } else if (source == navigateButton) {
            previousMode = currentMode;
            setMode(MouseMode.NAVIGATION, false);

        } else if (source == boundButton) {
            boundIsActive = true;
            panIsActive = false;
            mapPanel.setCursor(crossHairCursor);

        }  else if (source == panButton) {
            panIsActive = true;
            boundIsActive = false;
            mapPanel.setCursor(openHandCursor);

        } else if (source == navControl) {
            String command = ae.getActionCommand();
            switch (command) {
                case GTPanZoomControl.RESET_COMMAND:
                    try {
                        ReferencedEnvelope env = baseContext.getLayerBounds();
                        if (env != null) {
                            mapArea = env;
                            mapPanel.reset();
                            zoomLevel = navControl.getMaximum();

                            setZoomLevel(zoomLevel);
                        }
                    } catch (IOException ioe) {
                        errorReporter.errorReport("Couldn't reset Map View: ", ioe);
                    }   break;
                case GTPanZoomControl.ZOOM_IN_COMMAND:
                    incrementZoomLevel(-1);
                    break;
                case GTPanZoomControl.ZOOM_OUT_COMMAND:
                    incrementZoomLevel(1);
                    break;
                default:
                    // a panning function selected
                    double distance = 0.0;
                    double direction = 0.0;
                    switch (command) {
                        case GTPanZoomControl.PAN_UP_COMMAND:
                            distance = mapArea.getHeight() / 3.0;
                            direction = GT2DUtils.PAN_UP_DIRECTION;
                            break;
                        case GTPanZoomControl.PAN_DOWN_COMMAND:
                            distance = mapArea.getHeight() / 3.0;
                            direction = GT2DUtils.PAN_DOWN_DIRECTION;
                            break;
                        case GTPanZoomControl.PAN_LEFT_COMMAND:
                            distance = mapArea.getWidth() / 3.0;
                            direction = GT2DUtils.PAN_LEFT_DIRECTION;
                            break;
                        case GTPanZoomControl.PAN_RIGHT_COMMAND:
                            distance = mapArea.getWidth() / 3.0;
                            direction = GT2DUtils.PAN_RIGHT_DIRECTION;
                            break;
                    }
                    // the new region of the map to display
                    mapArea = gtUtils.pan(mapArea, direction, distance);
                    updateEntityScaleAndZoom();
                    break;
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by KeyListener
    //----------------------------------------------------------

    @Override
    public void keyTyped(KeyEvent ke) {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent ke) {

        int code = ke.getKeyCode();
        switch(code) {

            case KeyEvent.VK_ESCAPE:
                setMode(MouseMode.SELECTION, true);
                toolBarManager.setTool(null);
                getViewManager().disableAssociateMode();
                break;

            case KeyEvent.VK_SHIFT:
                shiftActive = true;
                break;
        }

        Entity entity = null;

        if (!selectedEntities.isEmpty()) {
            //errorReporter.messageReport("Moving multiple items is not supported at this time.");
            entity = selectedEntities.get(0);
        }

        switch (currentMode) {
            case SELECTION:
                switch(code) {

                case KeyEvent.VK_DELETE:

                    if (entity != null &&
                        entity instanceof SegmentableEntity &&
                        ((SegmentableEntity)entity).isFixedLength() &&
                        ((SegmentableEntity)entity).getSelectedVertexID() >= 0) {

                        SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();
                        int vertexID = ((SegmentableEntity)entity).getSelectedVertexID();

                        if (!segments.isEnd(vertexID) && !segments.isStart(vertexID)) {

                            showMessageDialog(this,
                                "Cannot delete internal segments in fixed mode.",
                                "Delete Action",
                                JOptionPane.WARNING_MESSAGE);

                            mapPanel.repaint();
                            return;
                        }

                    }
                    break;

                }

            case PLACEMENT:

                int toolType = 0;

                // end the rotation action if there is one
                checkForRotation();

                if (entity != null)
                    toolType = entity.getType();

                switch(code) {
                    case KeyEvent.VK_DOWN:
                        if (entity != null && !associateMode) {
                            if (toolType != Tool.TYPE_MODEL)
                                return;

                            int step;

                            if (shiftActive)
                                step = 1;
                            else
                                step = MOUSEWHEEL_STEPUP;

                            GTEntityWrapper wrapper =
                                entityWrapperMap.get(entity.getEntityID());

                            setHeading(wrapper.getHeading() + step);
                        }
                        break;

                    case KeyEvent.VK_UP:
                        if (entity != null && !associateMode) {
                            int step;

                            if (toolType != Tool.TYPE_MODEL)
                                return;

                            if (shiftActive)
                                step = 1;
                            else
                                step = MOUSEWHEEL_STEPUP;

                            GTEntityWrapper wrapper =
                                entityWrapperMap.get(entity.getEntityID());

                            setHeading(wrapper.getHeading() - step);
                        }
                        break;

                    case KeyEvent.VK_ESCAPE:

                        changeSelection(EMPTY_ENTITY_LIST);

                        break;
                }
                break;

            case NAVIGATION:

                double distance;
                double direction;

                switch (code) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_KP_UP:

                        distance = mapArea.getHeight() * 0.33;
                        direction = GT2DUtils.PAN_UP_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_KP_DOWN:

                        distance = mapArea.getHeight() * 0.33;
                        direction = GT2DUtils.PAN_DOWN_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_KP_LEFT:

                        distance = mapArea.getWidth() * 0.33;
                        direction = GT2DUtils.PAN_LEFT_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_KP_RIGHT:

                        distance = mapArea.getWidth() * 0.33;
                        direction = GT2DUtils.PAN_RIGHT_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_PAGE_UP:

                        distance = mapArea.getHeight() * 0.8;
                        direction = GT2DUtils.PAN_UP_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_PAGE_DOWN:

                        distance = mapArea.getHeight() * 0.8;
                        direction = GT2DUtils.PAN_DOWN_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_HOME:

                        distance = mapArea.getWidth() * 0.8;
                        direction = GT2DUtils.PAN_LEFT_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_END:

                        distance = mapArea.getWidth() * 0.8;
                        direction = GT2DUtils.PAN_RIGHT_DIRECTION;
                        mapArea = gtUtils.pan(mapArea, direction, distance);
                        updateEntityScaleAndZoom();
                        break;

                    case KeyEvent.VK_PLUS:
                    case KeyEvent.VK_ADD:
                        incrementZoomLevel(-1);
                        break;

                    case KeyEvent.VK_MINUS:
                    case KeyEvent.VK_SUBTRACT:
                        incrementZoomLevel(1);
                        break;
                }
                break;

        } // switch(currentMode)
    }

    /**
     * Notification that a key that was previously pressed is now released.
     *
     * @param ke The event that caused this method to be called
     */
    @Override
    public void keyReleased(KeyEvent ke) {

        int code = ke.getKeyCode();

        switch(code) {
            case KeyEvent.VK_SHIFT:
                shiftActive = false;
                break;
        }
    }

    //----------------------------------------------------------
    // Methods required by MouseWheelListener
    //----------------------------------------------------------

    /**
     * Controls entity rotation and panel zoom
     *
     * @param mwe The mouse wheel event
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {

        int wheelRotation = mwe.getWheelRotation();

        switch (currentMode) {
            case SELECTION:
            case PLACEMENT:

                // placement should only have a single selected entity
                if (selectedEntities.isEmpty()) {
                    return;
                }

                Entity entity = selectedEntities.get(0);

                if (entity != null) {

                    int step;
                    int toolType = entity.getType();

                    if (!associateMode) {
                        if (toolType != Tool.TYPE_MODEL) {
                            return;
                        }
                        if (shiftActive) {
                            step = 1;
                        } else {
                            step = MOUSEWHEEL_STEPUP;
                        }
                        GTEntityWrapper eWrapper =
                            entityWrapperMap.get(entity.getEntityID());
                        setHeading(eWrapper.getHeading() + wheelRotation * step);
                    }
                }

                break;

            case NAVIGATION:
                // zoom
                incrementZoomLevel(wheelRotation);
                break;
        }

    }

    //----------------------------------------------------------
    // Methods required by MouseMotionListener
    //----------------------------------------------------------

    /**
     * Controls the placement of entities on the entity layer and
     * panning of the map area.
     *
     * @param me The MouseEvent
     */
    @Override
    public void mouseDragged(MouseEvent me) {

//System.out.println("GT2DView.mouseDragged");
//System.out.println("    mode: " + currentMode);

        Point currentMousePoint = me.getPoint();

        // the difference between the current and previous mouse position
        int deltaX = lastMousePoint.x - currentMousePoint.x;
        int deltaY = lastMousePoint.y - currentMousePoint.y;

        lastMousePoint = currentMousePoint;

        Entity entity = null;

        if (!selectedEntities.isEmpty()) {
            //errorReporter.messageReport("Moving multiple items is not supported at this time.");
            entity = selectedEntities.get(0);
        }

        switch (currentMode) {
            case SELECTION:

                if (entity != null) {

                    // check whether an entity is being relocated
                    if (activeButton != MouseEvent.BUTTON1) {
                        return;
                    }

                    if (ignoreDrag) {
                        entityDragging = false;

                        // set a threshhold for move to fire warnings
                        int threshhold = 10;

                        // get the location of the selected vertex
                        int[] screenPosition = new int[2];
                        double[] worldPosition = new double[3];

                        if (entity instanceof PositionableEntity) {

                            if (entity instanceof SegmentableEntity &&
                                ((SegmentableEntity)entity).getSelectedVertexID()!= -1) {

                                worldPosition = ((SegmentableEntity)entity).getSelectedVertexPosition();
                            }

                        } else {
                            ((PositionableEntity)entity).getPosition(worldPosition);
                        }

                        convertWorldPosToScreenPos(worldPosition, screenPosition);

                        // get how far we've moved from the original position
                        deltaX = abs(currentMousePoint.x - screenPosition[0]);
                        deltaY = abs(currentMousePoint.y - screenPosition[1]);

                        // if we've moved too far, fire warning
                        if ((deltaX > threshhold) || (deltaY > threshhold)) {
                            if (entity instanceof SegmentableEntity &&
                                entity.isSegmentedEntity()) {

                                Segment seg = ((SegmentableEntity)entity).getSelectedSegment();
                                if(seg != null) {

                                    // If we have a selected segment, then
                                    // insert a new vertex here and start the
                                    // drag process.
                                    int newVertexID =
                                            addVertex(entity, currentMousePoint.x, currentMousePoint.y);

                                    Command cmd =
                                        new SplitSegmentCommand(model,
                                                                entity.getEntityID(),
                                                                seg.getSegmentID(),
                                                                newVertexID);
                                    cmd.setErrorReporter(errorReporter);
                                    model.applyCommand(cmd);
                                } else {

                                    showMessageDialog(this,
                                        "Movement of entire segmented objects are not supported at this time.",
                                        "Placement Action",
                                        JOptionPane.WARNING_MESSAGE);
                                    mapPanel.repaint();
                                    return;
                                }
                            }

                            if (((SegmentableEntity)entity).isFixedLength()) {
                                showMessageDialog(this,
                                    "Movement of internal vertex of fixed length fences are not supported at this time.",
                                    "Placement Action",
                                    JOptionPane.WARNING_MESSAGE);
                                mapPanel.repaint();
                                return;
                            }
                        } else {
                            return;
                        }
                    }

                    if (!entityDragging) {
                        entityDragging = true;
                        setMode(MouseMode.PLACEMENT, false);
                    }
                } else {
                    if (boundInProgress) {
                         // the drag is controlling a bounds selection
                        boundRectangle.width = currentMousePoint.x - boundRectangle.x;
                        boundRectangle.height = currentMousePoint.y - boundRectangle.y;

                        mapPanel.setCursor(crossHairCursor);

                        mapPanel.repaint();
                    } else {
                        navControl.mouseDragged(me);
                    }
                }

                break;

            case PLACEMENT:

                if (entityDragging) {

                    if (entity instanceof SegmentableEntity && entity.isSegmentedEntity()) {

                        double[] pos = new double[3];

                        if (((SegmentableEntity)entity).isFixedLength()) {

                            SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();
                            List<SegmentVertex> vertices =  segments.getVertices();

                            if (segments.isStart(((SegmentableEntity)entity).getSelectedVertexID())) {
                                pos = vertices.get(1).getPosition();
                            } else if (segments.isEnd(((SegmentableEntity)entity).getSelectedVertexID())) {
                                pos = vertices.get(vertices.size() - 2).getPosition();
                            }

                            int screenPosition[] = new int[2];
                            convertWorldPosToScreenPos(pos, screenPosition);

                            float x;
                            float y;

                            x = currentMousePoint.x - screenPosition[0];
                            y = currentMousePoint.y - screenPosition[1];

                            float len = (float) sqrt(x * x + y * y);

                            if (len > 0) {
                                x = x / len;
                                y = y / len;
                            }

                            x = screenPosition[0] + round(x * segmentLength / abs((mapScale)));
                            y = screenPosition[1] + round(y * segmentLength / abs((mapScale)));

                            convertScreenPosToWorldPos(round(x), round(y), pos);

                        } else {

                            SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();
                            double[] vertexPos =
                                segments.getVertexPosition(((SegmentableEntity)entity).getSelectedVertexID());

                            pos[0] = vertexPos[0] - deltaX*mapScale;
                            pos[1] = vertexPos[1];
                            pos[2] = vertexPos[2] - deltaY*mapScale;
                        }


                        if (!inTransient) {
                            transactionID = model.issueTransactionID();
                            inTransient = true;
                        }

                        //currentEntity.getEntity().getSelectedVertexID()
                        // TODO: Need to calc velo
                        MoveVertexTransientCommand cmd = new MoveVertexTransientCommand(
                            model,
                            transactionID,
                            entity.getEntityID(),
                            ((SegmentableEntity)entity).getSelectedVertexID(),
                            pos,
                            new float[3]);
                        cmd.setErrorReporter(errorReporter);
                        model.applyCommand(cmd);
                    } else {

                        double[] pos = new double[] {0, 0, 0};
                        if (entity instanceof PositionableEntity) {
                            ((PositionableEntity)entity).getPosition(pos);
                        }

                        pos[0] -= deltaX*mapScale;
                        pos[2] -= deltaY*mapScale;

                        if (!inTransient) {
                            transactionID = model.issueTransactionID();
                            inTransient = true;
                        }

                        // TODO: Need to calc velo
                        MoveEntityTransientCommand cmd = new MoveEntityTransientCommand(
                            model,
                            transactionID,
                            entity.getEntityID(),
                            pos,
                            new float[3]);
                        cmd.setErrorReporter(errorReporter);
                        model.applyCommand(cmd);
                    }
                }
                break;

            case NAVIGATION:
                if (!mapIsAvailable) {
                    return;
                }

                if (boundInProgress) {
                    // the drag is controlling a bounds selection
                    boundRectangle.width = currentMousePoint.x - boundRectangle.x;
                    boundRectangle.height = currentMousePoint.y - boundRectangle.y;

                    mapPanel.repaint();

                } else if (panInProgress) {
                    // the drag is controlling a pan operation

                    Rectangle panelBounds = mapPanel.getBounds();

                    // the dimensions of the panel
                    double panelWidth = panelBounds.getWidth();
                    double panelHeight = panelBounds.getHeight();

                    // the offset from the center of the panel, by the
                    // drag amount from the last mouse position
                    double centerOffsetX = (panelBounds.width/2 + deltaX);
                    double centerOffsetY = (panelBounds.height/2 + deltaY);

                    // the map extent
                    double mapWidth = mapArea.getWidth();
                    double mapHeight = mapArea.getHeight();

                    // calculate the current map coordinates that will become the new center
                    double mapX = (centerOffsetX * mapWidth / panelWidth) + mapArea.getMinX();
                    double mapY =  mapArea.getMaxY() - (centerOffsetY * mapHeight / panelHeight);

                    double mapWidth2 = mapWidth / 2.0;
                    double mapHeight2 = mapHeight / 2.0;

                    // the new region of the map to display - dragged to follow the mouse
                    mapArea = new ReferencedEnvelope(
                        mapX - mapWidth2,
                        mapX + mapWidth2,
                        mapY - mapHeight2,
                        mapY + mapHeight2,
                        crs);

                    updateEntityScaleAndZoom();
                } else {
                    navControl.mouseDragged(me);
                }
                break;

        }

    }

    /**
     * Controls the drawing parameters of segmented entities on the
     * entity layer and produces mouse over world coordinates.
     *
     * @param me The MouseEvent
     */
    @Override
    public void mouseMoved(MouseEvent me) {
        inMousePressed = false;

        Point currentMousePoint = me.getPoint();

        switch (currentMode) {

        case SELECTION:
        case PLACEMENT:
            mouseX = me.getX();
            mouseY = me.getY();

            Entity entity = null;

            if (!selectedEntities.isEmpty()) {
                //errorReporter.messageReport("Moving multiple items is not supported at this time.");
                entity = selectedEntities.get(0);
            }

            if ((entity != null) && (multiSegmentOp == true) &&
                    entity instanceof SegmentableEntity &&
                    ((SegmentableEntity)entity).isVertexSelected()) {

                GTEntitySearchReturn over_entity =
                    findEntity(currentMousePoint.x, currentMousePoint.y);
                GTEntityWrapper eWrapper = over_entity.getEntityWrapper();

                int vertexID = -1;

                if (eWrapper != null) {
                    vertexID = over_entity.getVertexID();
                }

                // Clear all entities of highlights except current one
                Entity[] entities = model.getModelData();

                int len = entities.length;

                for(int i=0; i < len; i++) {
                    if (entities[i] == null)
                        continue;

                    if (entities[i].isSegmentedEntity()) {
                        ((SegmentableEntity)entities[i]).setHighlightedVertexID(-1);
                    }

                }

                // Highlight the one found
                if (vertexID != -1) {
                    ((SegmentableEntity)entity).setHighlightedVertexID(vertexID);
                }

                mapPanel.entityUpdateRequired();
            }

            if ((entity != null)
                && entity instanceof SegmentableEntity
                && ((SegmentableEntity)entity).isFixedLength()
                && multiSegmentOp == true
                && ((SegmentableEntity)entity).isVertexSelected()) {

                float x;
                float y;

                SegmentSequence fenceSegment = ((SegmentableEntity)entity).getSegmentSequence();
                convertWorldPosToScreenPos(fenceSegment.getEndPosition(),
                                            lastSegmentPosition);

                x = mouseX - lastSegmentPosition[0];
                y = mouseY - lastSegmentPosition[1];

                float len = (float) sqrt(x * x + y * y);

                if (len > 0) {
                    x = x / len;
                    y = y / len;
                }

                x = lastSegmentPosition[0] + round(x * segmentLength / abs((mapScale)));
                y = lastSegmentPosition[1] + round(y * segmentLength / abs((mapScale)));

                // adjust for the icon size
                imgWidth = (int) (toolSegmentImage.getWidth(null)* iconScaleX);
                imgHeight = (int) (toolSegmentImage.getHeight(null) * iconScaleY);

                x = x - round(imgWidth / 2);
                y = y - round(imgHeight / 2);

                toolTransform.setToTranslation(x, y);
                toolTransform.scale(iconScaleX,iconScaleY);

            } else {

                toolTransform.setToTranslation(mouseX-iconCenterX,mouseY-iconCenterY);
                toolTransform.scale(iconScaleX,iconScaleY);
            }

            if (toolImage != null) {
                mapPanel.repaint();
            }

            break;

        case NAVIGATION:
            boolean overControl = navControl.contains(currentMousePoint);
            if (overControl) {
                if (isOverNavControl) {
                    navControl.mouseMoved(me);
                } else {
                    navControl.mouseEntered(me);
                    isOverNavControl = true;
                }
            } else {
                if (isOverNavControl) {
                    navControl.mouseExited(me);
                    if (boundIsActive) {
                        mapPanel.setCursor(crossHairCursor);
                    } else if (panIsActive) {
                        mapPanel.setCursor(openHandCursor);
                    }
                }
                isOverNavControl = false;
            }

            break;

        }

        // calculate the world coordinates of the mouse position
        if (mapIsAvailable) {

            // the current mouse position
            double _mouseX = currentMousePoint.x;
            double _mouseY = currentMousePoint.y;

            // the map extent
            double mapWidth = mapArea.getWidth();
            double mapHeight = mapArea.getHeight();

            Rectangle panelBounds = mapPanel.getBounds();

            // the dimensions of the panel
            double panelWidth = panelBounds.getWidth();
            double panelHeight = panelBounds.getHeight();

            // translate the mouse position to world coordinates
            double x = (_mouseX * mapWidth / panelWidth) + mapArea.getMinX();
            double y = ((_mouseY * mapHeight) / panelHeight) - mapArea.getMaxY();

            position.x = x;
            position.y = y;

            if (coordFormat != null) {
                statusField.setText(coordFormat.format(position));
            }
        }

        if (!mapPanel.isFocusOwner()) {
            mapPanel.requestFocusInWindow();
        }
    }

    //----------------------------------------------------------
    // Methods required by MouseListener
    //----------------------------------------------------------

    /**
     * Controls centering the map on the mouse click position
     *
     * @param me The mouse event
     */
    @Override
    public void mouseClicked(MouseEvent me) {

//System.out.println("GT2DView.mouseClicked");
//System.out.println("    mode: " + currentMode);
//System.out.println("    inMousePressed: " + inMousePressed);

        if (activeButton == MouseEvent.BUTTON1) {

            GTEntitySearchReturn entReturn;
            GTEntityWrapper eWrapper;

            switch (currentMode) {
                case ASSOCIATE:
                    entReturn = findEntity(lastMousePoint.x, lastMousePoint.y);
                    eWrapper = entReturn.getEntityWrapper();

                    if (eWrapper == null) {
                        errorReporter.messageReport("Cannot Associate, No Entity Found");
                    } else {
                        List<Entity> selected = new ArrayList<>();
                        selected.add(eWrapper.getEntity());

                        changeSelection(selected);

                        setMode(MouseMode.SELECTION, false);
                    }
                    break;

                case SELECTION:
                    if (inMousePressed) {
                        return;
                    }

                    boundInProgress = false;

                    // look for a single entity
                    entReturn = findEntity(lastMousePoint.x, lastMousePoint.y);
                    eWrapper = entReturn.getEntityWrapper();

                    int vertexID = entReturn.getVertexID();
                    int segmentID = entReturn.getSegmentID();

                    // nothing found, set selection to location
                    if (eWrapper == null) {

                        changeSelection(EMPTY_ENTITY_LIST);

                        setMode(MouseMode.SELECTION, true);
                    } else {
                        setSelectedEntity(eWrapper, segmentID, vertexID);
                    }
                    break;

                case PLACEMENT:
                    break;

                case NAVIGATION:

                    if (!mapIsAvailable) {
                        return;
                    }

                    Point currentMousePoint = me.getPoint();

                    if (navControl.contains(currentMousePoint)) {
                        navControl.mouseClicked(me);

                    } else if (boundIsActive) {
                        return;

                    } else if (panIsActive && (me.getClickCount() > 1)) {
                        // on a double click

                        int button = me.getButton();
                        int delta = 0;
                        if (button == MouseEvent.BUTTON1) {
                            if (zoomLevel > navControl.getMinimum()) {
                                delta = -1;
                            } else {
                                // can't zoom in any farther
                                return;
                            }
                        } else if (button == MouseEvent.BUTTON3) {
                            if (zoomLevel < navControl.getMaximum()) {
                                delta = 1;
                            } else {
                                // can't zoom out any farther
                                return;
                            }
                        }
                        // and with 'room to zoom'

                        // pan to center on the mouse position
                        mapArea = gtUtils.panToPoint(
                            mapArea,
                            mapPanel.getBounds(),
                            currentMousePoint);

                        // then zoom in or out depending on the button that was clicked

                        incrementZoomLevel(delta);
                    }
                    break;
            }
        } else if (activeButton == MouseEvent.BUTTON3) {
            setMode(MouseMode.SELECTION, true);
            getViewManager().disableAssociateMode();
            toolBarManager.setTool(null);
        }
        inMousePressed = false;
    }

    /**
     * Initiates adding and manipulating entities
     *
     * @param me The mouse event
     */
    @Override
    public void mousePressed(MouseEvent me) {

//System.out.println("GT2DView.mousePressed");
//System.out.println("    mode: " + currentMode);

        inMousePressed = true;

        // save the location for potential panning operations
        activeButton = me.getButton();
        lastMousePoint = me.getPoint();

        // end the rotation action if there is one
        checkForRotation();

        if (activeButton == MouseEvent.BUTTON1) {

            switch (currentMode) {
                case ASSOCIATE:
                    break;

                case SELECTION:
                    // see if nothing is selected, if not then
                    // start the bound process for multi-select

                    // look for a single entity
                    GTEntitySearchReturn entReturn =
                        findEntity(lastMousePoint.x, lastMousePoint.y);
                    GTEntityWrapper eWrapper = entReturn.getEntityWrapper();

                    // nothing found, set selection to location
                    if (eWrapper == null) {
                        boundInProgress = true;
                        boundRectangle.x = lastMousePoint.x;
                        boundRectangle.y = lastMousePoint.y;
                        boundRectangle.width = 0;
                        boundRectangle.height = 0;

                        changeSelection(EMPTY_ENTITY_LIST);
                    }
                    break;

                case PLACEMENT:
                    break;

                case NAVIGATION:
                    if (navControl.contains(lastMousePoint)) {
                        navControl.mousePressed(me);

                    } else if (boundIsActive) {
                        boundInProgress = true;
                        boundRectangle.x = lastMousePoint.x;
                        boundRectangle.y = lastMousePoint.y;
                        boundRectangle.width = 0;
                        boundRectangle.height = 0;

                    } else {
                        panInProgress = true;
                        mapPanel.setCursor(closedHandCursor);
                    }

                    break;
            }

            // Set focus so keys will work
            mapPanel.requestFocusInWindow();

        } else if(activeButton == MouseEvent.BUTTON3) {
            setMode(MouseMode.SELECTION, true);
            getViewManager().disableAssociateMode();
            toolBarManager.setTool(null);
        }
    }

    /**
     * Terminates entity manipulation
     *
     * @param me The mouse event
     */
    @Override
    public void mouseReleased(MouseEvent me) {

        Point currentMousePoint = me.getPoint();

        if (me.isPopupTrigger()) {
            GTEntitySearchReturn entReturn = findEntity(me.getX(), me.getY());
            GTEntityWrapper eWrapper = entReturn.getEntityWrapper();

            if(eWrapper != null) {

                // Populate a new menu
                JPopupMenu menu = new JPopupMenu();

                DeleteAction delete =
                    new DeleteAction(true, null, model, eWrapper.getEntityID());
                delete.setEnabled(true);

                HighlightAssociatesAction highlight =
                    new HighlightAssociatesAction(true, null, model, eWrapper.getEntityID());
                highlight.setEnabled(true);

                menu.add(highlight);
                menu.add(new ShowChildrenAction(eWrapper, mapPanel));
                menu.addSeparator();
                menu.add(delete);

                menu.show(mapPanel, me.getX(), me.getY());

                return;
            }
        }

        // If it wasn't a popup trigger that was over an entity, then we
        // can do other actions.
        if (activeButton == MouseEvent.BUTTON1) {

            Entity entity = null;

            if (!selectedEntities.isEmpty()) {
                //errorReporter.messageReport("Moving multiple items is not supported at this time.");
                entity = selectedEntities.get(0);
                //System.out.println("    entityID: " + entity.getEntityID());
            }

            switch (currentMode) {
                case ASSOCIATE:
                    break;

                case SELECTION:
                     if (boundInProgress) {

                        int width = boundRectangle.width;
                        int height = boundRectangle.height;
                        if ((width == 0) || (height == 0)) {
                            // can't select a zero area
                            boundInProgress = false;
                            setMode(MouseMode.SELECTION, false);
                            return;
                        }

                        //TODO: get all entities in selected region
//                        System.out.println("SELECT MULTIPLE ENTITIES HERE!");
                        selectedEntities = findEntity(boundRectangle);

                        boundInProgress = false;
                        setMode(MouseMode.SELECTION, false);
                    } else {
                        // look for a single entity
                        GTEntitySearchReturn entReturn =
                            findEntity(lastMousePoint.x, lastMousePoint.y);
                        GTEntityWrapper eWrapper = entReturn.getEntityWrapper();

                        int vertexID = entReturn.getVertexID();
                        int segmentID = entReturn.getSegmentID();

                        // nothing found, set selection to location
                        if (eWrapper == null) {
                            boundInProgress = true;
                            boundRectangle.x = lastMousePoint.x;
                            boundRectangle.y = lastMousePoint.y;
                            boundRectangle.width = 0;
                            boundRectangle.height = 0;

                            changeSelection(EMPTY_ENTITY_LIST);
                        } else {
                            setSelectedEntity(eWrapper, segmentID, vertexID);
                        }
                    }

                    break;

                case PLACEMENT:

                    // end the rotation action if there is one
                    checkForRotation();

                    ignoreDrag = false;

                    if (entityDragging && (entity != null)) {

                        // Finalize drag items around
                        entityDragging = false;

                        // the difference between the current and previous mouse position
                        int deltaX = lastMousePoint.x - currentMousePoint.x;
                        int deltaY = lastMousePoint.y - currentMousePoint.y;

                        //lastMousePoint = currentMousePoint;
                        double[] pos = new double[3];

                        if (entity.isSegmentedEntity() && entity instanceof SegmentableEntity) {
                            if (((SegmentableEntity)entity).isVertexSelected()) {
                                pos = ((SegmentableEntity)entity).getSelectedVertexPosition();

                                pos[0] -= deltaX*mapScale;
                                pos[2] -= deltaY*mapScale;

                                MoveVertexCommand cmd = new MoveVertexCommand(
                                    model,
                                    transactionID,
                                    entity.getEntityID(),
                                    ((SegmentableEntity)entity).getSelectedVertexID(),
                                    pos,
                                    startPos);
                                cmd.setErrorReporter(errorReporter);
                                model.applyCommand(cmd);

                                // set the start position to the current position,
                                // in case of multiple moves on the same selection
                                startPos = pos;
                            } else if (((SegmentableEntity)entity).isSegmentSelected()){
                                // ignore, Don't move segments
                            }
                        } else {

                            if (entity instanceof PositionableEntity) {
                                ((PositionableEntity)entity).getPosition(pos);
                                pos[0] -= deltaX*mapScale;
                                pos[2] -= deltaY*mapScale;

                                MoveEntityCommand cmd = new MoveEntityCommand(
                                    model,
                                    transactionID,
                                    entity.getEntityID(),
                                    pos,
                                    startPos);
                                cmd.setErrorReporter(errorReporter);
                                model.applyCommand(cmd);

                                // set the start position to the current position,
                                // in case of multiple moves on the same selection
                                startPos = pos;
                            }
                        }

                        inTransient = false;
                        setMode(MouseMode.SELECTION, false);

                    } else if (!entityDragging && (currentTool != null)) {
                        //int id = 0;

                        EntityBuilder builder = EntityBuilder.getEntityBuilder();
                        Entity newEntity;

                        int entityID = model.issueEntityID();

                        // Place new items
                        switch(currentTool.getToolType()) {
                            case Tool.TYPE_MODEL:
                                convertScreenPosToWorldPos(currentMousePoint.x,
                                    currentMousePoint.y, tmpPos);

                                tmpRot[0] = 0;
                                tmpRot[1] = 1;
                                tmpRot[2] = 0;
                                tmpRot[3] = 0;

                                startRot = tmpRot.clone();
                                adjustRotation(true, startRot);
                                adjustRotation(true, tmpRot);

                                newEntity = builder.createEntity(model, entityID,
                                        tmpPos, tmpRot, currentTool);

                                AddEntityCommand cmd =
                                    new AddEntityCommand(model, newEntity);
                                model.applyCommand(cmd);

                                multiSegmentOp = false;

                                List<Entity> selected = new ArrayList<>();
                                selected.add(newEntity);

                                changeSelection(selected);
                                break;

                            case Tool.TYPE_WORLD:
                                // Need to clear current world?  Or at least change size of icons
                                errorReporter.messageReport("Not implemented yet");
                                break;

                            case Tool.TYPE_MULTI_SEGMENT:
                                // Add the entity the first time through

//System.out.println("TYPE_MULTI_SEGMENT");

                                if ((entity == null ) ||
                                        ((entity.getName().equals(currentTool.getName()) &&
                                                !((SegmentableEntity)entity).isVertexSelected() &&
                                                !((SegmentableEntity)entity).isSegmentSelected())
                                         ) || (!entity.getName().equals(currentTool.getName()))) {

                                    convertScreenPosToWorldPos(currentMousePoint.x,
                                        currentMousePoint.y, tmpPos);

                                    newEntity =
                                        builder.createEntity(model, entityID, tmpPos,
                                                new float[] {0,1,0,0}, currentTool);

                                    cmd = new AddEntityCommand(model, newEntity);
                                    model.applyCommand(cmd);

                                    String[] params = currentTool.getToolParams();
                                    if (params.length > 0) {
                                        float fixedLength = parseFloat(params[Tool.PARAM_SEGMENT_LENGTH]);
                                        if (fixedLength > 0) {
                                            segmentLength = fixedLength;
                                        }
                                    }

                                    List<Entity> selected2 = new ArrayList<>();
                                    selected2.add(newEntity);

                                    changeSelection(selected2);

                                    addVertex(newEntity,
                                        currentMousePoint.x,
                                        currentMousePoint.y);
                                } else if (((SegmentableEntity)entity).isVertexSelected()) {

                                    int startVertexID = ((SegmentableEntity)entity).getSelectedVertexID();
                                    int endVertexID;

                                    //MultiSegmentTool mst = (MultiSegmentTool) entity.getTool();

                                    if (((SegmentableEntity)entity).isLine()) {
                                        SegmentSequence fence = ((SegmentableEntity)entity).getSegmentSequence();
                                        startVertexID = fence.getVertexID(fence.getEndPosition());

                                        endVertexID =
                                            addVertex(entity, currentMousePoint.x, currentMousePoint.y);
                                    } else if (((SegmentableEntity)entity).getHighlightedVertexID() == -1) {
                                        endVertexID =
                                            addVertex(entity, currentMousePoint.x, currentMousePoint.y);
                                    } else {
                                        endVertexID = ((SegmentableEntity)entity).getHighlightedVertexID();
                                    }

                                    addSegment(entity, startVertexID, endVertexID);
                                } else if (((SegmentableEntity)entity).isSegmentSelected()) {

                                    int newVertexID = addVertex(entity, currentMousePoint.x, currentMousePoint.y);

                                    SplitSegmentCommand command = new SplitSegmentCommand(model, entity.getEntityID(),
                                            ((SegmentableEntity)entity).getSelectedSegmentID(), newVertexID);
                                    model.applyCommand(command);

                                    List<Entity> selected2 = new ArrayList<>();
                                    ((SegmentableEntity)entity).setSelectedVertexID(newVertexID);
                                    ((SegmentableEntity)entity).setSelectedSegmentID(-1);
                                    selected2.add(entity);

                                    changeSelection(selected2);
                                }

                                mapPanel.entityUpdateRequired();
                                break;
                            default:
                                errorReporter.messageReport("Unhandled tooltype: " + mapType(currentTool.getToolType()));
                        }
                    }

                    break;

                case NAVIGATION:

                    if (boundInProgress) {

                        int width = boundRectangle.width;
                        int height = boundRectangle.height;
                        if ((width == 0) || (height == 0)) {
                            // can't select a zero area
                            boundInProgress = false;
                            mapPanel.repaint();
                            return;
                        }
                        double x_min = boundRectangle.x;
                        double y_min = boundRectangle.y;
                        double x_max;
                        double y_max;
                        // rearrange the coords in case the drag has been
                        // working in a negative direction
                        if (width < 0) {
                            x_min += width;
                            x_max = x_min - width;
                        } else {
                            x_max = x_min + width;
                        }
                        if (height < 0) {
                            y_min += height;
                            y_max = y_min - height;
                        } else {
                            y_max = y_min + height;
                        }

                        double x_dim = (x_max - x_min);
                        double x_center = x_max - x_dim/2;

                        double y_dim = (y_max - y_min);
                        double y_center = y_max - y_dim/2;

                        Rectangle panelBounds = mapPanel.getBounds();

                        // pan to center the map on the center of the selected area
                        mapArea = gtUtils.panToPoint(
                            mapArea,
                            panelBounds,
                            new Point((int)x_center, (int)y_center));

                        // the dimensions of the panel
                        double panelWidth = panelBounds.getWidth();
                        double panelHeight = panelBounds.getHeight();

                        // the map extent
                        double mapWidth = mapArea.getWidth();
                        double mapHeight = mapArea.getHeight();

                        // calculate the map width & height of the selected area
                        double boundWidth = x_dim * mapWidth / panelWidth;
                        double boundHeight = y_dim * mapHeight / panelHeight;

                        // pick the smaller, add a bit
                        //double targetMapSpan = Math.min(boundWidth, boundHeight) * 1.2;
                        // no, lets pick the larger......
                        double targetMapSpan = max(boundWidth, boundHeight) * 1.2;

                        // determine the zoom increments necessary for the target
                        // span to fit into the map area.
                        int levels = 0;
                        double zoomWidth = mapWidth;
                        while(zoomWidth > targetMapSpan) {
                            zoomWidth /= zoomFactor;
                            levels--;
                        }
                        boundInProgress = false;
                        if (!navControl.contains(currentMousePoint)) {
                            mapPanel.setCursor(crossHairCursor);
                        }

                        // calculate the zoom map area
                        incrementZoomLevel(levels);

                    } else if (panInProgress) {
                        panInProgress = false;
                        if (!navControl.contains(currentMousePoint)) {
                            mapPanel.setCursor(openHandCursor);
                        }
                    } else {
                        navControl.mouseReleased(me);
                        if (!navControl.contains(currentMousePoint)) {
                            if (boundIsActive) {
                                mapPanel.setCursor(crossHairCursor);
                            } else if (panIsActive) {
                                mapPanel.setCursor(openHandCursor);
                            }
                        }
                    }

                    break;
            }
        }
    }

    /**
     * Resets the cursor if an entity tool is active
     *
     * @param me The mouse event
     */
    @Override
    public void mouseEntered(MouseEvent me) {

        switch (currentMode) {
            case SELECTION:
                insideMap = true;

                if (boundIsActive) {
                    mapPanel.setCursor(crossHairCursor);
                }

                mapPanel.repaint();
                break;

            case PLACEMENT:
                insideMap = true;
                mapPanel.repaint();
                break;

            case NAVIGATION:
                isOverNavControl = false;
                if (boundIsActive) {
                    mapPanel.setCursor(crossHairCursor);
                } else {
                    mapPanel.setCursor(openHandCursor);
                }
                break;
        }

    }

    /**
     * Resets the cursor, terminates any active entity rotations
     *
     * @param me The mouse event
     */
    @Override
    public void mouseExited(MouseEvent me) {

        insideMap = false;

        // finish any rotation action
        checkForRotation();

        switch (currentMode) {
            case SELECTION:
                setMode(MouseMode.SELECTION, false);
                break;

            case PLACEMENT:
                setMode(MouseMode.PLACEMENT, false);
                break;

            case NAVIGATION:
                isOverNavControl = false;
                break;
        }

        statusField.setText("");
    }

    //---------------------------------------------------------
    // Method defined by ChangeListener
    //---------------------------------------------------------

    /**
     * Track updates to the zoom control
     * @param ce
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        int newLevel = navControl.getValue();
        incrementZoomLevel(newLevel - zoomLevel);
    }

    //---------------------------------------------------------
    // Methods defined by EntityPropertyListener
    //---------------------------------------------------------

    @Override
    public void propertyAdded(int entityID, String propertySheet, String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void propertyRemoved(int entityID, String propertySheet, String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void propertyUpdated(int entityID, String propertySheet, String propertyName) {

        GTEntityWrapper eWrapper = entityWrapperMap.get(entityID);
        Entity entity = eWrapper.getEntity();

        // Grab the size of the entity, so we can check if is has changed
        float[] newSize = new float[3];
        if ((entity.getType() != Tool.TYPE_WORLD) &&
            (entity instanceof PositionableEntity)) {

            ((PositionableEntity)entity).getSize(newSize);

        }

        // This completes the UI update of the updated entity property
        entitySizeChanged(true, entityID, newSize);
    }

    //---------------------------------------------------------
    // Method defined by Runnable
    //---------------------------------------------------------

    // a separate thread to allow the UI to refresh the
    // progress bar while the reader is initialized
    @Override
    public void run() {

//System.out.println("*** STARTING THREAD ***");

        // Solve the URL issue (TDN)
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(url_string);

        if (url == null) {
            setIsLoading(false);
            return;
        }

        url_string = url.toString();

        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException urise) {
            errorReporter.errorReport("Bad URL!", urise);
        }
        if (uri == null) {
            setIsLoading(false);
            return;
        }
        File target = new File(uri);
        org.geotools.coverage.grid.io.AbstractGridCoverage2DReader gridCoverReader = null;
        try {
            if (target.isFile()) {
                // if a file - assume that it's an image file and
                // use the world reader
                gridCoverReader = new WorldImageReader(target);
            } else {
                // otherwise, checkwith the directory for a particular file
                File shpFile = new File(target, "pyramid.shp");
                if (shpFile.exists()) {
                    // if a shapefile exists - assume that this is a single mosiac level
                    gridCoverReader = new ImageMosaicReader(shpFile, null);
                } else {
                    // if a properties file exists - assume that this is a pyramid
                    File propFile = new File(target, "pyramid.properties");
                    if (propFile.exists()) {
                        gridCoverReader = new ImagePyramidReader(propFile, null);
                    }
                }
            }
        } catch (IOException ioe) {
            errorReporter.errorReport("File Error!", ioe);
        }

        if (gridCoverReader == null) {
            System.err.println("Failed to load grid reader!!!  url: " + url);
            // no reader, no joy - punt.
            setIsLoading(false);
            return;
        }

        // the bounds of the mapped area
        Envelope ge = gridCoverReader.getOriginalEnvelope();

        // the coordinate reference system is defined in the projection
        // file and is read in by the image reader
        crs = ge.getCoordinateReferenceSystem();
        gtUtils = new GT2DUtils(crs);

        // coordinate formatter
        coordFormat = new CoordinateFormat();
        coordFormat.setCoordinateReferenceSystem(crs);

        // the initial map area encompasses the entire bounds of the
        // referenced image and is calculated from values in the world file
        mapArea = new ReferencedEnvelope(ge);
        try {
            mapArea.toBounds(crs);
        } catch (TransformException e) {
            errorReporter.errorReport("Grid Error!", e);
        }

        GridCoverage gridCover = null;
        try {
            gridCover = gridCoverReader.read(null);
        } catch (IllegalArgumentException | IOException e) {
            errorReporter.errorReport("Grid Error!", e);
        }

        // the rendering context for the background map coverage image
        baseContext = new DefaultMapContext(crs);

        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        StyleBuilder sb = new StyleBuilder(factory);
        RasterSymbolizer rs = sb.createRasterSymbolizer();
        rasterStyle = sb.createStyle(rs);

        baseContext.addLayer(gridCoverReader, rasterStyle);

        // rem: is this vestigal ??? ///////////////////////////////
        // set up the panel dimensions to have a common scale
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        int ratio = (int)(512 * height / width);
        imageSize = new Dimension(512, ratio);

        ////////////////////////////////////////////////////////////
        // configure the state of the UI controls

        boolean isPickModeSelected = pickAndPlaceButton.isSelected();
        boolean isNavModeSelected = navigateButton.isSelected();

        // default to navigation mode if a mode has not yet been chosen
        if (!(isNavModeSelected || isPickModeSelected)) {
            navigateButton.setSelected(true);
            currentMode = MouseMode.NAVIGATION;
        }

        pickAndPlaceButton.setEnabled(true);
        navigateButton.setEnabled(true);

        boolean isPanFuncSelected = panButton.isSelected();
        boolean isBoundFuncSelected = boundButton.isSelected();

        // default to pan navigation function if a function has not yet been chosen
        if (!(isPanFuncSelected || isBoundFuncSelected)) {
            panButton.setSelected(true);
            panIsActive = true;
        }

        if (currentMode == MouseMode.NAVIGATION) {
            panButton.setEnabled(true);
            boundButton.setEnabled(true);
        } else {
            panButton.setEnabled(false);
            boundButton.setEnabled(false);
        }

        setIsLoading(false);

        zoomLevel = navControl.getMaximum();
        navControl.setValue(zoomLevel);

        switch (currentMode) {
            case SELECTION:
                mapPanel.setCursor(null);
                break;

            case PLACEMENT:
                mapPanel.setCursor(null);
                break;

            case NAVIGATION:
                if (boundIsActive) {
                    mapPanel.setCursor(crossHairCursor);
                } else if (panIsActive) {
                    mapPanel.setCursor(openHandCursor);
                } else {
                    errorReporter.messageReport("GT2DView: Unknown Navigation Function");
                }

                break;
            default:
                errorReporter.messageReport("GT2DView: Unknown Operation Mode");
        }

        ////////////////////////////////////////////////////////////

        mapIsAvailable = true;

        // display the map
        mapPanel.coverageUpdateRequired();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    private void setSelectedEntity(GTEntityWrapper entityWrapper, int segmentID, int vertexID) {


        // save the starting rotation, so we can perform an undo
        startRot[0] = 0;
        startRot[1] = 1;
        startRot[2] = 0;
        startRot[3] = entityWrapper.getHeading() / 180.0f * (float) Math.PI;
        adjustRotation(true, startRot);

        // save the starting position, so we can perform an undo
        Entity entity = entityWrapper.getEntity();
        startPos = new double[3];

        if (entity instanceof SegmentableEntity) {
            ((SegmentableEntity)entity).setSelectedVertexID(vertexID);
            ((SegmentableEntity)entity).setSelectedSegmentID(segmentID);

            multiSegmentOp = true;
            if (((SegmentableEntity)entity).getSelectedVertexID() != -1) {
                // use the vertex position
                //System.out.println("    startPos: " + startPos[0] +  ", " + startPos[2]);
                startPos = ((SegmentableEntity)entity).getSelectedVertexPosition();
            } else {
                // otherwise use the entity position
                ((PositionableEntity)entity).getPosition(startPos);
            }
        } else {
            // otherwise use the entity position
            ((PositionableEntity)entity).getPosition(startPos);
        }

        // do selected
        //currentEntity = entityWrapper;
        model.addEntityChangeListener(entity, this);
        entity.addEntityPropertyListener(this);

        String iconURL = entity.getIcon();
        Image image = imageMap.get(iconURL);

        if (image == null) {
            image = getImage(iconURL);
        }

        imgWidth = image.getWidth(null);
        imgHeight = image.getHeight(null);

        float[] size = new float[3];
        ((PositionableEntity)entity).getSize(size);

        float toolWidth = size[0];
        float toolLength = size[2];

        calcScaleFactor(entity.isFixedAspect(), imgWidth, imgHeight,
                toolWidth, toolLength, tmpScale, tmpCenter);

        iconScaleX = tmpScale[0];
        iconScaleY = tmpScale[1];

        iconCenterX = tmpCenter[0];
        iconCenterY = tmpCenter[1];

        // stop allowing dragging for now
        ignoreDrag = false;
        if (entity instanceof SegmentableEntity) {

            if (vertexID == -1) {
                ignoreDrag = true;
            } else if (((SegmentableEntity)entity).isFixedLength()) {

                SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();

                if (!segments.isEnd(vertexID) && !segments.isStart(vertexID)) {
                    ignoreDrag = true;
                }
            }
        }

        List<Entity> selected = new ArrayList<>();
        selected.add(entity);

        changeSelection(selected);
        setMode(MouseMode.SELECTION, true);
    }

    /**
     * Set the operational mode of the View
     *
     * @param mode The mode to set. NAVIGATION, PLACEMENT, or SELECTION
     * @param resetState Reset the current state variables,
     *          selected tool, cursor, etc.
     */
    private void setMode(MouseMode mode, boolean resetState) {

        // initialize state for navigation mode
        if (resetState) {
            resetState();
        }

        resetNavigateState();

        switch(mode) {
            case NAVIGATION:
                currentMode = MouseMode.NAVIGATION;

                navigateButton.setSelected(true);
                panButton.setEnabled(true);
                boundButton.setEnabled(true);
                if (boundIsActive) {
                    mapPanel.setCursor(crossHairCursor);
                } else if (panIsActive) {
                    mapPanel.setCursor(openHandCursor);
                }
                mapPanel.repaint();

                break;

            case PLACEMENT:
                // initialize state for place mode
                currentMode = MouseMode.PLACEMENT;

                pickAndPlaceButton.setSelected(true);
                panButton.setEnabled(false);
                boundButton.setEnabled(false);

                mapPanel.setCursor(null);
                mapPanel.repaint();

                break;

            case SELECTION:
                // initialize state for pick mode
                currentMode = MouseMode.SELECTION;

                mapPanel.setCursor(null);

                pickAndPlaceButton.setSelected(true);
                panButton.setEnabled(false);
                boundButton.setEnabled(false);

                entityDragging = false;
                mapPanel.repaint();

                break;

            case ASSOCIATE:

                // initialize state for pick mode
                currentMode = MouseMode.ASSOCIATE;

                pickAndPlaceButton.setSelected(true);
                panButton.setEnabled(false);
                boundButton.setEnabled(false);

                entityDragging = false;
                ignoreDrag = true;
                associateMode = true;

                mapPanel.setCursor(associateCursor);
                mapPanel.repaint();

                break;

            default:
                errorReporter.messageReport("GT2DView: Unknown Operation Mode");
        }
    }

    /**
     * Add a segment to the end of the selected sequence
     *
     * @param startVertexID The starting vertex
     * @param endVertexID The ending vertex
     */
    private void addSegment(Entity entity, int startVertexID, int endVertexID) {

        if (entity instanceof SegmentableEntity) {
            multiSegmentOp = true;

            SegmentSequence fenceSegment = ((SegmentableEntity)entity).getSegmentSequence();

            int currentSegmentID = fenceSegment.getNextSegmentID();

            // Before adding check to make sure it doesn't exist
            if (currentSegmentID > 0) {

                if (!((SegmentableEntity)entity).isVertexSelected()) {
                    ((SegmentableEntity)entity).setSelectedVertexID(fenceSegment.getLastSegmentID());
                }
            }

            int entityID = entity.getEntityID();
            ((SegmentableEntity)entity).setSelectedVertexID(endVertexID);
            ((SegmentableEntity)entity).setHighlightedVertexID(-1);

            AddSegmentCommand cmd =
                new AddSegmentCommand(model, entityID, currentSegmentID, startVertexID, endVertexID);
            cmd.setErrorReporter(errorReporter);

            // TODO: How to deal with undo sets?

            model.applyCommand(cmd);

            List<Entity> selected = new ArrayList<>();
            selected.add(entity);

            changeSelection(selected);

            mapPanel.entityUpdateRequired();

        }
    }

    /**
     * Add a segment to the end of the selected sequence
     *
     * @param entity The entity to add the vertex to
     * @param x The screen x position for the requested vertex
     * @param y The screen y position for the requested vertex
     * @return The new vertexID created
     */
    private int addVertex(Entity entity, int x, int y) {

        if (entity instanceof SegmentableEntity) {
            multiSegmentOp = true;

            SegmentSequence fenceSegment = ((SegmentableEntity)entity).getSegmentSequence();

            if (((SegmentableEntity)entity).isFixedLength()) {

                float newx;
                float newy;

                if (((SegmentableEntity)entity).getSelectedVertexID() == 0) {

                    x = mouseX;
                    y = mouseY;

                } else {
                    convertWorldPosToScreenPos(fenceSegment.getEndPosition(),
                                               lastSegmentPosition);

                    newx = mouseX - lastSegmentPosition[0];
                    newy = mouseY - lastSegmentPosition[1];

                    float len = (float) sqrt(newx * newx + newy * newy);

                    if (len > 0) {
                        newx = newx / len;
                        newy = newy / len;
                    }

                    x = (int) (lastSegmentPosition[0] +
                        round(newx * segmentLength / abs((mapScale))));
                    y = (int) (lastSegmentPosition[1] +
                        round(newy * segmentLength / abs((mapScale))));
                }
            }

            convertScreenPosToWorldPos(x, y, tmpPos);

            // Before adding check to make sure it doesn't exist
            if (((SegmentableEntity)entity).getSelectedVertexID() > 0) {

                if (!((SegmentableEntity)entity).isVertexSelected()) {
                    ((SegmentableEntity)entity).setSelectedVertexID(fenceSegment.getLastVertexID());
                }
                if (fenceSegment.contains(tmpPos)) {

                    showMessageDialog(this,
                            "You cannot add a vertex to a position that already has a vertex defined.",
                            "Placement Action",
                            JOptionPane.WARNING_MESSAGE);
                    mapPanel.repaint();
                    return -1;
                }
            }

            int entityID = entity.getEntityID();
            int vertexID = fenceSegment.getNextVertexID();

            ((SegmentableEntity)entity).setSelectedVertexID(vertexID);
            ((SegmentableEntity)entity).setHighlightedVertexID(-1);

            AddVertexCommand cmd =
                new AddVertexCommand(model, entityID, vertexID, tmpPos);

            cmd.setErrorReporter(errorReporter);

            // TODO: Not sure why this would be transient
            // if this is the first vertex, make the command transient
            // This is to make undo work right.  Need to seperate out the logic with command sets?
            if (vertexID == 0) {
                cmd.setTransient(true);
            }

            model.applyCommand(cmd);

            List<Entity> selected = new ArrayList<>();
            selected.add(entity);

            changeSelection(selected);

            return vertexID;
        } else {
            return -1;
        }
    }

    /**
     * Helper method to end a rotation
     */
    private void checkForRotation() {

        // if the last action was to rotate the entity, then finalize the command
        if (entityRotating) {

            //should only be one selected
            Entity entity = selectedEntities.get(0);

            adjustRotation(true, tmpRot);

            RotateEntityCommand cmd = new RotateEntityCommand(
                model,
                transactionID,
                entity.getEntityID(),
                tmpRot,
                startRot);
            cmd.setErrorReporter(errorReporter);
            model.applyCommand(cmd);

            inTransient = false;

            entityRotating = false;
        }
    }

    /**
     * Set the heading of the current entity.
     *
     * @param angle The angle in degrees);
     */
    private void setHeading(int angle) {

        if (!inTransient) {
            transactionID = model.issueTransactionID();
            inTransient = true;
        }

        //should only be one selected
        Entity entity = selectedEntities.get(0);

        tmpRot[0] = 0;
        tmpRot[1] = 1;
        tmpRot[2] = 0;
        tmpRot[3] = angle / 180.0f * (float) Math.PI;

        //adjustRotation(true, tmpRot);

        entityRotating = true;

        RotateEntityTransientCommand cmd = new RotateEntityTransientCommand(
            model,
            transactionID,
            entity.getEntityID(),
            tmpRot);

        cmd.setErrorReporter(errorReporter);
        model.applyCommand(cmd);
    }

    /**
     * Find an entity given a screen location.  This will return
     * the closest entity.
     *
     * @param The x position
     * @param The y position
     * @return The entity search return
     */
    private GTEntitySearchReturn findEntity(final int x, final int y) {

        if (wrapperList.isEmpty())
            return new GTEntitySearchReturn(null, -1, -1);

        int vertexID = -1;
        int segmentID = -1;
        GTEntityWrapper closest = null;

        int[] entityPosition = new int[2];
        int[] startVertexPos = new int[2];
        int[] endVertexPos = new int[2];

        double closestDistance = Double.MAX_VALUE;

        for (GTEntityWrapper eWrapper : wrapperList) {
            Entity entity = eWrapper.getEntity();

            // For segmented picking, we first check to see if we are near a
            // vertex. If we are, that is always picked in preference to the
            // segment. Otherwise, we look to see if we are near a segment
            // and select that.
            //
            if (entity instanceof SegmentableEntity && entity.isSegmentedEntity()) {
                SegmentSequence segSeq = ((SegmentableEntity)entity).getSegmentSequence();

                List<SegmentVertex> vertices = segSeq.getVertices();
                int vertexFound = -1;
                int segmentFound = -1;

                for (SegmentVertex vtx : vertices) {
                    // get the vertex in screen position
                    convertWorldPosToScreenPos(vtx.getPosition(),
                            entityPosition);

                    // the distance from the mouse position to the center of the entity
                    double dx = x - entityPosition[0];
                    double dy = y - entityPosition[1];
                    double distance = dx * dx + dy * dy;

                    if (distance <= VERTEX_PICK_RADIUS * VERTEX_PICK_RADIUS) {
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closest = eWrapper;
                            vertexFound = vtx.getVertexID();
                        }
                    }
                }

                List<Segment> segments = segSeq.getSegments();

                // Didn't find a matching vertex? Well then let's see if a
                // segment is close.
                if(vertexID == -1) {
                    for (Segment segment : segments) {
                        SegmentVertex startVertex =
                                segSeq.getVertex(segment.getStartIndex());
                        SegmentVertex endVertex =
                                segSeq.getVertex(segment.getEndIndex());

                        // get the vertex in screen position
                        convertWorldPosToScreenPos(startVertex.getPosition(),
                                startVertexPos);
                        convertWorldPosToScreenPos(endVertex.getPosition(),
                                endVertexPos);


                        // if the distance between the two vertices of this
                        // segment is less than the segment pick distance
                        // then the results from the per-vertex test above
                        // must have been dodgy. Can't do much about that, so
                        // let's just assume that they are not, and keep going.

                        // Rename so the equations are simpler to read
                        float x1 = startVertexPos[0];
                        float y1 = startVertexPos[1];
                        float x2 = endVertexPos[0];
                        float y2 = endVertexPos[1];

                        float u = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) /
                                ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

                        if(u < 0 || u > 1)
                            continue;

                        float px = x1 + u * (x2 - x1);
                        float py = y1 + u * (y2 - y1);

                        float distance = (x - px) * (x - px) + (y - py) * (y - py);

                        if(distance <=
                                SEGMENT_PICK_DISTANCE * SEGMENT_PICK_DISTANCE) {
                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closest = eWrapper;
                                segmentFound = segment.getSegmentID();
                            }
                        }
                    }
                }

                // Did we really pick this segmented entity or not? If both
                // the vertex and segment ID are negative then we didn't
                // actually intersect with this segmented object, so we will
                // now ignore it and move on to checking the next item.
                if(vertexFound >= 0 || segmentFound >= 0) {
                    segmentID = segmentFound;
                    vertexID = vertexFound;
                }
            } else {
                eWrapper.getScreenPosition(entityPosition);

                // the distance from the mouse position to the center of the entity
                double dx = x - entityPosition[0];
                double dy = y - entityPosition[1];

                // TODO: This scheme doesn't work great for PatrolZones where
                // the (x, y) center is starts at one end of the zone

                if (Math.abs(dx) < SELECTION_RADIUS && Math.abs(dy) < SELECTION_RADIUS) {
                    closest = eWrapper;
                }
            }
        }

        if (closest != null) {
            boolean entitySelected = closest.isSelected();
            Entity entity = closest.getEntity();

            int segSelected = -1;
            int vertSelected = -1;
            if (entity instanceof SegmentableEntity) {
                segSelected = ((SegmentableEntity)entity).getSelectedSegmentID();
                vertSelected = ((SegmentableEntity)entity).getSelectedVertexID();
            }

            if (!closest.isSelected() || (entitySelected && segSelected > -1 && vertSelected > -1)) {
                return new GTEntitySearchReturn(closest, -1,-1);
            } else if (segSelected < 0 && segmentID > -1) {
                return new GTEntitySearchReturn(closest, segmentID, -1);
            }
        }

        return new GTEntitySearchReturn(closest, -1, vertexID);
    }

    /**
     * Find all entities given a screen bounding box.  This will return
     * the list of entities.
     *
     * @param bounds The search box
     * @return The entity list
     */
    private List<Entity> findEntity(Rectangle bounds) {

        List<Entity> entityList = new ArrayList<>();

        if (!entityWrapperMap.isEmpty()) {

            int[] screenPosition = new int[2];
            double[] worldPosition = new double[3];

            int[] screenCheck = new int[2];
            Matrix4f rotMatrix;

            int width = boundRectangle.width;
            int height = boundRectangle.height;

            int x_min = boundRectangle.x;
            int y_min = boundRectangle.y;
            int x_max;
            int y_max;

            // rearrange the coords in case the drag has been
            // working in a negative direction
            if (width < 0) {
                x_min += width;
                x_max = x_min - width;
            } else {
                x_max = x_min + width;
            }
            if (height < 0) {
                y_min += height;
                y_max = y_min - height;
            } else {
                y_max = y_min + height;
            }

            for (GTEntityWrapper eWrapper : wrapperList) {
                // get the screen postion and heading
                eWrapper.getScreenPosition(screenPosition);
                eWrapper.getWorldPosition(worldPosition);
                float radians = eWrapper.getHeadingRadians();

                // Set the rotation to use
                rotMatrix = new Matrix4f();
                rotMatrix.setIdentity();
                rotMatrix.rotY(radians);

                // Sget the offset to use
                int sizeX2 = (int) round(eWrapper.getIconWidth() * 0.5);
                int sizeY2 = (int) round(eWrapper.getIconHeight() * 0.5);

                //****** Left Top Coner ******//
                getCheckPoint(screenPosition, worldPosition, rotMatrix,
                        -sizeX2, -sizeY2, screenCheck);

                // if outside the left-hand corner then stop
                if ((screenCheck[0] < x_min) || (screenCheck[0] > x_max) ||
                        (screenCheck[1] < y_min) || (screenCheck[1] > y_max)) {
                    continue;
                }

                //****** Right Top Coner ******//
                getCheckPoint(screenPosition, worldPosition, rotMatrix,
                        sizeX2, -sizeY2, screenCheck);

                // if outside the left-hand corner then stop
                if ((screenCheck[0] < x_min) || (screenCheck[0] > x_max) ||
                        (screenCheck[1] < y_min) || (screenCheck[1] > y_max)) {
                    continue;
                }

                //****** Right Bottom Coner ******//
                getCheckPoint(screenPosition, worldPosition, rotMatrix,
                        sizeX2, sizeY2, screenCheck);

                // if outside the left-hand corner then stop
                if ((screenCheck[0] < x_min) || (screenCheck[0] > x_max) ||
                        (screenCheck[1] < y_min) || (screenCheck[1] > y_max)) {
                    continue;
                }

                //****** Left Bottom Coner ******//
                getCheckPoint(screenPosition, worldPosition, rotMatrix,
                        -sizeX2, sizeY2, screenCheck);

                // if outside the left-hand corner then stop
                if ((screenCheck[0] < x_min) || (screenCheck[0] > x_max) ||
                        (screenCheck[1] < y_min) || (screenCheck[1] > y_max)) {
                    continue;
                }

                entityList.add(eWrapper.getEntity());
            }
        }

        return entityList;
    }


    /**
     *
     * @param screenPosition
     * @param worldPosition
     * @param rotMatrix
     * @param offsetX
     * @param offsetY
     * @param screenCheck
     */
    private void getCheckPoint(int[] screenPosition, double[] worldPosition,
            Matrix4f rotMatrix, int offsetX, int offsetY, int[] screenCheck) {

        double[] worldCheck = new double[3];
        Vector3f pos;

        // get the check point
        screenCheck[0] = screenPosition[0] + offsetX;
        screenCheck[1] = screenPosition[1] + offsetY;

        convertScreenPosToWorldPos(screenCheck[0], screenCheck[1], worldCheck);

        // move check point relative to origin
        worldCheck[0] = worldCheck[0] - worldPosition[0];
        worldCheck[1] = worldCheck[1] - worldPosition[1];
        worldCheck[2] = worldCheck[2] - worldPosition[2];

        // create check vector from origin to check point
        pos =
            new Vector3f((float) worldCheck[0],
                (float) worldCheck[1],
                (float) worldCheck[2]);

        // rotate about origin
        rotMatrix.transform(pos);

        // traslate back to start
        worldCheck[0] = pos.x + worldPosition[0];
        worldCheck[1] = pos.y + worldPosition[1];
        worldCheck[2] = pos.z + worldPosition[2];

        // change to screen positioning
        convertWorldPosToScreenPos(worldCheck, screenCheck);
    }

    /**
     * Convert mouse coordinates into world coordinates.
     *
     * @param panelX The panel x coordinate
     * @param panelY The panel y coordinate
     * @param position The world position in meters
     */
    private void convertScreenPosToWorldPos(int panelX, int panelY, double[] position) {

        if (!mapIsAvailable)
            return;

        // the map extent
        double mapWidth = mapArea.getWidth();
        double mapHeight = mapArea.getHeight();

        Rectangle panelBounds = mapPanel.getBounds();

        // the dimensions of the panel
        double panelWidth = panelBounds.getWidth();
        double panelHeight = panelBounds.getHeight();

        // translate the mouse position to map coordinates
        double x = (panelX * mapWidth / panelWidth) + mapArea.getMinX();
        double y = ((panelY * mapHeight) / panelHeight) - mapArea.getMaxY();

        position[0] = x;
        position[1] = 0;
        position[2] = y;
    }

    /**
     * Convert world coordinates in meters to panel pixel location.
     *
     * @param position World coordinates
     * @param pixel Mouse coordinates
     */
    private void convertWorldPosToScreenPos(double[] position, int[] pixel) {

        if (!mapIsAvailable)
            return;

        // the map extent
        double mapWidth = mapArea.getWidth();
        double mapHeight = mapArea.getHeight();

        Rectangle panelBounds = mapPanel.getBounds();

        // the dimensions of the panel
        double panelWidth = panelBounds.getWidth();
        double panelHeight = panelBounds.getHeight();

        // convert world coordinates to panel coordinates
/*
System.out.println("GT2DView.convertWorldPosToScreenPos()");
System.out.println("    position[0]: " + position[0]);
System.out.println("    mapArea.getMinX(): " +  mapArea.getMinX());
System.out.println("    panelWidth: " + panelWidth);
System.out.println("    mapWidth: " + mapWidth);
System.out.println("    x = ((" + position[0] + " - " + mapArea.getMinX() + ") * " + panelWidth + ") / " + mapWidth);
System.out.println("    position[2]: " + position[2]);
System.out.println("    mapArea.getMaxY(): " +  mapArea.getMaxY());
System.out.println("    panelHeight: " + panelHeight);
System.out.println("    mapHeight: " + mapHeight);
System.out.println("    y = ((" + position[2] + " - " + mapArea.getMaxY() + ") * " + panelHeight + ") / " + mapHeight);
*/
        int x = (int)round(((position[0] - mapArea.getMinX()) * panelWidth) / mapWidth);
        int y = (int)round(((position[2] + mapArea.getMaxY()) * panelHeight) / mapHeight);

        pixel[0] = x;
        pixel[1] = y;
    }

    /**
     * Get the value of an attribute as a float
     *
     * @param The attribute name
     * @param The parent element
     * @return The value as a float.
     */
    private float getAttributeFloatValue(String name, Element e) {
        String val = e.getAttribute(name);

        if (val == null) {
            errorReporter.messageReport("No attribute: " + name);
            return 0;
        }

        return parseFloat(val.trim());
    }

    /**
     * Get the value of an attribute as a boolean
     *
     * @param The attribute name
     * @param The parent element
     * @return The value as a boolean.
     */
    private boolean getAttributeBooleanValue(String name, Element e) {
        String val = e.getAttribute(name);

        if (val == null) {
            errorReporter.messageReport("No attribute: " + name);
            return false;
        }

        return val.equalsIgnoreCase("TRUE");
    }

    /**
     * Calculate the scaling factors based on the icon size and the tool size.
     * The goal is to have a correctly scaled icon with some minimum size representation.
     *
     * @param fixedAspect Is the aspect ratio of the icon fixed
     * @param imgWidth The icon image width in pixels
     * @param imgHeight The icon image height in pixels
     * @param toolWidth The tool width
     * @param toolLength The tool length
     * @param scale The calculated x and y scale
     * @param center The calculated x and y center
     */
    private void calcScaleFactor(boolean fixedAspect, int imgWidth, int imgHeight, float toolWidth,
        float toolLength, float[] scale, int[] center) {

        float scale_f = (float)(mapScale);

        if (!fixedAspect) {
            iconScaleX = abs(toolWidth / scale_f / (imgWidth));
            iconScaleY = abs(toolLength / scale_f / (imgHeight));
        } else {
            if (toolLength > toolWidth) {
                iconScaleX = abs(toolWidth / scale_f / (imgWidth * toolWidth / toolLength));
                iconScaleY = abs(toolLength / scale_f / imgHeight);
            } else if (toolLength < toolWidth) {
                iconScaleX = abs(toolWidth / scale_f / imgWidth);
                iconScaleY = abs(toolLength / scale_f / imgHeight * toolLength / toolWidth);
            } else if (toolLength == toolWidth) {
                iconScaleX = abs(toolWidth / scale_f / (imgWidth));
                iconScaleY = abs(toolLength / scale_f / (imgHeight));
            }
        }

        //errorReporter.messageReport("iconScaleX: " + iconScaleX + " y: " + iconScaleY);
        if ((scale_f == 0) || ((iconScaleY * imgHeight < ICON_MINIMUM) && (iconScaleX * imgWidth < ICON_MINIMUM))) {
            if (toolWidth > toolLength) {
                //                    iconScaleX = (float) ICON_MINIMUM / imgWidth;
                //                    iconScaleY = ICON_MINIMUM * toolLength / toolWidth / imgHeight;
                iconScaleX = (float) ICON_MINIMUM / imgWidth;
                iconScaleY = (float) ICON_MINIMUM / imgHeight;
            } else {
                //                    iconScaleY = (float) ICON_MINIMUM / imgHeight ;
                //                    iconScaleX = ICON_MINIMUM * toolWidth / toolLength / imgWidth ;
                iconScaleY = (float) ICON_MINIMUM / imgHeight ;
                iconScaleX = (float) ICON_MINIMUM / imgWidth ;
            }

            //errorReporter.messageReport("Icon too small, increasing.  x: " + imgWidth * iconScaleX + " y: " + imgHeight * iconScaleY);
        }

        //errorReporter.messageReport("Icon scale: " + iconScaleX + " " + iconScaleY + " size x: " + imgWidth * iconScaleX + " y: " + imgHeight * iconScaleY);

        scale[0] = iconScaleX;
        scale[1] = iconScaleY;

        if (!fixedAspect) {
            center[0] = (int) ceil(imgWidth / 2 * iconScaleX);
            center[1] = (int) ceil(imgHeight / 2 * iconScaleY);
        } else {
            if (toolLength >= toolWidth) {
                //center[0] = (int) Math.ceil((imgWidth * toolWidth / toolLength) / 2 * iconScaleX);
                //              center[1]  = (int) Math.ceil(imgHeight / 2 * iconScaleY);

                center[0] = (int) ceil(imgWidth / 2 * iconScaleY);
                center[1] = (int) ceil(imgHeight / 2 * iconScaleX);

            } else {
                center[0] = (int) ceil(imgWidth / 2 * iconScaleX);
                center[1] = (int) ceil((imgHeight * toolLength / toolWidth) / 2 * iconScaleY);
            }
        }

        //errorReporter.messageReport("Icon size: " + (imgWidth * iconScaleX) + " " + (imgHeight * iconScaleY) + " center: " + center[0] + " " + center[1]);
    }

    /**
     * Create an Image from a url.  Cache the results.
     *
     * @return The Image or null if not found
     */
    private Image getImage(String url) {

        Image image = imageMap.get(url);
        if (image == null) {

            if (url == null || url.isEmpty()) {
                errorReporter.messageReport("Can't find image: " + url);
                return null;
            }

            ImageIcon icon = new ImageIcon(url);
            image = icon.getImage();
            imageMap.put(url, image);
        }

        return image;
    }

    /**
     * Calculate the scaling factor for the entity image and
     * return it in the argument array
     *
     * @param eWrapper The entity instance for which to calculate the scale factor
     * @param scale The argument array to initialize with the scale factor
     */
    private void calcEntityScale(GTEntityWrapper eWrapper, float[] scale) {

        ToolRenderer image = entityRendererMapper.getRenderer(eWrapper.getEntity());
        imgWidth = image.getWidth();
        imgHeight = image.getHeight();

        float[] size = new float[] {0, 0, 0};
        if (eWrapper.getEntity() instanceof PositionableEntity) {
            ((PositionableEntity)eWrapper.getEntity()).getSize(size);
        }


        float toolWidth = size[0];
        float toolLength = size[2];

        calcScaleFactor(
            eWrapper.getEntity().isFixedAspect(),
            imgWidth,
            imgHeight,
            toolWidth,
            toolLength,
            scale,
            tmpCenter);
    }

    /**
     * Adjust a rotation based on the orientation of the image.
     *
     * @param toMap Adjust 3D to the map, or map to 3D
     * @param rot The rotation
     */
    private void adjustRotation(boolean toMap, float[] rot) {
        // TODO: Need to generalize this
        if (toMap) {
            if (swap) {
                if (mapScale < 0) {
                    rot[3] = - rot[3];
                    rot[3] -= Math.PI / 2;
                }
            }
        } else {
            if (swap) {
                if (mapScale < 0) {
                    rot[3] += Math.PI / 2;
                    rot[3] = - rot[3];
                }
            }
        }
    }

    /**
     * Change the selected entity.
     *
     * @param id The entity selected
     * @param subid The sub entity id
     */
    private void changeSelection(List<Entity> selected) {

        List<Selection> list = new ArrayList<>(selected.size());

        for (Entity e : selected) {
            int segmentID = -1;
            int vertexID = -1;
            if (e instanceof SegmentableEntity) {
                segmentID = ((SegmentableEntity)e).getSelectedSegmentID();
                vertexID = ((SegmentableEntity)e).getSelectedVertexID();
            }

            Selection selection =
                    new Selection(e.getEntityID(), segmentID, vertexID);

            list.add(selection);
        }

        // if nothing is selected then select the location
        if (list.isEmpty()) {
            Entity location = model.getLocationEntity();
            Selection selection = new Selection(location.getEntityID(), -1, -1);

            list.add(selection);
        }

        model.changeSelection(list);
    }

    /**
     * Resets all entity management state variables.
     */
    private void resetState() {
        toolImage = null;
        currentTool = null;
        multiSegmentOp = false;
        mapPanel.setCursor(null);
    }

    /**
     * Reset the navigation control parameters to a default settings
     */
    private void resetNavigateState() {
        isOverNavControl = false;
        panInProgress = false;
        boundInProgress = false;
    }

    /**
     * Inform the view that a map image reader is being initialized
     * @param isLoading
     */
    public void setIsLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(true);
            progressBar.setString("Loading");
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            progressBar.setStringPainted(false);
        }
    }

    /**
     * Calculate new parameters given the argument change in zoom levels.
     * Updates and causes a redraw of the map to accommodate the changed
     * level.
     *
     * @param delta The number of levels to change
     */
    private void incrementZoomLevel(int delta) {

        // No point doing anything if we don't have a map right now.
        if (!mapIsAvailable)
            return;

        int newLevel = zoomLevel + delta;
        int minLevel = navControl.getMinimum();
        int maxLevel = navControl.getMaximum();

        if (newLevel < minLevel) {
            newLevel = minLevel;
        } else if (newLevel > maxLevel) {
            newLevel = maxLevel;
        }

        int levelChange = newLevel - zoomLevel;

        // If no change, then exit now, otherwise continue on and
        // recalculate all, causing a repaint of the window.
        if (levelChange == 0)
            return;

        double centerX = mapArea.getWidth() / 2.0;
        double centerY = mapArea.getHeight() / 2.0;

        double mapX = centerX + mapArea.getMinX();
        double mapY = centerY + mapArea.getMinY();

        double zlevel;
        if (levelChange < 0) {
            zlevel = zoomFactor * pow(zoomFactor, (-1 - levelChange));
        } else {
            zlevel = 1.0 / (zoomFactor * pow(zoomFactor, levelChange - 1));
        }

        // the new region of the map to display
        mapArea = new ReferencedEnvelope(
            mapX - (centerX / zlevel),
            mapX + (centerX / zlevel),
            mapY - (centerY / zlevel),
            mapY + (centerY / zlevel),
            crs);

        setZoomLevel(newLevel);
    }

    /**
     * Set an explicit zoom level. This assumes that a map area has been
     * set for the zoom level before this happens,
     *
     * @param level The zoom level to use
     */
    private void setZoomLevel(int level) {
        zoomLevel = level;

        lastZoomUpdateTime = currentTimeMillis();

        javax.swing.Timer timer =
            new javax.swing.Timer(ZOOM_UPDATE_CHECK_DELAY + 10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    long delay = currentTimeMillis() - lastZoomUpdateTime;
                    if(delay >= ZOOM_UPDATE_CHECK_DELAY) {
                        updateEntityScaleAndZoom();
                        mapPanel.repaint();
                    }
                }
            });

        timer.setRepeats(false);
        timer.start();

        navControl.setValue(zoomLevel);
    }

    /**
     * Initialize the User Interface, toolbar, map panel & status bar
     */
    private void initUI() {
        this.setLayout(new BorderLayout());

        toolBar = new JToolBar();

        Toolkit tk = getDefaultToolkit();

        Image image = tk.createImage(getSystemResource("images/2d/selectIcon.png"));
        pickAndPlaceButton = new JToggleButton(new ImageIcon(image));
        pickAndPlaceButton.setToolTipText("Pick & Place Mode");
        pickAndPlaceButton.setEnabled(false);
        pickAndPlaceButton.addActionListener(this);

        image = tk.createImage(getSystemResource("images/2d/panIcon.png"));
        navigateButton = new JToggleButton(new ImageIcon(image));
        navigateButton.setToolTipText("Navigate Mode");
        navigateButton.setEnabled(false);
        navigateButton.addActionListener(this);

        modeGroup = new ButtonGroup();
        modeGroup.add(navigateButton);
        modeGroup.add(pickAndPlaceButton);

        image = tk.createImage(getSystemResource("images/2d/openHandIcon.png"));
        panButton = new JToggleButton(new ImageIcon(image));
        panButton.setToolTipText("Pan");
        panButton.setEnabled(false);
        panButton.addActionListener(this);

        image = tk.createImage(getSystemResource("images/2d/boundIcon.png"));
        boundButton = new JToggleButton(new ImageIcon(image));
        boundButton.setToolTipText("Select Area");
        boundButton.setEnabled(false);
        boundButton.addActionListener(this);
        boundRectangle = new Rectangle();

        navigateGroup = new ButtonGroup();
        navigateGroup.add(panButton);
        navigateGroup.add(boundButton);

        toolBar.add(pickAndPlaceButton);
        toolBar.add(navigateButton);
        toolBar.addSeparator();
        toolBar.add(panButton);
        toolBar.add(boundButton);

        this.add(toolBar, BorderLayout.NORTH);

        mapPanel = new ImagePanel();
        mapPanel.setPreferredSize(new Dimension(512, 512));

        mapPanel.addMouseListener(this);
        mapPanel.addMouseMotionListener(this);
        mapPanel.addMouseWheelListener(this);
        mapPanel.addKeyListener(this);

        navControl = new GTPanZoomControl(mapPanel, NAV_CONTROL_LOCATION);
        navControl.addActionListener(this);
        navControl.addChangeListener(this);

        this.add(mapPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new GridLayout(1, 3, 1, 1));

        statusField = new JTextField();
        statusField.setHorizontalAlignment(SwingConstants.CENTER);
        statusField.setEditable(false);
        statusPanel.add(statusField);

        scaleField = new JTextField();
        scaleField.setHorizontalAlignment(SwingConstants.CENTER);
        scaleField.setEditable(false);
        statusPanel.add(scaleField);

        progressBar = new JProgressBar(0, 100);
        statusPanel.add(progressBar);

        this.add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Set the directory to load images from.
     *
     * @param dir The image dir
     */
    private void setImageDirectory(String dir) {
        if (dir == null) {
            dir = getProperty("user.dir");
            errorReporter.warningReport("No image directory set, using: " + dir, null);
        }

        if (!dir.endsWith("/"))
            imgDir = dir + "/";
        else
            imgDir = dir;
    }

    /**
     * Highlight a set of tools.
     *
     * @param state True to highlight the tool, false to unhighlight all tools
     */
    private void highlightTools(String[] validTools, boolean state) {
        // Highlight all acceptable tools
        Entity[] entities = model.getModelData();

        int len = entities.length;

//System.out.println("Highlighting tools: " + state);
        for(int i=0; i < len; i++) {
            if (entities[i] == null)
                continue;

            if (state) {
                for (String validTool : validTools) {
                    if (entities[i].getName().equals(validTool)) {
                        entities[i].setHighlighted(true);
//System.out.println("Found entity to highlite: " + entities[i]);
                        break;
                    }
                }
            } else {
                entities[i].setHighlighted(false);
            }
        }

        mapPanel.entityUpdateRequired();
        //mapPanel.repaint();
    }

    /**
     * Convenience method to update the entity list for scale and zoom
     * based on the map changing size or the panel.
     */
    private void updateEntityScaleAndZoom() {

        mapPanel.updateMapArea();

        for (GTEntityWrapper eWrapper : wrapperList) {
            updateEntityScaleAndZoom(eWrapper);
        }

        mapPanel.coverageUpdateRequired();
    }

    /**
     * Convenience method to update a single entity for scale and zoom
     * based on the map changing size or the panel.
     */
    private void updateEntityScaleAndZoom(GTEntityWrapper eWrapper) {

        // set the current map info
        eWrapper.setMapArea(mapArea);

        if (eWrapper.getEntity().isSegmentedEntity()) {

            // get and set the scale factor
            //if (eWrapper.getTool().isFixedSize()) {
            //    eWrapper.setScale(1, 1);
            //} else {
                // calculate the scale factor of the entity relative to the map scale
            //    calcEntityScale(eWrapper, eScale);
            //    eWrapper.setScale(eScale[0], eScale[1]);
            //}
            eWrapper.setScale(1, 1);
            eWrapper.setScreenPosition(0, 0);
            eWrapper.updateTransform();
        } else {
            // get and set the scale factor
            if (eWrapper.isFixedSize()) {
                eWrapper.setScale(1, 1);
            } else {
                // calculate the scale factor of the entity relative to the map scale
                float[] eScale = new float[2];

                calcEntityScale(eWrapper, eScale);
                eWrapper.setScale(eScale[0], eScale[1]);
            }

            // get the position of the entity in world coordinates
            eWrapper.getWorldPosition(tmpPos);

            // convert world coordinates to viewer coordinates
            int[] pixel = new int[2];
            convertWorldPosToScreenPos(tmpPos, pixel);
            eWrapper.setScreenPosition(pixel[0], pixel[1]);
            eWrapper.updateTransform();
        }
    }
}

