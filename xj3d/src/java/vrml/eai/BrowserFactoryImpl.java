/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai;

import java.net.UnknownHostException;
import java.net.InetAddress;

/**
 * The factory implementation interface for obtaining references to browser
 * instances.
 *  <p>
 * Any implementation of a VRML browser that wishes to provide their own
 * customized version of the browser factory should must subclass this class.
 * In particular this is useful if the implementation needs to stay within the
 * package defined by the application for other reasons.
 *  <p>
 * A default implementation of this class is the DefaultBrowserFactoryImpl which
 * is package access only.
 *
 * @version 1.1 29 August 1998
 */
public interface BrowserFactoryImpl
{
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
  VrmlComponent createComponent(String[] params)
    throws NotSupportedException;

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
  @SuppressWarnings("deprecation")
  Browser getBrowser(java.applet.Applet applet)
    throws NotSupportedException, NoSuchBrowserException, ConnectionException;

  /**
   * Get a browser from the given java applet reference one some named page and
   * at some embed location. Used when attempting to access a browser on
   * another HTML page within a multi-framed environment, or if there are a
   * number of VRML browser instances located on the same page.
   *  <p>
   * If the frame name is a zero length string or null then it is assumed to be
   * located on the same HTML page as the applet. The index is the number of
   * the embed VRML browser starting from the top of the page. If there are
   * other non-VRML plugins embedded in the page these are not taken into
   * account in calculating the embed index.
   *
   * @param applet The applet reference to use
   * @param frameName The name of the frame to look into for the browser
   * @param index The embed index of the VRML browser in the page
   * @return A reference to the Browser implementation
   * @exception NotSupportedException The implementation does not support this
   *    type of VRML browser.
   * @exception NoSuchBrowserException Could not locate a VRML browser on the
   *    same page as the applet.
   * @exception ConnectionException An error occurred during the connecting
   *    process
   */
  @SuppressWarnings("deprecation")
  Browser getBrowser(java.applet.Applet applet, String frameName, int index)
    throws NotSupportedException, NoSuchBrowserException, ConnectionException;

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
  Browser getBrowser(InetAddress address, int port)
    throws NotSupportedException,
           NoSuchBrowserException,
           UnknownHostException,
           ConnectionException;
}





