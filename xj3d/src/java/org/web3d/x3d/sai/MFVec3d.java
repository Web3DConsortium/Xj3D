/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Representation of a MFVec3d field.
 *
 * @version 1.0 30 April 1998
 */
public interface MFVec3d extends MField {

    /**
     * Places a new value at the end of the existing value, increasing the field
     * length accordingly.
     *
     * @param value The value to append value[0] = X <br>
     * value[1] = Y <br>
     * value[2] = Z
     * @exception ArrayIndexOutOfBoundsException A value did not contain at
     * least three values for the vector definition.
     */
    public void append(double value[]);

    /**
     * Removes all values in the field and changes the field size to zero.
     */
    public void clear();

    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where <br>
     * vec[i][0] = X <br>
     * vec[i][1] = Y <br>
     * vec[i][2] = Z
     * @exception ArrayIndexOutOfBoundsException The provided array was too
     * small
     */
    public void getValue(double[][] vec);

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 3 times the size of the array.
     *
     * @param vec The array to be filled in where the vec[i + 0] = X <br>
     * vec[i + 1] = Y <br>
     * vec[i + 2] = Z <br>
     * @exception ArrayIndexOutOfBoundsException The provided array was too
     * small
     */
    public void getValue(double[] vec);

    /**
     * Get the value of a particular vector value in the event out array.
     *
     * @param index The position to get the vector value from.
     * @param vec The array to place the value in where. vec[0] = X <br>
     * vec[1] = Y <br>
     * vec[2] = Z
     * @exception ArrayIndexOutOfBoundsException The provided array was too
     * small or the index was outside the current data array bounds.
     */
    public void get1Value(int index, double[] vec);

    /**
     * Inserts a value into an existing index of the field. Current field values
     * from the index to the end of the field are shifted down and the field
     * length is increased by one to accommodate the new element.
     *
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     *
     * @param index The position at which to insert
     * @param value The new element to insert value[0] = X <br>
     * value[1] = Y <br>
     * value[2] = Z
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the
     * current field size.
     * @exception ArrayIndexOutOfBoundsException A value did not contain at
     * least three values for the vector definition.
     */
    public void insertValue(int index, double value[])
            throws ArrayIndexOutOfBoundsException;

    /**
     * Removes one value from the field. Values at indices above the removed
     * element will be shifted down by one and the size of the field will be
     * reduced by one.
     *
     * @param index The position of the value to remove.
     * @exception ArrayIndexOutOfBoundsException The index was outside the
     * current field size.
     */
    public void removeValue(int index)
            throws ArrayIndexOutOfBoundsException;

    /**
     * Set the value of the array of 3D vectors. Input is an array of doubles If
     * value[i] does not contain at least three values it will generate an
     * ArrayIndexOutOfBoundsException. If value[i] contains more than three
     * items only the first three values will be used and the rest ignored.
     * <p>
     * If one or more of the values for value[i] are null then the resulting
     * event that is sent to the VRML scene graph is implementation dependent but
     * no error indicator will be set here.
     *
     * @param numVec The number of items to copy from the array
     * @param value The array of vec3d values where <br>
     * value[i] = X <br>
     * value[i+1] = Y <br>
     * value[i+2] = Z
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at
     * least three values for the vector definition.
     */
    public void setValue(int numVec, double[] value);

    /**
     * Set the value of the array of 3D vectors. Input is an array of doubles If
     * value[i] does not contain at least three values it will generate an
     * ArrayIndexOutOfBoundsException. If value[i] contains more than three
     * items only the first three values will be used and the rest ignored.
     * <p>
     * If one or more of the values for value[i] are null then the resulting
     * event that is sent to the VRML scene graph is implementation dependent but
     * no error indicator will be set here.
     *
     * @param numVec The number of items to copy from the array
     * @param value The array of vec3d values where <br>
     * value[i][0] = X <br>
     * value[i][1] = Y <br>
     * value[i][2] = Z
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at
     * least three values for the vector definition.
     */
    public void setValue(int numVec, double[][] value);

    /**
     * Set a particular vector value in the given eventIn array. To the VRML
     * world this will generate a full MFVec3f event with the nominated index
     * value changed.
     * <p>
     * The value array must contain at least three elements. If the array
     * contains more than 3 values only the first 3 values will be used and the
     * rest ignored.
     * <p>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the vector value
     * @param value The array of vector values where <br>
     * value[0] = X <br>
     * value[1] = Y <br>
     * value[2] = Z
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at
     * least 3 values for the vector
     */
    public void set1Value(int index, double[] value);
}
