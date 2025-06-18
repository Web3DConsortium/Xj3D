/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.loading;

// External imports
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

// Local imports
// None


/**
 * Customized queue implementation specifically designed to handle the needs
 * of X3D/VRML external content loading by compressing multi requests for the
 * same URL into a single structure.
 * <p>
 *
 * The queue sorts the incoming requests based on priority. The priority can
 * be defined based on the value of the system property
 * <code>org.xj3d.core.loading.sort.order</code>. See the package documentation
 * for details on the value of this property. This can be changed at runtime
 * and have the queue resorted by calling the {@link #requestResort()} method
 * called on this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class ContentLoadQueue 
{
    /** Message when the URL to be loaded is either null or zero length */
    private static final String NULL_URL_MESSAGE =
        "The URL provided to the content load queue did not contain anything.";

    /** Message when the loadConstantsType constant is null */
    private static final String NULL_TYPE_MESSAGE =
        "The node type constant was null. A valid type is needed.";

    /** A flag to indicate the class is currently undergoing a purge */
    private boolean purging;

    /** Count of threads waiting in getNext, used to ensure that a
     *  purge completes before resetting the purge flag */
    private int numberOfWaitingThreads = 0;

    /** List of LoadRequests to be loaded for this URL. */
    private TreeSet<LoadRequest> loadRequstTreeSetQueue; // TODO warning synchronizing on non=final field which is overwritten when re-sorted

    /** The priority sorter for the loading queue. */
    private final LoadPriorityComparator loadPriorityComparatorSorter;

    /**
     * The current set of URLs that are in the queue mapped to their object
     * representation in LoadRequest.
     */
    private final Map<LoadDetails, LoadRequest> loadDetailsToLoadRequestMap;

    /**
     * Constructor to create a new instance of this class. 
     * Package private to prevent direct instantiation.
     */
    ContentLoadQueue() {
        loadPriorityComparatorSorter = new LoadPriorityComparator();
        loadRequstTreeSetQueue       = new TreeSet<>(loadPriorityComparatorSorter);
        loadDetailsToLoadRequestMap  = new HashMap<>();
        purging = false;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Add the given load request onto the queue. If the URL is already sitting
     * on the queue, the details will be inserted into the existing load
     * request so that they may all be serviced at once.
     *
     * @param loadConstantsType The node loadConstantsType classification. One of the defined constants in
    {@link LoadConstants}.
     * @param url The urls of the item to load
     * @param loadRequestHandler The class that will process the load request once it is
     *    pulled grabbed from the queue for processing
     * @param loadDetails Detail set for what should be loaded
     * @throws IllegalArgumentException Either the URL list was null/zero length,
   or the loadConstantsType was null
     */
    public synchronized void add(String   loadConstantsType,
                                 String[] url,
                                 LoadRequestHandler loadRequestHandler,
                                 LoadDetails        loadDetails) {

        if((url == null) || (url.length == 0))
            throw new IllegalArgumentException(NULL_URL_MESSAGE);

        LoadRequest loadRequest        = new LoadRequest();
        loadRequest.url                = url;
        loadRequest.loadRequestHandler = loadRequestHandler;
        loadRequest.loadConstantsType  = loadConstantsType;

        synchronized(loadRequstTreeSetQueue) // TODO consider warning on synchronization of non-final field
        {
            if (loadRequstTreeSetQueue.contains(loadRequest)) 
            {
                SortedSet<LoadRequest> sortedSet = loadRequstTreeSetQueue.tailSet(loadRequest);

                loadRequest = sortedSet.first();

                loadRequest.loadDetailsList.add(loadDetails);
            } 
            else {
                loadRequest.loadDetailsList.add(loadDetails);
                loadRequstTreeSetQueue.add(loadRequest);
            }

            loadDetailsToLoadRequestMap.put(loadDetails, loadRequest);
        }
        notify();
    }

    /**
     * Get the next item from the queue. Block until an item is available.
     *
     * @return The next item on the queue
     */
    public synchronized LoadRequest getNext() 
    {
        LoadRequest loadRequest = null;

        while (!purging && loadRequest == null) {
            while (!purging && loadRequstTreeSetQueue.isEmpty()) {
                numberOfWaitingThreads++;
                try {
                    synchronized (this) // TODO synchronization warning, nested blocks
                    {
                        wait();
                    }
                } catch (InterruptedException e) {
                }
                numberOfWaitingThreads--;
            }

            synchronized (loadRequstTreeSetQueue)  // TODO synchronization warning, nested blocks
            {
                if (!loadRequstTreeSetQueue.isEmpty()) 
                {
                    loadRequest = loadRequstTreeSetQueue.first();
                    loadRequstTreeSetQueue.remove(loadRequest);

                    int num_details = loadRequest.loadDetailsList.size();
                    for (int i = 0; i < num_details; i++) 
                    {
                        loadDetailsToLoadRequestMap.remove(loadRequest.loadDetailsList.get(i));
                    }
                }
            }
        }

        if (purging && (numberOfWaitingThreads == 0)) 
        {
            purging = false;
        }
        return loadRequest;
    }

    /**
     * Return the size of the queue.
     *
     * @return size of queue
     */
    public synchronized int size() {
        return loadRequstTreeSetQueue.size();
    }

    /**
     * Remove all elements from queue. Also unblock those who are waiting for
     * items in the queue. They leave the getNext() method with null.
     */
    public synchronized void purge() {
        clear();
        purging = true;
        notifyAll();
    }

    /**
     * Clear the queue of items. If there are users of the class that are
     * blocked while waiting for elements in the queue, they remain so.
     */
    public synchronized void clear() 
    {
        loadRequstTreeSetQueue.clear();
        loadDetailsToLoadRequestMap.clear();
    }

    /**
     * Remove the given item from the queue.
     *
     * @param url The url of the object to be removed
     * @param loadDetails The instance of the detail to be removed from the URL
     */
    public void remove(String[] url, LoadDetails loadDetails) 
    {
        synchronized(loadRequstTreeSetQueue) 
        {
            LoadRequest loadRequest = loadDetailsToLoadRequestMap.get(loadDetails);

            if(loadRequest == null || !loadRequest.loadDetailsList.contains(loadDetails))
                return;

            // remove from the load queue first so that we make sure we get
            // it before the content loader snaffles it. Then go back and grab
            // it from the hashmap.
            if(loadRequest.loadDetailsList.size() == 1) 
            {
                loadRequstTreeSetQueue.remove(loadRequest);
            } 
            else 
            {
                loadRequest.loadDetailsList.remove(loadDetails);
            }
            loadDetailsToLoadRequestMap.remove(loadDetails);
        }
    }

    /**
     * If the sorting priority system property has changed, call this method to
     * reload the property and force a resorting of the queue. This will not
     * effect items that are currently being processed, only items that are
     * waiting to be processed.
     */
    public void requestResort()
    {
        loadPriorityComparatorSorter.updatePriorities();
        TreeSet<LoadRequest> new_queue = new TreeSet<>(loadPriorityComparatorSorter);
        new_queue.addAll(loadRequstTreeSetQueue);

        loadRequstTreeSetQueue = new_queue;
    }
}
