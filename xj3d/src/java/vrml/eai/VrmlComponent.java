/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai;

/**
 * Provides for implementation of a VRML browser than runs as a
 * component and able to extract a Browser reference from it.
 *  <p>
 * Generally this is used to provide a definition of an AWT component with a
 * VRML display capability. There is no reason why this could not be used for
 * other browser representations such as off screen renderers or file savers.
 *
 * @version 2.0 29 August 1998
 */
public interface VrmlComponent
{
    /**
    * Get a browser reference from this component that represents the
    * internals of this browser.
    *
    * @return A reference to the browser object represented by this component.
    */
    Browser getBrowser();

    /**
     * Get a reference to the component implementation. For example, if this
     * is an AWT component, it would return an instance
     * of {@link java.awt.Component}.
     * @return a reference to the component implementation
     */
    Object getImplementation();

    /**
     * Shutdown the component because it will no longer be needed. If the
     * component has already had this method called, it will silently ignore
     * any further requests.
     */
    void shutdown();
}





