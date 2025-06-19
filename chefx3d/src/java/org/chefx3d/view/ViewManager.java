/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

// Internal Imports
import org.chefx3d.model.*;
import org.chefx3d.tool.Tool;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * The manager of all views. Used to communicate to all views.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public class ViewManager {

    /** The singleton manager */
    private static ViewManager manager;

    /** The world model */
    private WorldModel model;

    /** The list of managed views */
    private List<View> views;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Private Constructor.
     */
    private ViewManager() {
        views = new ArrayList<>();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the ViewManager.
     *
     * @return The singleton view manager
     */
    public static ViewManager getViewManager() {
        if (manager == null) {
            manager = new ViewManager();
        }

        return manager;
    }

    /**
     * Sets the WorldModel to use as the backing ot he views
     *
     * @param model
     */
    public void setWorldModel(WorldModel model) {
        this.model = model;
    }

    /**
     * Add a view to be managed. Duplicates will be ignored.
     *
     * @param view The view to add
     */
    public void addView(View view) {
        if (!views.contains(view))
            views.add(view);
    }

    /**
     * Remove a view from management. If the view is not managed it will be
     * ignored.
     *
     * @param view The view to remove.
     */
    public void removeView(View view) {
        views.remove(view);
    }

    /**
     * Clear the WorldModel and Views
     */
    public void clear() {

        model = null;
        views.clear();

    }

    /**
     * Set the current tool.
     *
     * @param tool The tool
     */
    public void setTool(Tool tool) {
        if (tool == null) {
            return;
        }

        int type = tool.getToolType();

        if (type == Tool.TYPE_WORLD) {

            System.out.println("*** Adding World!");

            // YES by default
            int answer = JOptionPane.YES_OPTION;

            Entity[] entities = model.getModelData();

            for (Entity entitie : entities) {
                if ((entitie != null) && (entitie.getType() == Tool.TYPE_WORLD) && (entities.length > 1)) {
                    answer = JOptionPane.showConfirmDialog(null,
                            "Are you sure you you want to change locations?  " +
                                    "This will clear the current model and you cannot undo this command.",
                            "Select Location Action",
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }

            // If still YES then clear model
            if (answer == JOptionPane.YES_OPTION) {
                ClearModelCommand clearCmd = new ClearModelCommand(model);
                clearCmd.setErrorReporter(errorReporter);
                model.applyCommand(clearCmd);

                int entityID = model.issueEntityID();

                EntityBuilder builder = EntityBuilder.getEntityBuilder();

                Entity newEntity =
                    builder.createEntity(model, entityID, new double[3],
                            new float[] {0,1,0,0}, tool);

                AddEntityCommand cmd = new AddEntityCommand(model, newEntity);
                model.applyCommand(cmd);

                List<Entity> selected = new ArrayList<>();
                selected.add(newEntity);

                changeSelection(selected);
            }
        }

        for (View view : views) {
            view.setTool(tool);
        }
    }

    /**
     * Go into associate mode. The next selection in any view will issue a
     * selection event and do nothing else.
     * @param validTools
     */
    public void enableAssociateMode(String[] validTools) {
        //System.out.println("ViewManager.enableAssociateMode()");
        for (View view : views) {
            view.enableAssociateMode(validTools);
        }
    }

    /**
     * Exit associate mode.
     */
    public void disableAssociateMode() {
        //System.out.println("ViewManager.disableAssociateMode()");
        for (View view : views) {
            view.disableAssociateMode();
        }
    }

    /**
     * Set the helper display mode on all views.
     *
     * @param mode The helper mode
     */
    public void setHelperDisplayMode(int mode) {
        for (View view : views) {
            view.setHelperDisplayMode(mode);
        }
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Change the selected entity.
     *
     * @param id The entity selected
     * @param subid The sub entity id
     */
    private void changeSelection(List<Entity> selected) {

        List<Selection> list = new ArrayList<>(selected.size());

        for (Entity e : selected) {
            int segmentID = -1;
            int vertexID = -1;

            if (e instanceof SegmentableEntity) {
                segmentID = ((SegmentableEntity)e).getSelectedSegmentID();
                vertexID = ((SegmentableEntity)e).getSelectedVertexID();

            }
            Selection selection =
                    new Selection(e.getEntityID(), segmentID, vertexID);

            list.add(selection);
        }

        // if nothing is selected then select the location
        if (list.isEmpty()) {
            Entity location = model.getLocationEntity();
            Selection selection = new Selection(location.getEntityID(), -1, -1);

            list.add(selection);
        }

        model.changeSelection(list);
    }

}