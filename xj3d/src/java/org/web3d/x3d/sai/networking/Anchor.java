/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai.networking;

import org.web3d.x3d.sai.X3DGroupingNode;

/**
 * Defines the requirements of an X3D Anchor node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Anchor extends X3DGroupingNode {

    /**
     * Return the description String value.
     *
     * @return The description String value.
     */
    String getDescription();

    /**
     * Set the description field.
     *
     * @param val The String to set.
     */
    void setDescription(String val);

    /**
     * Return the number of MFString items in the parameter field.
     *
     * @return the number of MFString items in the parameter field.
     */
    int getNumParameter();

    /**
     * Return the parameter value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getParameter(String[] val);

    /**
     * Set the parameter field.
     *
     * @param val The String[] to set.
     */
    void setParameter(String[] val);

    /**
     * Return the number of MFString items in the url field.
     *
     * @return the number of MFString items in the url field.
     */
    int getNumUrl();

    /**
     * Return the url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getUrl(String[] val);

    /**
     * Set the url field.
     *
     * @param val The String[] to set.
     */
    void setUrl(String[] val);
    
    /**
     * Get the autoRefresh value for this link.
     * @return 
     */
    public double getAutoRefresh();

    /**
     * Set the autoRefresh value for this link
     * @param newValue
     */
    public void setAutoRefresh (double newValue);

    /**
     * Get the autoRefreshTimeLimit value to associate with the link.
     * @return The current autoRefreshTimeLimit value
     */
    public double getAutoRefreshTimeLimit();

    /**
     * Set the autoRefreshimeLimit value for this link.
     * @param newValue The new autoRefresh value to set
     */
    public void setAutoRefreshTimeLimit (double newValue);
}
