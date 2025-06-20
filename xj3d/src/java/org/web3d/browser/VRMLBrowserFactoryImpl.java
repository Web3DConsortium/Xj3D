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

package org.web3d.browser;

// External imports
import java.lang.reflect.InvocationTargetException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.Map;

// Local imports
import vrml.eai.BrowserFactoryImpl;
import vrml.eai.NotSupportedException;
import vrml.eai.VrmlComponent;
import vrml.eai.ConnectionException;
import vrml.eai.Browser;
import vrml.eai.NoSuchBrowserException;

/**
 * An implementation of the EAI {@link vrml.eai.BrowserFactoryImpl} interface
 * that creates a browser that uses Java3D for the renderer.
 * <p>
 * This browser factory delegates to the other known browser factories based on
 * a hard coded search sequence and an optional user supplied renderer hint.
 *
 * @author Brad Vender
 * @version $Revision: 1.9 $
 */
public class VRMLBrowserFactoryImpl implements BrowserFactoryImpl {

    /** The factory which deals with networked connections. */
    private static BrowserFactoryImpl networkFactory;

    /** The factory to use if OpenGL is requested */
    private static BrowserFactoryImpl openglFactory;

    /** The factory to use if no hint is supplied */
    private static final BrowserFactoryImpl DEFAULT_FACTORY;

    /** Error message for passing a null String in loadURL */
    private static final String NULL_PARAMETER_ERROR = "Null parameter strings not allowed.";

    /** Error message for malformed parameter String in loadURL */
    private static final String MALFORMED_PARAMETER_STRING_ERROR =
        "Malformed parameter string."+
        "  Expecting strings of the form A=B";

    // Parameter names. Kept private because we don't want people directly
    // accessing this class in their code because that would stuff portability.
    private static final String RENDERER_TYPE_PARAM = "Xj3D_RendererType";

    /** Class name for network browser factory */
    private static final String NETWORK_FACTORY_CLASS =
        "org.web3d.vrml.scripting.external.neteai.NetworkBrowserFactoryImpl";

    /** Class name for OpenGL browser factory */
    private static final String OPENGL_FACTORY_CLASS =
        "org.xj3d.ui.awt.browser.ogl.VRMLOGLBrowserFactoryImpl";

    /** Locate existing browser factories.
     */
    static {
        try {
            openglFactory=(BrowserFactoryImpl) Class.forName(OPENGL_FACTORY_CLASS).getDeclaredConstructor().newInstance();
            networkFactory=(BrowserFactoryImpl) Class.forName(NETWORK_FACTORY_CLASS).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace(System.err);
        }

        DEFAULT_FACTORY=openglFactory;
    }

    /**
     * Create a VRML browser that can be used as an AWT component. The component
     * returned is guaranteed to be an instance of VrmlComponent.
     *
     * @param params Parameters to control the look and feel.
     * @return The component browser initialised to be empty.
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @see VrmlComponent
     */
    @Override
    public VrmlComponent createComponent(String[] params)
        throws NotSupportedException {

        BrowserFactoryImpl selected=DEFAULT_FACTORY;

        if((params != null) && (params.length != 0)) {
            Map<String, String> p_map = parseParameters(params);
            String val = p_map.get(RENDERER_TYPE_PARAM);
            if (val!=null)
                if(val.equalsIgnoreCase("opengl"))
                    selected=openglFactory;
                else if (val.equalsIgnoreCase("network"))
                    selected=networkFactory;
            /* Should an unexpected arg exception get thrown? */
        }
        if (selected==null)
            selected=DEFAULT_FACTORY;
        return selected.createComponent(params);
    }

    /**
     * Get a browser from the given java applet reference as a base in the
     * current HTML page. Used when attempting to access a browser on the current
     * page as this applet and is the first browser on the page. Generically, the
     * same as calling getBrowser(applet, "", 0);
     *
     * @param applet The applet reference to use
     * @return A reference to the Browser implementation
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser
     * @exception NoSuchBrowserException Could not locate a VRML browser on the
     *    same page as the applet.
     * @exception ConnectionException An error occurred during the connecting
     *    process
     */
    @Override
    @SuppressWarnings("deprecation")
    public Browser getBrowser(java.applet.Applet applet)
        throws NotSupportedException,
               NoSuchBrowserException,
               ConnectionException {
        throw new NotSupportedException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public Browser getBrowser(java.applet.Applet applet, String frameName, int index)
        throws NotSupportedException,
               NoSuchBrowserException,
               ConnectionException {
        throw new NotSupportedException();
    }

    /**
     * Get a reference to a browser that is located on a remote machine. This
     * a server application to send scene updates to a number of client browsers
     * located on remote machines. If there are a number of browsers running on
     * a remote machine, they can be differentiated by the port number they are
     * listening on.
     *  <p>
     * There is no default port number for VRML browsers.
     *
     * @param address The address of the machine to connect to
     * @param port The port number on that machine to connect to.
     * @return A reference to the Browser implementation
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @exception NoSuchBrowserException Could not locate a VRML browser on the
     *    same page as the applet.
     * @exception UnknownHostException Could not find the machine named in the
     *    address.
     * @exception ConnectionException An error occurred during the connecting
     *    process
     */
    @Override
    public Browser getBrowser(InetAddress address, int port)
        throws NotSupportedException,
               NoSuchBrowserException,
               UnknownHostException,
               ConnectionException {
        if (networkFactory!=null)
            return networkFactory.getBrowser(address,port);
        else
            throw new NotSupportedException();
    }

    /**
     * Parse all the strings and place them into a map so it is easy to look
     * them up. Assumes the list is non-null.
     *
     * @param params The given parameter list
     * @return a map of the parsed parameters
     */
    private Map<String, String> parseParameters(String[] params) {
        Map<String, String> ret_val = new HashMap<>();

        for (String param : params) {
            if (param == null) {
                throw new IllegalArgumentException(NULL_PARAMETER_ERROR);
            }
            int index = param.indexOf('=');
            if (index<1)
                throw new IllegalArgumentException(MALFORMED_PARAMETER_STRING_ERROR);
            ret_val.put(param.substring(0, index), param.substring(index + 1));
        }

        return ret_val;
    }

}
