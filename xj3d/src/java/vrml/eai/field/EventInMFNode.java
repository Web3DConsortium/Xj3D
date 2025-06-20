/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.field;

import vrml.eai.Node;

/**
 * VRML eventIn class for MFNode.
 *  <p>
 * Set the values of a node array to the given values. The java
 * <code>null</code> reference is treated to be equivalent to the VRML
 * <code>NULL</code> field values.
 *  <p>
 * In the cases where this eventIn is used to represent eventIn
 * MFNode fields such as <code>addChildren</code> and
 * <code>removeChildren</code> calling the set methods with a zero length array
 * or null will result in nothing happening. No event will be propagated to the
 * VRML browser scene graph (although the browser native code may receive this
 * information internally).
 *  <p>
 * Where this eventIn is used to represent exposedFields such as the
 * <code>children</code> field of grouping nodes calling the set methods with
 * null or zero length arrays will result in that field being cleared of all
 * its children.
 *  <p>
 * If the eventIn is constructed from an exposedField where the <i>set_</i>
 * modifier has been specified this shall be treated as though a normal
 * exposedField has been called.
 *  <p>
 * It is legal to construct an array where some members of the array are
 * null pointers. Due to no specification on the intended result in the VRML
 * specification, the response given by the browser is implementation
 * dependent. Calls will not generate an exception, but the value of actual
 * event passed to the scene graph may vary until the issue is resolved.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInMFNode extends EventIn
{
  /**
   * Construct an instance of this class. The superclass constructor is called
   * with the type MFNode.
   */
  protected EventInMFNode()
  {
    super(MFNode);
  }

  /**
   * Set the value of the array of nodes. Input is an array of valid Node
   * references. If the length is zero or the node reference is null, then
   * the actions to take are according to the class introduction above. If the
   * array contains a null reference then th resulting event passed to the
   * eventIn is implementation dependent
   *  <p>
   * If any of the node references have had their dispose methods called, an
   * InvalidNodeException will be generated and no event sent to the
   * scene graph.
   *
   * @param value The array of node references
   */
  public abstract void setValue(Node[] value);

  /**
   * Set a particular node value in the given eventIn array. To the VRML
   * world this will generate a full MFNode event with the nominated index
   * value changed.
   *  <p>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated. If the Node value is
   * null the behaviour will be undefined as far as generating an event to the
   * scene graph is concerned in order to be consistent with the behaviour
   * described in the class introduction. This method call will not generate
   * an exception if the node reference is null.
   *  <p>
   * If the node reference passed to this method has already had the dispose
   * method called then an InvalidNodeException will be generated.
   *
   * @param index The position to set the colour value
   * @param value The node reference
   * @exception ArrayIndexOutOfBoundsException The index was out of bounds of the
   *     array currently.
   */
  public abstract void set1Value(int index, Node value);
}









