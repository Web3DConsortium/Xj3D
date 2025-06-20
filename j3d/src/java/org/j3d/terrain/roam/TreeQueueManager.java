/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)QueueManager.java 1.1 02/01/10 09:27:29
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    -Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *    -Redistribution in binary form must reproduct the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed,licensed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */
package org.j3d.terrain.roam;

// External imports
import java.util.TreeSet;

// Local imports
// none

/**
 * A queue manager implementation that uses sorted TreeSets to manage the
 * queue.
 *
 * @author  Paul Byrne, Justin Couch
 * @version
 */
class TreeQueueManager implements QueueManager
{
    private TreeSet<TreeNode> triQueue;
    private TreeSet<TreeNode> diamondQueue;

    /**
     * Creates new QueueManager.
     */
    public TreeQueueManager()
    {
        triQueue = new TreeSet<>(new TriangleComparator());
        diamondQueue = new TreeSet<>(new DiamondComparator());
    }

    /**
     * Add a new triangle to the queue.
     *
     * @param node The new node to add
     */
    @Override
    public void addTriangle(QueueItem node)
    {
        if(!(node instanceof TreeNode))
            throw new RuntimeException("Not a TreeNode");

        triQueue.add((TreeNode) node);
    }

    /**
     * Remove the given triangle the queue.
     *
     * @param node The new node to remove
     */
    @Override
    public void removeTriangle(QueueItem node)
    {
        if(!(node instanceof TreeNode))
            throw new RuntimeException("Not a TreeNode");

        triQueue.remove((TreeNode)node);
    }

    /**
     * Add a new triangle to the queue.
     *
     * @param node The new node to add
     */
    @Override
    public void addDiamond(QueueItem node)
    {
        if(!(node instanceof TreeNode))
            throw new RuntimeException("Not a TreeNode");

        diamondQueue.add((TreeNode) node);
    }

    /**
     * Remove the given diamond from the queue.
     *
     * @param node The new node to remove
     */
    @Override
    public void removeDiamond(QueueItem node)
    {
        if(!(node instanceof TreeNode))
            throw new RuntimeException("Not a TreeNode");

        diamondQueue.remove((TreeNode)node);
    }

    /**
     * Clear everything from the queue.
     */
    @Override
    public void clear()
    {
        triQueue.clear();
        diamondQueue.clear();
    }

    public TreeNode getSplitCandidate()
    {
        TreeNode ret_val = null;

        if(!triQueue.isEmpty())
            ret_val = triQueue.last();

        return ret_val;
    }

    public TreeNode getMergeCandidate()
    {
        TreeNode ret_val = null;

        if(!diamondQueue.isEmpty())
            ret_val = diamondQueue.first();

        return ret_val;
    }
}
