/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.view;

// External Imports
import java.util.Map;

// Internal Imports
import org.chefx3d.model.WorldModel;
import org.chefx3d.util.ErrorReporter;

/**
 * Factory for creating Views
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public interface ViewFactory {

    int PICTURE_VIEW = 0;

    int OPENMAP_VIEW = 1;

    int TOP_X3D_VIEW = 2;

    int LEFT_X3D_VIEW = 3;

    int RIGHT_X3D_VIEW = 4;

    int PERSPECTIVE_X3D_VIEW = 5;

    int PROPERTY_VIEW = 6;

    int ENTITY_TREE_VIEW = 7;

    String PARAM_IMAGES_DIRECTORY = "imagesDirectory";
    String PARAM_INITIAL_WORLD = "initialWorld";

    /**
     * Create a view.
     *
     * @param model
     * @param type The type of view to create
     * @return The View
     */
    View createView(WorldModel model, int type);

    /**
     * Create a view.
     *
     * @param model
     * @param type The type of view to create
     * @param params View specific parameters.
     * @return The View
     */
    View createView(WorldModel model, int type, Map<String, String> params);

    void setErrorReporter(ErrorReporter reporter);

}
