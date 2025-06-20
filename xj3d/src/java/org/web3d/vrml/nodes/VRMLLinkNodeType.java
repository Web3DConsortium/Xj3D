/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * Representation of a node that represents navigatable links to other files.
 * <p>
 *
 * Link nodes do not require content to come from other places, but they do
 * want to provide information about where that object might allow the user to
 * navigate too.
 * <p>
 *
 * All URLs returned from the {@link #getUrl()} call are required to be fully
 * qualified. The one exception are links within the same world. These links
 * shall have the first character start with '#'. This will then refer to the
 * DEF name of a viewpoint that is the new location to travel to.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface VRMLLinkNodeType
{
    /**
     * Get the description to associate with the link. This is a line of text
     * suitable for mouseovers, status information etc. If there is no
     * description set then it returns null.
     *
     * @return The current description or null
     */
    String getDescription();

    /**
     * Set the description string for this link. Setting a value of null will
     * clear the current description.
     *
     * @param desc The new description to set
     */
    void setDescription(String desc);
    
    /**
     * Get the autoRefresh value to associate with the link.
     * @return The current autoRefresh time
     */
    double getAutoRefresh();

    /**
     * Set the autoRefresh value for this link.
     * @param newValue The new autoRefresh value to set
     */
    void setAutoRefresh(double newValue);
    
    /**
     * Get the autoRefreshTimeLimit value to associate with the link.
     * @return The current autoRefreshTimeLimit time
     */
    double getAutoRefreshTimeLimit();

    /**
     * Set the autoRefreshTimeLimit value for this link.
     * @param newValue The new autoRefreshTimeLimit value to set
     */
    void setAutoRefreshTimeLimit(double newValue);
    
    /**
     * Get the load value to associate with the link.
     * @return The current load time
     */
    boolean getLoad();

    /**
     * Set the load value for this link.
     * @param newValue The new load value to set
     */
    void setLoad(boolean newValue);

    /**
     * Get the current list of parameters registered for this link. If there
     * are none set then this will return null. No format checking of the
     * strings are performed.
     *
     * @return The list of current parameter values or null
     */
    String[] getParameter();

    /**
     * Set the parameter list to the new series of values. A value of null for
     * the parameter list will clear the current list.
     *
     * @param params The new list of parameters to use
     */
    void setParameter(String[] params);

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    void setWorldUrl(String url);

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    String[] getUrl();

    /**
     * Set the URL to a new value. If the value is null, it removes the old
     * contents (if set) and treats it as though there is no content.
     *
     * @param url The list of urls to set or null
     * @param numValid The number of valid values to copy from the array
     */
    void setUrl(String[] url, int numValid);
}
