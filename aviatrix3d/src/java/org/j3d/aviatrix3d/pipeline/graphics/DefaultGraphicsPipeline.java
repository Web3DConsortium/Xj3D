/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.rendering.LayerCullable;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * The default implementation of the rendering pipeline usable by most
 * applications.
 * <p>
 * This implementation is targeted towards single threaded architectures.
 * After setting the stages, the render command will not return until
 * everything is complete.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.10 $
 */
public class DefaultGraphicsPipeline implements GraphicsRenderPipeline
{
    /** The culling stage to be used */
    private GraphicsCullStage culler;

    /** The sorting stage to be used */
    private GraphicsSortStage sorter;

    /** Surface to draw to */
    private GraphicsOutputDevice drawable;

    /** The listener to pass cull to sort */
    private CullToSingleSortListener ctsListener;

    /** The listener to pass sort to the drawable listener */
    private SortToSingleDrawListener stdListener;

    /** The list of layers this pipeline manages */
    private LayerCullable[] layers;

    /** The number of layers to process */
    private int numLayers;

    /** Storage variable for the screen orientation values */
    private float[] screenOrientation;

    /** Storage variable for the eye offset values */
    private float[] eyePoint;

    /** Flag to say explicit screen orientation values have been provided */
    private boolean useOrientation;

    /** Flag to say explicit eyepoint values have been provided */
    private boolean useEyePoint;

    /** Time to stop the pipeline */
    private boolean terminate;

    /**
     * Other data to send down the pipe. Is only non-null between
     * the various set requests and the next render() or displayOnly()
     * calls. A new instance is created each time it is sent down the
     * pipeline.
     */
    private GraphicsRequestData otherData;

    /** Local reporter to put errors in */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the pipeline with nothing registered.
     */
    public DefaultGraphicsPipeline()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        ctsListener = new CullToSingleSortListener();
        stdListener = new SortToSingleDrawListener();

        screenOrientation = new float[4];
        eyePoint = new float[3];

        terminate = false;
        useEyePoint = false;
        useOrientation = false;
        layers = new LayerCullable[1];
    }

    /**
     * Construct a pipeline with the sort and cull stages provided.
     *
     * @param ss The sort stage instance to use
     * @param cs The cull stage instance to use
     */
    public DefaultGraphicsPipeline(GraphicsCullStage cs, GraphicsSortStage ss)
    {
        this();

        culler = cs;
        sorter = ss;

        if(cs != null)
            cs.setCulledGeometryReceiver(ctsListener);

        if(ss != null)
        {
            ctsListener.setSorter(ss);
            ss.setSortedGeometryReceiver(stdListener);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsRenderPipeline
    //---------------------------------------------------------------

    @Override
    public void setGraphicsOutputDevice(GraphicsOutputDevice device)
    {
        stdListener.setGraphicsOutputDevice(device);

        drawable = device;
    }

    @Override
    public GraphicsOutputDevice getGraphicsOutputDevice()
    {
        return drawable;
    }

    @Override
    public void setEyePointOffset(float x, float y, float z)
    {
        if(culler != null)
            culler.setEyePointOffset(x, y, z);

        useEyePoint = true;
        eyePoint[0] = x;
        eyePoint[1] = y;
        eyePoint[2] = z;
    }

    @Override
    public void setScreenOrientation(float x, float y, float z, float a)
        throws IllegalArgumentException
    {
        if(culler != null)
            culler.setScreenOrientation(x, y, z, a);

        useOrientation = true;
        screenOrientation[0] = x;
        screenOrientation[1] = y;
        screenOrientation[2] = z;
        screenOrientation[3] = a;
    }

    //---------------------------------------------------------------
    // Methods defined by RenderPipeline
    //---------------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter;

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /*
     * This class expects the data to be Graphics-oriented.
     */
    @Override
    public void setRequestData(RenderableRequestData data)
    {
        otherData = (GraphicsRequestData)data;
    }

    @Override
    public boolean render()
    {
        if(numLayers == 0)
            return true;

        boolean draw_state = true;

        GraphicsProfilingData profilingData = new GraphicsProfilingData();

        if(culler != null)
        {
            culler.cull(otherData, profilingData, layers, numLayers);
            otherData = null;

            // then draw after the cull is complete
            if(!terminate && drawable != null)
                draw_state = drawable.draw(profilingData);

            // If failed, check to see if the underlying surface died.
            // If so, continue to pass on the failure message.
            if(!draw_state && !drawable.isDisposed())
                draw_state = true;

        }

        return draw_state && !terminate;
    }

    @Override
    public boolean displayOnly()
    {
        boolean draw_state = true;
        GraphicsProfilingData profilingData = new GraphicsProfilingData();

        if(!terminate && (numLayers != 0) && (drawable != null))
            draw_state = drawable.draw(profilingData);

        if(!draw_state && !drawable.isDisposed())
            draw_state = true;

        return draw_state;
    }

    @Override
    public void swapBuffers()
    {
        if(!terminate && drawable != null)
            drawable.swap();
    }

    @Override
    public void setRenderableLayers(LayerCullable[] layers, int numLayers)
    {
        if(layers != null)
        {
            if(this.layers.length < numLayers)
                this.layers = new LayerCullable[numLayers];

            System.arraycopy(layers, 0, this.layers, 0, numLayers);
        }

        this.numLayers = numLayers;
    }

    @Override
    public void halt()
    {
        terminate = true;

        if(culler != null)
            culler.halt();

        if(sorter != null)
            sorter.halt();

        // JOGL resources and renderer
        if(drawable != null)
            drawable.dispose();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the sorter instance to be used. If the instance is null, the current
     * sorter is removed.
     *
     * @param ss The sorter instance to use or null
     */
    public void setSorter(GraphicsSortStage ss)
    {
        ctsListener.setSorter(ss);

        if(sorter != null)
            sorter.setSortedGeometryReceiver(null);

        if(ss != null)
            ss.setSortedGeometryReceiver(stdListener);

        sorter = ss;
    }

    /**
     * Set the cull instance to be used. If the instance is null, the current
     * culler is removed.
     *
     * @param cs The cull instance to use or null
     */
    public void setCuller(GraphicsCullStage cs)
    {
        if(culler != null)
            culler.setCulledGeometryReceiver(null);

        if(cs != null)
        {
            cs.setCulledGeometryReceiver(ctsListener);
            if(useEyePoint)
                cs.setEyePointOffset(eyePoint[0], eyePoint[1], eyePoint[2]);

            if(useOrientation)
                cs.setScreenOrientation(screenOrientation[0],
                                        screenOrientation[1],
                                        screenOrientation[2],
                                        screenOrientation[3]);
        }

        culler = cs;
    }
}
