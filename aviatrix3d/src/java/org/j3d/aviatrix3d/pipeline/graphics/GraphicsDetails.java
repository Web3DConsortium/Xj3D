/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderDetails;

/**
 * Class for passing the detailed rendering information through the pipeline.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.0 $
 */
public class GraphicsDetails extends RenderDetails
{
    /** The transform from the root of the scene graph to here */
    public float[] transform;
    
    /**
     * Construct a default instance with nothing initialised.
     */
    public GraphicsDetails()
    {
        transform = new float[16];
    }
}
