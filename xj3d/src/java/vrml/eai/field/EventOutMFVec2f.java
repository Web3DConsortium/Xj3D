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

/**
 * VRML eventIn class for MFVec2f.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutMFVec2f extends EventOutMField
{
  /**
   * Construct an instance of this class. The superclass is called with the
   * type MFVec2f
   */
  protected EventOutMFVec2f()
  {
    super(MFVec2f);
  }

  /**
   * Get the value of the array of 2D vectors. Output is an array of floats
   *  <p>
   * @return The array of vec2f values where <br>
   *    value[i][0] = X <br>
   *    value[i][1] = Y
   */
  public abstract float[][] getValue();


  /**
   * Write the value of the event out to the given array.
   *
   * @param vec The array to be filled in where <br>
   *    vec[i][0] = X <br>
   *    vec[i][1] = Y
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[][] vec);

  /**
   * Get the values of the event out flattened into a single 1D array. The
   * array must be at least 3 times the size of the array.
   *
   * @param vec The array to be filled in where the
   *   vec[i + 0] = X <br>
   *   vec[i + 1] = Y
   * @exception ArrayIndexOutOfBoundsException The provided array was too small
   */
  public abstract void getValue(float[] vec);

  /**
   * Get a particular vector value in the given eventOut array.
   *  <p>
   * If the index is out of the bounds of the current array of data values an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param index The position to get the vector value from
   * @return The array of vector values where <br>
   *    value[0] = X <br>
   *    value[1] = Y
   *
   * @exception ArrayIndexOutOfBoundsException The index was outside the current data
   *    array bounds.
   */
  public abstract float[] get1Value(int index);

  /**
   * Get the value of a particular vector value in the event out array.
   *
   * @param index The position to get the vector value from.
   * @param vec The array to place the value in where.
   *    vec[0] = X <br>
   *    vec[1] = Y
   * @exception ArrayIndexOutOfBoundsException The provided array was too small or
   *     the index was outside the current data array bounds.
   */
  public abstract void get1Value(int index, float[] vec);
}









