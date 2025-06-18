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

package org.xj3d.impl.core.loading;

// External imports
import java.io.IOException;

// Local imports
import org.web3d.browser.BrowserCore;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.IntHashMap;
import org.j3d.util.ObjectArray;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.sav.VRMLReader;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import org.xj3d.core.loading.SceneBuilder;
import org.xj3d.core.loading.SceneBuilderFactory;
import org.xj3d.core.loading.WorldLoader;

/**
 * Internal default implementation of the WorldLoader interface.
 * <p>
 *
 * The default implementation does a lot of caching of internal structures to
 * try to save on both memory consumption and startup time wherever possible.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class DefaultWorldLoader implements WorldLoader 
{
    /** Map for the renderer to scene builder mapping */
    private static final IntHashMap<SceneBuilderFactory> builderFactoryMap;

    /**
     * Map of renderer type to a list of the available scene builder
     * instances. Used for caching.
     */
    private static final IntHashMap<ObjectArray> builderInstanceMap;

    /** Map for the renderer to parser mapping */
    private static final IntHashMap<VRMLParserFactory> vrmlParserFactoryMap;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The frame state manager for nodes loaded by this class */
    private final FrameStateManager frameStateManager;

    /**
     * Static initializer to get the caching set up correctly.
     */
    static {
        builderFactoryMap = new IntHashMap<>();
        builderInstanceMap = new IntHashMap<>();
        vrmlParserFactoryMap = new IntHashMap<>();
    }

    /**
     * Construct a new instance of the world loader that uses the given
     * frame state manager.
     *
     * @param fsm The state manager for this loader to use
     */
    DefaultWorldLoader(FrameStateManager fsm) {
        frameStateManager = fsm;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-----------------------------------------------------------------------
    // Methods defined by WorldLoader
    //-----------------------------------------------------------------------

    @Override
    public VRMLScene loadNow(BrowserCore core, InputSource source)
        throws IOException, VRMLParseException {
        return loadNow(core, source, false);
    }

    @Override
    public VRMLScene loadNow(BrowserCore browserCore,
                             InputSource inputSource,
                             boolean     ignoreHeader)
        throws IOException, VRMLParseException {

        int rendererType = browserCore.getRendererType();

        SceneBuilder sceneBuilder = getSceneBuilder(rendererType);

        if(sceneBuilder == null)
            throw new VRMLException ("Unable to find a builder to locate SceneBuilder instance");

        VRMLParserFactory vrmlParserFactory = getVrmlParserFactory(rendererType);

        if(vrmlParserFactory == null)
            throw new VRMLException("Unable to find a builder to locate Parser instance");

        sceneBuilder.reset();
        sceneBuilder.setFrameStateManager(frameStateManager);
        sceneBuilder.setErrorReporter(errorReporter);

        VRMLReader vrmlReader = vrmlParserFactory.newVRMLReader();

        vrmlReader.setHeaderIgnore(ignoreHeader);
        vrmlReader.setContentHandler(sceneBuilder);
        vrmlReader.setScriptHandler(sceneBuilder);
        vrmlReader.setProtoHandler(sceneBuilder);
        vrmlReader.setRouteHandler(sceneBuilder);
        vrmlReader.setErrorReporter(errorReporter);

        vrmlReader.parse(inputSource);

        VRMLScene vrmlScene = sceneBuilder.getScene();
        sceneBuilder.releaseScene();

        VRMLNodeType rootVrmlNodeType = (VRMLNodeType) vrmlScene.getRootNode();
        if (rootVrmlNodeType == null)
        {
            System.out.println("*** Xj3D DefaultWorldLoader rootVrmlNodeType is null");
        }
        else
        {
            rootVrmlNodeType.setFrameStateManager(frameStateManager);
        }

        // release back to the cache
        releaseBuilder(rendererType, sceneBuilder);

        return vrmlScene;
    }

    @Override
    public VRMLScene loadNow(BrowserCore browserCore,
                             InputSource inputSource,
                             boolean     ignoreHeader,
                             int         specMajorVersion,
                             int         specMinorVersion)
        throws IOException, VRMLParseException {

// TODO: Later stages come back as 3 and use the wrong parser for XML booleans.
//majorVersion = 0;

        if(specMajorVersion == 0)
            return loadNow(browserCore, inputSource, ignoreHeader);

        int rendererType = browserCore.getRendererType();

        SceneBuilder sceneBuilder = getSceneBuilder(rendererType);

        if(sceneBuilder == null)
            throw new VRMLException ("Unable to find a builder to locate SceneBuilder instance");

        VRMLParserFactory vrmlParserFactory = getVrmlParserFactory(rendererType);

        if (vrmlParserFactory == null)
            throw new VRMLException("Unable to find a builder to locate Parser instance");

        sceneBuilder.reset();
        sceneBuilder.setFrameStateManager(frameStateManager);
        sceneBuilder.setErrorReporter(errorReporter);

        String version = specMajorVersion + "." + specMinorVersion;

        vrmlParserFactory.setProperty(VRMLParserFactory.REQUIRE_VERSION_PROP,version);
        VRMLReader vrmlReader = vrmlParserFactory.newVRMLReader();

        vrmlReader.setHeaderIgnore(ignoreHeader);
        vrmlReader.setContentHandler(sceneBuilder);
        vrmlReader.setScriptHandler(sceneBuilder);
        vrmlReader.setProtoHandler(sceneBuilder);
        vrmlReader.setRouteHandler(sceneBuilder);

        vrmlReader.parse(inputSource);

        VRMLScene vrmlScene = sceneBuilder.getScene();
        sceneBuilder.releaseScene();

        if (vrmlScene == null) // avoid NPE
            return null;
        
        VRMLNodeType rootVrmlNodeType = (VRMLNodeType) vrmlScene.getRootNode();
        rootVrmlNodeType.setFrameStateManager(frameStateManager);

        // release back to the cache
        releaseBuilder(rendererType, sceneBuilder);

        return vrmlScene;
    }

    @Override
    public void shutdown () 
    {
           builderFactoryMap.clear();
          builderInstanceMap.clear();
        vrmlParserFactoryMap.clear();
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register the scene builder factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the factory for the given renderer type from the
     * map.
     *
     * rendererType renderer The ID of the renderer type
     * @param sceneBuilderFactory The instance of the factory to use
     */
    static void registerBuilderFactory(int rendererType,
                                       SceneBuilderFactory sceneBuilderFactory) {
        if(sceneBuilderFactory == null)
            builderFactoryMap.remove(rendererType);
        else
            builderFactoryMap.put(rendererType, sceneBuilderFactory);
    }

    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param rendererType The ID of the renderer type
     * @param factory The instance of the factory or null
     */
    static SceneBuilderFactory getBuilderFactory(int rendererType) {
        return builderFactoryMap.get(rendererType);
    }

    /**
     * Register the parser factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the factory for the given renderer type from the
     * map.
     *
     * @param rendererType The ID of the renderer type
     * @param vrmlParserFactory The instance of the factory to use
     */
    static void registerParserFactory(int rendererType, VRMLParserFactory vrmlParserFactory) {
        if(vrmlParserFactory == null)
            vrmlParserFactoryMap.remove(rendererType);
        else
            vrmlParserFactoryMap.put(rendererType, vrmlParserFactory);
    }

    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param rendererType The ID of the renderer type
     */
    static VRMLParserFactory getVrmlParserFactory(int rendererType)
    {
        return vrmlParserFactoryMap.get(rendererType);
    }

    /**
     * Fetch a scene builder instance for the given renderer type. It will
     * attempt to grab one from the cache, but will create another if all
     * the available instances are in use. If no factory is registered for
     * the type it will return null.
     *
     * @param rendererType The ID of the renderer type
     * @return An instance of a scene builder for the renderer
     */
    private static synchronized SceneBuilder getSceneBuilder(int rendererType) {

        ObjectArray items = builderInstanceMap.get(rendererType);
        SceneBuilder ret_val = null;

        if((items != null) && (items.size() != 0))
            ret_val = (SceneBuilder)items.remove(items.size() - 1);
        else 
        {
            SceneBuilderFactory sceneBuilderFactory = getBuilderFactory(rendererType);

            if (sceneBuilderFactory != null)
                ret_val = sceneBuilderFactory.createBuilder();
        }

        return ret_val;
    }

    /**
     * Release the scene builder instance back to the cache.
     *
     * @param rendererType The ID of the renderer type
     * @param sceneBuilder the builder instance to insert
     */
    private static void releaseBuilder(int rendererType, SceneBuilder sceneBuilder) 
    {
        ObjectArray items = builderInstanceMap.get(rendererType);

        if (items == null) 
        {
            items = new ObjectArray();
            builderInstanceMap.put(rendererType, items);
        }

        items.add(sceneBuilder);
    }
}
