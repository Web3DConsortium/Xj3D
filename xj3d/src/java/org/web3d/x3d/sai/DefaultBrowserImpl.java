/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Map;

/**
 * The factory implementation interface for obtaining references to browser
 * instances.
 * <p>
 * Any implementation of a VRML browser that wishes to provide their own
 * customized version of the browser factory should must subclass this class.
 * In particular this is useful if the implementation needs to stay within the
 * package defined by the application for other reasons.
 * <p>
 * A default implementation of this class is the PlainBrowserFactoryImpl which
 * is package access only.
 *
 * @version 1.1 29 August 1998
 */
class DefaultBrowserImpl implements BrowserFactoryImpl {

    private static final String ERR_MSG =
        "Default Browser implementation cannot create browser reference";

    /**
     * Create a VRML browser that can be used as an AWT component. The component
     * returned is guaranteed to be an instance of VrmlComponent.
     *
     * @param params Parameters to control the look and feel.
     * @return The component browser initialised to be empty.
     * @exception NotSupportedException The implementation does not support this
     *    type of VRML browser.
     * @see X3DComponent
     */
    @Override
    public X3DComponent createComponent(Map params)
        throws NotSupportedException {

        throw new NotSupportedException(ERR_MSG);
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
     */
    @Override
    @SuppressWarnings("deprecation")
    public ExternalBrowser getBrowser(java.applet.Applet applet)
        throws NotSupportedException, NoSuchBrowserException {

        throw new NotSupportedException(ERR_MSG);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ExternalBrowser getBrowser(java.applet.Applet applet,
                                      String frameName,
                                      int index)
        throws NotSupportedException, NoSuchBrowserException {

        throw new NotSupportedException(ERR_MSG);
    }

    @Override
    public ExternalBrowser getBrowser(InetAddress address, int port)
        throws NotSupportedException,
               NoSuchBrowserException,
               UnknownHostException {

      throw new NotSupportedException(ERR_MSG);
    }
}





