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

package org.j3d.renderer.aviatrix3d.device.input.mouse;

// External imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

import org.j3d.device.input.Tracker;
import org.j3d.device.input.TrackerDevice;

// Local imports
// None

/**
 * A mouse device implementation.  This mouse is a typical picking and
 * navigation style mouse.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class MouseDevice
    implements TrackerDevice,
               MouseListener,
               MouseMotionListener,
               MouseWheelListener {

    /** The sensors for this device */
    private MouseTracker[] trackers;

    /** The name of this device */
    private String name;

    /**
     * Construct a new mouse device that interacts with the given surface,
     * and is named.
     *
     * @param surface The surface to track
     * @param name The device name string
     */
    public MouseDevice(GraphicsOutputDevice surface, String name) {
        this.name = name;

        trackers = new MouseTracker[1];
        trackers[0] = new MouseTracker(surface, name + "-Tracker-0");
    }

    //------------------------------------------------------------------------
    // Methods defined by InputDevice
    //------------------------------------------------------------------------

    /**
     * Get the name of this device.  Names are of the form class-#.  Valid
     * classes are Gamepad, Joystick, Wheel, Midi, GenericHID.
     *
     * @return The name
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Tracker[] getTrackers() {
        return trackers;
    }

    @Override
    public int getTrackerCount() {
        return 1;
    }

    //------------------------------------------------------------------------
    // Methods defined by MouseListener
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mousePressed(MouseEvent evt) {
        trackers[0].mousePressed(evt);
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseReleased(MouseEvent evt) {
        trackers[0].mouseReleased(evt);
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseClicked(MouseEvent evt) {
        trackers[0].mouseClicked(evt);
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseEntered(MouseEvent evt) {
        trackers[0].mouseEntered(evt);
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseExited(MouseEvent evt) {
        trackers[0].mouseExited(evt);
    }

    //------------------------------------------------------------------------
    // Methods defined by MouseMotionListener
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseDragged(MouseEvent evt) {
        trackers[0].mouseDragged(evt);
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseMoved(MouseEvent evt) {
        trackers[0].mouseMoved(evt);
    }

    //------------------------------------------------------------------------
    // Method defined by MouseWheelListener
    //------------------------------------------------------------------------

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        trackers[0].mouseWheelMoved(mwe);
    }
}
