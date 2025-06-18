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

package org.xj3d.impl.core.loading;

// External imports
import java.io.IOException;
import java.util.Vector;

//import org.ietf.uri.URL;
import java.util.List;
import org.ietf.uri.URL;

import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.xj3d.io.ReadProgressListener;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.j3d.util.ErrorReporter;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import org.xj3d.core.loading.FileCache;
import org.xj3d.core.loading.LoadDetails;
import org.xj3d.core.loading.LoadRequestHandler;
import org.xj3d.core.loading.WorldLoader;

/**
 * Independent thread used to load a world from a list of URLs and then
 * place it in the given node.
 * <p>
 *
 * This implementation is designed to work as both a loadURL() and
 * createVrmlFromUrl() call handler. The difference is defined by what data
 * is supplied to the thread. If the target node is specified, then we assume
 * that the caller wants us to put the results there. If it is null, then
 * assume that we're doing a loadURL call and replace the entire world.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class WorldLoadHandler
    implements LoadRequestHandler, ReadProgressListener {

    /** Error message when nothing loaded in a LoadURL */
    private static final String LOAD_URL_FAIL_MSG =
        "No valid URLs found for loadURL call.";

    /** Error message when nothing loaded in a createVrmlFromURL */
    private static final String CREATE_FAIL_MSG =
        "No valid URLs found for createVrmlFromURL call.";

    /** Error message when the setFieldValue fails */
    private static final String SET_FIELD_MSG =
        "Odd field error in createVrmlFromUrl";

    /** Flag to say that the world loading should be aborted now */
    private boolean terminateCurrent;

    /** The cache representation that this loader is using */
    private FileCache fileCache;

    /** The current progressListener */
    private ProgressListener progressListener;

    /** The final url of the current item */
    private String finalUrl;

    /**
     * Create a new empty world loader. It does not start the thread running.
     * That is the job of the caller code.
     *
     * @param cache The file cache implementation to use for this handler
     */
    public WorldLoadHandler(FileCache cache) {
        fileCache = cache;
        terminateCurrent = false;
    }

    //---------------------------------------------------------------
    // Methods required by ReadProgressListener
    //---------------------------------------------------------------

    @Override
    public void progressUpdate(long value)
    {
        if (progressListener != null)
        {
            // TODO: Can we avoid the garbage
            // TODO: can we get the current resource?
            ProgressEvent event = new ProgressEvent(null, ProgressEvent.DOWNLOAD_UPDATE,finalUrl,(int)value);
            progressListener.downloadUpdate(event);
        }
    }

    @Override
    public void streamClosed()
    {
        ProgressEvent event = new ProgressEvent(null, ProgressEvent.DOWNLOAD_END,finalUrl,0);
        progressListener.downloadEnded(event);
    }

    //----------------------------------------------------------
    // Methods defined by LoadRequestHandler
    //----------------------------------------------------------

    @Override
    public void processLoadRequest(ErrorReporter reporter,
                                   String[] urls,
                                   Vector<LoadDetails> loadList) {
        VRMLScene scene = null;

        WorldLoadDetails worldLoadDetails = (WorldLoadDetails)loadList.remove(0); // only removes 0th/lowest element if present
        WorldLoader      worldLoader      =  worldLoadDetails.worldLoaderManager.fetchLoader();

        if (!terminateCurrent)
        {
            for (String url : urls)
            {
                if (terminateCurrent)
                {
                    worldLoadDetails.worldLoaderManager.releaseLoader(worldLoader);
                    continue;
                }
                if (terminateCurrent) {
                    worldLoadDetails.worldLoaderManager.releaseLoader(worldLoader);
                    return;
                }

                InputSource savInputSource;
                finalUrl = url;
                try {
                    if (url.contains("http") || url.contains("https"))
                        savInputSource = new InputSource(new URL(finalUrl));
                    else // file: or something else local?
                        savInputSource = new InputSource(finalUrl);

                    if (worldLoadDetails.progressListener != null) {
                        progressListener = worldLoadDetails.progressListener;
                        savInputSource.setProgressListener(worldLoadDetails.progressListener);
                        savInputSource.setReadProgressListener(this, 100_000);
                    }
                    // TODO hack did not work: if not finding version, use most permissive in order to avoid "missing field" errors
                    // TODO handle version 4.0
//                    if (details.majorVersion == 0)
//                    {
//                        details.majorVersion = 3;
//                        details.minorVersion = 3;
//                    }

                    scene = worldLoader.loadNow(worldLoadDetails.browserCore,
                            savInputSource,
                            false,
                            worldLoadDetails.specMajorVersion,
                            worldLoadDetails.specMinorVersion);
                } 
                catch (IOException ioe) 
                {
                    reporter.warningReport("I/O Error loading " + finalUrl,
                            ioe);
                    System.out.println ("WorldLoadHandler exception: " + ioe.getLocalizedMessage()); // debug
                    ioe.printStackTrace(System.out); // debug
                    finalUrl = "<NONE>";
                }
                catch (VRMLParseException vpe) {
                    reporter.warningReport("VRML Parse exception loading " + finalUrl, vpe);
                    finalUrl = "<NONE>";
                }
                progressListener = null;
                if (scene != null) {
                    break;
                }
            }
        }
        worldLoadDetails.worldLoaderManager.releaseLoader(worldLoader);

        if(scene == null) {
            // Produce invalid URL notifications here
            String msg = worldLoadDetails.isLoadURL ? LOAD_URL_FAIL_MSG : CREATE_FAIL_MSG;
            reporter.warningReport(msg, null);

            worldLoadDetails.browserCore.sendURLFailEvent(msg);
            return;
        }

        if(terminateCurrent)
            return;

        if(worldLoadDetails.isLoadURL) {
            worldLoadDetails.browserCore.setScene(scene, null);
        } else {
            VRMLNode root = scene.getRootNode();
            VRMLWorldRootNodeType world = (VRMLWorldRootNodeType)root;

            // Get the children nodes and then force the world root to delete
            // them. This is because if we leave the nodes as part of the world
            // root, they have a Java3D parent. If we try to add them later on
            // to part of the live scene graph, they will generate multiple
            // parent exceptions. This avoids that problem.
            VRMLNodeType[] children = world.getChildren();
            world.setChildren((VRMLNodeType)null);

            // Since createVrmlFromUrl returns back to the space it was called
            // in, the route manager just adds a big pile of routes to the loaded
            // space.
            List<ROUTE> listRoutes = scene.getRoutes();
            int size = listRoutes.size();
            for (int i = 0; i < size && !terminateCurrent; i++)
            {
                ROUTE route = listRoutes.get(i);
                worldLoadDetails.routeManager.addRoute(worldLoadDetails.executionSpace, route);
            }

            if(terminateCurrent)
                return;

            try 
            {
                worldLoadDetails.vrmlNodeType.setValue(worldLoadDetails.fieldIndex,
                                      children,
                                      children.length);
            } 
            catch(VRMLException ife) 
            {
                reporter.errorReport(SET_FIELD_MSG, ife);
            }
        }
    }

    @Override
    public void abortCurrentFile() {
        terminateCurrent = true;
    }

    @Override
    public void shutdown() {
        terminateCurrent = true;
    }
}
