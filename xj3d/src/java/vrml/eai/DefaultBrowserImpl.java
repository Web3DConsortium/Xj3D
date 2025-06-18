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
 * A default implementation of this class is the PlainBrowserFactoryImpl which
 * is package access only.
 *
 * @version 1.1 29 August 1998
 */
class DefaultBrowserImpl
  implements BrowserFactoryImpl
{
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
   * @see VrmlComponent
   */
  @Override
  public VrmlComponent createComponent(String[] params)
    throws NotSupportedException
  {
    throw new NotSupportedException(ERR_MSG);
  }

  @Override
  @SuppressWarnings("deprecation")
  public Browser getBrowser(java.applet.Applet applet)
    throws NotSupportedException, NoSuchBrowserException
  {
    throw new NotSupportedException(ERR_MSG);
  }

  @Override
  @SuppressWarnings("deprecation")
  public Browser getBrowser(java.applet.Applet applet, String frameName, int index)
    throws NotSupportedException, NoSuchBrowserException
  {
    throw new NotSupportedException(ERR_MSG);
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
   * @exception NoSuchHostException Could not find the machine named in the
   *    address.
   */
  @Override
  public Browser getBrowser(InetAddress address, int port)
    throws NotSupportedException, NoSuchBrowserException, UnknownHostException
  {
    throw new NotSupportedException(ERR_MSG);
  }
}





