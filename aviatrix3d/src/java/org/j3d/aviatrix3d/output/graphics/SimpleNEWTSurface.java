/*
Copyright (c) 1995-2014 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.movesinstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.j3d.aviatrix3d.output.graphics;

// External imports
import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;

import com.jogamp.opengl.*;

// Local imports
// None

/**
 * Implementation of the most basic drawable surface, supporting the minimal
 * number of features.  Patterned after SimpleAWTSurface.  NEWT is JOGL's
 * High Performance Native Windowing Toolkit
 * <p>
 *
 * This implementation of GraphicsOutputDevice renders to a normal GLWindow
 * instance and provides pBuffer support as needed. Stereo support is not
 * provided and all associated methods always indicate negative returns on query
 * about support.
 *
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.j3d.aviatrix3d.output.graphics.SimpleNEWTSurface">Terry Norbraten, NPS MOVES</a>
 * @version $Id: SimpleNEWTSurface.java 12713 2023-05-26 21:35:52Z tnorbraten $
 */
public class SimpleNEWTSurface extends BaseNEWTSurface
{
    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     */
    public SimpleNEWTSurface(GLCapabilities caps)
    {
        this(caps, null, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public SimpleNEWTSurface(GLCapabilities caps,
                             GLCapabilitiesChooser chooser)
    {
        this(caps, chooser, null);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public SimpleNEWTSurface(GLCapabilities caps,
                             BaseSurface sharedSurface)
    {
        this(caps, null, sharedSurface);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public SimpleNEWTSurface(GLCapabilities caps,
                             GLCapabilitiesChooser chooser,
                             BaseSurface sharedSurface)
    {
        super(sharedSurface);

        init(caps, chooser);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseNEWTSurface
    //---------------------------------------------------------------

    @Override
    protected void initCanvas(GLCapabilities caps, GLCapabilitiesChooser chooser)
    {   
        float[] reqSurfacePixelScale = new float[] {ScalableSurface.AUTOMAX_PIXELSCALE, ScalableSurface.AUTOMAX_PIXELSCALE};
        Display nDisplay = NewtFactory.createDisplay(null);
        Screen nScreen = NewtFactory.createScreen(nDisplay, 0);
        Window nWindow = NewtFactory.createWindow(nScreen, caps);
        nWindow.setCapabilitiesChooser(chooser);
        canvas = GLWindow.create(nWindow);
        ((GLWindow)canvas).setSurfaceScale(reqSurfacePixelScale);
        canvas.addGLEventListener(this);
        canvas.setAutoSwapBufferMode(true);
        canvasRenderer = new StandardRenderingProcessor(this);
        canvasRenderer.setOwnerBuffer(canvasDescriptor);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

}
