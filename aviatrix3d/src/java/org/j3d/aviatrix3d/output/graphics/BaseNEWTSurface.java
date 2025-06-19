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
import com.jogamp.opengl.*;

// Local imports
// None
//import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
//import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;

/**
 * Implementation of the most basic drawable surface extended to provide JOGL
 * v2+ NEWT-specific features.  Patterned after BaseAWTSurface.  NEWT is JOGL's
 * High Performance Native Windowing Toolkit
 *
 * @see http://jogamp.org/jogl/doc/NEWT-Overview.html
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.j3d.aviatrix3d.output.graphics.BaseNEWTSurface">Terry Norbraten, NPS MOVES</a>
 * @version $Id: BaseNEWTSurface.java 12689 2021-05-20 18:13:00Z tnorbraten $
 */
public abstract class BaseNEWTSurface extends BaseSurface
{    
    /**
     * Construct a surface which shares its GL context with the given surface.
     * This is useful for constructing multiple view displays of the same scene
     * graph, but from different viewing directions, such as in a CAD
     * application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     * </p>
     *
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    protected BaseNEWTSurface(BaseSurface sharedWith)
    {
        super(sharedWith);
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

    @Override
    public Object getSurfaceObject()
    {
        // Since we know that the canvas is GLJPanel or GLCanvas, we can just
        // return the raw drawable here for casting.
        return canvas;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Ask the final class to create the actual canvas now, based on the selected chooser and
     * caps that JOGL needs, rather than the AV3D-specific classes that the external caller
     * makes use of.
     *
     * @param caps The capabilities to select for
     * @param chooser The optional chooser that will help select the required capabilities
     */
    protected abstract void initCanvas(GLCapabilities caps, GLCapabilitiesChooser chooser);

    /**
     * Common internal initialisation for the constructors.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    protected void init(GLCapabilities caps, GLCapabilitiesChooser chooser)
    {
//        GLDrawableFactory fac = GLDrawableFactory.getDesktopFactory();
//        AbstractGraphicsDevice screen_device = fac.getDefaultDevice();
//        GLProfile selected_profile = GLProfile.get(screen_device, GLProfile.GL2);

//        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, selected_profile);
//        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        GLCapabilities jogl_caps = caps;
        GLCapabilitiesChooser jogl_chooser = chooser;

        initCanvas(jogl_caps, jogl_chooser);
        initBasicDataStructures();
    }
 }
