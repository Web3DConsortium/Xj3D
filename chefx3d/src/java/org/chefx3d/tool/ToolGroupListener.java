/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.tool;

// External Imports
// None

/**
 * A listener for changes in the individual tool groups.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface ToolGroupListener {

    /**
     * A tool has been added.  Batched additions will come through
     * the toolsAdded method.
     *
     * @param evt The event that caused this method to be called
     */
    void toolAdded(ToolGroupEvent evt);

    /**
     * A tool group has been added. Batched adds will come through the
     * toolsAdded method.
     *
     * @param evt The event that caused this method to be called
     */
    void toolGroupAdded(ToolGroupEvent evt);

    /**
     * A tool has been removed. Batched removes will come through the
     * toolsRemoved method.
     *
     * @param evt The event that caused this method to be called
     */
    void toolRemoved(ToolGroupEvent evt);

    /**
     * A tool has been removed.  Batched removes will come through the
     * toolsRemoved method.
     *
     * @param evt The event that caused this method to be called
     */
    void toolGroupRemoved(ToolGroupEvent evt);
}
