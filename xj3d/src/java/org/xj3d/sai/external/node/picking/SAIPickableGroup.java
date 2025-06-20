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

package org.xj3d.sai.external.node.picking;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.picking.PickableGroup;

/** A concrete implementation of the PickableGroup node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPickableGroup extends SAINode implements PickableGroup {

/** The children inputOutput field */
private MFNode children;

/** The addChildren inputOnly field */
private MFNode addChildren;

/** The removeChildren inputOnly field */
private MFNode removeChildren;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** The pickable inputOutput field */
private SFBool pickable;

/** The objectType inputOutput field */
private MFString objectType;

/** Constructor
     * @param queue */ 
public SAIPickableGroup ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFNode items in the children field. 
 * @return the number of MFNode items in the children field.  */
    @Override
    public int getNumChildren() {
  if ( children == null ) { 
    children = (MFNode)getField( "children" ); 
  }
  return( children.getSize( ) );
}

/** Return the children value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
    @Override
    public void getChildren(X3DNode[] val) {
  if ( children == null ) { 
    children = (MFNode)getField( "children" ); 
  }
  children.getValue( val );
}

/** Set the children field. 
 * @param val The X3DNode[] to set.  */
    @Override
    public void setChildren(X3DNode[] val) {
  if ( children == null ) { 
    children = (MFNode)getField( "children" ); 
  }
  children.setValue( val.length, val );
}

/** Set the addChildren field. 
 * @param val The X3DNode[] to set.  */
    @Override
    public void addChildren(X3DNode[] val) {
  if ( addChildren == null ) { 
    addChildren = (MFNode)getField( "addChildren" ); 
  }
  addChildren.setValue( val.length, val );
}

/** Set the removeChildren field. 
 * @param val The X3DNode[] to set.  */
    @Override
    public void removeChildren(X3DNode[] val) {
  if ( removeChildren == null ) { 
    removeChildren = (MFNode)getField( "removeChildren" ); 
  }
  removeChildren.setValue( val.length, val );
}

/** Return the bboxCenter value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getBboxCenter(float[] val) {
  if ( bboxCenter == null ) { 
    bboxCenter = (SFVec3f)getField( "bboxCenter" ); 
  }
  bboxCenter.getValue( val );
}

/** Set the bboxCenter field. 
 * @param val The float[] to set.  */
    @Override
    public void setBboxCenter(float[] val) {
  if ( bboxCenter == null ) { 
    bboxCenter = (SFVec3f)getField( "bboxCenter" ); 
  }
  bboxCenter.setValue( val );
}

/** Return the bboxSize value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getBboxSize(float[] val) {
  if ( bboxSize == null ) { 
    bboxSize = (SFVec3f)getField( "bboxSize" ); 
  }
  bboxSize.getValue( val );
}

/** Set the bboxSize field. 
 * @param val The float[] to set.  */
    @Override
    public void setBboxSize(float[] val) {
  if ( bboxSize == null ) { 
    bboxSize = (SFVec3f)getField( "bboxSize" ); 
  }
  bboxSize.setValue( val );
}

/** Return the pickable boolean value. 
 * @return The pickable boolean value.  */
    @Override
    public boolean getPickable() {
  if ( pickable == null ) { 
    pickable = (SFBool)getField( "pickable" ); 
  }
  return( pickable.getValue( ) );
}

/** Set the pickable field. 
 * @param val The boolean to set.  */
    @Override
    public void setPickable(boolean val) {
  if ( pickable == null ) { 
    pickable = (SFBool)getField( "pickable" ); 
  }
  pickable.setValue( val );
}

/** Return the number of MFString items in the objectType field. 
 * @return the number of MFString items in the objectType field.  */
    @Override
    public int getNumObjectType() {
  if ( objectType == null ) { 
    objectType = (MFString)getField( "objectType" ); 
  }
  return( objectType.getSize( ) );
}

/** Return the objectType value in the argument String[]
 * @param val The String[] to initialize.  */
    @Override
    public void getObjectType(String[] val) {
  if ( objectType == null ) { 
    objectType = (MFString)getField( "objectType" ); 
  }
  objectType.getValue( val );
}

/** Set the objectType field. 
 * @param val The String[] to set.  */
    @Override
    public void setObjectType(String[] val) {
  if ( objectType == null ) { 
    objectType = (MFString)getField( "objectType" ); 
  }
  objectType.setValue( val.length, val );
}

}
