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

package org.xj3d.core.loading;

// External imports
import java.util.Map;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * A loader thread for a single piece of content at any given time.
 * <p>
 *
 * The content loader is used to wait on a queue of available content and
 * load the next available item in the queue.
 * <p>
 *
 * When loading, the content loader loads the complete file, it ignores any
 * reference part of the URI. This allows for better caching.
 *
 * The loader is used to
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.3 $
 */
class ContentLoader implements Runnable
{
    /** Message for an unexpected exception message */
    private static final String UNEXPECTED_EXCEPTION_MSG =
        "[ContentLoader] unexpected Xj3D exception during model loading";

    private static final String XJ3D_ISSUE_LIST =
            "New Xj3D issue needed to improve the open source, please see\n" +
            "  https://gitlab.nps.edu/Savage/xj3d/issues";

    /** The threading running this loader */
    private Thread thread;

    /** The list of data we are fetching from */
    private final ContentLoadQueue pendingContentLoadQueue;

    /** The map of nodes we are currently loading to their loader */
    private final Map<String[], LoadRequest> inProgressLoadRequestMap;

    /** Flag indicating that the current load should be terminated */
    private boolean terminateCurrent;

    /** Flag indicating we should stop completely */
    private boolean processNext;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The currently processing load request */
    private LoadRequest currentLoadRequest;

    /**
     * Create a content loader that reads values from the given queue and
     * stores intermediate results in the given map.
     *
     * @param threadGroup The thread group to put this thread in
     * @param pendingQueue The list holding pending items to process
     * @param processingLoadRequestMap The map of items currently processing
     */
    ContentLoader(ThreadGroup threadGroup, ContentLoadQueue pendingQueue, Map<String[], LoadRequest> processingLoadRequestMap) {

        pendingContentLoadQueue = pendingQueue;
        inProgressLoadRequestMap = processingLoadRequestMap;
        terminateCurrent = false;
        processNext = true;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        // Let's get running!
        thread = new Thread(threadGroup, this, "Xj3D Content Loader");
        thread.setDaemon(true);
        thread.start();
    }

    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------

    /**
     * Run the loader to read content from the list
     */
    @Override
    public void run()
    {
        while(processNext)
        {
            try {
                currentLoadRequest = pendingContentLoadQueue.getNext();

                if ((currentLoadRequest == null) || (currentLoadRequest.loadDetailsList.isEmpty()))
                {
                    if(processNext)
                        continue;
                    else
                        break;
                }

                // Ignore this as we have not yet registered that we are
                // actually processing anything.
                terminateCurrent = false;

                // Register now that we are processing this object
                inProgressLoadRequestMap.put(currentLoadRequest.url, currentLoadRequest);

                currentLoadRequest.loadRequestHandler.processLoadRequest(errorReporter,
                                                                         currentLoadRequest.url,
                                                                     currentLoadRequest.loadDetailsList);
                // Register now that we finished processing this object
                inProgressLoadRequestMap.remove(currentLoadRequest.url);
            }
            catch (Exception e)
            {
                // Any other exception
//                System.out.println ("ContentLoader exception:"); // debug
                errorReporter.errorReport  (UNEXPECTED_EXCEPTION_MSG, e);
                errorReporter.messageReport(XJ3D_ISSUE_LIST);
//                e.printStackTrace(System.out); // debug
                inProgressLoadRequestMap.remove(currentLoadRequest.url);
                continue;
            }

            currentLoadRequest = null;

            // just to be nice
            Thread.yield(); // TODO fix warning, might possibly mask synchronization process
        }
        // Release thread resources
        thread = null;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Is this thread alive?
     *
     *@return Thread.isAlive
     */
    boolean isAlive() {
        if (thread == null)
            return false;

        return thread.isAlive();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null && !terminateCurrent && processNext)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Notification to abort loading the current resource. If there is one
     * loading, it will terminate the procedure immediately and start fetching
     * the next available URI. This will only work if we are currently
     * processing a file. If we are not processing a file then this is
     * ignored.
     */
    void abortCurrentFile() {
        terminateCurrent = true;

        if (currentLoadRequest != null)
            currentLoadRequest.loadRequestHandler.abortCurrentFile();
    }

    /**
     * Notification to shut down the load process entirely for this thread.
     * It probably means we are about to close down the whole system. If we
     * are held in the queue, blocked waiting for input, the caller should
     * call {@link org.web3d.util.BlockingQueue#purge()} on the queue
     * <i>after</i> calling this method. That will force the block to exit
     * and this thread to end.
     */
    void shutdown() {
        terminateCurrent = true;
        processNext = false;

        if(currentLoadRequest != null)
            currentLoadRequest.loadRequestHandler.shutdown();
    }
}
