/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import java.util.*;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.browser.*;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.util.URLChecker;

import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.EventModelInitListener;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.LayerRenderingManager;

/**
 * Minimalist representation of a core of a browser implemented using the OpenGL
 * rendering APIs.
 * <p>
 *
 * Does not support rendering to screen captures or profiling.
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public class OGLMinimalBrowserCore
    implements BrowserCore, LayerRenderingManager,
        EventModelInitListener {

    /** Description string of this world */
    private String worldDescription;

    /** The Scene we are currently working with */
    private VRMLScene currentScene;

    /** The next Scene to load */
    private VRMLScene nextScene;

    /** The space that represents the complete world we are running */
    private VRMLExecutionSpace currentSpace;

    /** Frame manager for the update cycles */
    private PerFrameManager frameManager;

    /** Viewport manager for all the layers */
    private ViewpointResizeManager viewpointManager;
    
    /** The current VRMLViewpointNodeType */
    private VRMLViewpointNodeType viewpoint;

    /** Event model evaluator to use */
    private EventModelEvaluator eventModel;

    /** The listeners for core content */
    private List<BrowserCoreListener> coreListeners;

    /** The list of Profiling listeners */
    private List<ProfilingListener> profilingListeners;

    /** The last render timing */
    private long lastRenderTime;

    /**
     * Does the device require a field of view. 0 = no, otherwise use this
     * instead of content
     */
    private float hardwareFOV;

    /** The list of ViewpointStatus listeners */
    private List<ViewpointStatusListener> viewpointStatusListeners;

    /** The list of SensorStatus listeners */
    private List<SensorStatusListener> sensorStatusListeners;

    /** The list of NavigationStatus listeners */
    private List<NavigationStateListener> navigationStateListeners;

    /** Collection of layer managers that are active */
    private LayerManager[] allManagers;

    /** The number of active layers current */
    private int numActiveLayers;

    /** The error errorReporter instance */
    private ErrorReporter errorReporter;

    /** Global rendering style currently set */
    private int globalRenderingStyle;

    /** Is stereo enabled */
    private boolean stereoEnabled;

    /** The current minimum frame cycle time. */
    private int frameCycleTime;

    /** The frame cycle time set by the end user. */
    private int userCycleTime;

    /** The frame cycle time set by the browser internals. */
    private int internalCycleTime;

    /** The next navigation mode */
    private String nextNavigationMode;

    /** Should we fit to world on the next App call */
    private boolean fitToWorldChanged;

    /** Should we animate the next fit to world */
    private boolean animateFitToWorld;

    /** Is there is a new hardwareFOV */
    private boolean hardwareFOVChanged;

    /** Did the stereoEnabled flag change */
    private boolean stereoEnabledChanged;

    /** The next stereo enabled */
    private boolean nextStereoEnabled;

    /** The next hardware FOV */
    private float nextHardwareFOV;

    /**
     * Construct a default, empty universe that contains no scene graph.
     *
     * @param eme The class used to evaluate the event model
     */
    public OGLMinimalBrowserCore(EventModelEvaluator eme) {
        eventModel = eme;
        eventModel.setInitListener(OGLMinimalBrowserCore.this);

        allManagers = new LayerManager[1];

        numActiveLayers = 0;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        coreListeners = new ArrayList<>();

        sensorStatusListeners = new ArrayList<>(1);
        viewpointStatusListeners = new ArrayList<>(1);
        navigationStateListeners = new ArrayList<>(1);

        // TODO: Do we need a viewport resize manager?
        viewpointManager = new ViewpointResizeManager();

        frameCycleTime = 0;
        userCycleTime = 0;
        internalCycleTime = 0;
        hardwareFOV = 0f;
        globalRenderingStyle = Xj3DConstants.RENDER_SHADED;
    }

    //----------------------------------------------------------
    // Methods defined by EventModelInitListener
    //----------------------------------------------------------

    /**
     * Notification from the event model evaluator that the
     * initialization phase is now complete. Use this to send off
     * the external Browser init event.
     */
    @Override
    public void worldInitComplete() {
        fireInitEvent();
    }

    /**
     * Notification that its safe to clear the world.  The underlying
     * rendering layer should now be cleared and loaded with the
     * world.
     */
    @Override
    public void changeWorld() {
        if(currentScene != null)
            fireShutdownEvent();

        currentScene = nextScene;
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCore
    //----------------------------------------------------------

    /**
     * Get the type of renderer that implements the browser core. The only
     * valid values returned are the constants in this interface.
     *
     * @return The renderer type
     */
    @Override
    public int getRendererType() {
        return Xj3DConstants.OPENGL_RENDERER;
    }

    /**
     * Get the ID string for this renderer.
     *
     * @return The String token for this renderer.
     */
    @Override
    public String getIDString() {
        return Xj3DConstants.OPENGL_ID;
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for all layers. Various options are available based on the constants
     * defined in this interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    @Override
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].setRenderingStyle(style);

        globalRenderingStyle = style;
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    @Override
    public int getRenderingStyle() {
        return globalRenderingStyle;
    }

    /**
     * Get the currently set rendering style for a specific layer. The default
     * style is RENDER_SHADED.
     *
     * @param layerId ID for layer of interest
     * @return one of the RENDER_ constants
     */
    @Override
    public int getRenderingStyle(int layerId) {
        // TODO: Need to handle this per layer
        return globalRenderingStyle;
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources taken up
     * by the 3D renderer. By default it will use all of them. The second
     * parameter is used to control whether this is a user-set hard minimum or
     * something set by the browser internals. User set values are always
     * treated as the minimum unless the browser internals set a value that is
     * a slower framerate than the user set. If the browser then sets a faster
     * framerate than the user set value, the user value is used instead.
     *
     * @param millis The minimum time in milliseconds.
     * @param userSet true if this is an end-user set minimum
     */
    @Override
    public void setMinimumFrameInterval(int millis, boolean userSet) {
        if(userSet)
            userCycleTime = millis;
        else
            internalCycleTime = millis;

        frameCycleTime = (userCycleTime >= internalCycleTime) ?
                         userCycleTime : internalCycleTime;
    }

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     */
    @Override
    public int getMinimumFrameInterval() {
        return frameCycleTime;
    }

    /**
     * Get the clock instance in use by the core. We need this for when
     * new nodes are added to the scene to make sure they are all appropriately
     * configured.
     *
     * @return The clock used by the browser core
     */
    @Override
    public VRMLClock getVRMLClock() {
        return eventModel.getVRMLClock();
    }

    /**
     * Get the mapping of DEF names to the node instances that they represent.
     * Primarily used for the EAI functionality. The map instance changes each
     * time a new world is loaded so will need to be re-fetched. If no mappings
     * are available (eg scripting replaceWorld() type call) then the map will
     * be empty.
     *
     * @return The current mapping of DEF names to node instances
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, VRMLNode> getDEFMappings() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Convenience method to ask for the execution space that the world is
     * currently operating in. Sometimes this is not known, particularly if
     * the end user has called a loadURL type function that is asynchronous.
     * This will change each time a new scene is loaded.
     *
     * @return The current world execution space.
     */
    @Override
    public VRMLExecutionSpace getWorldExecutionSpace() {
        return currentSpace;
    }

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    @Override
    public String getDescription() {
        return worldDescription;
    }

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     */
    @Override
    public void setDescription(String desc) {
        worldDescription = desc;
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    @Override
    public float getCurrentSpeed() {
        return 0.0f;
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    @Override
    public float getCurrentFrameRate() {
        float frame_time = lastRenderTime / 1000f;
        return 1 / frame_time;
    }

    /**
     * Set the last frame render time used for FPS calculations.  Only the
     * per frame manager should call this.
     *
     * @param lastTime The time it took to render the last frame in milliseconds.
     */
    @Override
    public void setLastRenderTime(long lastTime) {
        lastRenderTime = lastTime;
    }

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    @Override
    public void setEventModelStatusListener(EventModelStatusListener l) {
        frameManager.setEventModelStatusListener(l);
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addNavigationStateListener(NavigationStateListener l) {
        if(l == null)
            return;

        if(!navigationStateListeners.contains(l))
            navigationStateListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addNavigationStateListener(l);
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeNavigationStateListener(NavigationStateListener l) {
        if(l == null)
            return;

        navigationStateListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeNavigationStateListener(l);
    }

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addSensorStatusListener(SensorStatusListener l) {
        if(l == null)
            return;

        if(!sensorStatusListeners.contains(l))
            sensorStatusListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addSensorStatusListener(l);
    }

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeSensorStatusListener(SensorStatusListener l) {
        if(l == null)
            return;

        sensorStatusListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeSensorStatusListener(l);
    }

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addViewpointStatusListener(ViewpointStatusListener l) {
        if(l == null)
            return;

        if(!viewpointStatusListeners.contains(l))
            viewpointStatusListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addViewpointStatusListener(l);
    }

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeViewpointStatusListener(ViewpointStatusListener l) {
        if(l == null)
            return;

        viewpointStatusListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeViewpointStatusListener(l);
    }

    /**
     * Add an observer for a specific node type. A single instance may be
     * registered for more than one type. Each type registered will result in
     * a separate call per frame - one per type. If the observer is currently
     * added for this type ID, the request is ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to add
     */
    @Override
    public void addNodeObserver(int nodeType, NodeObserver obs) {
        eventModel.addNodeObserver(nodeType, obs);
    }

    /**
     * Remove the given node observer instance for the specific node type. It
     * will not be removed for any other requested node types. If the instance
     * is not registered for the given node type ID, the request will be
     * silently ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to remove
     */
    @Override
    public void removeNodeObserver(int nodeType, NodeObserver obs) {
        eventModel.removeNodeObserver(nodeType, obs);
    }

    /**
     * Notify the core that it can dispose all resources.  The core cannot be used for
     * rendering after that.
     */
    @Override
    public void dispose() {
        fireDisposeEvent();
    }

    /**
     * Get the fully qualified URL of the currently loaded world. This returns
     * the entire URL including any possible arguments that might be associated
     * with a CGI call or similar mechanism. If the initial world is replaced
     * with <code>loadURL</code> then the string will reflect the new URL. If
     * <code>replaceWorld</code> is called then the URL still represents the
     * original world.
     *
     * @return A string of the URL or null if not supported.
     */
    @Override
    public String getWorldURL() {
        String ret_val;

        if(currentScene != null)
            ret_val = currentScene.getWorldRootURL();
        else {
            ret_val = AccessController.doPrivileged((PrivilegedAction<String>) () -> {
                return URLChecker.prependFileScheme(System.getProperty("user.dir"));
            });
        }

        return ret_val;
    }

    /**
     * Set the scene to use within this universe. If null, this will clear this
     * scene and de-register all listeners. The View will be detached from the
     * ViewPlatform and therefore the canvas will go blank.
     *
     * @param scene The new scene to load, or null
     * @param viewpoint The viewpoint.description to bind to or null for default
     */
    @Override
    public void setScene(VRMLScene scene, String viewpoint) {
        nextScene = scene;
        eventModel.setScene(scene, viewpoint);
    }

    /**
     * Request that this viewpoint object is bound at the start of the next
     * frame. This method should only be called by external users such as
     * UI toolkits etc that need to synchronize the viewpoint change with
     * rendering loop, but are not able to synchronize themselves because they
     * exist on a different thread that cannot block.
     *
     * @param vp The new viewpoint instance to bind to
     */
    @Override
    public void changeViewpoint(VRMLViewpointNodeType vp) {
        viewpoint = vp;
        eventModel.changeViewpoint(vp);
    }

    /**
     * Add a listener for browser core events. These events are used to notify
     * all listeners of internal structure changes, such as the browser
     * starting and stopping. A listener can only be added once. Duplicate
     * requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addCoreListener(BrowserCoreListener l) {
        if((l != null) && !coreListeners.contains(l))
            coreListeners.add(l);
    }

    /**
     * Remove a browser core listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeCoreListener(BrowserCoreListener l) {
        coreListeners.remove(l);
    }

    /**
     * Request notification of profiling information.
     *
     * @param l The listener
     */
    @Override
    public void addProfilingListener(ProfilingListener l) {
        /*
        if(l == null)
            return;

        if(!profilingListeners.contains(l))
            profilingListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addProfilingListener(l);
        */
    }

    /**
     * Remove notification of profiling information.
     *
     * @param l The listener
     */
    @Override
    public void removeProfilingListener(ProfilingListener l) {
        /*
        if(l == null)
            return;

        if(!profilingListeners.contains(l))
            profilingListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeProfilingListener(l);
        */
    }

    /**
     * Send to the core listeners the error message that a URL failed to load
     * for some reason. This is for the EAI/ESAI spec conformance.
     *
     * @param msg The message to send
     */
    @Override
    public void sendURLFailEvent(String msg) {
        fireFailedURL(msg);
    }

    /**
     * Set the desired navigation mode. The mode string is one of the
     * spec-defined strings for the NavigationInfo node in the VRML/X3D
     * specification.
     *
     * @param mode The requested mode.
     */
    @Override
    public void setNavigationMode(String mode) {
        nextNavigationMode = mode;
    }

    /**
     * Get the user's location and orientation.  This will use the viewpoint
     * bound in the active layer.
     *
     * @param pos The current user position
     * @param ori The current user orientation
     */
    public void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        LayerManager mgr = getActiveNavLayer();
        mgr.getUserPosition(pos, ori);
    }

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transistion be animated.  Defaults to FALSE.
     */
    @Override
    public void fitToWorld(boolean animated) {
        fitToWorldChanged = true;
        animateFitToWorld = animated;
    }

    /**
     * Sync UI updates with the Application thread.  This method alls the core
     * to push work off to the app thread.
     */
    @Override
    public void syncUIUpdates() {
        if (nextNavigationMode != null) {
            LayerManager mgr = getActiveNavLayer();

            mgr.setNavigationMode(nextNavigationMode);
        }

        if (fitToWorldChanged) {
            LayerManager mgr = getActiveNavLayer();

            mgr.fitToWorld(animateFitToWorld);

            fitToWorldChanged = false;
        }

        if (hardwareFOVChanged) {
            hardwareFOV = nextHardwareFOV;

            for(int i = 0; i < numActiveLayers; i++)
                allManagers[i].setHardwareFOV(hardwareFOV);

        }

        if (stereoEnabledChanged) {
            stereoEnabled = nextStereoEnabled;

            // TODO: What to do about new layers
            for(int i = 0; i < numActiveLayers; i++)
                allManagers[i].setStereoEnabled(stereoEnabled);

            stereoEnabledChanged = false;
        }
    }

    /**
     * Capture the screen on the next render.
     *
     * @param listener Listener for capture results
     */
    @Override
    public void captureScreenOnce(ScreenCaptureListener listener) {
    }

    /**
     * Capture the screen on each render until told to stop.
     *
     * @param listener Listener for capture results
     */
    @Override
    public void captureScreenStart(ScreenCaptureListener listener) {
    }

    /**
     * Stop capturing the screen on each render.
     */
    @Override
    public void captureScreenEnd() {
    }

    //----------------------------------------------------------
    // Methods defined by LayerRenderingManager
    //----------------------------------------------------------

    /**
     * Set the list of current layers that should be rendered.
     *
     * @param layers The list of layer managers to be rendered
     * @param numLayers The number of active items in the list
     */
    @Override
    public void setActiveLayers(LayerManager[] layers, int numLayers) {
        // No need to clear out the old listeners from the existing layers
        // becase we assume that the LayerManager.clear() call did that
        // for us.
        allManagers = new LayerManager[numLayers];

        for(int i = 0; i < numLayers; i++) {
            OGLLayerManager l = (OGLLayerManager) layers[i];
            l.setViewpointResizeManager(viewpointManager);

            l.setHardwareFOV(hardwareFOV);
            l.setStereoEnabled(stereoEnabled);
            allManagers[i] = l;

            switch(l.getViewportType()) {
                case LayerManager.VIEWPORT_FULLWINDOW:
                    // To do
                    break;

                case LayerManager.VIEWPORT_PROPORTIONAL:
                    // TODO
                    // Implement a viewport manager that can handle
                    // proportional resizes.
                    break;

                case LayerManager.VIEWPORT_FIXED:
                    break;
            }

            // Add all the listeners back into this layer
            sensorStatusListeners.forEach(sl -> {
                l.addSensorStatusListener(sl);
            });
            navigationStateListeners.forEach(sl -> {
                l.addNavigationStateListener(sl);
            });
            for (ProfilingListener profilingListener : profilingListeners) {
                /*
                ProfilingListener sl =
                (ProfilingListener)profilingListeners.get(j);
                l.addProfilingListener(sl);
                 */
                /*
                ProfilingListener sl =
                (ProfilingListener)profilingListeners.get(j);
                l.addProfilingListener(sl);
                 */
            }
            viewpointStatusListeners.forEach(sl -> {
                l.addViewpointStatusListener(sl);
            });
        }

        numActiveLayers = numLayers;
    }

    /**
     * Set the rendering order for all the layers on this manager
     *
     * @param order The index of the list of rendered layers ids
     * @param numValid The number of valid items in the order list
     */
    @Override
    public void setRenderOrder(int[] order, int numValid) {
        // Ignored for now.
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for for a specific layer. Various options are available based on the
     * constants defined in this interface.
     *
     * @param style One of the RENDER_* constants
     * @param layerId The ID of the layer that should have the style changed
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    @Override
    public void setRenderingStyle(int style, int layerId)
        throws IllegalArgumentException {

        // ignore for now
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    @Override
    public void shutdown() {
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Force clearing all currently managed layers from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    @Override
    public void clear() {
        // Clear local refrences too. Don't call clear() on the layer here
        // as that will be called before this method is called. just clear the
        // local references.
        for(int i = 0; i < allManagers.length; i++)
            allManagers[i] = null;
    }

    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------

    /**
     * Set the per-frame manager instance to use with this group.
     *
     * @param mgr The manager for dealing with the frame clocking
     */
    public void setPerFrameManager(PerFrameManager mgr) {
        frameManager = mgr;
    }

    /**
     * Override the file field of view values with a value that suits
     * the given output device. A value of 0 = no, otherwise use this
     * instead of content
     *
     * @param fov The fov in degrees.
     */
    public void setHardwareFOV(float fov) {
        hardwareFOVChanged = true;
        nextHardwareFOV = fov;
    }

    /**
     * Set whether stereo is enabled for all layers.
     * @param enabled
     */
    public void setStereoEnabled(boolean enabled) {
        nextStereoEnabled = enabled;
        stereoEnabledChanged = true;
    }

    /**
     * Get the scene that this universe is currently holding. If none is set
     * then null is returned.
     *
     * @return The currently set scene instance
     */
    public VRMLScene getScene() {
        return currentScene;
    }

    /**
     * Get the currently selected viewpoint in the active navigation layer.
     * If there is no scene set then this returns null.
     *
     * @return The current active viewpoint
     */
    public VRMLViewpointNodeType getViewpoint() {
        return viewpoint;
    }

    /**
     * Go through all the layers and find the active navigation layer.
     */
    private LayerManager getActiveNavLayer() {
        LayerManager ret_val = null;

        for (LayerManager allManager : allManagers) {
            if (allManager.isActiveNavigationLayer()) {
                ret_val = allManager;
                break;
            }
        }

        return ret_val;
    }

    /**
     * Fire an initialised event to all the listeners. The listeners are given
     * the instance of currentScene.
     */
    private void fireInitEvent() {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l = coreListeners.get(i);

                l.browserInitialized(currentScene);
            } catch(Exception e) {
                System.err.println("Error sending init event " + e);
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Fire an event about a URL failing to load to all the listeners. The
     * listeners are given the instance of currentScene.
     */
    private void fireFailedURL(String msg) {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l = coreListeners.get(i);

                l.urlLoadFailed(msg);
            } catch(Exception e) {
                System.err.println("Error sending init event " + e);
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Fire a shutdown event to all the listeners.
     */
    private void fireShutdownEvent() {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l = coreListeners.get(i);

                l.browserShutdown();
            } catch(Exception e) {
                System.err.println("Error sending init event " + e);
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Fire a dispose event to all the listeners.
     */
    private void fireDisposeEvent() {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l = coreListeners.get(i);

                l.browserDisposed();
            } catch(Exception e) {
                System.err.println("Error sending init event " + e);
                e.printStackTrace(System.err);
            }
        }
    }
}
