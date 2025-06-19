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

package org.chefx3d.property;

// External Imports
import org.w3c.dom.Node;

/**
 * A holder for panel layout.
 * 
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public class PanelItem {

    public enum DataEditorType {LABEL, TEXTFIELD, DATAEDITOR, VECTORFIELD};
    
    /** This item is A label */
    //public static final int TYPE_LABEL = 0;

    /** This item is A text field */
    //public static final int TYPE_TEXTFIELD = 1;

    /** This item is A custom data editor */
    //public static final int TYPE_DATAEDITOR = 2;

    private DataEditorType type;

    private int col;

    private Node node;

    private Object component;

    public PanelItem(DataEditorType type, int col, Node node, Object component) {
        this.type = type;
        this.col = col;
        this.node = node;
        this.component = component;
    }

    public void setType(DataEditorType t) {
        type = t;
    }

    public DataEditorType getType() {
        return type;
    }

    public void setCol(int c) {
        col = c;
    }

    public int getCol() {
        return col;
    }

    public void setComponent(Object c) {
        component = c;
    }

    public Object getComponent() {
        return component;
    }

    public void setNode(Node n) {
        node = n;
    }

    public Node getNode() {
        return node;
    }
}
