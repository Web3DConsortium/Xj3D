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
package org.web3d.vrml.renderer.common.input.dis;

// Standard imports
// None

// Application specific imports
import edu.nps.moves.dis7.pdus.EntityStatePdu;

import org.web3d.vrml.nodes.VRMLDISNodeType;

/**
 * Writer List entries structure.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
class WriterListEntry extends ListEntry {
    VRMLDISNodeType node;
    long lastTime;
    EntityStatePdu currEspdu;
    EntityStatePdu lastEspdu;

    public WriterListEntry(VRMLDISNodeType node) {
        this.node = node;
    }
}
