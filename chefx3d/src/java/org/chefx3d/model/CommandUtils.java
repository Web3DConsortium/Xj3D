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

package org.chefx3d.model;

//External Imports
import java.util.ArrayList;
import java.util.List;

//Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A set of helper methods for executing model Commands
 *
 * @author Russell Dodds
 * @version $Revision: 1.6 $
 */
public class CommandUtils  {

    /** The model */
    private BaseWorldModel model;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    public CommandUtils(WorldModel model) {

        // Cast to package definition to access protected methods
        this.model = (BaseWorldModel) model;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }


    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

    /**
     * A helper method to delete the currently selected entity, if one is
     * selected
     *
     * @param local
     * @param listener
     */
    public void resetModel(boolean local, ModelListener listener) {

        model.clear(local, listener);
        model.clearHistory();

        Selection select = new Selection(-1, -1, -1);
        List<Selection> selection = new ArrayList<>();
        selection.add(select);
        model.changeSelection(selection);

    }

    /**
     * A helper method to delete the currently selected entity, if one is
     * selected
     *
     * @param local
     * @param listener
     */
    public void removeSelectedEntity(boolean local, ModelListener listener) {

        // Get the currentlySelected Entity
        Entity selectedEntity = model.getSelectedEntity();

        if (selectedEntity != null) {
            if ((selectedEntity instanceof SegmentableEntity) &&
                (((SegmentableEntity)selectedEntity).isSegmentedEntity()) &&
                (((SegmentableEntity)selectedEntity).isVertexSelected())) {

                SegmentSequence segments = ((SegmentableEntity)selectedEntity).getSegmentSequence();
                int count = segments.getLength();

                if (count == 1) {

                    // Create the remove entity command
                    RemoveEntityCommand cmd = new RemoveEntityCommand(model, selectedEntity);
                    cmd.setErrorReporter(errorReporter);
                    model.applyCommand(cmd);

                    Selection sel = new Selection(-1, -1, -1);
                    List<Selection> list = new ArrayList<>(1);
                    list.add(sel);

                    model.changeSelection(list);

                } else {

                    int vertexID = ((SegmentableEntity)selectedEntity).getSelectedVertexID();

                    // Create the remove vertex command
                    RemoveVertexCommand cmd = new RemoveVertexCommand(
                            model, selectedEntity.getEntityID(), vertexID);
                    cmd.setErrorReporter(errorReporter);

                    if (((SegmentableEntity)selectedEntity).isFixedLength()) {

                        if (segments.isStart(vertexID) || segments.isEnd(vertexID)) {

                            model.applyCommand(cmd);

                        } else {

                            errorReporter.messageReport("Cannot delete internal segments in fixed mode.");
                            return;
                        }

                    } else {

                        model.applyCommand(cmd);

                    }

                    Selection sel = new Selection(selectedEntity.getEntityID(), -1, -1);
                    List<Selection> list = new ArrayList<>(1);
                    list.add(sel);

                    model.changeSelection(list);

                }

            } else {

                // Create the remove command
                RemoveEntityCommand cmd = new RemoveEntityCommand(model, selectedEntity);
                cmd.setErrorReporter(errorReporter);
                model.applyCommand(cmd);

                Selection sel = new Selection(-1, -1, -1);
                List<Selection> list = new ArrayList<>(1);
                list.add(sel);

                model.changeSelection(list);
            }
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
}
