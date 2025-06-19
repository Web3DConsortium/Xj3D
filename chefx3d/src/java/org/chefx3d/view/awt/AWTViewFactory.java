/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.view.awt;

//External Imports
import java.util.Map;

//Internal Imports
import org.chefx3d.view.View;
import org.chefx3d.view.ViewFactory;
import org.chefx3d.model.WorldModel;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * Factory for creating Views
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class AWTViewFactory implements ViewFactory {

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * A new AWTViewFactory
     *
     */
    public AWTViewFactory() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Create a view.
     *
     * @param model
     * @return The View
     */
    @Override
    public View createView(WorldModel model, int type) {
        return createView(model, type, null);
    }

    /**
     * Create a view.
     *
     * @param model
     * @param type The type of view to create
     * @param params View specific parameters.
     * @return The View
     */
    @Override
    public View createView(WorldModel model, int type, Map<String, String> params) {

        String initialWorld = null;

        if (params != null) {
            initialWorld = params.get(PARAM_INITIAL_WORLD);
        }

        switch(type) {
            case PICTURE_VIEW:
                return null;
            case OPENMAP_VIEW:
                errorReporter.messageReport("OpenMap View not implemented yet");
                return null;
            case TOP_X3D_VIEW:
                errorReporter.messageReport("3D Top View not implemented yet");
                return null;
            case LEFT_X3D_VIEW:
                errorReporter.messageReport("3D Left View not implemented yet");
                return null;
            case RIGHT_X3D_VIEW:
                errorReporter.messageReport("3D Right View not implemented yet");
                return null;
            case PERSPECTIVE_X3D_VIEW:
                return new Watcher3DView(model, initialWorld);

            default:
                IllegalArgumentException e = new IllegalArgumentException("Unsupported view type: " + type);
                errorReporter.errorReport(e.getMessage(), e);
                throw e;
        }

    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
