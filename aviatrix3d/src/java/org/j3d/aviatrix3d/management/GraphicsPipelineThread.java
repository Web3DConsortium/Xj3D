/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.management;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

/** NOTE: Not used
 * Wrapper around a single pipeline that controls its management in a separate
 * thread.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class GraphicsPipelineThread extends Thread
{
    /** flag to tell the thread to exit now */
    private boolean terminate;

    /** The pipeline we're managing */
    private GraphicsRenderPipeline pipeline;

    /** Callback to tell the system we've finished this render */
    private PipelineStateObserver observer;

    /** Lock object used to control when to run the display */
    private final Object displayLock;

    /** Indicate that this is waiting for management to complete */
    private boolean waiting;

    /** Flag to decide whether we should only display the last frame */
    private boolean noProcessing;

    /**
     * Construct an instance that manages the given pipeline.
     */
    GraphicsPipelineThread(GraphicsRenderPipeline rp)
    {
        super("Aviatrix3D multithreaded render pipeline");

        pipeline = rp;
        terminate = false;
        waiting = false;
        noProcessing = false;
        displayLock = new Object();
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Endless loop that does the display calls asynchronously.
     */
    @Override
    public void run()
    {
        boolean valid_drawable = true;

        while(!terminate && valid_drawable)
        {
            try
            {
                boolean draw_failed;

                if(noProcessing)
                    draw_failed = pipeline.displayOnly();
                else
                    draw_failed = pipeline.render();

                if(terminate)
                    break;

                if(draw_failed)
                {
                    GraphicsOutputDevice dev =
                        pipeline.getGraphicsOutputDevice();
                    if(dev.isDisposed())
                        valid_drawable = false;
                }

                if(terminate)
                    break;

                synchronized(displayLock)
                {
                    if(observer != null)
                        observer.frameFinished();

                    if(terminate)
                        break;

                    displayLock.wait();
                }

                Thread.yield();
            }
            catch(InterruptedException ie)
            {
            }

            if(terminate)
                break;

            pipeline.swapBuffers();
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    void halt()
    {
        pipeline.halt();

        try
        {
            synchronized(displayLock)
            {
                if(observer != null)
                    observer.frameFinished();

                displayLock.wait();
            }
        }
        catch(InterruptedException ie)
        {
        }
    }

    /**
     * Instruct the pipeline to only display and not do any processing.
     */
    void displayOnly()
    {
        noProcessing = true;

        synchronized(displayLock)
        {
            displayLock.notify();
        }
    }

    /**
     * Request the contained pipeline renders now. If the pipeline is currently
     * processing a previous display request, this is ignored.
     */
    void render()
    {
        noProcessing = false;

        synchronized(displayLock)
        {
            displayLock.notify();
        }
    }

    /**
     * Force a shutdown of the pipeline now. If the display is currently
     * processing, the current processing finishes before the thread
     * terminates.
     */
    void shutdown()
    {
        terminate = true;
        GraphicsOutputDevice surface = pipeline.getGraphicsOutputDevice();
        surface.dispose();
        pipeline.halt();

        synchronized(displayLock)
        {
            displayLock.notify();
        }
    }

    /**
     * Set the observer instance used to watch for when this pipeline has
     * finished management.
     *
     * @param pso
     */
    void setStateObserver(PipelineStateObserver pso)
    {
        observer = pso;
    }
}
