/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.util;

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * A UserDataHandler that clones all its userData contents.
 * 
 * @author Alan Hudson
 * @version
 */
public class CloneUserDataHandler implements UserDataHandler {
    @Override
    public void handle(short operation, String key, Object data, Node src,
            Node dst) {
        System.out.println("Cloning: " + src.hashCode() + " to: "
                + dst.hashCode());
        switch (operation) {
        case NODE_CLONED:
        case NODE_ADOPTED:
        case NODE_IMPORTED:
            // dst.setUserData(key, ((Cloneable)data).clone(), this);
            break;
        }
    }
}