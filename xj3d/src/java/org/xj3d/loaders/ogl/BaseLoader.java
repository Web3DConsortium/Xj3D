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

package org.xj3d.loaders.ogl;

// External imports
import java.io.*;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;
import org.ietf.uri.URN;

import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVModel;

// Local imports
import org.web3d.vrml.parser.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.resolve.Web3DURNResolver;

import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.common.input.DefaultSensorManager;
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLMinimalBrowserCore;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;
import org.xj3d.impl.core.eventmodel.*;
import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

/**
 * Common base implementation Aviatrix3D file loader implementation for reading
 * loading files and building a Aviatrix3D scene graph with them.
 * <p>
 *
 * The loader considers sensor handling and routing to be behaviours. Some
 * asynchronous loading of files for textures is performed. Sound file loading
 * is performed if audio clips are asked for. For example, if behaviours are
 * not requested then Inlines will not have their content loaded.
 * <p>
 *
 * If the loader asks for no runtime, then we will still load nodes that
 * need a runtime system to work, but will disable their use. For example, a
 * LOD will still need to have all of the geometry loaded, just not shown or
 * activated because the LOD's internal behavior is disabled. Scripts are
 * considered to be behaviours, and they will not be loaded at all if runtime
 * loading is disabled.
 * <p>
 *
 * The implementation only makes use of two behaviours. One is a per-frame
 * behaviour for the event model evaluation. The other is a handler for
 * trapping user input events. If you disable behaviours, you loose both
 * of these. For content other than static geometry, such as animations,
 * turning off behaviours will result in no animations. However, every loaded
 * scene will be attempting to do work like navigation. This will become quite
 * CPU intensive because every model will be performing picking operations.
 * To cut down on this CPU usage, the navigation processing is turned off
 * by default. If you want the loaded code to also do the navigation of the
 * viewpoints, then you can call the setNavigationEnable() method.
 *
 * The default setup for runtime activities is
 * {@link org.xj3d.impl.core.eventmodel.ListsRouterFactory} and
 * {@link org.xj3d.impl.core.loading.MemCacheLoadManager}
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public abstract class BaseLoader implements AVLoader {

    /** Global flag to indicate properties have been set up already */
    private static boolean setupComplete = false;

    /** Flag to say if the scene builder factory should only use VRML97 */
    protected boolean vrml97Only;

    /** The currently set load flags */
    private int loadFlags;

    /** Flag defining whether we should keep the internal model too. */
    private boolean keepModel;

    /** Class that represents the external reporter */
    protected ErrorReporter errorReporter;

    /** Flag to say if navigation handling should be disabled */
    private boolean navigationEnabled;

    /** Resolver for processing URNs like Hamin and GeoVRML */
    private Web3DURNResolver resolver;

    /**
     * Construct a default loader implementation with no flags set. When asked
     * to load a file it will not produce anything unless flags have been
     * set through the <code>setFlags()</code> method.
     */
    protected BaseLoader() {
        vrml97Only = false;

        loadFlags = LOAD_ALL;
        keepModel = false;

        resolver = new Web3DURNResolver();
        navigationEnabled = false;
    }

    //---------------------------------------------------------------
    // Methods defined by AVLoader
    //---------------------------------------------------------------

    /**
     * Load a model from the given URL.
     *
     * @param url The url to load the model from
     * @return A representation of the model at the URL
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(URL url) throws IOException {
        InputSource is = new InputSource(url);

        return load(is);
    }

    /**
     * Load a model from the given input stream. If the file format would
     * prefer to use a {@link java.io.Reader} interface, then use the
     * {@link java.io.InputStreamReader} to convert this stream to the desired
     * type. The caller will be responsible for closing down the stream at the
     * end of this process.
     *
     * @param stream The stream to load the model from
     * @return A representation of the model from the stream contents
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(InputStream stream) throws IOException {
        String worldURL = null;

        try {
            worldURL = AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> System.getProperty("user.dir"));
        } catch (PrivilegedActionException pae) {
            System.out.println("Cannot get user dir in BaseLoader");
        }

        InputSource is = new InputSource(worldURL, stream);

        return load(is);
    }

    /**
     * Load a model from the given file.
     *
     * @param file The file instance to load the model from
     * @return A representation of the model in the file
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(File file) throws IOException {
        if(!file.exists())
            throw new FileNotFoundException("File does not exist");

        if(file.isDirectory())
            throw new FileNotFoundException("File is a directory");

        InputSource is = new InputSource(file);

        return load(is);
    }

    /**
     * Set the flags for which parts of the file that should be loaded.
     * The flags are bit-fields, so can be bitwise OR'd together.
     *
     * @param flags The collection of flags to use
     */
    @Override
    public void setLoadFlags(int flags) {
        loadFlags = flags;
    }

    /**
     * Get the current set collection of load flags.
     *
     * @return A bitmask of flags that are currently set
     */
    @Override
    public int getLoadFlags() {
        return loadFlags;
    }

    /**
     * Define whether this loader should also keep around it's internal
     * representation of the file format, if it has one. If kept, this can be
     * retrieved through the {@link AVModel#getRawModel()} method and cast to
     * the appropriate class type.
     *
     * @param enable true to enable keeping the raw model, false otherwise
     */
    @Override
    public void keepInternalModel(boolean enable) {
        keepModel = enable;
    }

    /**
     * Check to see whether the loader should be currently keeping the internal
     * model.
     *
     * @return true when the internal model should be kept
     */
    @Override
    public boolean isInternalModelKept() {
        return keepModel;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    public boolean getNavigationEnabled() {
        return navigationEnabled;
    }

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     *
     * @param state true to enable navigation
     */
    public void setNavigationEnabled(boolean state) {
        navigationEnabled = state;
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        }

    /**
     * Add a prefix and directory to the URN resolution system. Whenever the
     * prefix is found under the web3d area, this directory will be searched.
     *
     * @param prefix The subspace prefix to use
     * @param directory The directory that GeoVRML is installed in
     * @throws IllegalArgumentException The directory is not valid
     */
    public void registerURNLocation(String prefix, String directory)
            throws IllegalArgumentException {

        resolver.registerPrefixLocation(prefix, directory);
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return The scene description
     * @throws IncorrectFormatException The file is not one our loader
     *    understands (VRML 1.0 or X3D content)
     * @throws ParsingErrorException An error parsing the file
     */
    private AVModel load(InputSource is) throws IOException {

        // set the loading flags. Ignore the behaviours for the moment
        // as we don't have anything to translate that to.
        boolean use_bg = (loadFlags & BACKGROUNDS) != 0;
        boolean use_fog = (loadFlags & FOGS) != 0;
        boolean use_light = (loadFlags & LIGHTS) != 0;
        boolean use_audio = true;
        boolean use_view = (loadFlags & VIEWPOINTS) != 0;
        boolean use_behaviors = (loadFlags & RUNTIMES) != 0;

        // Set the load requirements. We duplicate the load behavior flag
        // for both the external and behavior node requirements.
        SceneBuilderFactory builder_fac =
                new OGLSceneBuilderFactory(vrml97Only,
                use_bg,
                use_fog,
                use_light,
                use_audio,
                use_view,
                use_behaviors);

        VRMLParserFactory parser_fac = null;

        try {
            parser_fac = VRMLParserFactory.newVRMLParserFactory();
            setupParserFactory(parser_fac);
        } catch(FactoryConfigurationError fce) {
            throw new RuntimeException("Failed to load factory");
        }

        FrameStateManager fsm = new DefaultFrameStateManager();

        Xj3DModel ret_val;

        // If we are using behaviours, use a different codepath that sets up
        // everything that you need to know about to make it run.
        if(use_behaviors) {

            RouteManager rm = new DefaultRouteManager();
            rm.setRouterFactory(new ListsRouterFactory());

            ScriptManager s_mgr = new DefaultScriptManager();
            SensorManager sens_mgr = new DefaultSensorManager();

            EventModelEvaluator event_model = new DefaultEventModelEvaluator();
            ContentLoadManager load_mgr = new MemCacheLoadManager();

            DefaultHumanoidManager hanim_manager = new DefaultHumanoidManager();
            NetworkManager network_manager = new DefaultNetworkManager();
            DefaultRigidBodyPhysicsManager physics_manager =
                    new DefaultRigidBodyPhysicsManager();
            DefaultParticleSystemManager particle_manager =
                    new DefaultParticleSystemManager();
            DISProtocolHandler dis_handler = new DISProtocolHandler();
            network_manager.addProtocolHandler(dis_handler);

            NodeManager[] node_mgrs = {
                network_manager,
                hanim_manager,
                physics_manager,
                particle_manager
            };

            OGLMinimalBrowserCore root_group =
                    new OGLMinimalBrowserCore(event_model);

            ViewpointManager vp_mgr = new DefaultViewpointManager(root_group);
            LayerManagerFactory lmf = new NullLayerManagerFactory();
            lmf.setErrorReporter(errorReporter);

            event_model.initialize(s_mgr,
                    rm,
                    sens_mgr,
                    fsm,
                    load_mgr,
                    vp_mgr,
                    lmf,
                    new NullLayerRenderingManager(),
                    node_mgrs);
            event_model.setErrorReporter(errorReporter);

            WorldLoaderManager w_loader =
                    new DefaultWorldLoaderManager(root_group, fsm, rm);
            w_loader.setErrorReporter(errorReporter);
            w_loader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
                    builder_fac);
            w_loader.registerParserFactory(Xj3DConstants.OPENGL_RENDERER,
                    parser_fac);

            Xj3DClockRuntime runtime_clock =
                    new Xj3DClockRuntime(event_model, root_group);
            root_group.setPerFrameManager(runtime_clock);

            setupProperties(root_group, w_loader);

            WorldLoader ldr = w_loader.fetchLoader();
            VRMLScene parsed_scene = ldr.loadNow(root_group, is);
            w_loader.releaseLoader(ldr);

            ret_val = new Xj3DModel(parsed_scene);

// LAYERS:
// No longer have a single global user input handler.
//            UserInputHandler ui_handler = sens_mgr.getUserInputHandler();
//            ui_handler.setNavigationEnabled(navigationEnabled);

            ScriptLoader s_loader = new DefaultScriptLoader();
            s_mgr.setScriptLoader(s_loader);

            ScriptEngine jsai = new VRML97ScriptEngine(root_group,
                    rm,
                    fsm,
                    w_loader);
            ScriptEngine ecma = new JavascriptScriptEngine(root_group,
                    rm,
                    fsm,
                    w_loader);

            ScriptEngine java_sai = new JavaSAIScriptEngine(root_group,
                    vp_mgr,
                    rm,
                    fsm,
                    w_loader);

            ScriptEngine ecma_sai = new ECMAScriptEngine(root_group,
                    vp_mgr,
                    rm,
                    fsm,
                    w_loader);

            jsai.setErrorReporter(errorReporter);
            ecma.setErrorReporter(errorReporter);
            ecma_sai.setErrorReporter(errorReporter);
            java_sai.setErrorReporter(errorReporter);

            s_loader.registerScriptingEngine(jsai);
            s_loader.registerScriptingEngine(ecma);
            s_loader.registerScriptingEngine(java_sai);
            s_loader.registerScriptingEngine(ecma_sai);

            // Add the behaviour that is per frame behaviour
            root_group.setScene(parsed_scene, null);
            ret_val.addRuntimeComponent(runtime_clock);

        } else {
            RouteManager rm = new DefaultRouteManager();
            BrowserCore core = new StaticBrowserCore();

            WorldLoaderManager w_loader =
                    new DefaultWorldLoaderManager(core, fsm, rm);
            w_loader.setErrorReporter(errorReporter);
            w_loader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
                    builder_fac);
            w_loader.registerParserFactory(Xj3DConstants.OPENGL_RENDERER,
                    parser_fac);

            setupProperties(core, w_loader);

            WorldLoader ldr = w_loader.fetchLoader();
            VRMLScene parsed_scene = ldr.loadNow(core, is);
            w_loader.releaseLoader(ldr);

            ret_val = new Xj3DModel(parsed_scene);
        }

        ret_val.setValues(keepModel);

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     * May be overridden by derived classes, but should also call this
     * for the standard setup.
     *
     * @param core The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    private void setupProperties(final BrowserCore core,
            final WorldLoaderManager wlm) {

        if (setupComplete) {
            return;
        }

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            // Disable font cache to fix getBounds nullPointer bug
            System.setProperty("sun.awt.font.advancecache", "off");
            
            if (System.getProperty("uri.content.handler.pkgs") == null) {
                System.setProperty("uri.content.handler.pkgs",
                        "vlc.net.content");
            }
            
            if (System.getProperty("uri.protocol.handler.pkgs") == null) {
                System.setProperty("uri.protocol.handler.pkgs",
                        "vlc.net.protocol");
            }
            
            ContentHandlerFactory c_fac =
                    URI.getContentHandlerFactory();
            
            if (!(c_fac instanceof VRMLContentHandlerFactory)) {
                c_fac = new VRMLContentHandlerFactory(core, wlm);
                URI.setContentHandlerFactory(c_fac);
            }
            
            
            FileNameMap fn_map = URI.getFileNameMap();
            if (!(fn_map instanceof VRMLFileNameMap)) {
                fn_map = new VRMLFileNameMap(fn_map);
                URI.setFileNameMap(fn_map);
            }
            
            URN.addResolver(resolver);
            
            setupPropertiesProtected();
            return null;
        });

        setupComplete = true;
    }

    /**
     * Set up the system properties needed to run the browser within the
     * context of a privileged block. Default implementation is empty. May be
     * overridden by derived class.
     */
    void setupPropertiesProtected() {
    }

    /**
     * Set up any derived-class specific properties for the parser factory
     * instance. This will be called just before a new parser factory will
     * be instantiated.
     * <p>
     *
     * The default implementation of this method is to do nothing.
     *
     * @param factory The factory instance to place any configuration info
     */
    void setupParserFactory(VRMLParserFactory factory) {
    }
}
