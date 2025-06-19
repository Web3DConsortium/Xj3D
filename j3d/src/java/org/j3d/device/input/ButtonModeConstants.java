/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.device.input;

// External imports
// None

// Local imports
// None

/**
 * Button action constants.  Allows a tracker to specify what each
 * button can be used for.
 *
 * Picking is modal whereas all others are specific modes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface ButtonModeConstants
{
    int NOTHING = 0;
    int PICKING = 1;

    /** Absolute choice of a mode.  Disallowed modes due to NavInfo will be ignored. */
    int WALK = 2;
    int FLY = 3;
    int EXAMINE = 4;
    int PAN = 5;
    int PAN_COLLISION = 5;
    int TILT = 6;

    /** These specify which slot of the NavigationInfo.type field to use */
    int NAV1 = 10;
    int NAV2 = 11;
    int NAV3 = 12;
    int NAV4 = 13;
    int NAV5 = 14;
    int NAV6 = 15;
    int NAV7 = 16;
    int NAV8 = 17;

    /** Action a button might perform */
    int VIEWPOINT_NEXT = 18;
    int VIEWPOINT_PREV = 19;
    int VIEWPOINT_RESET = 20;
}
