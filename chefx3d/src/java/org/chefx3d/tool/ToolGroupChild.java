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

// Local imports
// None

/**
 * Marker interface to declare class types that are valid children of a
 * {@link ToolGroup}
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface ToolGroupChild {
    
    /**
     * Get the tool's name. Ideally all tools in the system shall be unique.
     *
     * @return The name string
     */
    String getName();

    /**
     * Return the parent of this tool group child. If there is no parent
     * reference the parent is either the catalog or this is an orphaned item.
     *
     * @return The current parent of this item
     */
    ToolGroupChild getParent();
}
