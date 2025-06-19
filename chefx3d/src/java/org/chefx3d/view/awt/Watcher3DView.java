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

package org.chefx3d.view.awt;

// Standard Imports
import java.awt.*;
import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import org.chefx3d.model.*;

import org.chefx3d.tool.Tool;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.chefx3d.view.ViewConfig;
import org.chefx3d.view.ViewManager;
import org.chefx3d.view.ViewX3D;
import org.chefx3d.view.WorldLoaderListener;
import org.w3c.dom.Document;
import org.web3d.sai.util.SceneUtils;
import org.j3d.util.IntHashMap;
import org.web3d.x3d.sai.*;
import org.web3d.x3d.sai.environmentalsensor.ProximitySensor;
import org.web3d.x3d.sai.grouping.Group;
import org.web3d.x3d.sai.grouping.Transform;
import org.web3d.x3d.sai.navigation.Viewpoint;
import org.web3d.x3d.sai.pickingsensor.LinePicker;
import org.web3d.x3d.sai.rendering.Coordinate;
import org.web3d.x3d.sai.rendering.IndexedLineSet;
import org.web3d.x3d.sai.shape.Shape;
import org.web3d.x3d.sai.time.TimeSensor;
import org.xj3d.sai.Xj3DBrowser;

/**
 * A View which is backed by a full 3D scene.
 * No editing is possible right now, just viewing.
 *
 * 3D Views use the x3d_view stylesheet.  This stylesheet must be self-contained.
 *
 * @author Alan Hudson
 * @version $Revision: 1.41 $
 */
class Watcher3DView extends JPanel implements ViewX3D, ViewConfig, Runnable, BrowserListener,
    WorldLoaderListener, X3DFieldEventListener, ModelListener, EntityChangeListener {

    /** The initial world to load */
    private String initialWorld;

    /** Time in ms of no navigation activity till we issue a non-transient ChangeViewCommand */
    private static final int CHANGE_VIEW_TIME = 250;

    /** The world model */
    private WorldModel model;

    /** The X3D browser */
    private ExternalBrowser x3dBrowser;

    /** The current url */
    private String currentURL;

    /** The current scene */
    private X3DScene mainScene;

    /** Map between entityID and loaded models Transform node */
    private IntHashMap modelMap;

    /** The X3DComponent in use */
    private X3DComponent x3dComp;

    /** An X3D exporter */
    private X3DExporter exporter;

    /** Are we in associate mode */
    //private boolean associateMode;

    /** What mode are we in, master, slave, free nav */
    private int navMode;

    /** The unique viewID */
    private long viewID;

    /** The proximity sensor used for determining position */
    private ProximitySensor proxSensor;

    /** The position changed field of the proximitySensor */
    private SFVec3f posChanged;

    /** The orientation changed field of the proximitySensor */
    private SFRotation oriChanged;

    /** The master viewpoint */
    private Viewpoint masterViewpoint;

    /** The slaved viewpoint */
    private Viewpoint slavedViewpoint;

    /** The free viewpoint */
    private Viewpoint freeViewpoint;

    /** Scratch var for converting positions */
    private float[] tmpPosF;

    /** Scratch var for converting positions */
    private double[] tmpPosD;

    /** Scratch var for sending linear velocity */
    private float[] linearVelocity;

    /** Scratch var for sending angular velocity */
    private float[] angularVelocity;

    /** The last position */
    private double[] lastPos;

    /** The last orientation */
    private float[] lastOri;

    /** The field count of changed fields */
    private int fieldCnt;

    /** The last position update from X3D */
    private float[] newPosition;

    /** The last orientation update from X3D */
    private float[] newOrientation;

    /** The current transactionID */
    private int transactionID;

    /** The last time of navigation activity */
    private long lastNavigationTime;

    /** The last frame time */
    private long lastFrameTime;

    /** Should we terminate the worker thread */
    private boolean terminate;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** Has the main world loaded */
    private boolean worldLoaded;

    /////////////////////////////////////////////////////////////////////////
    // picking variables

    /** Debugging flag that makes the pick line visible */
    private static final boolean SHOW_PICK_LINE = false;

    /** Flag indicating whether this view should manage entity
    *  elevation setting */
    private boolean configElevation = false;

    /** The pick sensor */
    private LinePicker pickNode;

    /** The pick sensor's isActive field */
    private SFBool isActive;

    /** The sensor pick point output field */
    private MFVec3f pickedPoint;

    /** The sensor pick target field. Used to determine when the
    *  targets have been established for a picking request. */
    private MFNode pickTarget;

    /** Flag indicating that the targets have been established
    *  for a picking request. */
    private boolean pickTargetReady;

    /** Flag indicating that the picking geometry has been relocated
    *  to the desired coordinates for a picking request. */
    private boolean pickGeometryReady;

    /** Flag indicating whether the picking geometry is intersecting
    *  pick targets. */
    private boolean pickNodeIsActive;

    /** The pick line geometry coordinate field */
    private Coordinate coord;

    /** The pick line coordinate point field */
    private MFVec3f coordPoint;

    /** The initial pick line coordinate points */
    private float[] line_point = new float[]{ 0, 10_000, 0, 0, -10_000, 0 };

    /** The pick line coordinate point indices */
    private int[] line_point_indicies = new int[]{ 0, 1 };

    /** The queue of picks to process */
    private List<EntityConfigData> pickQueue;

    /** The queue of picks that have been processed and are
    *  awaiting an ack in the form of a move command. */
    private List<EntityConfigData> completeQueue;

    /** Flag indicating that an elevation pick is active */
    private boolean pickInProgress;

    /** The collection of pickable targets in the scene. This includes
    *  the location geometry and any non-segmented entities. */
    private Vector<X3DNode> targetList;

    /** TimeSensor used for detecting event model cycles. Used on new
    *  world loads to manage initialization of scene dependent objects. */
    private TimeSensor timeSensor;

    /** TimeSensor field that listens for detecting event model cycles. */
    private SFTime time;

    /** A frame counting watchdog, to terminate picks that are initiated
     *  but the pick geometry does not intersect a target. */
    private int pickWatchdog;

    /////////////////////////////////////////////////////////////////////////

    public Watcher3DView(WorldModel model, String initialWorld) {
        super(new BorderLayout());

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        if (initialWorld == null) {
            errorReporter.errorReport("No initial world specified for Watcher3D", null);
        }

        this.initialWorld = initialWorld;

        model.addModelListener(Watcher3DView.this);
        this.model = model;

        // Setup browser parameters
        // TODO: Watcher3DView - HashMap has mixed types, is not type safe in 1.5 generics
        Map<String, Object> requestedParameters = new HashMap<>();
        requestedParameters.put("Xj3D_FPSShown",Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown", Boolean.FALSE);
        requestedParameters.put("Xj3D_LocationPosition", "top");
        requestedParameters.put("Xj3D_LocationReadOnly", Boolean.FALSE);
        requestedParameters.put("Xj3D_OpenButtonShown", Boolean.FALSE);
        requestedParameters.put("Xj3D_ReloadButtonShown", Boolean.FALSE);
        requestedParameters.put("Xj3D_ShowConsole",Boolean.FALSE);
        requestedParameters.put("Xj3D_StatusBarShown",Boolean.TRUE);

        init(requestedParameters);

        viewID = (long) (Math.random() * Long.MAX_VALUE);
        navMode = MODE_SLAVED;
        tmpPosF = new float[3];
        tmpPosD = new double[3];
        linearVelocity = new float[3];
        angularVelocity = new float[4];
        lastPos = new double[3];
        lastOri = new float[4];
        newPosition = new float[3];
        newOrientation = new float[4];
        fieldCnt = 0;

        terminate = false;
        Thread thread = new Thread(this, "Watcher3D Tasks");
        thread.start();

        ViewManager.getViewManager().addView(Watcher3DView.this);

        pickQueue = new ArrayList<>();
        completeQueue = new ArrayList<>();
    }

    public Watcher3DView(Map<String, Object> params) {
        super(new BorderLayout());

        init(params);
    }

    /**
     * The current tool changed.
     *
     * @param tool The new tool
     */
    @Override
    public void setTool(Tool tool) {
        // ignored
    }

    /**
     * Go into associate mode.  The next selection in any view
     * will issue a selection event and do nothing else.
     */
    @Override
    public void enableAssociateMode(String[] validTools) {
        //associateMode = true;
    }

    /**
     * Exit associate mode.
     */
    @Override
    public void disableAssociateMode() {
        //associateMode = false;
    }

    /**
     * Shutdown this view.
     */
    @Override
    public void shutdown() {
        model.removeModelListener(this);

        x3dBrowser.dispose();
        x3dComp.shutdown();

        x3dBrowser = null;
        mainScene = null;
        x3dComp = null;

        terminate = true;
    }

    /**
     * Common initialization code.
     *
     * @param params Xj3D Params to pass through
     */
    private void init(Map<String, Object> params) {

        // Create an SAI component
        x3dComp = BrowserFactory.createX3DComponent(params);

        add((Component) x3dComp, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1,3));
        JButton masterButton = new JButton(
            new ControlAction("Master",MODE_MASTER, this));

        JButton slavedButton = new JButton(
            new ControlAction("Slaved",MODE_SLAVED, this));

        JButton freeButton = new JButton(
            new ControlAction("Free",MODE_FREE_NAV, this));

        buttonPanel.add(masterButton);
        buttonPanel.add(slavedButton);
        buttonPanel.add(freeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Get an external browser
        x3dBrowser = x3dComp.getBrowser();
        x3dBrowser.addBrowserListener(this);

        ((Xj3DBrowser)x3dBrowser).setMinimumFrameInterval(40);

        modelMap = new IntHashMap(100);

        exporter = new X3DExporter("3.2", "Immersive", null, null);

        ViewManager.getViewManager().addView(this);
    }

    /**
     * Return the X3D component in use.
     *
     * @return The component
     */
    @Override
    public X3DComponent getX3DComponent() {
        return x3dComp;
    }

    //----------------------------------------------------------
    // Methods required by Runnable
    //----------------------------------------------------------

    @Override
    public void run() {
        model.reissueEvents(this, this);

        // Handle issueing of ChangeViewCommands once navigation has stopped */
        while(!terminate) {
            try {
                Thread.sleep(CHANGE_VIEW_TIME);
            } catch(InterruptedException e) {
                // ignore
            }

            if (transactionID != 0 && (System.currentTimeMillis() - lastNavigationTime) >= CHANGE_VIEW_TIME) {
                // TODO: Might have threading issues with transient usage of newPosition/newOrientation vars
                if (navMode == MODE_MASTER) {
                    tmpPosD[0] = newPosition[0];
                    tmpPosD[1] = newPosition[1];
                    tmpPosD[2] = newPosition[2];

                    //errorReporter.messageReport("***Changing view to: " + java.util.Arrays.toString(tmpPosD));

                    ChangeViewCommand cmd = new ChangeViewCommand(model, transactionID, tmpPosD,
                        newOrientation, (float) Math.PI / 4);
                    model.applyCommand(cmd);

                    transactionID = 0;
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by WorldLoaderListener
    //----------------------------------------------------------

    @Override
    public void newWorldLoaded(X3DScene mainScene) {
        // TODO: this does not handle the initialization for
        // automated elevation configuration. BAD things will
        // surely happen if this method is called.
        this.mainScene = mainScene;
    }

    //----------------------------------------------------------
    // Methods required by View
    //----------------------------------------------------------

    /**
     * Set the location.
     *
     * @param url The url of the location.
     */
    public void setLocation(String url) {
        //errorReporter.messageReport("Loading Location: " + url);
        worldLoaded = false;

        /////////////////////////////////////////////////////////////////////////////
        // load the initial

        X3DScene initialScene = x3dBrowser.createX3DFromURL(new String[] { initialWorld });

        /////////////////////////////////////////////////////////////////////////////
        // validate that the loaded scene meets the minimum requirements.
        // if not, create one that does and populate it with the nodes from
        // the initial scene.
        int version_major = 3;
        int version_minor = 2;
        ProfileInfo profileInfo = x3dBrowser.getProfile("Immersive");
        ComponentInfo[] componentInfo =
            new ComponentInfo[]{x3dBrowser.getComponentInfo("PickingSensor", 1)};

        boolean sceneIsCompatible = SceneUtils.validateSceneCompatibility(
            initialScene,
            version_major,
            version_minor,
            profileInfo,
            componentInfo);

        if (sceneIsCompatible) {
            mainScene = initialScene;
        } else {
            // TODO: should check the initial scene for additional
            // included components that are required......
            mainScene = x3dBrowser.createScene(profileInfo, componentInfo);

            SceneUtils.copyScene(initialScene, mainScene);
        }

        /////////////////////////////////////////////////////////////////////////////
        // load the new location

        X3DScene locationScene = x3dBrowser.createX3DFromURL(new String[]{url});

        SceneUtils.copyScene(locationScene, mainScene);

        /////////////////////////////////////////////////////////////////////////////
        // build the picker and the pick geometry

        pickNode = (LinePicker)mainScene.createNode("LinePicker");
        pickNode.setIntersectionType("GEOMETRY");
        pickNode.setSortOrder("ALL");

        isActive = (SFBool)pickNode.getField("isActive");
        isActive.addX3DEventListener(this);

        pickedPoint = (MFVec3f)pickNode.getField("pickedPoint");
        pickedPoint.addX3DEventListener(this);

        pickTarget = (MFNode)pickNode.getField("pickTarget");
        pickTarget.addX3DEventListener(this);

        IndexedLineSet ils = (IndexedLineSet)mainScene.createNode("IndexedLineSet");
        coord = (Coordinate)mainScene.createNode("Coordinate");
        coordPoint = (MFVec3f)coord.getField("point");
        coordPoint.addX3DEventListener(this);

        coord.setPoint(line_point);
        ils.setCoord(coord);
        ils.setCoordIndex(line_point_indicies);

        pickNode.setPickingGeometry(ils);
        pickNode.setEnabled(false);

        mainScene.addRootNode(pickNode);

        if ( SHOW_PICK_LINE ) {
            Shape shape = (Shape)mainScene.createNode("Shape");
            shape.setGeometry(ils);

            Group group = (Group)mainScene.createNode("Group");
            group.setChildren( new X3DNode[]{shape} );

            mainScene.addRootNode(group);
        }

        // clear the pick target collection when loading a new world
        targetList = null;

        /////////////////////////////////////////////////////////////////////////////
        // setup a time sensor for tracking cycles through the event model
        // on initialization

        timeSensor = (TimeSensor)mainScene.createNode("TimeSensor");
        timeSensor.setLoop(true);
        timeSensor.setEnabled(false);

        time = (SFTime)timeSensor.getField("time");
        time.addX3DEventListener(this);

        mainScene.addRootNode(timeSensor);

        /////////////////////////////////////////////////////////////////////////////
        // Replace the current world with the new one

        // TODO: Need to figure out how to not add for non networked
        addNetworkNodes();

        x3dBrowser.replaceWorld(mainScene);
    }

    /**
     * Set the location.
     *
     * @param url The url of the location.
     */
    public void oldsetLocation(String url) {
        //errorReporter.messageReport("Loading Location: " + url);

        if (!url.startsWith("file:")) {
            File file = new File(".");

            try {
                String path = file.toURI().toURL().toExternalForm();
                path = path.substring(0,path.length() - 2);   // remove ./

                url = path + url;
            } catch(MalformedURLException e) {
                errorReporter.errorReport("File Error!", e);
            }
        }

        // Create an X3D scene by loading a file.  Blocks till the world is loaded.
        try {
            mainScene = x3dBrowser.createX3DFromURL(new String[] { url });
        } catch(InvalidBrowserException | InvalidURLException | InvalidX3DException e) {
            errorReporter.errorReport("Error loading file: " + url, e);
            return;
        }

        // TODO: What if the main scene is not Immersive?

        // Add master,slaved,free viewpoints
        masterViewpoint = (Viewpoint) mainScene.createNode("Viewpoint");
        slavedViewpoint = (Viewpoint) mainScene.createNode("Viewpoint");
        freeViewpoint = (Viewpoint) mainScene.createNode("Viewpoint");

        // Add a ProximitySensor for tracking movement

        proxSensor = (ProximitySensor)mainScene.createNode("ProximitySensor");
//        proxSensor.setSize(new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE });
        // TODO: Need to support infinite size sensors.
        proxSensor.setSize(new float[] { 50_000, 50_000, 50_000});

        posChanged = (SFVec3f) proxSensor.getField("position_changed");
        posChanged.addX3DEventListener(this);

        oriChanged = (SFRotation) proxSensor.getField("orientation_changed");
        oriChanged.addX3DEventListener(this);
/*
        mainScene.addRootNode(proxSensor);
        mainScene.addRootNode(masterViewpoint);
        mainScene.addRootNode(slavedViewpoint);

        mainScene.addRootNode(freeViewpoint);
*/
        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);
    }

    /**
     * Control of the view has changed.
     *
     * @param newMode The new mode for this view
     */
    @Override
    public void controlChanged(int newMode) {
        navMode = newMode;

System.out.println("Control Changed: " + newMode);
        // TODO: This was in setLocation, moved sonon networked worlds wouldn't
        // get these viewpoints.  Not tested yet
        // Add master,slaved,free viewpoints

        addNetworkNodes();

        if (posChanged == null) {return;}

        if (navMode == MODE_MASTER) {
            ChangeMasterCommand cmd = new ChangeMasterCommand(model, viewID);
            model.applyCommand(cmd);

            posChanged.getValue(newPosition);
            oriChanged.getValue(newOrientation);

            //errorReporter.messageReport("*** newPos: " + java.util.Arrays.toString(newPosition));

            // Move all slaved VP's to Masters current location
            tmpPosD[0] = newPosition[0];
            tmpPosD[1] = newPosition[1];
            tmpPosD[2] = newPosition[2];

            // TODO: This doesn't work if the user has never moved.
            transactionID = model.issueTransactionID();
            ChangeViewCommand cmd2 = new ChangeViewCommand(model, transactionID, tmpPosD,
                newOrientation, (float) Math.PI / 4);
            transactionID = 0;

            masterViewpoint.setPosition(newPosition);

            lastPos[0] = newPosition[0];
            lastPos[1] = newPosition[1];
            lastPos[2] = newPosition[2];

            masterViewpoint.setOrientation(newOrientation);

            model.applyCommand(cmd2);

        }
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
        int entityID = entity.getEntityID();
        double[] position = new double[] {0, 0, 0};
        float[] rotation = new float[] {0, 0, 0, 0};

        if (entity instanceof PositionableEntity) {
            ((PositionableEntity)entity).getPosition(position);
            ((PositionableEntity)entity).getRotation(rotation);
        }

        Map<String,Document> props = entity.getProperties();

        int type = entity.getType();

        //errorReporter.messageReport("Loading Entity: " + entity.getEntityID());

        if (type == Tool.TYPE_WORLD) {
            String url = entity.getURL();

            modelMap.clear();

            if (url.equals(currentURL))
                return;

            setLocation(url);

            currentURL = url;


            return;
        }

        X3DNode t = (X3DNode) modelMap.get(entityID);

        if (t != null) {
            // ignore dups as we expect them in a networked environment
            return;
        }

        if (mainScene != null) {
            Transform transform = (Transform)mainScene.createNode("Transform");
            MFNode children = (MFNode) transform.getField("children");

            if (entity.getType() != Tool.TYPE_MULTI_SEGMENT) {
                // Do not translate segment tools, they are in world coords

                transform.setTranslation(new float[] {(float)position[0],(float)position[1],(float)position[2]});
                transform.setRotation(rotation);
            }

            generateX3D(entityID, children, mainScene);
            mainScene.addRootNode(transform);
            modelMap.put(entityID, transform);

            if (!entity.isSegmentedEntity()) {
                if (configElevation) {
                    queueConfigElevationRequest(new EntityConfigData(entityID, position));
                } else {
                    if (targetList == null)
                        initializePickTargets();
                    targetList.add(transform);
                    pickTargetReady = false;
                    pickNode.setPickTarget(targetList.toArray(new X3DNode[targetList.size()]));
                }
            }
        }

        model.addEntityChangeListener(entity, this);
    }

    /**
     * An entity was removed.
     *
     * @param entityID The id
     */
    @Override
    public void entityRemoved(boolean local, Entity entity) {
        int entityID = entity.getEntityID();
        X3DNode transform = (X3DNode) modelMap.get(entityID);

        if (transform == null) {
            errorReporter.messageReport("Entity not found for removal in Watcher3D: " + entity.getEntityID());
            return;
        }

        mainScene.removeRootNode(transform);
        modelMap.remove(entityID);

        // remove the entity from the pickable target collection,
        // note that segmented entities should never get on the list
        if (!entity.isSegmentedEntity()) {
            if (targetList.remove(transform)) {
                pickTargetReady = false;
                pickNode.setPickTarget(targetList.toArray(new X3DNode[targetList.size()]));
            }
        }
    }

    /**
     * The entity moved.
     *
     * @param entityID the id
     * @param position The position in world coordinates(meters, Y-UP, X3D System).
     */
    @Override
    public void entityMoved(boolean local, int entityID, double[] position) {
        X3DNode transform = (X3DNode) modelMap.get(entityID);

        // TODO: Do we want to use to GeoLocation for double precision?
        ((Transform)transform).setTranslation(new float[]{
                (float)position[0],
                (float)position[1],
                (float)position[2]});

        if (configElevation) {
            boolean moveProcessed = false;
            int num = completeQueue.size();
            // check to see if this move command is a result of an elevation
            // change caused by a pick. if so - ignore it.
            for (int i=0; i<num; i++) {
                EntityConfigData data = completeQueue.get(i);
                if ((data.entityID == entityID)&&
                    (data.vertexID == -1)&&
                    (Arrays.equals(data.position, position))) {
                    moveProcessed = true;
                    completeQueue.remove(i);
                    break;
                }
            }
            if(!moveProcessed) {
                synchronized( this ) {
                    // if any picks are queued for this entity, delete them,
                    // as they are obsolete.
                    num = pickQueue.size( );
                    for(int i = num-1; i>=0; i--) {
                        EntityConfigData data = pickQueue.get(i);
                        if (data.entityID == entityID) {
                            pickQueue.remove(i);
                        }
                    }
                    // remove this entity from the target list, and queue the pick request
                    if (targetList.remove(transform)) {
                        pickTargetReady = false;
                        pickNode.setPickTarget(targetList.toArray(new X3DNode[targetList.size()]));
                    }
                    queueConfigElevationRequest(new EntityConfigData(entityID, position));
                }
            }
        }
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

        if (mainScene == null)
            return;

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);
    }

    /**
     * A segment was split.
     *
     * @param local Was this action initiated from the local UI
     * @param entityID The unique entityID assigned by the view
     * @param segmentID The unique segmentID
     * @param vertexID The starting vertexID
     */
    @Override
    public void segmentSplit(boolean local, int entityID,
            int segmentID, int vertexID) {

        if (mainScene == null)
            return;

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);
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

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);
    }

    /**
     * A vertex was added from the segment sequence.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the segment sequence
     * @param pos The x position in world coordinates
     */
    @Override
    public void segmentVertexAdded(boolean local, int entityID, int vertexID,
            double[] position) {

        if (mainScene == null)
            return;

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);

        if (configElevation) {
            queueConfigElevationRequest(new EntityConfigData(entityID, vertexID, position));
        }
    }

    /**
     * A vertex was updated.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the segment sequence
     * @param propertyName The property name
     * @param propertySheet The property sheet
     * @param propertyValue The property value
     */
    @Override
    public void segmentVertexUpdated(boolean local, int entityID, int vertexID,
            String propertyName, String propertySheet, String propertyValue) {
        if (mainScene == null)
            return;

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);
    }

    /**
     * A vertex was moved.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the segment sequence
     * @param pos The new position in world coordinates
     */
    @Override
    public void segmentVertexMoved(boolean local, int entityID, int vertexID,
            double[] position) {
        if (mainScene == null)
            return;

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);

        if (configElevation) {
            boolean moveProcessed = false;
            int num = completeQueue.size();
            // check to see if this move command is a result of an elevation
            // change caused by a pick. if so - ignore it.
            for (int i=0; i<num; i++) {
                EntityConfigData data = completeQueue.get(i);
                if ((data.entityID == entityID)&&
                    (data.vertexID == vertexID)&&
                    (Arrays.equals(data.position, position))) {
                    moveProcessed = true;
                    completeQueue.remove(i);
                    break;
                }
            }
            if (!moveProcessed) {
                synchronized( this ) {
                    // if any picks are queued for this vertex, delete them,
                    // as they are obsolete.
                    num = pickQueue.size( );
                    for(int i = num-1; i>=0; i--) {
                        EntityConfigData data = pickQueue.get(i);
                        if ((data.entityID == entityID)&&
                            (data.vertexID == vertexID)) {

                            pickQueue.remove(i);
                        }
                    }
                    queueConfigElevationRequest(new EntityConfigData(entityID, vertexID, position));
                }
            }
        }
    }

    /**
     * A vertex was removed from the segment sequence.
     *
     * @param local Was this a local change
     * @param entityID The unique entityID assigned by the view
     * @param vertexID The unique vertexID assigned by the segment sequence
     * @param pos The x position in world coordinates
     */
    @Override
    public void segmentVertexRemoved(boolean local, int entityID, int vertexID) {
        if (mainScene == null)
            return;

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);
    }

    /**
     * An entity has changed size.
     *
     * @param entityID The unique entityID assigned by the view
     * @param size The new size in meters
     */
    @Override
    public void entitySizeChanged(boolean local, int entityID, float[] size) {
        if (mainScene == null) {
            return;
        }

        X3DNode transform = (X3DNode) modelMap.get(entityID);
        MFNode children = (MFNode) transform.getField("children");

        generateX3D(entityID, children, mainScene);
    }

    /**
     * A property changed.
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

        //System.out.println("propertyChanged for entityID: " + entityID);

        Entity entity = model.getEntity(entityID);

        //TODO: what to do if a propertyChange event impacts the 3D view,
        // right now we just handle segmentEntity.  We issuea separate
        // sizeChnaged event for the Box sizing but later on users can
        // specify 3D updates.
        if (entity.isSegmentedEntity()) {
            if (mainScene == null) {
                return;
            }

            X3DNode transform = (X3DNode) modelMap.get(entityID);
            MFNode children = (MFNode) transform.getField("children");

            generateX3D(entityID, children, mainScene);
        }

    }

    /**
     * An entity was associated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityAssociated(boolean local, int parent, int child) {
        // ignore
    }

    /**
     * Set how helper objects are displayed.
     *
     * @param mode The mode
     */
    @Override
    public void setHelperDisplayMode(int mode) {
        // Retained mode is a bit harder, setup a switch?
        errorReporter.messageReport("Helper display not implemented on Watcher3DView");
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

    /**
     * An entity was unassociated with another.
     *
     * @param parent The parent entityID
     * @param child The child entityID
     */
    @Override
    public void entityUnassociated(boolean local, int parent, int child) {
        // ignore
    }

    /**
     * The entity was scaled.
     *
     * @param entityID the id
     * @param scale The scaling factors(x,y,z)
     */
    @Override
    public void entityScaled(boolean local, int entityID, float[] scale) {
    }

    /**
     * The entity was rotated.
     * @param rotation The rotation(axis + angle in radians)
     */
    @Override
    public void entityRotated(boolean local, int entityID, float[] rotation) {
        X3DNode transform = (X3DNode) modelMap.get(entityID);
        ((Transform)transform).setRotation(rotation);
    }

    /**
     * The entity was selected.
     *
     * @param selection The list of selected entities.  The last one is the latest.
     */
    @Override
    public void selectionChanged(List<Selection> selection) {
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
        if (navMode == MODE_SLAVED) {
            //errorReporter.messageReport("Got new pos: " + Arrays.toString(pos));

            tmpPosF[0] = (float) pos[0];
            tmpPosF[1] = (float) pos[1];
            tmpPosF[2] = (float) pos[2];

            slavedViewpoint.setPosition(tmpPosF);

            slavedViewpoint.setOrientation(rot);

            // TODO: Ignore fov for now
        }
    }

    /**
     * The master view has changed.
     *
     * @param view The view which is master
     */
    @Override
    public void masterChanged(boolean local, long viewID) {
        if (this.viewID == viewID && masterViewpoint != null) {
            masterViewpoint.setBind(true);

        } else if (navMode == MODE_MASTER && slavedViewpoint != null) {
            navMode = MODE_SLAVED;
            slavedViewpoint.setBind(true);

        } else if (navMode == MODE_SLAVED && slavedViewpoint != null) {
            // rebind to vp
            slavedViewpoint.setBind(true);
        }
    }

    /**
     * The model has been reset.
     *
     * @param local Was this action initiated from the local UI
     */
    @Override
    public void modelReset(boolean local) {
        // TODO: clear model
    }

    //----------------------------------------------------------
    // Methods defined by X3DFieldEventListener
    //----------------------------------------------------------

    /**
     * Handle field changes from the X3D world.
     *
     * @param evt The event
     */
    @Override
    public void readableFieldChanged(X3DFieldEvent evt) {
        Object src = evt.getSource();

        if (src.equals(posChanged)) {
            fieldCnt++;

            posChanged.getValue(newPosition);
            lastNavigationTime = System.currentTimeMillis();
        } else if (src.equals(oriChanged)) {
            fieldCnt++;

            oriChanged.getValue(newOrientation);
            lastNavigationTime = System.currentTimeMillis();
        } else if (src.equals(pickTarget)) {
            // picking targets have been established, enable the pick
            // processing to happen on the next pickedPoint event
            pickTargetReady = true;

        } else if (src.equals(pickedPoint)) {

            Command cmd = null;
            EntityConfigData data = (EntityConfigData)pickedPoint.getUserData();

            if (data != null) {

                synchronized(this) {
                    // check to see if a new request for the same entity has been
                    // queued while we waited for the picking and target geometry
                    // to become ready.
                    int queued = pickQueue.size( );
                    EntityConfigData next = null;
                    for(int i = queued-1; i>=0; i--) {
                        EntityConfigData queued_data = pickQueue.get(i);
                        if ((data.entityID == queued_data.entityID)&&
                            (data.vertexID == queued_data.vertexID)) {
                            // if a new pick request is pending on the same entity,
                            // remove it from the queue, make it the active request,
                            // and don't bother to process this one
                            queued_data = pickQueue.remove(i);
                            if (next == null) {
                                next = queued_data;
                            }
                            break;
                        }
                    }
                    if (next != null) {
                        // configPick() will set pickGeometryReady to false,
                        // and cause the following processing loop to be bypassed
                        configPick(next);
                    }
                }

                if (pickGeometryReady && pickTargetReady) {

                    // clear for the next pick
                    pickedPoint.setUserData(null);

                    // get the picked point
                    int num = pickNode.getNumPickedPoint();
                    float[] pickPoint = new float[num*3];
                    pickNode.getPickedPoint(pickPoint);

                    // if there are multiple, use the highest elevation point
                    if (num > 1) {
                        float max_elevation = Float.NEGATIVE_INFINITY;
                        int idx = 0;
                        for( int i=0; i<num; i++) {
                            float y = pickPoint[i*3+1];
                            if (y > max_elevation) {
                                idx = i;
                                max_elevation = y;
                            }
                        }
                        float[] max_elevation_point = new float[3];
                        System.arraycopy(pickPoint, idx*3, max_elevation_point, 0, 3);
                        pickPoint = max_elevation_point;
                    }

                    X3DNode transform = (X3DNode)modelMap.get(data.entityID);

                    // cast to double for comparison with the original position
                    double[] pickPoint_d = new double[]{
                        (double)pickPoint[0],
                        (double)pickPoint[1],
                        (double)pickPoint[2]};

                    // if the picked point elevation differs from the initial position,
                    // issue the move commands to pass the data on
                    if (data.position[1] != pickPoint_d[1]) {
                        if (data.isSegmented()) {
                            // since the segmented entity is created 'enmass', a vertex's
                            // elevation cannot be changed directly by it's Transform
                            cmd = new MoveVertexCommand(
                                model,
                                model.issueTransactionID(),
                                data.entityID,
                                data.vertexID,
                                pickPoint_d,
                                data.position);
                        } else {
                            // non-segmented entities have a unique associated Transform,
                            // and can be configured directly
                            ((Transform)transform).setTranslation(pickPoint);
                            cmd = new MoveEntityCommand(
                                model,
                                model.issueTransactionID(),
                                data.entityID,
                                pickPoint_d,
                                data.position);
                        }
                        // recreate the data object to contain the picked point, this will be used
                        // in the subsequent entityMoved() command processing to identify the
                        // source of the move as this, and to prevent it from picking again.
                        data = new EntityConfigData(data.entityID, data.vertexID, pickPoint_d);
                    }
                    // place the node into the pick targets once a pick is complete, unless:
                    // 1) the entity is segmented, segmented entities are not picked
                    // 2) the entity is already on the list (being defensive - this shouldn't happen)
                    if (!data.isSegmented() && !targetList.contains(transform)) {
                        targetList.add(transform);
                        pickTargetReady = false;
                        pickNode.setPickTarget(targetList.toArray(new X3DNode[targetList.size()]));
                    }
                }
                // manage the pickQueue
                synchronized (this) {
                    if (!pickQueue.isEmpty()) {
                        // if another pick is ready, configure it
                        configPick(pickQueue.remove(0));
                    } else {
                        // otherwise, return the picker state to the idle condition
                        pickInProgress = false;
                        pickGeometryReady = false;
                        pickNode.setEnabled(false);
                    }
                }
            }
            // process any move commands that have been generated
            if ( cmd != null ) {
                completeQueue.add(data);
                cmd.setErrorReporter(errorReporter);
                model.applyCommand(cmd);
            }
        } else if (src.equals(isActive)) {
            pickNodeIsActive = pickNode.getIsActive( );
            if (!pickNodeIsActive) {
                if (pickInProgress && pickGeometryReady) {
                    pickedPoint.setUserData(null);
                }
                pickGeometryReady = false;
                pickInProgress = false;
                pickQueue.clear();
            }
        } else if (src.equals(time)) {
            // the initial time event occurs when a new location is loaded,
            // after the browser initialized event
            if (!worldLoaded) {
                //timeSensor.setEnabled(false);
                initializePickTargets();
                x3dBrowser.nextViewpoint();
                worldLoaded = true;

            } else if (pickInProgress && pickGeometryReady && !pickNodeIsActive) {
                // a pick was initiated, the pick geometry is ready,
                // but not intersecting a target
                if (pickWatchdog-- <= 0) {
                    EntityConfigData data = (EntityConfigData)pickedPoint.getUserData();
                    pickedPoint.setUserData(null);
                    if (!data.isSegmented()) {
                        X3DNode transform = (X3DNode)modelMap.get(data.entityID);
                        if(!targetList.contains(transform)) {
                            targetList.add(transform);
                            pickTargetReady = false;
                            pickNode.setPickTarget(targetList.toArray(new X3DNode[targetList.size()]));
                        }
                    }
                    // queue the next pick
                    synchronized (this) {
                        if (!pickQueue.isEmpty()) {
                            // if another pick is ready, configure it
                            configPick(pickQueue.remove(0));
                        } else {
                            // otherwise, return the picker state to the idle condition
                            pickInProgress = false;
                            pickGeometryReady = false;
                            pickNode.setEnabled(false);
                        }
                    }
                }
            }
        } else if (src.equals(coordPoint)) {
            // each relocation of the pick line will cause this,
            // indicating that the pick geometry is ready
            pickGeometryReady = true;
            // set the watchdog, in case the pick geometry
            // does not interset anything at the coordinate
            pickWatchdog = 2;
        }

        // After both have changed send

        if (fieldCnt == 2) {
            if (navMode == MODE_MASTER) {
                if (transactionID == 0) {
                    transactionID = model.issueTransactionID();
                }

                tmpPosD[0] = newPosition[0];
                tmpPosD[1] = newPosition[1];
                tmpPosD[2] = newPosition[2];

                float factor = 1000f / (System.currentTimeMillis() - lastFrameTime);
                linearVelocity[0] = (float) (newPosition[0] - lastPos[0]) * factor;
                linearVelocity[1] = (float) (newPosition[1] - lastPos[1]) * factor;
                linearVelocity[2] = (float) (newPosition[2] - lastPos[2]) * factor;

                lastFrameTime = System.currentTimeMillis();

                angularVelocity[0] = newOrientation[0] - lastOri[0];
                angularVelocity[1] = newOrientation[1] - lastOri[1];
                angularVelocity[2] = newOrientation[2] - lastOri[2];

                lastPos[0] = newPosition[0];
                lastPos[1] = newPosition[1];
                lastPos[2] = newPosition[2];

                lastOri[0] = newOrientation[0];
                lastOri[1] = newOrientation[1];
                lastOri[2] = newOrientation[2];
                lastOri[3] = newOrientation[3];

//System.out.println("Sending pos: " + Arrays.toString(newPosition) + " ori: " + Arrays.toString(newOrientation));
                ChangeViewTransientCommand cmd = new ChangeViewTransientCommand(model, transactionID, tmpPosD,
                    newOrientation, linearVelocity, angularVelocity, (float) Math.PI / 4);
                model.applyCommand(cmd);
            }

            fieldCnt = 0;
        }
    }

    //---------------------------------------------------------
    // Methods defined by BrowserListener
    //---------------------------------------------------------

    /** The Browser Listener. */
    @Override
    public void browserChanged( final BrowserEvent be ) {
        final int id = be.getID( );
        switch (id) {
        case BrowserEvent.INITIALIZED:
            timeSensor.setEnabled(true);
            break;
        case BrowserEvent.SHUTDOWN:

        }
    }

    //----------------------------------------------------------
    // Methods defined by ViewConfig
    //----------------------------------------------------------

    /**
     * Enable the automated configuration of the elevation for added
     * and moved entities.
     *
     * @param enable The enabled state
     */
    @Override
    public void setConfigElevation(boolean enable) {
        configElevation = enable;
    }

    /**
     * Return the state of automated configuration of the elevation
     * for added and moved entities.
     *
     * @return The enabled state
     */
    @Override
    public boolean getConfigElevation() {
        return(configElevation);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Generate the X3D for an entity and replace the children contents with it.
     *
     * @param entityID The entity to generate
     * @param children The children to overwrite
     * @param scene The scene to add routes
     */
    private void generateX3D(int entityID, MFNode children, X3DScene mainScene) {
        StringWriter writer = new StringWriter(1_024);

        exporter.export(model, entityID, "view", writer);
        String x3d = writer.toString();

        // if the X3D is empty then exit
        if (x3d.equals(""))
            return;

//System.out.println("X3D:" + x3d.length());
//System.out.println(x3d);
//System.out.println("Done X3D");

        X3DScene scene = null;

        try {
            scene = x3dBrowser.createX3DFromString(x3d);
        } catch (InvalidBrowserException | InvalidX3DException e) {
            errorReporter.errorReport("Error parsing x3d: ", e);
        }

        // Copy the scene into the main one
        X3DNode[] nodes = scene.getRootNodes();

        int len = nodes.length;

        // Nodes must be removed before adding to another scene
        for(int i=0; i < len; i++) {
//System.out.println("Node name: " + nodes[i].getNodeName());

            scene.removeRootNode(nodes[i]);
        }

//System.out.println("children.getSize(): " + children.getSize());

        children.setValue(len, nodes);

        // TODO: Need to handle routes and how to remove old ones?
    }

    /**
     * Queue a request to have an entity's elevation configured.
     *
     * @param data The identifier info of the entity.
     */
    private void queueConfigElevationRequest(EntityConfigData data) {
        synchronized(this) {
            if (!pickInProgress || !pickNodeIsActive) {
                configPick(data);
            } else {
                pickQueue.add(data);
            }
        }
    }

    /**
     * Configure the pick node to get the elevation data at the desired location
     *
     * @param data The configuration request data
     */
    private void configPick(EntityConfigData data) {
        float x = (float)data.position[0];
        float z = (float)data.position[2];
        line_point[0] = x;
        line_point[2] = z;
        line_point[3] = x;
        line_point[5] = z;
        pickInProgress = true;
        pickGeometryReady = false;
        coord.setPoint(line_point);
        pickedPoint.setUserData(data);
        pickNode.setEnabled(true);
    }

    /**
     * Initialize the pick node's target geometry with everything
     * pickable in the main scene
     */
    private void initializePickTargets() {
        // get references to the pickable geometry in the scene
        X3DNode[] node = mainScene.getRootNodes();
        targetList = new Vector<>();
        SceneUtils.getPickTargets(node, targetList);
        node = targetList.toArray(new X3DNode[targetList.size()]);
        pickTargetReady = false;
        pickNode.setPickTarget(node);
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

    private void addNetworkNodes() {
        if (mainScene == null) {return;}
        masterViewpoint = (Viewpoint) mainScene.createNode("Viewpoint");
        masterViewpoint.setDescription("Networked_Master");
        slavedViewpoint = (Viewpoint) mainScene.createNode("Viewpoint");
System.out.println("Slaved VP: " + slavedViewpoint);
        masterViewpoint.setDescription("Networked_Slave");
        freeViewpoint = (Viewpoint) mainScene.createNode("Viewpoint");
        masterViewpoint.setDescription("Networked_Free");

        // Add a ProximitySensor for tracking movement

        proxSensor = (ProximitySensor)mainScene.createNode("ProximitySensor");
//        proxSensor.setSize(new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE });
        // TODO: Need to support infinite size sensors.
        proxSensor.setSize(new float[] { 50_000, 50_000, 50_000});

        posChanged = (SFVec3f) proxSensor.getField("position_changed");
        posChanged.addX3DEventListener(this);

        oriChanged = (SFRotation) proxSensor.getField("orientation_changed");
        oriChanged.addX3DEventListener(this);


        mainScene.addRootNode(proxSensor);
        mainScene.addRootNode(masterViewpoint);
        mainScene.addRootNode(slavedViewpoint);
        mainScene.addRootNode(freeViewpoint);
    }
}
