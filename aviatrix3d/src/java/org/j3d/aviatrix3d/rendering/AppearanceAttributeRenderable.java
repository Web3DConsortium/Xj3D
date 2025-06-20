/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * A renderable object that contributes to an attribute to an appearance
 * renderable.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface AppearanceAttributeRenderable extends ObjectRenderable
{
    /** The attribute represents blend attributes */
    int BLEND_ATTRIBUTE = 1;

    /** The attribute represents depth attributes */
    int DEPTH_ATTRIBUTE = 2;

    /** The attribute represents line attributes */
    int LINE_ATTRIBUTE = 3;

    /** The attribute represents point attributes */
    int POINT_ATTRIBUTE = 4;

    /** The attribute represents polygon attributes */
    int POLYGON_ATTRIBUTE = 5;

    /** The attribute represents stencil attributes */
    int STENCIL_ATTRIBUTE = 6;

    /** The attribute represents alpha test attributes */
    int ALPHA_ATTRIBUTE = 7;

    /**
     * Get the type this visual attribute represents.
     *
     * @return One of the _ATTRIBUTE constants
     */
    int getAttributeType();
}
