/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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

// External imports
// None

// Local imports
// None

/**
 * <p>
 * A listener for changes in layer state.
 * </p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface LayerListener {
    
    /**
     * The pickable status of the layer has changed.
     *
     * @param pickable Is this layer pickable
     */
    void pickableStateChanged(boolean pickable);
}
