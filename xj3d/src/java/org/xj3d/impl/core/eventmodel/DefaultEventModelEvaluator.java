/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.eventmodel;

// Export imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;

import org.web3d.browser.NodeObserver;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.IntHashMap;
import org.j3d.util.HashSet;
import org.j3d.util.ObjectArray;
import org.web3d.vrml.util.NodeArray;
import org.web3d.vrml.util.NodeTemplateArray;

import org.xj3d.core.loading.ContentLoadManager;

/**
 * Default implementation of the event model evaluator that is
 * renderer-independent.
 * <p>
 *
 * Event model evaluation works for both VRML97 and X3D, with mixtures
 * of file types.
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public class DefaultEventModelEvaluator implements EventModelEvaluator {

    /** Error message when sending out the start synchronisation calls */
    private static final String SYNCH_START_MSG =
        "Error during external synchronised node pre-event callback";

    /** Error message when sending out the start synchronisation calls */
    private static final String SYNCH_STOP_MSG =
        "Error during external synchronised node post-event callback";

    /** Message when we have found a bindable that there is no manager for */
    private static final String NO_BINDABLE_MGR_MSG =
        "attempting to add a bindable node that has no manager for its layer.";

    /** Constant array for turning on/off layer node listening */
    private static final int[] LAYER_NODE_TYPES = {
        TypeConstants.LayerNodeType};

    /** Number of items to create in the initial array size */
    private static final int NUM_NODES = 32;

    /** How many do we increment by if we have to resize */
    private static final int NUM_NODES_INC = 8;


    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Init listener from the browser core */
    private EventModelInitListener initListener;

    /** Manager of routes */
    private RouteManager routeManager;

    /** Manager of all sensors */
    private SensorManager sensorManager;

    /** Manager of viewpoints */
    private ViewpointManager viewpointManager;

    /** Manager of the script nodes */
    private ScriptManager scriptManager;

    /** Frame state manager for this event model instance */
    private FrameStateManager stateManager;

    /** Content loader for all things other than scripts */
    private ContentLoadManager contentLoader;

    /** List of all the current external interfaces available */
    private final List<ExternalView> externalViews;

    /** List of all the current external synchronised nodes */
    private final NodeArray externalSyncNodes;

    /** The current main scene that this evaluator is processing */
    private VRMLScene currentScene;

    /** Flag to say if we are currently activeLayer and processing a scene */
    private boolean active;

    /** Working array used during the scene load process */
    private VRMLNodeType[] tmpNodes;

    /** The carrier used to copy stuff into from the Scene to managers */
    private final NodeArray tmpArray;

    /** Mapping of a script node to it's containing scene */
    private final Map<VRMLNode, BasicScene> scriptToSceneMap;

    /**
     * Flag to say that the world has changed and needs to be reloaded. Used in
     * combination with sceneToLoad. However, note that sceneToLoad may be null
     * in the case where someone has called the SAI's replaceWorld(null) to
     * clear the existing world to null.
     */
    private boolean sceneNeedsChanging;

    /** The next scene to load */
    private VRMLScene sceneToLoad;

    /**
     * A new viewpoint to bind to at the start of the next frame. We have
     * this sent in from an outside source such as a user interface or
     * LinkSelectionListener instance.
     */
    private VRMLViewpointNodeType pendingViewpoint;

    /** The DEF name of the initial viewpoint to use */
    private String initialViewpoint;

    /** List of node managers for pre-event model evaluation */
    private NodeManager[] preEventManagers;

    /** Number of valid managers in the array that managed to init */
    private int numPreEventManagers;

    /** List of node managers for post-event model evaluation */
    private NodeManager[] postEventManagers;

    /** Number of valid managers in the array that managed to init */
    private int numPostEventManagers;

    /**
     * Mapping of a scene to the list of scripts that need to be loaded before
     * the rest of the scene loading can proceed. This is only used for VRML97
     * worlds. The value of the map is a HashSet that contains the listing of
     * scripts that still need to be loaded. When the map becomes empty, the
     * key is removed and the rest of the world is queued for loading. The scene
     * is the VRML parent scene (VRMLScene) rather than the proto scenes.  This
     * means that when first loading the world, all the proto instances are walked
     * and have the scene mapping of the script to the containing world file.
     */
    private final Map<BasicScene, HashSet<VRMLNode>> sceneToScriptListMap;

    /** Flag to say if the scene has just started and needs init notification */
    private boolean justStarted;

    /**
     * Flag used for VRML97 worlds only to say that the initial script
     * loading required by the specification is completed.
     */
    private boolean initialWorldLoadComplete;

    /** The root space of this world */
    private VRMLExecutionSpace rootSpace;

    /** The last simulation time, avoids passing it around all methods. */
    private long lastTime;

    /** Flag to tell everything we've just been told to shut down */
    private boolean shutdownNow;

    /** Factory for generating new layers as needed */
    private LayerManagerFactory layerManagerFactory;

    /** Rendering manager for those layers */
    private LayerRenderingManager layerRenderer;

    /** The collection of layer managers */
    private LayerManager[] layerManagers;

    /** The current number of valid layers */
    private int numValidLayerManagers;

    /** The number of activeLayer layer managers from the current scene. */
    private int numActiveLayerManagers;

    /** The currently activeLayer layer manager for navigation */
    private int activeNavigationLayer;

    /** The rendering order of layer managers this frame */
    private int[] renderOrder;

    /** Mapping of a VRMLLayerNodeType to it's containing LayerRenderer */
    private final Map<VRMLNode, LayerManager> layerToRendererMap;

    /** True if there is a LayerSet in the current file last frame */
    private boolean haveLayerSet;

    /** Map of node type id to NodeObserver instances for that node */
    private final IntHashMap<List<NodeObserver>> nodeObserverMap;

    /**
     * Construct a new instance of the evaluator.
     */
    public DefaultEventModelEvaluator() {

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        externalViews = new ArrayList<>();
        externalSyncNodes = new NodeArray();
        scriptToSceneMap = new HashMap<>();
        sceneToScriptListMap = new HashMap<>();

        // populate the bindables map with at least two stacks - viewpoint
        // and NavigationInfo. These will always be needed, but others we can
        // add and create on the fly.
        tmpNodes = new VRMLNodeType[NUM_NODES];
        tmpArray = new NodeArray(NUM_NODES);

        active = false;
        justStarted = false;
        initialWorldLoadComplete = false;
        shutdownNow = false;
        haveLayerSet = false;
        sceneNeedsChanging = false;

        renderOrder = new int[1];
        layerManagers = new LayerManager[1];

        // Don't store the default layer in this map.
        layerToRendererMap = new HashMap<>();

        nodeObserverMap = new IntHashMap<>();
    }

    //----------------------------------------------------------
    // Methods defined by EventModelEvaluator
    //----------------------------------------------------------

    @Override
    public void initialize(ScriptManager scripts,
                           RouteManager router,
                           SensorManager sensors,
                           FrameStateManager fsm,
                           ContentLoadManager elm,
                           ViewpointManager vm,
                           LayerManagerFactory lmf,
                           LayerRenderingManager lrm,
                           NodeManager[] extManagers) {

        scriptManager = scripts;
        routeManager = router;
        sensorManager = sensors;
        stateManager = fsm;
        contentLoader = elm;
        viewpointManager = vm;
        layerManagerFactory = lmf;
        layerRenderer = lrm;

        // Find out how many node managers we have and sort them into the two
        // lists for pre and post event model processing.
        int ext_cnt = 0;
        int int_cnt = 0;
        for(NodeManager nm : extManagers) {
            if(nm.evaluatePreEventModel())
                int_cnt++;

            if(nm.evaluatePostEventModel())
                ext_cnt++;
        }

        preEventManagers = new NodeManager[int_cnt];
        postEventManagers = new NodeManager[ext_cnt];

        int_cnt = 0;
        ext_cnt = 0;
        VRMLClock clk = sensorManager.getVRMLClock();

        for(NodeManager nm : extManagers) {
            if(!nm.initialize()) {
                errorReporter.warningReport(
                    "Manager " + nm.getClass() +
                    " failed to initialise. Ignoring", null);

                // Go through the list of its supported components and disable
                // them in the node factory.
                continue;
            }

            if(nm.evaluatePreEventModel())
                preEventManagers[int_cnt++] = nm;

            if(nm.evaluatePostEventModel())
                postEventManagers[ext_cnt++] = nm;

            stateManager.listenFor(nm.getManagedNodeTypes());
            nm.setErrorReporter(errorReporter);
            nm.setVRMLClock(clk);
        }

        stateManager.listenFor(new int[] { TypeConstants.ViewpointNodeType});

        numPreEventManagers = int_cnt;
        numPostEventManagers = ext_cnt;

        // Always create the default layer first up.
        layerManagers = new LayerManager[1];
        layerManagers[0] = layerManagerFactory.createLayerManager();
        layerManagers[0].setLayerId(0);
        layerManagers[0].initialise(sensorManager);

        numValidLayerManagers = 1;
        numActiveLayerManagers = 0;
    }

    @Override
    public void addNodeObserver(int nodeType, NodeObserver obs) {
        List<NodeObserver> observers = nodeObserverMap.get(nodeType);

        if(observers == null) {
            observers = new ArrayList<>();
            nodeObserverMap.put(nodeType, observers);
        }

        if(observers.isEmpty())
            stateManager.listenFor(new int[] {nodeType});

        if(!observers.contains(obs))
            observers.add(obs);
    }

    @Override
    public void removeNodeObserver(int nodeType, NodeObserver obs) {
        List<NodeObserver> observers = nodeObserverMap.get(nodeType);

        if(observers == null)
            return;

        observers.remove(obs);

        if(observers.isEmpty())
            stateManager.removeListenFor(new int[] {nodeType});

        // Don't bother removing the array list if it is empty. We may need
        // it again at a later time. Doesn't cost us anything in the way of
        // memory anyway.
    }

    @Override
    public void shutdown() {
        shutdownNow = true;

        for(int i = 0; i < numPreEventManagers; i++) {
            preEventManagers[i].clear();
            preEventManagers[i].shutdown();
        }

        for(int i = 0; i < numPostEventManagers; i++) {
            postEventManagers[i].clear();
            postEventManagers[i].shutdown();
        }

        viewpointManager.shutdown();
    }

    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        routeManager.setErrorReporter(errorReporter);
        sensorManager.setErrorReporter(errorReporter);
        viewpointManager.setErrorReporter(errorReporter);
        scriptManager.setErrorReporter(errorReporter);
        stateManager.setErrorReporter(errorReporter);
        contentLoader.setErrorReporter(errorReporter);
        layerManagerFactory.setErrorReporter(errorReporter);

        for(int i = 0; i < numValidLayerManagers; i++)
            layerManagers[i].setErrorReporter(errorReporter);

        for(int i = 0; i < numPreEventManagers; i++)
            preEventManagers[i].setErrorReporter(errorReporter);

        for(int i = 0; i < numPostEventManagers; i++)
            postEventManagers[i].setErrorReporter(errorReporter);
    }

    @Override
    public LayerManager getLayerManager(int id) {
        if(id < numValidLayerManagers)
            return layerManagers[id];
        else
            return null;
    }

    @Override
    public VRMLClock getVRMLClock() {
        return sensorManager.getVRMLClock();
    }

    @Override
    public void addExternalView(ExternalView view) {
        if(!externalViews.contains(view))
            externalViews.add(view);
    }

    @Override
    public void removeExternalView(ExternalView view) {
        externalViews.remove(view);
    }

    @Override
    public void setScene(VRMLScene scene, String useView) {
        sceneNeedsChanging = true;
        sceneToLoad = scene;
        initialViewpoint = useView;
    }

    @Override
    public void changeViewpoint(VRMLViewpointNodeType vp) {
        pendingViewpoint = vp;
    }

    @Override
    public void clear() {
        active = false;

        scriptManager.shutdown();
        routeManager.clear();
		sensorManager.clear();
        stateManager.clear();
        contentLoader.clear();

        externalViews.forEach(view -> {
            view.clear();
        });
		externalSyncNodes.clear();
        scriptToSceneMap.clear();
        sceneToScriptListMap.clear();

        for(int i = 0; i < numValidLayerManagers; i++)
            layerManagers[i].clear();

        for(int i = 0; i < numPreEventManagers; i++)
            preEventManagers[i].clear();

        for(int i = 0; i < numPostEventManagers; i++)
            postEventManagers[i].clear();

        for(int i = 0; i < tmpNodes.length; i++)
                tmpNodes[i] = null;

        pendingViewpoint = null;
        currentScene = null;
        rootSpace = null;

        haveLayerSet = false;
    }

    @Override
    public void evaluate(long time) {
        lastTime = time;

        if(sceneNeedsChanging) {
            if(initListener != null)
                initListener.changeWorld();

            finishSetScene(time);
            sceneNeedsChanging = false;
            return;
        }

        if(!active)
            return;

        // Process the scene tree to inject any added scenes into the
        // route processing. First make sure that the scripts are
        // correctly processed.

        if(!initialWorldLoadComplete)
            evaluateTimeZero(time);
        else {
            if(justStarted) {
                if(initListener != null) {
                    try {
                        initListener.worldInitComplete();
                    } catch(Exception e) {
                        errorReporter.warningReport("Error sending core init", e);
                    }
                }

                for(int i = 0; i < numPreEventManagers; i++)
                    preEventManagers[i].resetTimeZero();

                for(int i = 0; i < numPostEventManagers; i++)
                    postEventManagers[i].resetTimeZero();

                justStarted = false;
            }

            evaluateRunning(time);
        }
    }

    @Override
    public void setInitListener(EventModelInitListener l) {
        initListener = l;
    }

    @Override
    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    @Override
    public RouteManager getRouteManager() {
        return routeManager;
    }

    @Override
    public ContentLoadManager getContentLoader() {
        return contentLoader;
    }

    @Override
    public FrameStateManager getFrameStateManager() {
        return stateManager;
    }

    @Override
    public NodeManager[] getNodeManagers() {
        int size = numPreEventManagers + numPostEventManagers;

        if(size == 0)
            return null;
        else {
            NodeManager[] ret_val = new NodeManager[size];

            int cnt = 0;
            for(int i = 0; i < numPreEventManagers; i++)
                ret_val[cnt++] = preEventManagers[i];

            for(int i = 0; i < numPostEventManagers; i++)
                ret_val[cnt++] = postEventManagers[i];

            return ret_val;
        }
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Finish setting the scene at the right time.
     *
     * @param time The current simulation time.
     */
    private void finishSetScene(double time) {
        if(active)
            clear();

        currentScene = sceneToLoad;
        sceneToLoad = null;

        // Are we clearing the scene?
        if(currentScene == null) {
            active = false;
            return;
        }

        rootSpace = (VRMLExecutionSpace)currentScene.getRootNode();
        ((VRMLNodeType)rootSpace).setFrameStateManager(stateManager);
        SceneMetaData md = currentScene.getMetaData();

        layerManagers[0].clear();

        if(md.isVrmlSpec()) {
            loadVRMLScripts(rootSpace, currentScene, currentScene, true);
            stateManager.removeListenFor(LAYER_NODE_TYPES);
            haveLayerSet = false;
        } else {
            if(currentScene.getFirstLayerSet() != null) {
                haveLayerSet = true;
                stateManager.listenFor(LAYER_NODE_TYPES);
            } else {
                haveLayerSet = false;
                stateManager.removeListenFor(LAYER_NODE_TYPES);
            }

            loadX3DScene(rootSpace, currentScene, true);
        }

        routeManager.updateSpaces();
        routeManager.updateRoutes();

        active = true;

        justStarted = (scriptToSceneMap.isEmpty());
        initialWorldLoadComplete = justStarted;
    }

    /**
     * Evaluate the "event model" for pre-time zero load conditions. This is
     * the world loaded, but not all the scripts have arrived yet. Only applies
     * to VRML97 worlds where we are required to not start before all scripts
     * have been loaded.
     *
     * @param time The time when everything happens
     */
    private void evaluateTimeZero(long time) {
        processScripts();

        if(shutdownNow)
            return;

        processViewpoints(time);

        // Now, have we just reached the point where all top level nodes have
        // loaded and the scripts initialized? If so, run one rev of the
        // event model to propagate the routes around. Unlike the normal
        // event model, we only call the route manager once. This ensures
        // that the events at least make it to the destination node, but
        // we don't want to have the events get processed by the
        // script.processEvent call. This is mainly to make sure things like
        // scripts setting the first bound viewpoint and setup of time sensors
        // actually make sense.
        if(!scriptToSceneMap.isEmpty())
            return;

        scriptManager.initializeScripts(time);

        if(shutdownNow)
            return;

        scriptManager.processEvents();

        if(shutdownNow)
            return;

        // Do route processing, but convert to VRML time in the process
        routeManager.processRoutes(time * 0.001);

        if(shutdownNow)
            return;

        routeManager.updateSpaces();

        if(shutdownNow)
            return;

        NodeArray urlNodes = stateManager.getAddedUrlNodes();

        if(shutdownNow)
            return;

        contentLoader.queueNodesLoad(urlNodes);

        if(shutdownNow)
            return;

        processBindables();

        if(shutdownNow)
            return;

        routeManager.updateRoutes();

        if(shutdownNow)
            return;

        sensorManager.addSensors(stateManager.getAddedSensors());

        if(shutdownNow)
            return;

        sensorManager.addViewDependentNodes(
            stateManager.getAddedViewDependents());

        if(shutdownNow)
            return;

        scriptManager.addScripts(stateManager.getAddedScripts());

        if(shutdownNow)
            return;

        stateManager.clearRemovedNodes();
        stateManager.clearAddedNodes();

        if(shutdownNow)
            return;

        stateManager.frameFinished();

        justStarted = true;
        initialWorldLoadComplete = true;
    }

    /**
     * Evaluate the normal running event model. At least the root of the world
     * is running, inlines and externprotos may not have resolved yet, but at
     * least the main world is.
     *
     * @param time The time when everything happens
     */
    private void evaluateRunning(long time) {

        if(pendingViewpoint != null) {
            pendingViewpoint.setBind(true, true, time);
            pendingViewpoint = null;
        }

        VRMLLayerSetNodeType layer_set = currentScene.getFirstLayerSet();

        // If we have a valid layer set, then find the activeLayer nagivation layer.
        // If not, then set the activeLayer layer to be layer 0. Do this every frame
        // as the cost is low and we may transition from a time of having a
        // layer set to not having one, thus requiring a reset back to the
        // default.
        if(layer_set != null) {
            int activeLayer = layer_set.getActiveNavigationLayer();

            if(activeLayer != activeNavigationLayer) {
                layerManagers[activeNavigationLayer].setActiveNavigationLayer(false);
                layerManagers[activeLayer].setActiveNavigationLayer(true);
                activeNavigationLayer = activeLayer;
            }

            if(!haveLayerSet) {
                haveLayerSet = true;
                stateManager.listenFor(LAYER_NODE_TYPES);
            }
        } else if(activeNavigationLayer != 0) {
            layerManagers[activeNavigationLayer].setActiveNavigationLayer(false);
            layerManagers[0].setActiveNavigationLayer(true);
            activeNavigationLayer = 0;

            haveLayerSet = false;
            stateManager.removeListenFor(LAYER_NODE_TYPES);
        }

        processViewpoints(time);
        externalSyncNodes.remove(stateManager.getRemovedExtSynchronizedNodes());

        if(shutdownNow)
            return;

        externalSyncNodes.add(stateManager.getAddedExtSynchronizedNodes());

        if(shutdownNow)
            return;

        processSynchronisedNodes(true);

        if(shutdownNow)
            return;

        // The step numbers here are taken from the X3D Part 2 specification.
        // ISO/IEC 17775-2 4.8.2 Event model evaluation order
        // 1. Update camera based on currently bound Viewpoint's position
        //    and orientation.
        // 2. Evaluate sensor inputs
        sensorManager.processUserInput(activeNavigationLayer, time);

        if(shutdownNow)
            return;

        // Treat these a sensor nodes for event model
        processPreEventManagers(time);
        processNodeObservers();

        if(shutdownNow)
            return;

        for (ExternalView view : externalViews) {
            if(shutdownNow)
                return;

            view.processEvents();
        }

        if(shutdownNow)
            return;

        // 4. Call the prepareEvents script service for all live script
        //    nodes in the scene.
        double vrml_time = time * 0.001;
        scriptManager.prepareEvents(vrml_time);

        if(shutdownNow)
            return;

        // 5. Evaluate routes.
        routeManager.processRoutes(vrml_time);

        if(shutdownNow)
            return;

        scriptManager.processEvents();

        if(shutdownNow)
            return;

        loadScenes(stateManager.getAddedScenes());

        if(shutdownNow)
            return;

        stateManager.clearAddedScenes();

        if(shutdownNow)
            return;

        NodeArray urlNodes = stateManager.getAddedUrlNodes();

        if(shutdownNow)
            return;

        contentLoader.queueNodesLoad(urlNodes);

        if(shutdownNow)
            return;

        // What about externprotos?
        NodeTemplateArray externs = stateManager.getAddedExternProtos();

        if(shutdownNow)
            return;

        if(externs.size() != 0) {
            int size = externs.size();
            for(int i = 0; i < size; i++) {
                VRMLExternProtoDeclare proto =
                    (VRMLExternProtoDeclare)externs.get(i);

                if(shutdownNow)
                    return;

                contentLoader.queueExternProtoLoad(proto);
            }
        }

        do {
            // 6. Call the shutdown service on scripts that have received
            //    set_url events or are being removed from the scene
            scriptManager.shutdownActiveScripts();

            if(shutdownNow)
                return;

            scriptManager.removeScripts(stateManager.getRemovedScripts());

            if(shutdownNow)
                return;

            ObjectArray rscenes = stateManager.getRemovedScenes();

            int size = rscenes.size();
            VRMLExecutionSpace space;

            for(int i=0; i < size; i++) {
                space = (VRMLExecutionSpace) rscenes.get(i);
                removeScene(space);
            }

            if(shutdownNow)
                return;

            // 7. Generate final events for any sensors removed from the scene.
            sensorManager.removeSensors(stateManager.getRemovedSensors());

            if(shutdownNow)
                return;

            sensorManager.removeViewDependentNodes(
                stateManager.getRemovedViewDependents());

            if(shutdownNow)
                return;

            routeManager.updateSpaces();

            if(shutdownNow)
                return;

            // process bindable nodes that have been added or removed
            processBindables();

            if(shutdownNow)
                return;

            // 8. Add/remove any routes required by an invocation of the
            //    dynamicRouteHandling service request as defined in 6.4.16,
            //    dynamicRouteHandling from any script execution in Step5 & 6.
            routeManager.updateRoutes();

            if(shutdownNow)
                return;

            sensorManager.addSensors(stateManager.getAddedSensors());

            if(shutdownNow)
                return;

            sensorManager.addViewDependentNodes(
                stateManager.getAddedViewDependents());

            if(shutdownNow)
                return;

            // 9. Call the eventsProcessed script service for scripts that
            //    have sent events generated in Step 6.
            scriptManager.eventsProcessed();

            if(shutdownNow)
                return;

            // 10. Call the initialize service for newly loaded internal
            //     interaction code.
            processScripts();

            if(shutdownNow)
                return;

            scriptManager.addScripts(stateManager.getAddedScripts());

            if(shutdownNow)
                return;

            scriptManager.initializeScripts(time);

            if(shutdownNow)
                return;

            stateManager.clearRemovedNodes();
            stateManager.clearAddedNodes();

            if(shutdownNow)
                return;

            // 11. If any events were generated from steps 5 through 10,
            //     go to step 5 and continue.
        } while(routeManager.processRoutes(vrml_time));


        if(shutdownNow)
            return;

        processPostEventManagers(time);

        if(shutdownNow)
            return;

        // Finally set up the rendered order of layers on screen.
        if(layer_set != null) {
            int num_order = layer_set.getNumRenderedLayers();

            //////////////////////////////////////////////
            // save layer 0 manager, may need later
            LayerManager lm0 = layerManagers[0];
            //////////////////////////////////////////////
            if(renderOrder.length < num_order) {
                renderOrder = new int[num_order];
                layerManagers = new LayerManager[num_order];
            }

            // Look for removed layers:

            if(layer_set.hasLayerListChanged()) {
                // Get the removed and added layers this frame
                NodeArray l =
                    stateManager.getRemovedNodes(TypeConstants.LayerNodeType);

                int size = l.size();
                for(int i = 0; i < size; i++) {
                    VRMLLayerNodeType n = (VRMLLayerNodeType)l.get(i);

                    LayerManager r =
                        layerToRendererMap.remove(n);

                    r.clear();
                    r.shutdown();
                }

                numValidLayerManagers -= size;

                l = stateManager.getAddedNodes(TypeConstants.LayerNodeType);

                size = l.size();
                for(int i = 0; i < size; i++) {
                    VRMLLayerNodeType n = (VRMLLayerNodeType)l.get(i);

                    LayerManager r = layerManagerFactory.createLayerManager();
                    r.setLayerId(n.getLayerId());
                    r.initialise(sensorManager);
                    r.setManagedLayer(n);
                    /////////////////////////////////////////////////////////
                    // calling initialBind() before setActiveLayers()
                    // has been called results in a NPE
                    //r.initialBind();
                    /////////////////////////////////////////////////////////

                    layerToRendererMap.put(n, r);
                }

                numValidLayerManagers += size;

                VRMLNodeType[] layers = layer_set.getLayers();

                /////////////////////////////////////////////////////////
                // restore layer 0 manager
                layerManagers[0] = lm0;
                ////////////////////////////////////////////////////////
                for(int i = 0; i < layers.length; i++) {
                    layerManagers[i + 1] =
                        layerToRendererMap.get(layers[i]);
                }

                layerRenderer.setActiveLayers(layerManagers,
                                              numValidLayerManagers);
            }

            if(layer_set.hasRenderOrderChanged()) {
                layer_set.getRenderOrder(renderOrder);
                layerRenderer.setRenderOrder(renderOrder, num_order);
                sensorManager.setRenderOrder(renderOrder, num_order);
            }
        }

        stateManager.frameFinished();

        if(shutdownNow)
            return;

        // Now apply the view matrix
        for(int i = 0; i < numActiveLayerManagers; i++)
            layerManagers[i].updateViewMatrix();

        if(shutdownNow)
            return;

        processSynchronisedNodes(false);
    }

    /**
     * Convenience method to process the list of added and removed viewpoints
     * in this cycle.
     *
     * @param time The simulation time
     */
    private void processViewpoints(long time) {
        viewpointManager.updateViewpoint(time);
    }

    /**
     * Convenience method to process the list of added and removed bindable
     * nodes in this cycle.
     */
    private void processBindables() {
        NodeArray bindables = stateManager.getRemovedBindables();

        int i;
        int size = bindables.size();
        VRMLNodeType node;
        BindableNodeManager mgr;

// LAYERS:
// This is not really correct because we only want to remove the bindable
// from the layer it was removed from (if it was added to multiples, such as
// in a script. Perhaps we can just assume that if someone does something
// dodgy like that, we can just ignore it and remove it completely.
        for(i = 0; i < size; i++) {
            node = (VRMLNodeType)bindables.get(i);

            int[] layers = node.getRemovedLayerIds();

            if (layers == null)
                continue;

            for(int j = 0; j < layers.length; j++) {

                int primary_type = node.getPrimaryType();
                mgr = layerManagers[layers[j]].getBindableManager(primary_type);

                if(mgr == null) {
                    // Should never happen. This is bad!
                    errorReporter.warningReport("attempting to remove a " +
                                                "bindable node that has no " +
                                                "manager",
                                                null);
                    continue;
                }

                mgr.removeNode((VRMLBindableNodeType)node);

                if(primary_type == TypeConstants.NavigationInfoNodeType)
                    continue;

                int[] types = node.getSecondaryType();

                for(int k=0; k < types.length; k++) {
                    if (types[k] != TypeConstants.NavigationInfoNodeType)
                        continue;

                    mgr =
                        layerManagers[layers[j]].getBindableManager(types[k]);

                    if(mgr == null) {
                        continue;
                    }

                    mgr.removeNode((VRMLBindableNodeType)node);
                }
            }
        }

        if(shutdownNow)
            return;

        bindables = stateManager.getAddedBindables();
        size = bindables.size();

        for(i = 0; i < size; i++) {
            node = (VRMLNodeType)bindables.get(i);

            int[] layers = node.getLayerIds();

            if(layers == null)
                continue;

            for(int j = 0; j < layers.length; j++) {
                int primary_type = node.getPrimaryType();
                mgr = layerManagers[layers[j]].getBindableManager(primary_type);

                if(mgr == null) {
                    // Should never happen. This is bad!
                    errorReporter.warningReport("attempting to remove a " +
                                                "bindable node that has no " +
                                                "manager",
                                                null);
                    continue;
                }

                if(!mgr.contains((VRMLBindableNodeType)node))
                    mgr.addNode((VRMLBindableNodeType)node, false);

                if(primary_type == TypeConstants.NavigationInfoNodeType)
                    continue;

                int[] types = node.getSecondaryType();

                for(int k=0; k < types.length; k++) {
                    if (types[k] != TypeConstants.NavigationInfoNodeType)
                        continue;

                    mgr =
                        layerManagers[layers[j]].getBindableManager(types[k]);

                    if(mgr == null) {
                        continue;
                    }

                    if(!mgr.contains((VRMLBindableNodeType)node)) {
                        mgr.addNode((VRMLBindableNodeType)node, false);
                    }
                }

            }
        }
    }

    /**
     * Process the node observers and everything they need from
     * the scene graph.
     */
    private void processNodeObservers() {

        int[] node_ids = nodeObserverMap.keySet();
        List<NodeObserver> observers;

        for(int i = 0; i < node_ids.length && !shutdownNow; i++) {
            observers = nodeObserverMap.get(node_ids[i]);

            if(observers.isEmpty())
                continue;

            NodeArray nodes = stateManager.getRemovedNodes(node_ids[i]);
            int num_nodes = nodes.size();
            if(num_nodes > 0) {
                if(num_nodes > tmpNodes.length)
                    tmpNodes = new VRMLNodeType[num_nodes];

                nodes.toArray(tmpNodes);


                for(int j = 0; j < observers.size() && !shutdownNow; j++) {
                    NodeObserver obs = observers.get(j);

                    obs.nodesRemoved(node_ids[i], tmpNodes, num_nodes);
                }
            }

            nodes = stateManager.getAddedNodes(node_ids[i]);
            num_nodes = nodes.size();

            if(num_nodes == 0)
                continue;

            if(num_nodes > tmpNodes.length)
                tmpNodes = new VRMLNodeType[num_nodes];

            nodes.toArray(tmpNodes);

            for(int j = 0; j < observers.size() && !shutdownNow; j++) {
                NodeObserver obs = observers.get(j);

                obs.nodesAdded(node_ids[i], tmpNodes, num_nodes);
            }
        }
    }

    /**
     * Convenience method to process the pre-event model nodes.
     *
     * @param time The timestamp of "now"
     */
    private void processPreEventManagers(long time) {
        for(int i = 0; i < numPreEventManagers && !shutdownNow; i++) {
            int[] types = preEventManagers[i].getManagedNodeTypes();

            if(shutdownNow)
                return;

            for(int j = 0; j < types.length; j++) {
                NodeArray nodes = stateManager.getRemovedNodes(types[j]);

                int size = nodes.size();
                for(int k = 0; k < size; k++) {
                    VRMLNodeType n = (VRMLNodeType)nodes.get(k);
                    preEventManagers[i].removeManagedNode(n);
                }

                if(shutdownNow)
                    return;

                nodes = stateManager.getAddedNodes(types[j]);
                size = nodes.size();

                for(int k = 0; k < size; k++) {
                    VRMLNodeType n = (VRMLNodeType)nodes.get(k);
                    preEventManagers[i].addManagedNode(n);
                }
            }

            if(shutdownNow)
                return;

            preEventManagers[i].executePreEventModel(time);
        }
    }

    /**
     * Convenience method to process the post-event model nodes.
     *
     * @param time The timesstamp of "now"
     */
    private void processPostEventManagers(long time) {

        for(int i = 0; i < numPostEventManagers && !shutdownNow; i++) {
            int[] types = postEventManagers[i].getManagedNodeTypes();

            if(shutdownNow)
                return;

            for(int j = 0; j < types.length; j++) {
                NodeArray nodes = stateManager.getRemovedNodes(types[j]);

                int size = nodes.size();
                for(int k = 0; k < size; k++) {
                    VRMLNodeType n = (VRMLNodeType)nodes.get(k);
                    postEventManagers[i].removeManagedNode(n);
                }

                if(shutdownNow)
                    return;

                nodes = stateManager.getAddedNodes(types[j]);
                size = nodes.size();

                for(int k = 0; k < size; k++) {
                    VRMLNodeType n = (VRMLNodeType)nodes.get(k);
                    postEventManagers[i].addManagedNode(n);
                }

                if(shutdownNow)
                    return;

            }

            if(shutdownNow)
                return;

            postEventManagers[i].executePostEventModel(time);
        }
    }

    /**
     * Process the added scripts from the scene graph manager. This performs
     * a lookup to see whether the scene should be permitted to run. It only
     * updates the counts on the tree nodes. A separate traversal of the tree
     * node graph is needed.
     */
    private void processScripts() {

        // Some optimisations to avoid doing any processing if there is
        // no need.
        if(scriptToSceneMap.isEmpty())
            return;

        scriptManager.getProcessedScripts(tmpArray);

        if(shutdownNow)
            return;

        int size = tmpArray.size();
        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType script = (VRMLScriptNodeType)tmpArray.get(i);
            BasicScene scene = scriptToSceneMap.get(script);

            if(shutdownNow)
                return;

            // if the scene is null, then just ignore it. This is likely to
            // happen any time we have no VRML97 scripts to work with.
            if(scene == null)
                continue;

            scriptToSceneMap.remove(script);

            HashSet<VRMLNode> scripts_left = sceneToScriptListMap.get(scene);

            // Sanity check. This should never fail....
            if(!scripts_left.contains(script)) {
                System.out.println("processScripts.invalid mapping happened!");
               continue;
            }

            if(shutdownNow)
                return;

            // Have we loaded enough yet to start moving on the loading
            // process for the rest of the scene? If yes, queue up the rest
            // of the scene, otherwise just remove this script from the list
            // of those left to work with.
            if(scripts_left.size() == 1) {
                completeVRMLSceneLoad(scene);
                sceneToScriptListMap.remove(scene);
            } else {
                scripts_left.remove(script);
            }
        }

        tmpArray.clear();
    }

    // Below here are all the setup methods used to create and register a new
    // scene in this manager.

    /**
     * Load the list of scenes into this event model.
     *
     * @param scenes The listing of the scenes that need to be loaded
     */
    private void loadScenes(ObjectArray scenes) {
        int size = scenes.size();

        for(int i = 0; i < size; i++) {

            VRMLExecutionSpace space = (VRMLExecutionSpace)scenes.get(i);
            BasicScene sc = space.getContainedScene();

            if(sc instanceof VRMLScene) {
                VRMLScene v_sc = (VRMLScene)sc;
                SceneMetaData md = v_sc.getMetaData();

                if(md.isVrmlSpec())
                    loadVRMLScripts(space, v_sc, v_sc, false);
                else
                    loadX3DScene(space, v_sc, false);
            } else {
                // This must be a proto. Doesn't matter how this happened
                // because the only way for this to happen is through
                // createVrmlFromUrl, which means we don't really care
                // about correct load order and that scripts get loaded
                // before everything else. Just treat it all like an
                // X3D scene.
                loadX3DScene(space, sc, false);
            }

            if(shutdownNow)
                return;
        }
    }

    /**
     * Complete the scene loading after all the scripts have loaded. This is
     * only used for VRML97 scenes where it is required that the world not
     * start processing events until all the scripts have been loaded.
     *
     * @param scene The scene that needs its load completed
     */
    private void completeVRMLSceneLoad(BasicScene scene) {
        // bindables first. They need to be in the right manager so that
        // we can operate correctly. Can't bind nodes until we have a
        // completed scene.
        List nodes =
            scene.getBySecondaryType(TypeConstants.BindableNodeType);
        int size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);
            BindableNodeManager mgr =
                layerManagers[0].getBindableManager(node.getPrimaryType());
            mgr.addNode((VRMLBindableNodeType)node, false);

            int[] types = node.getSecondaryType();

            for(int k=0; k < types.length; k++) {
                if (types[k] != TypeConstants.NavigationInfoNodeType)
                    continue;

                mgr =
                    layerManagers[0].getBindableManager(types[k]);

                if(mgr == null) {
                    continue;
                }

                if(!mgr.contains((VRMLBindableNodeType)node)) {
                    mgr.addNode((VRMLBindableNodeType)node, false);
                }
            }
        }

        if(shutdownNow)
            return;

        contentLoader.queueSceneLoad(scene);

        if(shutdownNow)
            return;

        // Walk the child proto instances as scenes and queue up anything
        // that's in them.
        nodes = scene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLProtoInstance proto = (VRMLProtoInstance)nodes.get(i);
            BasicScene p_scene = proto.getContainedScene();

            if(shutdownNow)
                return;

            completeVRMLSceneLoad(p_scene);
        }

        if(shutdownNow)
            return;

        if(currentScene == scene)
            initialBind();
    }

    /**
     * Load the scripts scene for the given parent VRML97 scene. Recurses
     * the protos of this scene to also pick up any scripts there. Should only
     * be called on scene that are VRML97 scenes. No checking is done.
     *
     * @param space the VRML execution space for this scene
     * @param worldScene The scene that represents the root of this world
     * @param localScene The scene that needs its load completed
     * @param isRoot True if this is the root scene and we should process the
     *   layers to remove any existing items
     */
    private void loadVRMLScripts(VRMLExecutionSpace space,
                                 VRMLScene worldScene,
                                 BasicScene localScene,
                                 boolean isRoot) {

        if(isRoot) {
            VRMLWorldRootNodeType root_node =
                (VRMLWorldRootNodeType)worldScene.getRootNode();

            root_node.setRootWorld();

            renderOrder[0] = 0;
            layerManagers[0].setSpecVersion(2, 0);
            layerManagers[0].setManagedNodes(root_node);
            layerManagers[0].setActiveNavigationLayer(true);


            activeNavigationLayer = 0;
            numValidLayerManagers = 1;
            numActiveLayerManagers = 1;
            haveLayerSet = false;

            layerRenderer.setActiveLayers(layerManagers, 1);
            layerRenderer.setRenderOrder(renderOrder, 1);
            sensorManager.setRenderOrder(renderOrder, 1);

            for(int i = 1; i < numValidLayerManagers; i++)
                layerManagers[i].clear();
        }

        List<VRMLNode> nodes =
            localScene.getByPrimaryType(TypeConstants.ScriptNodeType);

        int size = nodes.size();

        if(size != 0) {
            checkTmpArray(size);
            nodes.toArray((VRMLNode[])tmpNodes);
            tmpArray.add(tmpNodes, 0, size);
            scriptManager.addScripts(tmpArray);
            tmpArray.clear();

            // Add them to the mappings
            HashSet<VRMLNode> script_set = sceneToScriptListMap.get(worldScene);

            if(script_set == null) {
                script_set = new HashSet<>(size);
                sceneToScriptListMap.put(worldScene, script_set);
            }

            for(int i = 0; i < size; i++) {
                script_set.add(tmpNodes[i]);
                scriptToSceneMap.put(tmpNodes[i], worldScene);
            }
        } else {
            // No scripts to load, so complete
            completeVRMLSceneLoad(localScene);
        }

        if(shutdownNow)
            return;

        // Add the routes for this space.
        routeManager.addSpace(space);

        if(shutdownNow)
            return;

        sensorManager.loadScene(localScene);

        if(shutdownNow)
            return;

        // recurse the proto instances for their scripts to load.
        nodes = localScene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLProtoInstance proto = (VRMLProtoInstance)nodes.get(i);
            if(proto.getImplementationNode() == null)
                continue;

            if(shutdownNow)
                return;

            BasicScene p_scene = proto.getContainedScene();
            loadVRMLScripts(proto, worldScene, p_scene, false);
        }
    }

    /**
     * Complete the scene loading after all the scripts have loaded.
     *
     * @param space The space the scene belongs to
     * @param scene The scene that needs its load completed
     * @param isRoot True if this is the root scene and we should process the
     *   incoming layers
     */
    private void loadX3DScene(VRMLExecutionSpace space,
                              BasicScene scene,
                              boolean isRoot) {

        // If this is the root scene, setup layers.
        if(isRoot) {
            int major = scene.getSpecificationMajorVersion();
            int minor = scene.getSpecificationMinorVersion();

            VRMLScene main_scene = (VRMLScene)scene;
            VRMLWorldRootNodeType root_node =
                (VRMLWorldRootNodeType)main_scene.getRootNode();
            root_node.setRootWorld();

            layerManagers[0].setSpecVersion(major, minor);
            layerManagers[0].setManagedNodes(root_node);

            VRMLLayerSetNodeType layer_set = main_scene.getFirstLayerSet();

            for(int i = 1; i < numValidLayerManagers; i++)
                layerManagers[i].clear();

            int num_order;

            if(layer_set != null) {
                num_order = layer_set.getNumRenderedLayers();

                if(renderOrder.length < num_order)
                    renderOrder = new int[num_order];

                VRMLNodeType[] layers = layer_set.getLayers();
                int size = (layers == null) ? 0 : layers.length;

                if(layerManagers.length < size + 1) {
                    LayerManager[] tmp = new LayerManager[size + 1];
                    System.arraycopy(layerManagers,
                                     0,
                                     tmp,
                                     0,
                                     numValidLayerManagers);

                    layerManagers = tmp;
                }

                for(int i = 0; i < size; i++) {
                    VRMLLayerNodeType n = (VRMLLayerNodeType)layers[i];

                    LayerManager r = layerManagers[i + 1];

                    if(r == null) {
                        r = layerManagerFactory.createLayerManager();
                        layerManagers[i + 1] = r;

                        r.setLayerId(n.getLayerId());
                        r.initialise(sensorManager);
                    } else {
                        r.setLayerId(n.getLayerId());
                    }

                    r.setSpecVersion(major, minor);
                    r.setManagedLayer(n);

                    layerToRendererMap.put(n, r);
                }

                layer_set.getRenderOrder(renderOrder);

                int activeLayer = layer_set.getActiveNavigationLayer();

                layerManagers[activeLayer].setActiveNavigationLayer(true);
                activeNavigationLayer = activeLayer;

                numValidLayerManagers = size + 1;
                numActiveLayerManagers = size + 1;
            } else {
                layerManagers[0].setActiveNavigationLayer(true);
                activeNavigationLayer = 0;
                numValidLayerManagers = 1;
                numActiveLayerManagers = 1;

                renderOrder[0] = 0;
                num_order = 1;
            }

            layerRenderer.setActiveLayers(layerManagers,
                                          numActiveLayerManagers);
            layerRenderer.setRenderOrder(renderOrder, num_order);
            sensorManager.setRenderOrder(renderOrder, num_order);
        }

        // bindables first. They need to be in the right manager so that
        // we can operate correctly. Can't bind nodes until we have a
        // completed scene.
        List<VRMLNode> nodes = scene.getByPrimaryType(TypeConstants.ScriptNodeType);

        // scripts
        int size = nodes.size();

        if(size != 0) {
            checkTmpArray(size);
            nodes.toArray((VRMLNode[])tmpNodes);
            tmpArray.add(tmpNodes, 0, size);
            scriptManager.addScripts(tmpArray);
            tmpArray.clear();
        }

        if(shutdownNow)
            return;

        nodes = scene.getBySecondaryType(TypeConstants.BindableNodeType);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            int[] layers = node.getLayerIds();

            if (layers == null) {
                // Layers: What should get do if it gets added to a layer at a layer date
            } else {
                for(int j = 0; j < layers.length; j++) {

                    BindableNodeManager mgr =
                        layerManagers[layers[j]].getBindableManager(node.getPrimaryType());

                    if(mgr == null) {
                        // Should never happen. This is bad!
                        errorReporter.warningReport(NO_BINDABLE_MGR_MSG, null);
                        continue;
                    }

                    if(!mgr.contains((VRMLBindableNodeType)node))
                        mgr.addNode((VRMLBindableNodeType)node, false);

                    int[] types = node.getSecondaryType();

                    for(int k=0; k < types.length; k++) {
                        if (types[k] != TypeConstants.NavigationInfoNodeType)
                            continue;

                        mgr =
                            layerManagers[layers[j]].getBindableManager(types[k]);

                        if(mgr == null) {
                            continue;
                        }

                        if(!mgr.contains((VRMLBindableNodeType)node)) {
                            mgr.addNode((VRMLBindableNodeType)node, false);
                        }
                    }
                }
            }
        }

        if(shutdownNow)
            return;

        nodes = scene.getBySecondaryType(TypeConstants.ExternalSynchronizedNodeType);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);
            externalSyncNodes.add(node);
        }

        // Find all of the  nodes and register them with the appropriate
        // manager
        for(int i = 0; i < numPreEventManagers; i++) {
            int[] types = preEventManagers[i].getManagedNodeTypes();

            if(shutdownNow)
                return;

            for(int j = 0; j < types.length; j++) {
                List<VRMLNode> node_list = scene.getByPrimaryType(types[j]);

                if(shutdownNow)
                    return;

                size = node_list.size();
                for(int k = 0; k < size; k++) {
                    VRMLNodeType node = (VRMLNodeType)node_list.get(k);

                    if(shutdownNow)
                        return;

                    if(node.getPrimaryType() == TypeConstants.ProtoInstance) {
                        VRMLProtoInstance proto = (VRMLProtoInstance)node;
                        node = proto.getImplementationNode();

                        while((node != null) && (node instanceof VRMLProtoInstance))
                            node = ((VRMLProtoInstance)node).getImplementationNode();
                    }

                    if(shutdownNow)
                        return;

                    if(node != null)
                        preEventManagers[i].addManagedNode(node);
                }
            }
        }

        for(int i = 0; i < numPostEventManagers; i++) {
            int[] types = postEventManagers[i].getManagedNodeTypes();

            if(shutdownNow)
                return;

            for(int j = 0; j < types.length; j++) {
                List<VRMLNode> node_list = scene.getByPrimaryType(types[j]);

                if(shutdownNow)
                    return;

                size = node_list.size();
                for(int k = 0; k < size; k++) {
                    VRMLNodeType node = (VRMLNodeType)node_list.get(k);

                    if(shutdownNow)
                        return;

                    if(node.getPrimaryType() == TypeConstants.ProtoInstance) {
                        VRMLProtoInstance proto = (VRMLProtoInstance)node;
                        node = proto.getImplementationNode();

                        while((node != null) && (node instanceof VRMLProtoInstance))
                            node = ((VRMLProtoInstance)node).getImplementationNode();
                    }

                    if(shutdownNow)
                        return;

                    if(node != null)
                        postEventManagers[i].addManagedNode(node);
                }
            }
        }

/*
        // Disabled till we revisit GeoVRML

        // Find all of the GeoElevationGrid nodes and register them with the
        // terrain manager.
        List geom_list =
            currentScene.getByPrimaryType(TypeConstants.ComponentGeometryNodeType);

        size = geom_list.size();

        for(int i=0; i < size; i++) {
            node = (VRMLNodeType)geom_list.get(i);

            if(node.getPrimaryType() == TypeConstants.ProtoInstance)
                node = (VRMLNodeType)((VRMLProtoInstance)node).getImplementationNode();

            if(node instanceof VRMLTerrainSource) {
                terrainManager.addSector((VRMLTerrainSource)node);
            }
        }
*/


        // Queue up all the bits and pieces to load
        contentLoader.queueSceneLoad(scene);

        if(shutdownNow)
            return;

        routeManager.addSpace(space);

        if(shutdownNow)
            return;

        sensorManager.loadScene(scene);

        if(shutdownNow)
            return;

        // Walk the child proto instances as scenes and queue up anything
        // that's in them.
        nodes = scene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLProtoInstance proto = (VRMLProtoInstance)nodes.get(i);
            if(proto.getImplementationNode() == null)
                continue;

            BasicScene p_scene = proto.getContainedScene();

            if(shutdownNow)
                return;

            loadX3DScene(proto, p_scene, false);
        }

        if(shutdownNow)
            return;

        if(currentScene == scene)
            initialBind();
    }

    /**
     * Convenience method to go check the working array for being long enough.
     *
     * @param size The minimum number of items required
     */
    private void checkTmpArray(int size) {
        if(tmpNodes.length < size) {
            int new_size = tmpNodes.length + NUM_NODES_INC;

            while(new_size < size)
                new_size += NUM_NODES_INC;

            tmpNodes = new VRMLNodeType[new_size];
        }
    }

    /**
     * Perform the initial binding of the bindables.
     */
    private void initialBind() {
/*
  LAYERS:
    What happens with a foo.x3d#viewpoint url? We need to find which layer
    this viewpoint is in and then bind appropriately for it. Other layers
    still should be using their default viewpoint.

        // Do we need to go dig around the DEF maps for the viewpoints?
        if(initialViewpoint != null) {
            Map defs = currentScene.getDEFNodes();
            node = (VRMLNodeType)defs.get(initialViewpoint);

            if(node instanceof VRMLViewpointNodeType)
                vp = (VRMLViewpointNodeType)node;
            else if(node instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)node;
                VRMLNodeType impl = proto.getImplementationNode();

                if(impl instanceof VRMLViewpointNodeType)
                    vp = (VRMLViewpointNodeType)impl;
                else {
                    errorReporter.warningReport("Proto instance referenced by " +
                                                initialViewpoint + " is not a " +
                                                "viewpoint node type. Using " +
                                                "normal default behaviour",
                                                null);

//                    vp = (VRMLViewpointNodeType)viewpointStack.getFirstNode();
                }
            } else {
                errorReporter.warningReport("DEF name " + initialViewpoint +
                                            " does not describe a viewpoint. " +
                                            "Using the normal default behaviour",
                                            null);

                vp = (VRMLViewpointNodeType)viewpointStack.getFirstNode();
            }
        }
*/
        for(int i = 0; i < numActiveLayerManagers; i++)
            layerManagers[i].initialBind();
    }

    /**
     * Process the list of external synchronised nodes for either the start
     * or end of the frame.
     *
     * @param begin true if this is the beginning of the frame
     */
    private void processSynchronisedNodes(boolean begin) {
        int num_externals = externalSyncNodes.size();

        if(num_externals == 0)
            return;

        if(begin) {
            for(int i = 0; i < num_externals; i++) {
                try {
                    VRMLExternalSynchronizedNodeType n =
                        (VRMLExternalSynchronizedNodeType)externalSyncNodes.get(i);
                    n.preEventEvaluation();
                } catch(Exception e) {
                    errorReporter.errorReport(SYNCH_START_MSG, e);
                }
            }
        } else {
            for(int i = 0; i < num_externals; i++) {
                try {
                    VRMLExternalSynchronizedNodeType n =
                        (VRMLExternalSynchronizedNodeType)externalSyncNodes.get(i);
                    n.postEventEvaluation();
                } catch(Exception e) {
                    errorReporter.errorReport(SYNCH_STOP_MSG, e);
                }
            }
        }
    }

    /**
     * Remove scenes.  Cleanup all resource used.
     *
     * @param space The execution space containing the scene to remove
     */
    private void removeScene(VRMLExecutionSpace space) {
        BasicScene scene;
        scene = space.getContainedScene();

        // bindables first. They need to be in the right manager so that
        // we can operate correctly. Can't bind nodes until we have a
        // completed scene.
        List<VRMLNode> nodes = scene.getByPrimaryType(TypeConstants.ScriptNodeType);

        // scripts
        int size = nodes.size();

        if(size != 0) {
            checkTmpArray(size);
            nodes.toArray((VRMLNode[])tmpNodes);
            tmpArray.add(tmpNodes, 0, size);
            scriptManager.removeScripts(tmpArray);
            tmpArray.clear();
        }

        nodes = scene.getBySecondaryType(TypeConstants.BindableNodeType);
        size = nodes.size();

        int layer_id = scene.getLayerId();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);

            BindableNodeManager mgr =
                layerManagers[layer_id].getBindableManager(node.getPrimaryType());

            if(mgr == null) {
                // Should never happen. This is bad!
                errorReporter.warningReport("attempting to remove a " +
                                            "bindable node that has no " +
                                            "manager",
                                            null);
                continue;
            }

            mgr.removeNode((VRMLBindableNodeType)node);

            int[] types = node.getSecondaryType();

            for(int k=0; k < types.length; k++) {
                if (types[k] != TypeConstants.NavigationInfoNodeType)
                    continue;

                mgr =
                    layerManagers[layer_id].getBindableManager(types[k]);

                if(mgr == null) {
                    continue;
                }

                mgr.removeNode((VRMLBindableNodeType)node);
            }
        }

        nodes = scene.getBySecondaryType(TypeConstants.ExternalSynchronizedNodeType);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLNodeType node = (VRMLNodeType)nodes.get(i);
            externalSyncNodes.remove(node);
        }

        // Find all of the  nodes and register them with the appropriate
        // manager
        for(int i = 0; i < numPreEventManagers; i++) {
            int[] types = preEventManagers[i].getManagedNodeTypes();

            for(int j = 0; j < types.length; j++) {
                List<VRMLNode> node_list = scene.getByPrimaryType(types[j]);

                size = node_list.size();
                for(int k = 0; k < size; k++) {
                    VRMLNodeType node = (VRMLNodeType)node_list.get(k);

                    if(shutdownNow)
                        return;

                    if(node.getPrimaryType() == TypeConstants.ProtoInstance) {
                        VRMLProtoInstance proto = (VRMLProtoInstance)node;
                        node = proto.getImplementationNode();

                        while((node != null) && (node instanceof VRMLProtoInstance))
                            node = ((VRMLProtoInstance)node).getImplementationNode();
                    }

                    if(shutdownNow)
                        return;

                    if(node != null)
                        preEventManagers[i].removeManagedNode(node);
                }
            }
        }

        for(int i = 0; i < numPostEventManagers; i++) {
            int[] types = postEventManagers[i].getManagedNodeTypes();

            if(shutdownNow)
                return;

            for(int j = 0; j < types.length; j++) {
                List<VRMLNode> node_list = scene.getByPrimaryType(types[j]);

                if(shutdownNow)
                    return;

                size = node_list.size();
                for(int k = 0; k < size; k++) {
                    VRMLNodeType node = (VRMLNodeType)node_list.get(k);

                    if(shutdownNow)
                        return;

                    if(node.getPrimaryType() == TypeConstants.ProtoInstance) {
                        VRMLProtoInstance proto = (VRMLProtoInstance)node;
                        node = proto.getImplementationNode();

                        while((node != null) && (node instanceof VRMLProtoInstance))
                            node = ((VRMLProtoInstance)node).getImplementationNode();
                    }

                    if(shutdownNow)
                        return;

                    if(node != null)
                        postEventManagers[i].removeManagedNode(node);
                }
            }
        }


        routeManager.removeSpace(space);

        sensorManager.unloadScene(scene);

        // Walk the child proto instances as scenes and queue up anything
        // that's in them.
        nodes = scene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = nodes.size();

        for(int i = 0; i < size; i++) {
            VRMLProtoInstance proto = (VRMLProtoInstance)nodes.get(i);
            if(proto.getImplementationNode() == null)
                continue;

            removeScene(proto);
        }

        if(shutdownNow)
            return;

        if(currentScene == scene)
            initialBind();

    }
}
