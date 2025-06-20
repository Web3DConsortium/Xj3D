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

package org.j3d.renderer.aviatrix3d.swt.draw2d;

// External imports
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLContext;

import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.j3d.opengl.swt.draw2d.GLFigure;

// Local imports
import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.output.graphics.DebugRenderingProcessor;

/**
 * Implementation of the most basic drawable surface using Draw2D figure.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class DebugDraw2DSurface extends BaseDraw2DSurface
    implements KeyListener
{
    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param device The screen device context that holds the parent component.
     *    Must not be null
     */
    public DebugDraw2DSurface(Display device)
    {
        this(device, null, null, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param device The screen device context that holds the parent component.
     *    Must not be null
     * @param caps A set of required capabilities for this canvas.
     */
    public DebugDraw2DSurface(Display device, GLCapabilities caps)
    {
        this(device, caps, null, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param device The screen device context that holds the parent component.
     *    Must not be null
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public DebugDraw2DSurface(Display device,
                              GLCapabilities caps,
                              GLCapabilitiesChooser chooser)
    {
        this(device, caps, chooser, null);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param device The screen device context that holds the parent component.
     *    Must not be null
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public DebugDraw2DSurface(Display device,
                              GLCapabilities caps,
                              BaseSurface sharedWith)
    {
        this(device, caps, null, sharedWith);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param device The screen device context that holds the parent component.
     *    Must not be null
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public DebugDraw2DSurface(Display device,
                              GLCapabilities caps,
                              GLCapabilitiesChooser chooser,
                              BaseSurface sharedWith)
    {
        super(sharedWith);

        init(device, caps, chooser);
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

    /**
     * Notification of a key press event. When the 'd' key is pressed, dump
     * the next frame to stdout.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyPressed(KeyEvent evt)
    {
        if(evt.character == 'd')
            ((DebugRenderingProcessor)canvasRenderer).traceNextFrames(1);
    }

    /**
     * Notification of a key release event. Does nothing for this
     * implementation.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyReleased(KeyEvent evt)
    {
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Common internal initialisation for the constructors.
     *
     * @param device The screen device context that holds the parent component.
     *    Must not be null
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    private void init(Display device,
                      GLCapabilities caps,
                      GLCapabilitiesChooser chooser)
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = getSharedGLContext();

        glFigure = new GLFigure(device, caps, chooser, shared_context);
        glFigure.addGLFigureSizeListener(resizer);
        glFigure.setManualDrawing(true);

        canvas = glFigure.getGLAutoDrawable();
        ((GLAutoDrawable)canvas).setAutoSwapBufferMode(false);
        canvasContext = glFigure.getGLContext();
        canvasRenderer = new DebugRenderingProcessor(canvasContext, this);

        glFigure.addKeyListener(this);

        init();
    }
}
