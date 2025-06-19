/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.view;

import org.web3d.x3d.sai.X3DComponent;

/**
 * An X3D View.
 *
 * @author Alan Hudson
 */
public interface ViewX3D extends View {
    
    /**
     * Return the X3D component in use.
     *
     * @return The component
     */
    X3DComponent getX3DComponent();

    void shutdown();
}