/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
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
// none

// Local imports
// none

/**
 * <p>
 * Representation of a node that issue or receive network data.
 * </p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public interface VRMLNetworkInterfaceNodeType extends VRMLChildNodeType {

    /** What role this node plays: reader */
    int ROLE_READER = 0;

    /** What role this node plays: writer */
    int ROLE_WRITER = 1;

    /** What role this node plays: inactive */
    int ROLE_INACTIVE = 2;

    /** What role this node plays: manager */
    int ROLE_MANAGER = 3;

    /**
     * Get the protocol this node supports.
     *
     * @return The protocol.
     */
    String getProtocol();

    /**
     * Get the role of this node.
     *
     * @return One of the role constants, ROLE_*.
     */
    int getRole();

    /**
     * Add a NetworkRoleListener.
     *
     * @param l The listener.  Duplicates and nulls are ignored.
     */
    void addNetworkRoleListener(NetworkRoleListener l);

    /**
     * Remove a NetworkRoleListener.
     *
     * @param l The listener
     */
    void removeNetworkRoleListener(NetworkRoleListener l);
}
