/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.navigation;

// External imports
import  javax.vecmath.Matrix4f;

// Local imports
// none

/**
 * A listener interface used internally to notify of an update or change in the
 * system that will effect the display.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface FrameUpdateListener
{
    /**
     * Called after each phase of transition or mouse navigation.
     *
     * @param t3d The transform of the new position
     */
    void viewerPositionUpdated(Matrix4f t3d);

    /**
     * Called when a transition from one position to another has ended.
     *
     * @param t3d The transform of the new position
     */
    void transitionEnded(Matrix4f t3d);
}
