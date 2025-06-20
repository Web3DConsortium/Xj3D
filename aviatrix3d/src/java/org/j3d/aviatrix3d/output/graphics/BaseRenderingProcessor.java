/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.output.graphics;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.jogamp.opengl.*;
import java.util.List;
import java.util.Map;

import javax.vecmath.*;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.IntHashMap;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Common implementation for rendering handling to render for a single
 * output device - be it on-screen or off.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>generalRenderErrorMsg: Error message when some other general error
 *     occurred during the normal rendering loop.</li>
 * <li>glMajorVersionParseMsg: Error parsing the major version number</li>
 * <li>glMinorVersionParseMsg: Error parsing the minor version number</li>
 * <li>glslMajorVersionParseMsg: Error parsing the shader major version number</li>
 * <li>glslMinorVersionParseMsg: Error parsing the shader minor version number</li>
 * <li>surfaceListenerErrorMsg: Caught an error in userland code when sending
 *     the surfaceInfoChanged() method</li>
 * <li>unknownProjTypeMsg: An unknown projection type made it down to the
 *     renderer</li>
 * <li>nullDescriptorMsg: Internal problem where the buffer descriptor being
 *     created for this renderer is null</li>
 * </ul>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 3.69 $
 */
public abstract class BaseRenderingProcessor
    implements RenderingProcessor
{
    /** The invalid shader ID */
    protected static final Integer INVALID_SHADER = -1;

    /** If there's a GLException during the render loop */
    private static final String GL_RENDER_ERROR_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.generalRenderErrorMsg";

    /** If there's a badly formated number in the GL major version string */
    private static final String GL_MAJOR_VER_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.glMajorVersionParseMsg";

    /** If there's a badly formated number in the GL minor version string */
    private static final String GL_MINOR_VER_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.glMinorVersionParseMsg";

    /** If there's a badly formated number in the GL major version string */
    private static final String SHADER_MAJOR_VER_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.glslMajorVersionParseMsg";

    /** If there's a badly formated number in the GL minor version string */
    private static final String SHADER_MINOR_VER_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.glslMinorVersionParseMsg";

    /** Surfacelistener callback caught an error in userland */
    private static final String SURFACE_LIST_ERR_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.surfaceListenerErrorMsg";

    /** Unknown projection type made it down here */
    private static final String UNKNOWN_PROJECTION_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.unknownProjTypeMsg";

    /** Null set as the owner buffer descriptor */
    private static final String NULL_BUF_DESC_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseRenderingProcessor.nullDescriptorMsg";


    /** Constant not currently defined by JOGL 1.1.x */
    private static final int GL_MAX_COLOR_ATTACHMENTS =  0x8CDF;


    /** The owner device of us. Used to send out messages */
    private GraphicsOutputDevice ownerDevice;

    /** The current clear colour */
    protected float[] clearColor;

    /** Flag to say that colour needs to be reset this frame */
    protected boolean resetColor;

    /**
     * Alpha Test value for the parts of transparent objects that should be
     * handled as opaque
     */
    protected float alphaCutoff;

    /**
     * Flag indicating if we should do single or two-pass rendering of
     * transparent objects.
     */
    protected boolean useTwoPassTransparent;

    /** Local storage of the nodes that need to be rendered */
    protected GraphicsDetails[] renderableList;

    /** Local storage of the nodes that need to be rendered */
    protected RenderOp[] operationList;

    /** Number of items in the renderable list */
    protected int numRenderables;

    /** Local storage of the environment data collections */
    protected GraphicsEnvironmentData[] environmentList;

    /** Request object for deletions, shader stuff etc */
    protected GraphicsRequestData otherDataRequests;

    /** The current program ID for GLSLang shaders.  */
    protected Integer currentShaderProgramId;

    /** Stack of the available OGL light IDs that could be assigned to lights */
    protected Integer[] availableLights;

    /** index in the availableLights list of the last available */
    protected int lastLightIdx;

    /** Mapping of the object ID to it's used light ID */
    protected IntHashMap<Integer> lightIdMap;

    /** Stack of the available OGL clip IDs that could be assigned to clips */
    protected Integer[] availableClips;

    /** index in the availableClips list of the last available */
    protected int lastClipIdx;

    /** Mapping of the object ID to it's used clip ID */
    protected IntHashMap<Integer> clipIdMap;

    /**
     * Flag to say if we the default clear colour should be used, or
     * if there is at least one background that we can make use of
     */
    protected boolean alwaysLocalClear;

    /** Combined view and projection matrix. This used to be set
     * in setEnvironmentData when we had a single one. Now there is
     * one per layer and we need to do something about it.
     */
    protected Matrix4d surfaceMatrix;

    /** The eye offset from the normal position */
    protected float[] eyePoint;

    /** Data describing the current viewport if explicit values used */
    protected float[] currentViewport;

    /**
     * Flag indicating if any initialisation has been performed yet. Sometimes
     * we can get into situations where the GL context is already valid, but
     * we don't get the NEW context status being reported. If we haven't
     * initialised ourselves at this point, then we really need to, to stop
     * crashing.
     */
    protected boolean initComplete;

    /** Request that the current drawing terminate immediately. App closing */
    protected boolean terminate;

    /** Listeners for surface context changes */
    private SurfaceInfoListener surfaceListeners;

    /** Error reporter used to send out messages */
    protected ErrorReporter errorReporter;

    /** Flag indicating the context has not been destroyed yet */
    private boolean contextNotDestroyed;

    /** List of last found layer in the search list for mouse handling */
    private Map<String, GraphicsEnvironmentData> envSaveMap;

    /**
     * A reference to the containing renderable object that the contents of
     * this rendering will be applied to. If this is the main canvas then the
     * reference will be null.
     */
    protected BaseBufferDescriptor ownerRenderable;

    /**
     * Child renderables that have just been added, but not processed yet. These
     * will be processed in the next frame and have buffers allocated.
     */
    protected List<OffscreenBufferRenderable> addedBuffers;

    /** Mirror list of the matching rendering processors for added buffers */
    protected List<RenderingProcessor> addedProcessors;

    /**
     * Child renderables that have just been added, but not processed yet. These
     * will be processed in the next frame and have buffers allocated.
     */
    protected List<OffscreenBufferRenderable> updatedBuffers;

    /** Currently managed child buffers */
    protected Map<OffscreenBufferRenderable,
                      BaseBufferDescriptor> childBuffers;

    /**
     * Child renderables that have been removed, but have not yet had their
     * underlying resources deleted. Will be processed at the next frame.
     */
    protected List<OffscreenBufferRenderable> removedBuffers;

    /**
     * Construct handler for rendering objects to the main screen.
     *
     * @param owner The owning device of this processor
     */
    protected BaseRenderingProcessor(GraphicsOutputDevice owner)
    {
        ownerDevice = owner;

        clearColor = new float[4];
        surfaceMatrix = new Matrix4d();
        eyePoint = new float[3];

        alphaCutoff = 1.0f;
        useTwoPassTransparent = false;
        resetColor = false;
        initComplete = false;
        terminate = false;
        contextNotDestroyed = true;
        alwaysLocalClear = true;

        numRenderables = 0;

        lightIdMap = new IntHashMap<>();
        clipIdMap = new IntHashMap<>();
        envSaveMap = new HashMap<>();

        lastLightIdx = 0;
        lastClipIdx = 0;
        currentShaderProgramId = INVALID_SHADER;

        otherDataRequests = new GraphicsRequestData();
        currentViewport = new float[4];

        addedBuffers = new ArrayList<>();
        addedProcessors = new ArrayList<>();
        updatedBuffers = new ArrayList<>();
        childBuffers = new HashMap<>();
        removedBuffers = new ArrayList<>();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Methods defined by RenderingProcessor
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown.
     */
    @Override
    public void halt()
    {
        terminate = true;
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     */
    @Override
    public void setClearColor(float r, float g, float b, float a)
    {
        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
        clearColor[3] = a;

        resetColor = true;
    }

    /**
     * Set whether we should always force a local colour clear before
     * beginning any drawing. If this is set to false, then we can assume that
     * there is at least one background floating around that we can use to
     * clear whatever was drawn in the previous frame, and so we can ignore the
     * glClear(GL.GL_COLOR_BUFFER_BIT) call. The default is set to true.
     *
     * @param state true if we should always locally clear first
     */
    @Override
    public void setColorClearNeeded(boolean state)
    {
        alwaysLocalClear = state;
    }

    /**
     * Enable or disable two pass rendering of transparent objects. By default
     * it is disabled.
     *
     * @param state true if we should enable two pass rendering
     */
    @Override
    public void enableTwoPassTransparentRendering(boolean state)
    {
        useTwoPassTransparent = state;
    }

    /**
     * Check the state of the two pass transprent rendering flag.
     *
     * @return true if two pass rendering of transparent objects is enabled
     */
    @Override
    public boolean isTwoPassTransparentEnabled()
    {
        return useTwoPassTransparent;
    }

    /**
     * If two pass rendering of transparent objects is enabled, this is the alpha
     * test value used when deciding what to render. The default value is 1.0. No
     * sanity checking is performed, but the value should be between [0,1].
     *
     * @param cutoff The alpha value at which to enable rendering
     */
    @Override
    public void setAlphaTestCutoff(float cutoff)
    {
        alphaCutoff = cutoff;
    }

    /**
     * Get the current value of the alpha test cutoff number. Will always
     * return the currently set number regardless of the state of the
     * two pass rendering flag.
     *
     * @return The currently set cut off value
     */
    @Override
    public float getAlphaTestCutoff()
    {
        return alphaCutoff;
    }

    @Override
    public void setDrawableObjects(GraphicsRequestData otherData,
                                   GraphicsDetails[] nodes,
                                   RenderOp[] renderOps,
                                   int numValid,
                                   GraphicsEnvironmentData[] envData)
    {
        renderableList = nodes;
        operationList = renderOps;

        numRenderables = numValid;
        environmentList = envData;

        if(otherData != null)
        {
            if(otherData.deletionRequests != null)
            {
                int l = otherData.deletionRequests.length;
                otherDataRequests.deletionRequests =
                    new DeletableRenderable[l];
                System.arraycopy(otherData.deletionRequests,
                                 0,
                                 otherDataRequests.deletionRequests,
                                 0,
                                 l);

            }

            if(otherData.shaderInitList != null)
            {
                int l = otherData.shaderInitList.length;
                otherDataRequests.shaderInitList =
                    new ShaderSourceRenderable[l];
                System.arraycopy(otherData.shaderInitList,
                                 0,
                                 otherDataRequests.shaderInitList,
                                 0,
                                 l);

            }

            if(otherData.shaderLogList != null)
            {
                int l = otherData.shaderLogList.length;
                otherDataRequests.shaderLogList =
                    new ShaderSourceRenderable[l];
                System.arraycopy(otherData.shaderLogList,
                                 0,
                                 otherDataRequests.shaderLogList,
                                 0,
                                 l);

            }
        }
    }

    @Override
    public void prepareData(GLContext localContext)
    {
        if(terminate)
        {
            terminateCleanup(localContext);
            return;
        }

        try
        {
            if(ownerRenderable != null)
            {
                ownerRenderable.enable(localContext);
            }

            if(!terminate)
            {
                processRequestData(localContext);
            }

            if(terminate)
            {
                terminateCleanup(localContext);
            }
        }
        catch(GLException ie)
        {
            // Ignore interrupted exceptions, but it probably means we've
            // be shutdown.
            if(ie.getCause() instanceof InterruptedException)
            {
                terminate = true;
            }
            else
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(GL_RENDER_ERROR_PROP);
                errorReporter.errorReport(msg, ie);
            }
        }
    }

    @Override
    public void reinitialize(GLContext localContext)
    {
        ownerRenderable.initialise(localContext );
        init(localContext);
    }

    @Override
    public boolean render(GLContext localContext, GraphicsProfilingData profilingData)
    {
        if(terminate)
        {
            terminateCleanup(localContext);
            return false;
        }

        try
        {
            display(localContext, profilingData);

            if(terminate)
            {
                terminateCleanup(localContext);
            }

            if(ownerRenderable != null)
            {
                ownerRenderable.disable(localContext);
            }
        }
        catch(GLException ie)
        {
            // Ignore interrupted exceptions, but it probably means we've
            // be shutdown.
            if(ie.getCause() instanceof InterruptedException)
                terminate = true;
            else
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(GL_RENDER_ERROR_PROP);
                errorReporter.errorReport(msg, ie);
            }
        }

        return !terminate;
    }

    @Override
    public boolean getSurfaceToVWorld(int x,
                                   int y,
                                   int layer,
                                   int subLayer,
                                   Matrix4f matrix,
                                   String deviceId,
                                   boolean useLastFound)
    {
        GraphicsEnvironmentData data =
            matchEnvData(x, y, layer, subLayer, deviceId, useLastFound);

        if(data == null) {
            return false;
        }

        matrix.set(data.viewTransform);

        return true;
    }

    @Override
    public boolean getPixelLocationInSurface(int x,
                                          int y,
                                          int layer,
                                          int subLayer,
                                          Point3f position,
                                          String deviceId,
                                          boolean useLastFound)
    {
        GraphicsEnvironmentData data =
            matchEnvData(x, y, layer, subLayer, deviceId, useLastFound);

        if(data == null)
        {
            return false;
        }

        if(data.viewProjectionType ==
           ViewEnvironmentCullable.PERSPECTIVE_PROJECTION)
        {
            float width = data.viewport[ViewEnvironmentCullable.VIEW_WIDTH];
            float height = data.viewport[ViewEnvironmentCullable.VIEW_HEIGHT];

            int local_x = data.viewport[ViewEnvironmentCullable.VIEW_X];
            int local_y = data.viewport[ViewEnvironmentCullable.VIEW_Y];

            x = x - local_x;
            y = y - local_y;

            position.x = (float)((data.viewFrustum[1] - data.viewFrustum[0]) *
                         (x / width - 0.5f));
            position.y = (float) ((data.viewFrustum[3] - data.viewFrustum[2]) *
                         (y / height - 0.5f));
            position.z = (float) -data.viewFrustum[4];
        }
        else if(data.viewProjectionType ==
                ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION)
        {
            int local_x = data.viewport[ViewEnvironmentCullable.VIEW_X];
            int local_y = data.viewport[ViewEnvironmentCullable.VIEW_Y];
            float width = data.viewport[ViewEnvironmentCullable.VIEW_WIDTH];
            float height = data.viewport[ViewEnvironmentCullable.VIEW_HEIGHT];

            x = x - local_x;
            y = y - local_y;

            position.x = (float) data.viewFrustum[0] +
                         (float)((data.viewFrustum[1] - data.viewFrustum[0]) *
                         (x / width));
            position.y = (float) data.viewFrustum[2] +
                          (float) ((data.viewFrustum[3] - data.viewFrustum[2])
                          * (y / height));
            position.z = (float) -data.viewFrustum[4];
        }

        return true;
    }

    @Override
    public boolean getCenterEyeInSurface(int x,
                                      int y,
                                      int layer,
                                      int subLayer,
                                      Point3f position,
                                      String deviceId,
                                      boolean useLastFound)
    {
        GraphicsEnvironmentData data =
            matchEnvData(x, y, layer, subLayer, deviceId, useLastFound);

        if(data == null)
            return false;

        if(data.viewProjectionType ==
           ViewEnvironmentCullable.PERSPECTIVE_PROJECTION)
        {
            position.x = data.eyeOffset[0];
            position.y = data.eyeOffset[1];
            position.z = data.eyeOffset[2];
        }
        else if(data.viewProjectionType ==
                ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION)
        {
            // Ignore eye offset for ortho projection

            int local_x = data.viewport[ViewEnvironmentCullable.VIEW_X];
            int local_y = data.viewport[ViewEnvironmentCullable.VIEW_Y];
            float width = data.viewport[ViewEnvironmentCullable.VIEW_WIDTH];
            float height = data.viewport[ViewEnvironmentCullable.VIEW_HEIGHT];

            x = x - local_x;
            y = y - local_y;

            position.x = (float) data.viewFrustum[0] +
                         (float)((data.viewFrustum[1] - data.viewFrustum[0]) *
                         (x / width));
            position.y = (float) data.viewFrustum[2] + (float)
                         ((data.viewFrustum[3] - data.viewFrustum[2]) *
                         (y / height));
            position.z = 0;
        }

        return true;
    }

    @Override
    public void setOwnerBuffer(BaseBufferDescriptor desc)
    {
        if(desc == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_BUF_DESC_PROP);

            throw new IllegalArgumentException(msg);
        }

        ownerRenderable = desc;
    }

    @Override
    public BaseBufferDescriptor getOwnerBuffer()
    {
        return ownerRenderable;
    }

    @Override
    public void addChildBuffer(OffscreenBufferRenderable rend,
                               RenderingProcessor proc)
    {
        if(removedBuffers.contains(rend))
        {
            removedBuffers.remove(rend);
        }
        else if(!addedBuffers.contains(rend) && !childBuffers.containsKey(rend))
        {
            addedBuffers.add(rend);
            addedProcessors.add(proc);

            proc.setOwnerBuffer(new FBODescriptor(rend));
        }
    }

    @Override
    public void updateChildBuffer(OffscreenBufferRenderable rend)
    {
        // In the update case, we want to force the removal of the old buffer
        // first and then add it in later.
        BaseBufferDescriptor desc = childBuffers.get(rend);

        if((desc == null) || !desc.isInitialised())
            return;

        if(!updatedBuffers.contains(rend) && !addedBuffers.contains(rend))
            updatedBuffers.add(rend);

        if(removedBuffers.contains(rend))
            removedBuffers.remove(rend);
    }

    @Override
    public void removeChildBuffer(OffscreenBufferRenderable rend)
    {
        if(addedBuffers.contains(rend))
        {
            int idx = addedBuffers.indexOf(rend);
            addedBuffers.remove(rend);
            addedProcessors.remove(idx);
        }
        else if(childBuffers.containsKey(rend))
        {
            removedBuffers.add(rend);
        }
    }

    @Override
    public void addSurfaceInfoListener(SurfaceInfoListener l)
    {
        surfaceListeners =
            SurfaceInfoListenerMulticaster.add(surfaceListeners, l);
    }

    @Override
    public void removeSurfaceInfoListener(SurfaceInfoListener l)
    {
        surfaceListeners =
            SurfaceInfoListenerMulticaster.remove(surfaceListeners, l);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Called for each rendering loop.The derived class should now perform the
     * rendering that they need to do for the given type of renderer. The
     * context is made current before this call and is made non current after
     * this call. Derived code does not need to worry about context state
     * handling.
     *
     * @param localContext
     * @param profilingData The timing and load data
     */
    protected abstract void display(GLContext localContext, GraphicsProfilingData profilingData);

    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized or has changed; the GLContext has already been made
     * current when this method is called.
     *
     * @param localContext The GL context to do the drawing with
     */
    protected void init(GLContext localContext)
    {
        GL base_gl = localContext.getGL();
        GL2 gl = base_gl.getGL2();

        gl.glClearColor(clearColor[0],
                        clearColor[1],
                        clearColor[2],
                        clearColor[3]);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL.GL_SCISSOR_TEST);

        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);

        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glHint(GL2.GL_FOG_HINT, GL.GL_NICEST);

        // Find out the number of available lights and clip planes and
        // initialise the arrays to that length.

        int[] num_id = new int[1];

        gl.glGetIntegerv(GL2.GL_MAX_CLIP_PLANES, num_id, 0);

        int num_clips = num_id[0];

        availableClips = new Integer[num_id[0]];
        for(int i = 0; i < num_id[0]; i++)
            availableClips[i] = GL2.GL_CLIP_PLANE0 + i;

        gl.glGetIntegerv(GL2.GL_MAX_LIGHTS, num_id, 0);
        int num_lights = num_id[0];

        // GL_LIGHT0 is assumed to be used by the current viewpoint
        availableLights = new Integer[num_id[0]];

        for(int i = 0; i < num_id[0]; i++)
            availableLights[i] = GL2.GL_LIGHT1 + i;

        initComplete = true;

        // Send out events to any listeners
        if(surfaceListeners != null)
        {
            int num_textures;
            int num_mrts;
            int num_color_atch;
            int gl_major_version = 0;
            int gl_minor_version = 0;
            int sl_major_version = 0;
            int sl_minor_version = 0;

            HashSet<String> extensions = new HashSet<>();

            String gl_version = gl.glGetString(GL.GL_VERSION);
            String gl_vendor = gl.glGetString(GL.GL_VENDOR);
            String gl_renderer = gl.glGetString(GL.GL_RENDERER);
            String shader_version = gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION);
            String gl_ext = gl.glGetString(GL.GL_EXTENSIONS);

            if (gl_ext != null)
            {
                StringTokenizer strtok = new StringTokenizer(gl_ext);

                while(strtok.hasMoreTokens())
                    extensions.add(strtok.nextToken());
            }

            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, num_id, 0);
            num_textures = num_id[0];

            gl.glGetIntegerv(GL2.GL_MAX_DRAW_BUFFERS, num_id, 0);
            num_mrts = num_id[0];

            gl.glGetIntegerv(GL_MAX_COLOR_ATTACHMENTS, num_id, 0);
            num_color_atch = num_id[0];

            if (gl_version != null)
            {

                StringTokenizer strtok = new StringTokenizer(gl_version, ". ");
                String gl_major_str = strtok.nextToken();
                String gl_minor_str = strtok.nextToken();

                try
                {
                    gl_major_version = Integer.parseInt(gl_major_str);
                }
                catch(NumberFormatException nfe)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(GL_MAJOR_VER_PROP);

                    errorReporter.errorReport(msg, nfe);
                }

                try
                {
                    gl_minor_version = Integer.parseInt(gl_minor_str);
                }
                catch(NumberFormatException nfe)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(GL_MINOR_VER_PROP);
                    errorReporter.errorReport(msg, nfe);
                }

            }

            if (shader_version != null)
            {

                StringTokenizer strtok = new StringTokenizer(shader_version, ". ");
                String sl_major_str = strtok.nextToken();
                String sl_minor_str = strtok.nextToken();

                try
                {
                    sl_major_version = Integer.parseInt(sl_major_str);
                }
                catch(NumberFormatException nfe)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(SHADER_MAJOR_VER_PROP);

                    errorReporter.errorReport(msg, nfe);
                }

                try
                {
                    sl_minor_version = Integer.parseInt(sl_minor_str);
                }
                catch(NumberFormatException nfe)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(SHADER_MINOR_VER_PROP);
                    errorReporter.errorReport(msg, nfe);
                }

            }

            try
            {
                SurfaceInfo info =
                    new AV3DSurfaceInfo(gl_major_version,
                                        gl_minor_version,
                                        sl_major_version,
                                        sl_minor_version,
                                        gl_vendor,
                                        gl_renderer,
                                        num_lights,
                                        num_textures,
                                        num_clips,
                                        num_mrts,
                                        num_color_atch,
                                        extensions);

                surfaceListeners.surfaceInfoChanged(ownerDevice, info);
            }
            catch(Exception e)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(SURFACE_LIST_ERR_PROP);
                errorReporter.errorReport(msg , e);
            }
        }

        int renderableCount = numRenderables;
        RenderOp[] opList = operationList;
        GraphicsDetails[] renderables = renderableList;

        for(int i = 0; i < renderableCount && !terminate; i++)
        {
            switch(opList[i])
            {
                case START_SHADER_PROGRAM:
                    ShaderComponentRenderable prog = (ShaderComponentRenderable)renderables[i].renderable;

                    if(!prog.isValid(gl))
                    {
                        prog.reinitialize(gl);
                    }

                default:
                    // do nothing

            }
        }
    }

    /**
     * Process the shader and delete requests for this scene now. Should
     * normally be called at the start of the frame to ensure IDs are deleted
     * up front before being reallocated elsewhere.
     *
     * @param context The GL context to process the requests with
     */
    protected void processRequestData(GLContext context)
    {
        processChildBuffers(context);

        if(otherDataRequests == null)
            return;

        // Process the deleted nodes
        GL gl = context.getGL();
        GL2 gl2 = gl.getGL2();

        if(otherDataRequests.deletionRequests != null)
        {
            for(int i = 0;
                i < otherDataRequests.deletionRequests.length && ! terminate;
                i++)
            {
                otherDataRequests.deletionRequests[i].cleanup(gl2);
                otherDataRequests.deletionRequests[i] = null;
            }

            otherDataRequests.deletionRequests = null;
        }

        // Process the shader init calls and then log requests
        if(otherDataRequests.shaderInitList != null)
        {
            for(int i = 0;
                i < otherDataRequests.shaderInitList.length && !terminate;
                i++)
            {
                otherDataRequests.shaderInitList[i].initialize(gl2);
                otherDataRequests.shaderInitList[i] = null;
            }

            otherDataRequests.shaderInitList = null;
        }

        if(otherDataRequests.shaderLogList != null)
        {
            for(int i = 0;
                i < otherDataRequests.shaderLogList.length && !terminate;
                i++)
            {
                otherDataRequests.shaderLogList[i].fetchLogInfo(gl2);
                otherDataRequests.shaderLogList[i] = null;
            }

            otherDataRequests.shaderLogList = null;
        }
    }

    /**
     * Process the child buffer additions and removals.
     */
    private void processChildBuffers(GLContext localContext)
    {
        removedBuffers.forEach(rend -> {
            BaseBufferDescriptor desc = childBuffers.remove(rend);
            rend.unregisterBuffer(localContext);
            desc.delete(localContext);
        });

        for(int i = 0; i < addedBuffers.size(); i++)
        {
            OffscreenBufferRenderable rend = addedBuffers.get(i);
            RenderingProcessor proc = addedProcessors.get(i);

            // We know it is always FBO since those are the only
            // child types we support now.
            FBODescriptor desc = (FBODescriptor)proc.getOwnerBuffer();
            childBuffers.put(rend, desc);

            registerBuffer(localContext, desc, rend);
        }

        updatedBuffers.stream().map(rend -> childBuffers.get(rend)).forEachOrdered(desc -> {
            desc.resize(localContext);
        });

        addedBuffers.clear();
        addedProcessors.clear();
        updatedBuffers.clear();
        removedBuffers.clear();
    }

    /**
     * If termination has been requested during the last call to
     * the display loop, this method is called to destroy and cleanup
     * the context instance. Once called, this instance can no longer be
     * used.
     *
     * @param localContext The GL context to clean up with
     */
    protected void terminateCleanup(GLContext localContext)
    {
        if(contextNotDestroyed)
        {
            contextNotDestroyed = false;
            ownerRenderable.disable(localContext);
            ownerRenderable.delete(localContext);
        }
    }

    /**
     * Setup the viewport environment to be drawn, but do not yet set up the
     * viewpoint and other per-layer-specific effects. If a viewport has
     * multiple layers, then each layer could potentially have a different
     * viewpoint etc.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void setupViewport(GL2 gl, GraphicsEnvironmentData data)
    {
        gl.glViewport(data.viewport[GraphicsEnvironmentData.VIEW_X],
                      data.viewport[GraphicsEnvironmentData.VIEW_Y],
                      data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                      data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT]);
        gl.glScissor(data.scissor[GraphicsEnvironmentData.VIEW_X],
                     data.scissor[GraphicsEnvironmentData.VIEW_Y],
                     data.scissor[GraphicsEnvironmentData.VIEW_WIDTH],
                     data.scissor[GraphicsEnvironmentData.VIEW_HEIGHT]);
    }

    /**
     * Setup the viewport environment to be drawn for a multipass rendering.
     * The difference between this and the normal viewport setup is that this
     * always assumes starting at 0,0 and just uses the width and height to
     * setup the bounds. It is assuming rendering to one of the auxillary
     * buffers rather than the main back buffer.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void setupMultipassViewport(GL2 gl, GraphicsEnvironmentData data)
    {
        gl.glViewport(0,
                      0,
                      data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                      data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT]);
        gl.glScissor(data.scissor[GraphicsEnvironmentData.VIEW_X],
                     data.scissor[GraphicsEnvironmentData.VIEW_Y],
                     data.scissor[GraphicsEnvironmentData.VIEW_WIDTH],
                     data.scissor[GraphicsEnvironmentData.VIEW_HEIGHT]);
    }

    /**
     * Setup the view environment for a specific layer for drawing now. This
     * calls the render effects processor, draws the background, initiates the
     * viewpoint, frustum etc.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void preLayerEnvironmentDraw(GL2 gl, GraphicsEnvironmentData data)
    {
        if(data.effectsProcessor != null)
            data.effectsProcessor.preDraw(gl, data.userData);

        if(data.background != null)
        {
            gl.glDepthMask(false);
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_DEPTH_TEST);

            // If it is colour only, then don't bother with rest of the
            // the projection setup. It will be wasted.
            if(((BackgroundRenderable)data.background).is2D())
            {
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

                data.background.render(gl);
                data.background.postRender(gl);
            }
            else
            {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPushMatrix();
                gl.glLoadIdentity();

                gl.glFrustum(data.backgroundFrustum[0],
                             data.backgroundFrustum[1],
                             data.backgroundFrustum[2],
                             data.backgroundFrustum[3],
                             data.backgroundFrustum[4],
                             data.backgroundFrustum[5]);

                gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPushMatrix();

                gl.glLoadMatrixf(data.backgroundTransform, 0);

                data.background.render(gl);
                data.background.postRender(gl);

                gl.glPopMatrix();

                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
                gl.glMatrixMode(GL2.GL_MODELVIEW);
            }
        }

        // Always want to clean up here and make sure that we have the basics
        // enabled for the start of a rendering run. So, even though we turned
        // it off inside the if statement above, we turn it back on outside
        // here because the previous execution may have left everything in a
        // bad state - particularly if someone was using a preprocessor and
        // didn't clean up after themselves properly.
        gl.glDepthMask(true);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_BLEND);

        renderViewpoint(gl, data);

        if(data.fog != null)
        {
            gl.glEnable(GL2.GL_FOG);
            data.fog.render(gl);
        }
    }

    /**
     * Complete the view environment setup at the end of the layer. This
     * disables any fog, the current viewpoint and the post draw action on the
     * render effects processor.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     * @param profilingData The timing and load data
     */
    protected void postLayerEnvironmentDraw(GL2 gl,
                                            GraphicsEnvironmentData data,
                                            GraphicsProfilingData profilingData)
    {
        if(data.fog != null)
        {
            data.fog.postRender(gl);
            gl.glDisable(GL2.GL_FOG);
        }

        if(terminate)
            return;

        if(data.effectsProcessor != null)
            data.effectsProcessor.postDraw(gl, profilingData, data.userData);

        if(terminate)
            return;

        if(data.viewpoint != null)
            data.viewpoint.postRender(gl);
    }

    /**
     * Setup the view environment for a specific pass of a multipass rendering
     * for drawing now. This calls the multipass observer, and initiates the
     * viewpoint, frustum etc. Backgrounds are not drawn.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void preMPPassEnvironmentDraw(GL2 gl,
                                            GraphicsEnvironmentData data)
    {
//        if(data.effectsProcessor != null)
//            data.effectsProcessor.preDraw(gl, data.userData);

        renderViewpoint(gl, data);

        if(data.fog != null)
        {
            gl.glEnable(GL2.GL_FOG);
            data.fog.render(gl);
        }
    }

    /**
     * Complete the view environment setup at the end of the layer. This
     * disables any fog, the current viewpoint and the post draw action on the
     * render effects processor.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void postMPPassEnvironmentDraw(GL2 gl,
                                             GraphicsEnvironmentData data)
    {
        if(data.fog != null)
        {
            data.fog.postRender(gl);
            gl.glDisable(GL2.GL_FOG);
        }

        if(terminate)
            return;

//        if(data.effectsProcessor != null)
//            data.effectsProcessor.postDraw(gl, data.userData);

//        if(terminate)
//            return;

        if(data.viewpoint != null)
        {
            data.viewpoint.postRender(gl);
        }
    }

    /**
     * Render the viewpoint setup.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void renderViewpoint(GL2 gl, GraphicsEnvironmentData data)
    {
        // Will want to check here for the VP being non-live.
        if(data.viewpoint == null)
        {
            return;
        }

        updateProjectionMatrix(gl, data);

        gl.glLoadIdentity();

        data.viewpoint.render(gl);
        gl.glMultMatrixf(data.cameraTransform, 0);
    }

    /**
     * Update the projection matrix.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    protected void updateProjectionMatrix(GL2 gl, GraphicsEnvironmentData data)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        switch(data.viewProjectionType)
        {
            case ViewEnvironmentCullable.PERSPECTIVE_PROJECTION:
                 gl.glFrustum(data.viewFrustum[0],
                              data.viewFrustum[1],
                              data.viewFrustum[2],
                              data.viewFrustum[3],
                              data.viewFrustum[4],
                              data.viewFrustum[5]);
                break;

            case ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION:
                 gl.glOrtho(data.viewFrustum[0],
                            data.viewFrustum[1],
                            data.viewFrustum[2],
                            data.viewFrustum[3],
                            data.viewFrustum[4],
                            data.viewFrustum[5]);
                break;

            // Apply user specified custom projection matrix.
            case ViewEnvironmentCullable.CUSTOM_PROJECTION:
                gl.glLoadMatrixf(data.projectionMatrix, 0);
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROJECTION_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { data.viewProjectionType };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                errorReporter.warningReport(msg, null);
        }

        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    /**
     * Internal convenience method that takes a layer ID, sublayer Id and the
     * x,y surface coordinates and locates the GraphicsEnvironmentData instance
     * that corresponds to it. If there is none, then null is returned.
     *
     * @param x The X coordinate on the whole surface
     * @param y The Y coordinate on the whole surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. Typically 0
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     */
    private GraphicsEnvironmentData matchEnvData(int x,
                                                 int y,
                                                 int layer,
                                                 int subLayer,
                                                 String deviceId,
                                                 boolean useLastFound)
    {
        // Local copy to allow for proper dealing with asynchronous
        // interactions. This could be called by user code and mid call the
        // pipeline decides to send another set of updates. This ensures that
        // the boat doesn't change mid-check on us.
        if((environmentList == null) || (environmentList.length == 0))
            return null;

        GraphicsEnvironmentData ret_val = null;

        // If we should be using the one found last time this layer was
        // requested check for it first.
        if(useLastFound)
        {
            GraphicsEnvironmentData data = envSaveMap.get(deviceId);
            if(data.layerId == layer && data.subLayerId == subLayer)
                ret_val = data;
        }


        if(ret_val == null)
        {
            GraphicsEnvironmentData[] data = environmentList;

            for (GraphicsEnvironmentData ged : data)
            {
                if (ged != null &&
                    ged.layerId == layer && ged.subLayerId == subLayer &&
                    x >= ged.viewport[0] && y >= ged.viewport[1] &&
                    (x < ged.viewport[0] + ged.viewport[2]) &&
                    (y < ged.viewport[1] + ged.viewport[3]))
                {
                    ret_val = ged;
                    envSaveMap.put(deviceId, ged);
                    break;
                }
            }
        }

        return ret_val;
    }

    /**
     * Register an offscreen buffer now that we have a valid context. This will ensure
     * the right construction is made.
     *
     * @param localContext The GL context this is to be registered with
     * @param parentDesc The FBO descriptor that owns this buffer
     * @param renderable The renderable that defines what we need to have
     *   for the buffer
     */
    private void registerBuffer(GLContext localContext,
                                FBODescriptor parentDesc,
                                OffscreenBufferRenderable renderable)
    {
        // Note that since we are treating all offscreens as a flat list under
        // the root canvas, they all use the root GLContext instance as the
        // parent, regardless of whether the buffer is nested deeper.

        BufferSetupData buffer_data = renderable.getBufferSetup();

        for(int i = 1; i < buffer_data.getNumRenderTargets(); i++)
        {
            OffscreenRenderTargetRenderable kid_render =
                renderable.getRenderTargetRenderable(i);

            BaseBufferDescriptor kid_desc =
                parentDesc.getChildDescriptor(i);

            kid_render.registerBuffer(localContext, kid_desc);
        }

        if(renderable.hasSeparateDepthRenderable())
        {
            OffscreenRenderTargetRenderable kid_render =
                renderable.getDepthRenderable();

            BaseBufferDescriptor kid_desc =
                parentDesc.getDepthDescriptor();

            kid_render.registerBuffer(localContext, kid_desc);
        }

        // No pbuffers at all? Uh oh, we're in trouble now. Exit nicely.
        // TODO: Look at implementing some sort of multipass rendering
        // implementation that uses the normal buffers and copies them through
        // to the offscreen texture.

        renderable.registerBuffer(localContext, parentDesc);
    }
}
