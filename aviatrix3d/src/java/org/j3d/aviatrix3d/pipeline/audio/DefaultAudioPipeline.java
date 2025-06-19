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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.LayerCullable;
import org.j3d.aviatrix3d.rendering.ProfilingData;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * The default implementation of the audio pipeline usable by most
 * applications.
 * <p>
 * This implementation is targeted towards single threaded architectures.
 * After setting the stages, the render command will not return until
 * everything is complete.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.9 $
 */
public class DefaultAudioPipeline implements AudioRenderPipeline
{
    /** The culling stage to be used */
    private AudioCullStage culler;

    /** The sorting stage to be used */
    private AudioSortStage sorter;

    /** Surface to draw to */
    private AudioOutputDevice device;

    /** The listener to pass cull to sort */
    private AudioCullToSingleSortListener ctsListener;

    /** The listener to pass sort to the drawable listener */
    private AudioSortToSingleDeviceListener stdListener;

    /** The list of layers this pipeline manages */
    private LayerCullable[] layers;

    /** The number of layers to process */
    private int numLayers;

    /** Time to stop the pipeline */
    private boolean terminate;

    /**
     * Other data to send down the pipe. Is only non-null between
     * the various set requests and the next render() or displayOnly()
     * calls. A new instance is created each time it is sent down the
     * pipeline.
     */
    private RenderableRequestData otherData;

    /** Local reporter to put errors in */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the pipeline with nothing registered.
     */
    public DefaultAudioPipeline()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        ctsListener = new AudioCullToSingleSortListener();
        stdListener = new AudioSortToSingleDeviceListener();
        terminate = false;
        layers = new LayerCullable[1];
    }

    /**
     * Construct a pipeline with the sort and cull stages provided.
     *
     * @param ss The sort stage instance to use
     * @param cs The cull stage instance to use
     */
    public DefaultAudioPipeline(AudioCullStage cs, AudioSortStage ss)
    {
        this();

        culler = cs;
        sorter = ss;

        if(cs != null)
            cs.setCulledAudioReceiver(ctsListener);

        if(ss != null)
        {
            ctsListener.setSorter(ss);
            ss.setSortedAudioReceiver(stdListener);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by AudioRenderPipeline
    //---------------------------------------------------------------

    /**
     * Register a drawing surface that this pipeline will send its output to.
     * Setting a null value will remove the current drawable surface.
     *
     * @param device The audio output device instance to use or replace
     */
    @Override
    public void setAudioOutputDevice(AudioOutputDevice device)
    {
        stdListener.setDevice(device);

        this.device = device;
    }

    /**
     * Get the currently registered drawable device instance. If none is set,
     * return null.
     *
     * @return The currently set surface instance or null
     */
    @Override
    public AudioOutputDevice getAudioOutputDevice()
    {
        return device;
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

    @Override
    public void setRequestData(RenderableRequestData data)
    {
        otherData = data;
    }

    @Override
    public boolean render()
    {
        if(numLayers == 0)
            return true;

        boolean draw_state = true;

        ProfilingData profilingData = new ProfilingData();

        if(culler != null)
        {
            culler.cull(otherData, profilingData, layers, numLayers);
            otherData = null;

            // then draw after the cull is complete
            if(!terminate && device != null)
                draw_state = device.draw(profilingData);
        }

        return draw_state;
    }

    @Override
    public boolean displayOnly()
    {
        boolean draw_state = true;
        ProfilingData profilingData = new ProfilingData();

        if(!terminate && (numLayers != 0) && (device != null))
            draw_state = device.draw(profilingData);

        return draw_state;
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

        if(device != null)
            device.dispose();
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
    public void setSorter(AudioSortStage ss)
    {
        ctsListener.setSorter(ss);

        if(sorter != null)
            sorter.setSortedAudioReceiver(null);

        if(ss != null)
            ss.setSortedAudioReceiver(stdListener);

        sorter = ss;
    }

    /**
     * Set the cull instance to be used. If the instance is null, the current
     * culler is removed.
     *
     * @param cs The cull instance to use or null
     */
    public void setCuller(AudioCullStage cs)
    {
        if(culler != null)
            culler.setCulledAudioReceiver(null);

        if(cs != null)
            cs.setCulledAudioReceiver(ctsListener);

        culler = cs;
    }
}
