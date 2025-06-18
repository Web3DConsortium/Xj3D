/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2009
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

// External imports
import edu.nps.moves.dis7.enumerations.DisPduType;

import edu.nps.moves.dis7.pdus.DetonationPdu;
import edu.nps.moves.dis7.pdus.EntityID;
import edu.nps.moves.dis7.pdus.EntityStatePdu;
import edu.nps.moves.dis7.pdus.FirePdu;
import edu.nps.moves.dis7.pdus.Pdu;

import edu.nps.moves.dis7.utilities.DisThreadedNetworkInterface;
import edu.nps.moves.dis7.utilities.DisThreadedNetworkInterface.PduListener;

import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Quat4f;

// Local imports
import org.web3d.vrml.nodes.VRMLDISManagerNodeType;
import org.web3d.vrml.nodes.VRMLDISNodeType;
import org.web3d.vrml.nodes.VRMLNetworkInterfaceNodeType;

/**
 * Network connection handler for native DIS protocol
 * @version $Id: DISConnectionHandler.java,v 1.3 2009-09-16 00:30:37 tdnorbra Exp $
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.vrml.renderer.common.input.dis.DISConnectionHandler">Terry Norbraten</a>
 */
public class DISConnectionHandler implements PduListener {

    /**
     * The default order.
     */
    private static final int DEFAULT_ORDER = 2;

    /**
     * The default convergence interval.
     */
    private static final int DEFAULT_CONVERGENCE_INTERVAL = 200;

    DisThreadedNetworkInterface writer;

    DatagramSocket socket;

    InetAddress address;

    Thread readThread;

    private final int port;

    private final String group;

    int cnt;

    private final LinkedList liveList;

    // Scratch id to avoid gc
    private final DISId disId;

    // Scratch translation field
    private final float[] translation;

    // Scratch rotation field
    private final float[] rotation;

    private final float[] dRorientation;

    private Quat4f quaternion = null;

    /** The node to ID mapping */
    private final Map<DISId, NodeMapEntry> nodeMap;

    /** The list of managers */
    private final List<VRMLDISNodeType> managerList;

    /** The Entities we've placed on the addedEntities */
    private final Set<DISId> notifiedSet;

    /**
     * @param nodeMap
     * @param liveList
     * @param managerList
     * @param notifiedSet
     * @param group multicast group
     * @param port multicast port
     */
    public DISConnectionHandler(Map<DISId, NodeMapEntry> nodeMap, LinkedList liveList, List<VRMLDISNodeType> managerList, Set<DISId> notifiedSet, String group, int port) {
        this.nodeMap = nodeMap;
        this.group = group;
        this.port = port;
        this.liveList = liveList;
        this.managerList = managerList;
        this.notifiedSet = notifiedSet;

        disId = new DISId(0, 0, 0);
        translation = new float[3];
        rotation = new float[4];
        dRorientation = new float[3];
        quaternion = new Quat4f();

        writer = new DisThreadedNetworkInterface(this.group, this.port);
        writer.setVerbose(false);
        writer.addListener(DISConnectionHandler.this);
    }

    /**
     * Get a writer for this connection.
     *
     * @return The writer.
     */
    public DisThreadedNetworkInterface getWriter() {
        return writer;
    }

    //----------------------------------------------------------
    // Methods required by BehaviorConsumerIF
    //----------------------------------------------------------
    
    @Override
    public void incomingPdu(Pdu pdu) {
        cnt++;

        if (cnt % 100 == 0) {
//            System.out.println("Packets received: " + cnt);
        }

        EntityID eid;
        NodeMapEntry entry;
        VRMLDISNodeType di;
        long time;
        long timestamp;
        DisPduType pduType = pdu.getPduType();
        
        switch (pduType) {
//            case PduType.FIREFI:
            case FIRE:
                FirePdu firepdu = (FirePdu) pdu;

                eid = firepdu.getTargetEntityID();
                disId.setValue(eid.getSiteID(), eid.getApplicationID(), eid.getEntityID());

                entry = nodeMap.get(disId);

                if (entry == null) {
                    //System.out.println("Unknown espdu: " + disId);
                    return;
                }

                di = entry.node;

                if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                    // Ignore for non readers
                    return;
                }

                time = System.currentTimeMillis();
                timestamp = pdu.getTimestamp();
                
                if (entry.listEntry != null) {
                    // update last time
                    LiveListEntry lle = (LiveListEntry) entry.listEntry;

                    if (timestamp > lle.espduTimestamp) {
                        lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                        lle.lastTime = time;
                        if (lle.currEspdu != null) {
                            lle.lastEspdu = lle.currEspdu;
                        }
                        lle.currFire = firepdu;
                        lle.newPackets = true;
                    } else {
                        System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                    }
                } else {
                    // create new entry
                    LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                    entry.listEntry = newlle;
                    newlle.lastEspdu = null;
                    newlle.currEspdu = null;
                    newlle.currDetonate = null;
                    newlle.currFire = firepdu;
                    newlle.espduTimestamp = timestamp;
                    newlle.closeEnough = false;
                    newlle.avgTime = 0.01f;
                    newlle.newPackets = true;

                    liveList.add(newlle);
                    di.setIsActive(true);
                }

                break;
//            case PduTypeField.DETONATIONFI:
            case DETONATION:
                DetonationPdu dpdu = (DetonationPdu) pdu;
                
                eid = dpdu.getTargetEntityID();
                disId.setValue(eid.getSiteID(), eid.getApplicationID(), eid.getEntityID());

                entry = nodeMap.get(disId);

                if (entry == null) {
                    //System.out.println("Unknown espdu: " + disId);
                    return;
                }

                di = entry.node;

                if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                    // Ignore for non readers
                    return;
                }

                time = System.currentTimeMillis();
                timestamp = pdu.getTimestamp();
                if (entry.listEntry != null) {
                    // update last time
                    LiveListEntry lle = (LiveListEntry) entry.listEntry;

                    if (timestamp > lle.espduTimestamp) {

                        lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                        lle.lastTime = time;
                        if (lle.currEspdu != null) {
                            lle.lastEspdu = lle.currEspdu;
                        }
                        lle.currDetonate = dpdu;
                        lle.closeEnough = false;
                        lle.newPackets = true;
                    } else {
                        System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                    }
                } else {
                    // create new entry
                    LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                    entry.listEntry = newlle;
                    newlle.lastEspdu = null;
                    newlle.currEspdu = null;
                    newlle.currDetonate = dpdu;
                    newlle.espduTimestamp = timestamp;
                    newlle.closeEnough = false;
                    newlle.avgTime = 0.01f;
                    newlle.newPackets = true;

                    liveList.add(newlle);
                    di.setIsActive(true);
                }

                break;
            case ENTITY_STATE:
                EntityStatePdu espdu = (EntityStatePdu) pdu;

                eid = espdu.getEntityID();

                disId.setValue(eid.getSiteID(), eid.getApplicationID(), eid.getEntityID());

                entry = nodeMap.get(disId);

                if (entry == null) {

                    for (VRMLDISNodeType manager : managerList) {

                        if (!notifiedSet.contains(disId)) {
                            ((VRMLDISManagerNodeType)manager).entityArrived(espdu);

                            try {
                                // Clone Id to put on list
                                notifiedSet.add((DISId) disId.clone());
                            } catch (CloneNotSupportedException ex) {
                                Logger.getLogger(DISConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    return;
                }

                di = entry.node;

                if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                    System.out.println("Ignoring ESPDU");
                    // Ignore for non readers
                    return;
                }

                time = System.currentTimeMillis();
                timestamp = pdu.getTimestamp();

                if (entry.listEntry != null) {
                    // update last time
                    LiveListEntry lle = (LiveListEntry) entry.listEntry;

                    if (timestamp > lle.espduTimestamp) {
                        lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                        lle.lastTime = time;
                        lle.lastEspdu = lle.currEspdu;
                        lle.currEspdu = espdu;
                        lle.closeEnough = false;
                        lle.newPackets = true;
                    } else {
                        System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                    }
                } else {
                    // create new entry
                    LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                    entry.listEntry = newlle;
                    newlle.lastEspdu = espdu;
                    newlle.currEspdu = espdu;
                    newlle.rotationConverger = new OrderNQuat4dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                    newlle.translationConverger = new OrderNVector3dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                    newlle.espduTimestamp = timestamp;
                    newlle.closeEnough = false;
                    newlle.avgTime = 0.01f;
                    newlle.newPackets = true;

                    liveList.add(newlle);
                    di.setIsActive(true);
                }

                break;
            default:
                System.err.println("Unhandled DIS node:  type: " + pdu.getPduType() + " " + pduType.getDescription());
        }
    }

} // end class file DISConnectionHandler.java
