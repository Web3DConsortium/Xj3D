/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
// none

/**
 * <p>
 * A listener for changes in Texture Coordinate Mode changes.
 * </p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface TexCoordGenModeListener {
    /**
     * The texture coordinate mode has changed.
     *
     * @param mode  The new mode
     */
    void texCoordGenModeChanged(String mode);
}
