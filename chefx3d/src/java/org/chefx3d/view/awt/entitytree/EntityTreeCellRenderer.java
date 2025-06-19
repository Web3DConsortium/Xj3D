/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.view.awt.entitytree;

// External imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

// Local imports
import org.chefx3d.model.*;
import org.chefx3d.tool.*;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * An implementation of the TreeCellRenderer interface to provided a renderer
 * for Entity specific capabilities.
 * <p>
 *
 * This cell renderer is very simple - it just displays a label with the text
 * name of the node type and any relvant information about it.
 *
 * @author Russell Dodds
 * @version $Revision: 1.8 $
 */
public class EntityTreeCellRenderer extends JLabel implements TreeCellRenderer {

    private static final String DEFAULT_WORLD_LABEL = "World Model";

    /** Flag indicating this instance has been selected */
    private boolean selected;

    /** Flag indicating this instance has focus currently */
    private boolean focused;

    /** Flag indicating if icon should have a border around it */
    private boolean iconFocusBorder;

    /** Color for selected text */
    private Color textSelectColor;

    /** Color for unselected text */
    private Color textUnselectColor;

    /** Color of selected background */
    private Color bgSelectColor;

    /** Color of unselected background */
    private Color bgUnselectColor;

    /** Color of the selected border */
    private Color borderColor;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** The mapping of node types (shorts) to strings */
    // private static final ShortHashMap nodeNameMap;
    /**
     * Create a new instance of this renderer. Initialises a lot of values.
     */
    public EntityTreeCellRenderer() {
        /*
        textSelectColor = UIManager.getColor("Tree.selectionForeground");
        textUnselectColor = UIManager.getColor("Tree.textForeground");
        bgSelectColor = UIManager.getColor("Tree.selectionBackground");
        bgUnselectColor = UIManager.getColor("Tree.textBackground");
        borderColor = UIManager.getColor("Tree.selectionBorderColor");

        Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
        iconFocusBorder = (value != null && ((Boolean) value).booleanValue());

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        */
    }

    /**
     * Request the renderer that suits the given value type and for the given
     * tree.
     *
     * @param tree The source tree this node comes from
     * @param value The DOMTreeNode to be rendered
     * @param selected True if the node is selected
     * @param expanded True if expanded
     * @param leaf
     * @param row The row this node is on
     * @param hasFocus True if the node currently has focus
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        String name;
        Entity entity;

        this.selected = selected;
        this.focused = hasFocus;

        if (value instanceof EntityTreeNode) {

            entity = ((EntityTreeNode) value).getEntity();
            name = entity.getName() + " [ID: " + entity.getEntityID()
                    + "]";

            setText(name);
            setIcon(null);

        } else if (value instanceof VertexTreeNode) {

            double[] pos = ((VertexTreeNode) value).getVertexPosition();

            if (pos == null)  {
                name = "Vertex " + ((VertexTreeNode) value).getVertexID();

            } else {
                name = "Vertex " + ((VertexTreeNode) value).getVertexID() + " [" +
                pos[0] + ", " + pos[2] + "]";
            }

            setText(name);
            setIcon(null);

        } else if (value instanceof WorldTreeNode) {

            // set the default label
            name = DEFAULT_WORLD_LABEL;

            // get the world model
            WorldModel model = ((WorldTreeNode) value).getModel();

            // get the zero entity, check to see if location
            Entity[] entities = model.getModelData();
            if (entities.length > 0) {

                for (Entity entitie : entities) {
                    if (entitie != null) {
                        entity = entitie;
                        if (entity.getType() == Tool.TYPE_WORLD) {
                            name = entity.getName();
                            break;
                        }
                    }
                }
            }

            setText(name);
            setIcon(null);

        }

        setComponentOrientation(tree.getComponentOrientation());

        return this;
    }

    /**
     * Override the base class to properly set the painting. Code mostly stolen
     * from DefaultTreeCellRenderer without all the extra crap.
     *
     * @param g The graphics context to paint with
     */
    @Override
    public void paint(Graphics g) {
        int imageOffset = -1;
        int width = getWidth();
        int height = getHeight();

        if (focused) {
            if (iconFocusBorder) {
                imageOffset = 0;
            } else if (imageOffset == -1) {
                imageOffset = getLabelStart();
            }
        }

        if (selected) {
            g.setColor(bgSelectColor);
            if (getComponentOrientation().isLeftToRight()) {
                g.fillRect(imageOffset, 0, width - 1 - imageOffset, height);
            } else {
                g.fillRect(0, 0, width - 1 - imageOffset, height);
            }

            g.setColor(textSelectColor);
            setForeground(textSelectColor);
        } else {
            g.setColor(textSelectColor);
            setForeground(textSelectColor);
        }

        if (focused) {
            g.setColor(borderColor);
            if (getComponentOrientation().isLeftToRight()) {
                g.drawRect(imageOffset, 0, width - 1 - imageOffset, height - 1);
            } else {
                g.drawRect(0, 0, width - 1 - imageOffset, height - 1);
            }

        }
        super.paint(g);
    }

    /**
     * Convenience method to determine where the text should start if there is
     * an icon provided.
     *
     * @return number of pixels offset
     */
    private int getLabelStart() {
        Icon currentI = getIcon();
        int ret_val = 0;
        if (currentI != null && getText() != null) {
            ret_val = currentI.getIconWidth()
                    + Math.max(0, getIconTextGap() - 1);
        }

        return ret_val;
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
