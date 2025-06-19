/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2004-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.hanim;

// External imports
// None

// Local imports
// None

/**
 * Utility interface to mark an implementation that can generate each of the
 * HAnim nodes without needing to know the specifics of the rendering strategy
 * used.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface HAnimFactory
{
    /**
     * Create a new default Displacer instance.
     *
     * @return a new instance of the HAnimDisplacer object
     */
    HAnimDisplacer createDisplacer();

    /**
     * Create a new default Site instance.
     *
     * @return a new instance of the HAnimSite object
     */
    HAnimSite createSite();

    /**
     * Create a new default Segment instance.
     *
     * @return a new instance of the HAnimSegment object
     */
    HAnimSegment createSegment();

    /**
     * Create a new default Joint instance.
     *
     * @return a new instance of the HAnimJoint object
     */
    HAnimJoint createJoint();

    /**
     * Create a new default Humanoid instance.
     *
     * @return a new instance of the HAnimHumanoid object
     */
    HAnimHumanoid createHumanoid();

    /**
     * Create a new empty manager instance.
     *
     * @return a new instance of the HumanoidManager object
     */
    HumanoidManager createManager();

}
