/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// External imports
// None

// Local imports
// None

/**
 * <p>
 * Blocking 'First In First Out' (FIFO) queue.
 *
 * Based on the simple Queue but can be used concurrently by separate
 * threads. If there are not elements in the queue, getNext() will block until
 * it is not empty.
 * 
 * Taken from the VLC common code library
 * <a href="http://www.vlc.com.au/common/">http://www.vlc.com.au/common/</a>
 * This software is released under the
 * <a href="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</a>
 * </p>
 *
 * @author Justin Couch.
 * @version $Revision: 1.7 $
 * @see org.j3d.util.Queue
 */
public class BlockingQueue extends org.j3d.util.Queue {

    /** A flag to indicate the class is currently undergoing a purge */
    private boolean m_purging = false;

    /** Count of threads waiting in getNext, used to ensure that a
     *  purge completes before resetting the purge flag */
    private int numberOfWaitingThreads = 0;

    /**
     * Constructor. Create a simple queue.
     */
    public BlockingQueue() {
    }

    /**
     * Add an object to the end of the queue.
     *
     * @param o Object to add.
     */
    @Override
    public synchronized void add(Object o) {
        super.add(o);
        notify();
    }

    /**
     * Return the next element from the front of the queue, and remove it
     * from the queue. Under normal circumstances this method will always
     * return an object, blocking if it has to until something is available.
     * However, sometimes the queue needs to close so we unblock the queue
     * are return null instead.
     *
     * @return element at the front of the queue, will block until
     * queue is not empty.
     */
    @Override
    public synchronized Object getNext() {
        Object o=null;

        while(!m_purging && o == null) {
            while(!m_purging && !hasNext()) {
                numberOfWaitingThreads++;
                try {
                    synchronized(this) {
                        wait();
                    }
                } catch(InterruptedException e) {}
                numberOfWaitingThreads--;
            }

            synchronized(this) {
                if (hasNext())
                    o = super.getNext();
            }
        }

        if ( m_purging && ( numberOfWaitingThreads == 0 ) ) {
            m_purging = false;
        }

        return o;
    }

    /**
     * Get the next element from the front of the queue.
     *
     * @return element at the front of the queue, null if empty.
     */
    @Override
    public synchronized Object peekNext() {
        return super.peekNext();
    }

    /**
     * Check if queue has more objects.
     *
     * @return true if queue has more objects.
     */
    @Override
    public synchronized boolean hasNext() {
        return super.hasNext();
    }

    /**
     * Return the size of the queue.
     *
     * @return size of queue.
     */
    @Override
    public synchronized int size() {
        return super.size();
    }

    /**
     * Remove all elements from queue. Also unblock those who are waiting for
     * items in the queue. They leave the getNext() method with null.
     */
    public synchronized void purge() {
        super.clear();
        m_purging = true;
        notifyAll();
    }

    /**
     * Clear the queue of items. If there are users of the class that are
     * blocked while waiting for elements in the queue, they remain so.
     */
    @Override
    public synchronized void clear() {
        super.clear();
    }
}
