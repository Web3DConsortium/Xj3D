/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.j3d.util.HashSet;

/**
 * MFTime field object.
 *  <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class MFTime extends FieldScriptableObject {

    private static final String OBJECT_NOT_FLOAT_MSG =
        "The object you attempted to assign was not a double instance";

    /** The properties of this class */
    private ArrayList<ReusableFloat> valueList;

    /** Representation of the length as a class */
    private ReusableInteger sizeInt;

    /** Set of the valid property names for this object */
    private static final HashSet<String> propertyNames;

    /** Set of the valid function names for this object */
    private static final HashSet<String> functionNames;

    /** The Javascript Undefined value */
    private static Object jsUndefined;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("length");

        functionNames = new HashSet<>();
        functionNames.add("toString");
        functionNames.add("equals");

        jsUndefined = Context.getUndefinedValue();
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public MFTime() {
        super("MFTime");

        sizeInt = new ReusableInteger(0);
        valueList = new ArrayList<>();
    }

    /**
     * Construct a field based on the given array of data (sourced from a node).
     * @param values array of initial values
     * @param numValid The number of valid values to copy from the array number of valid values to use from the array
     */
    public MFTime(double[] values, int numValid) {
        this(); // invoke default constructor

        if(numValid > 0) {
            for(int i = 0; i < numValid; i++)
                valueList.add(new ReusableFloat(values[i]));

            sizeInt.setValue(numValid);
        } else {
            sizeInt.setValue(0);
        }
    }

    /**
     * Construct a field based on an array of times.
     *
     * @param args the objects
     */
    public MFTime(Object[] args) {
        this(); // invoke default constructor

        int cnt=0;

        for (Object arg : args) {
            if (arg == jsUndefined) {
                continue;
            }
            if (!(arg instanceof Number)) {
                throw new IllegalArgumentException("Non number given");
            }
            cnt++;
            valueList.add(new ReusableFloat(((Number) arg).doubleValue()));
        }

        sizeInt.setValue(cnt);
    }

    //----------------------------------------------------------
    // Methods used by ScriptableObject reflection
    //----------------------------------------------------------

    /**
     * Constructor for a new Rhino object
     * @return 
     */
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean inNewExpr) {

        MFTime result = new MFTime(args);

        return result;
    }

    //----------------------------------------------------------
    // Methods defined by Scriptable
    //----------------------------------------------------------

    /**
     * Check for the indexed property presence.
     * @return 
     */
    @Override
    public boolean has(int index, Scriptable start) {
        return (index >= 0);
    }

    /**
     * Check for the named property presence.
     *
     * @param start
     * @return true if it is a defined eventOut or field
     */
    @Override
    public boolean has(String name, Scriptable start) {
        boolean ret_val = false;

        if(propertyNames.contains(name))
            ret_val = true;
        else
            ret_val = super.has(name, start);

        return ret_val;
    }

    /**
     * Get the value at the given index.
     *
     * @param index The position of the value to read
     * @param start The object where the lookup began
     * @return the corresponding value
     */
    @Override
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;
        ReusableFloat flt;

        if((index >= 0) && (index < valueList.size())) {
            ret_val = valueList.get(index);
        } else if(index >= 0) {
            // Not in the array but the spec says we must expand to meet this
            // new size and return a valid object
            for(int i = valueList.size(); i <= index; i++) {
                flt = new ReusableFloat(0);

                if (i == index)
                    ret_val = flt;

                valueList.add(flt);
            }
            sizeInt.setValue(valueList.size());
        }

        return ret_val;
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = null;

        if(propertyNames.contains(name)) {
            return sizeInt;
        } else {
            ret_val = super.get(name, start);

            // it could be that this instance is dynamically created and so
            // the function name is not automatically registex by the
            // runtime. Let's check to see if it is a standard method for
            // this object and then create and return a corresponding Function
            // instance.
            if((ret_val == null) && functionNames.contains(name))
                ret_val = locateFunction(name);
        }

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Sets a property based on the index. According to C.6.15.1 if the
     * index is greater than the current number of nodes, expand the size
     * by one and add the new value to the end.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(int index, Scriptable start, Object value) {

        if(readOnly && !scriptField) {
            Context.reportError(READONLY_MSG);
            return;
        }

        if(!(value instanceof Number)) {
            Context.reportError(OBJECT_NOT_FLOAT_MSG);
            return;
        }

        Number num = (Number)value;
        ReusableFloat rf = new ReusableFloat(num.doubleValue());

        if(index >= valueList.size()) {
            int toAdd = index - valueList.size();

            valueList.ensureCapacity(index+1);

            // Add default values
            for(int i=0; i < toAdd; i++) {
                valueList.add(new ReusableFloat(0));
            }

            valueList.add(rf);
            sizeInt.setValue(valueList.size());
        } else if(index >= 0) {
            valueList.set(index, rf);
        }

        dataChanged = true;
    }

    /**
     * Sets the named property with a new value. We don't allow the users to
     * dynamically change the length property of this node. That would cause
     * all sorts of problems. Therefore it is read-only as far as this
     * implementation is concerned.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.24
    //

    /**
     * Creates a string version of this node. Just calls the standard
     * toString() method of the object.
     *
     * @return A VRML string representation of the field
     */
    public String jsFunction_toString() {
        return toString();
    }

    /**
     * Comparison of this object to another of the same type. Just calls
     * the standard equals() method of the object.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    public boolean jsFunction_equals(Object val) {
        return equals(val);
    }

    //----------------------------------------------------------
    // Methods defined by Object.
    //----------------------------------------------------------

    /**
     * Format the internal values of this field as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this field
     */
    @Override
    public String toString() {

        StringBuilder buf = new StringBuilder();
        int size = valueList.size();
        ReusableFloat flt;

        for(int i = 0; i < size; i++) {
            flt = valueList.get(i);
            buf.append(flt);
            buf.append(' ');
        }

        return buf.toString();
    }

    /**
     * Compares two objects for equality base on the components being
     * the same.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    @Override
    public boolean equals(Object val) {
        if(!(val instanceof MFTime))
            return false;

        MFTime o = (MFTime)val;

        int size = valueList.size();

        if(size != o.valueList.size())
            return false;

        return valueList.equals(o.valueList);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Update the node's raw data from the underlying model. If this wrapper
     * has a local changed copy of the data that has not yet been committed to
     * the underlying model, this request is ignored and the current data
     * stays.
     * @param values array of initial values
     * @param numValid The number of valid values to copy from the array number of valid values to use from the array
     */
    public void updateRawData(double[] values, int numValid) {
        if(dataChanged)
            return;

        valueList.clear();

        if(numValid != 0) {
            // back fill the list with empty data.
            valueList.ensureCapacity(numValid);
            for(int i = 0; i < numValid; i++)
                valueList.add(new ReusableFloat(values[i]));
        }

        sizeInt.setValue(numValid);
    }

    /**
     * Get the array of underlying double values.
     *
     * @return An array of the values
     */
    public double[] getRawData() {
        double[] ret_val = new double[valueList.size()];

        for(int i=0; i < valueList.size();i++) {
            ret_val[i] = valueList.get(i).doubleValue();
        }

        return ret_val;
    }

    /**
     * Alternative form to fetch the raw value by copying it into the provided
     * array.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(double[] value) {
        for(int i=0; i < valueList.size();i++) {
            value[i] = valueList.get(i).doubleValue();
        }
    }
}
