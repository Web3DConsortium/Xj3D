/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External imports
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

// Local imports
import org.web3d.util.I18nUtils;
import org.web3d.vrml.lang.*;

import org.web3d.parser.DefaultFieldParserFactory;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.renderer.norender.NRNodeFactory;

import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.sav.StringContentHandler;

import org.web3d.vrml.util.FieldValidator;

/**
 * Utility class for converting between String and Binary data
 * representations and managing the setting of ContentHandler's
 * fieldValue() methods.
 *
 * @author Rex Melton
 * @version $Revision: 1.13 $
 */
public class FieldValueHandler {

    /** Data types */
    private static final int BOOLEAN = 0;
    private static final int BOOLEAN_ARRAY = 1;
    private static final int INT = 2;
    private static final int INT_ARRAY = 3;
    private static final int LONG = 4;
    private static final int LONG_ARRAY = 5;
    private static final int FLOAT = 6;
    private static final int FLOAT_ARRAY = 7;
    private static final int DOUBLE = 8;
    private static final int DOUBLE_ARRAY = 9;
    private static final int STRING = 10;
    private static final int STRING_ARRAY = 11;

    /** Content Handler Types */
    private static final int HANDLER_BINARY = 0;
    private static final int HANDLER_STRING = 1;
    private static final int HANDLER_NULL = 2;

    /**
     * Message for when a field range is invalid
     */
    private static final String INVALID_FIELD_RANGE_MSG_PROP =
            "xj3d.filter.FieldValueHandler.invalidFieldRange";

    /**
     * Message for when a field is unparsable
     */
    private static final String INVALID_FIELD_VALUE_MSG_PROP =
            "xj3d.filter.FieldValueHandler.invalidFieldValue";

    /** Reader used for field conversion */
    private VRMLFieldReader fieldReader;

    /** The Node factory */
    private VRMLNodeFactory nodeFactory;

    /** Map of nodes, keyed by name */
    private Map<String, VRMLNode> nodeMap;

    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, a StringContentHandler, or null */
    private int handlerType;

    /** Binary Content Handler reference */
    private BinaryContentHandler bch;

    /** String Content Handler reference */
    private StringContentHandler sch;

    /** How strict to parse */
    protected ParsingType parsingType = ParsingType.STRICT;

    /**
     * Construct an instance of this class that will create it's own
     * field parser.
     *
     * @param version The spec version string
     * @param handler The content handler instance to send the parsed
     *     output to
     */
    public FieldValueHandler(String version, ContentHandler handler) {

        if (version == null) {
            throw new IllegalArgumentException(
                "FieldValueHandler: Invalid version String: "+ version);
        }
        // this is a crude way to get revision numbers.....
        int separator_index = version.indexOf(".");
        int majorVersion = Integer.parseInt(version.substring(separator_index-1, separator_index));
        int minorVersion = Integer.parseInt(version.substring(separator_index+1, separator_index+2));
        DefaultFieldParserFactory fac = new DefaultFieldParserFactory();
        fieldReader = fac.newFieldParser(majorVersion, minorVersion);

        init(handler);
    }

    /**
     * Construct an instance of this class with a field reader already determined.
     *
     * @param reader The field reader instance to use
     * @param handler The content handler instance to send the parsed output to
     */
    public FieldValueHandler(VRMLFieldReader reader, ContentHandler handler) {
        fieldReader = reader;
        // the 'real' version was used to instantiate the reader, but the interface
        // doesn't allow us to get it back out... using version 3.2 by default.
        init(handler);
    }

    public void setParsingType(ParsingType type) {
        parsingType = type;
    }

    /**
     * Set the field value into the content handler without knowing the type of
     * data that we started with.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The String representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, Object value) {

        if(value instanceof String)
            setFieldValue(nodeName, fieldName, (String)value);
        else if(value instanceof String[])
            setFieldValue(nodeName, fieldName, (String[])value);
        else {
            Class c = value.getClass();

            if(c.isArray()) {
               if(value instanceof int[]) {
                   int[] val = (int[])value;
                   setFieldValue(nodeName, fieldName, val, val.length);
               } else if(value instanceof float[]) {
                   float[] val = (float[])value;
                   setFieldValue(nodeName, fieldName, val, val.length);
               } else if(value instanceof double[]) {
                   double[] val = (double[])value;
                   setFieldValue(nodeName, fieldName, val, val.length);
               } else if(value instanceof boolean[]) {
                   boolean[] val = (boolean[])value;
                   setFieldValue(nodeName, fieldName, val, val.length);
               }
            } else {
               if(value instanceof Integer) {
                   int val = (Integer)value;
                   setFieldValue(nodeName, fieldName, val);
               } else if(value instanceof Float) {
                   float val = (Float)value;
                   setFieldValue(nodeName, fieldName, val);
               } else if(value instanceof Double) {
                   double val = (Double)value;
                   setFieldValue(nodeName, fieldName, val);
               } else if(value instanceof Boolean) {
                   boolean val = (Boolean)value;
                   setFieldValue(nodeName, fieldName, val);
               }
            }
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The String representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, String value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                int fieldType = getFieldType(nodeName, fieldName);
                try {
                    setData(nodeName, fieldName, fieldType, value);
                } catch(InvalidFieldFormatException e) {
                    I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.CRIT_MSG, new String[] {nodeName + "." + fieldName, value});
                    throw e;
                }
                break;
            case HANDLER_STRING:
                sch.fieldValue(value);
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The String[] representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, String[] value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                int fieldType = getFieldType(nodeName, fieldName);
                setData(nodeName, fieldName, fieldType, value);
                break;
            case HANDLER_STRING:
                sch.fieldValue(value);
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The String[] representation of the data.
     * @param length The number of valid values in the array.
     */
    public void setFieldValue(String nodeName,
        String fieldName,
        String[] value,
        int length) {

        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value, length);
                break;
            case HANDLER_STRING:
                if (length == value.length) {
                    sch.fieldValue(value);
                } else {
                    String[] s = new String[length];
                    System.arraycopy(value, 0, s, 0, length);
                    sch.fieldValue(s);
                }
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The int representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, int value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Integer.toString(value));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The int[] representation of the data.
     * @param length The number of valid values in the array.
     */
    public void setFieldValue(String nodeName, String fieldName, int[] value, int length) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value, length);
                break;
            case HANDLER_STRING:
                sch.fieldValue(toString(value, length));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The long representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, long value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Long.toString(value));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The long[] representation of the data.
     * @param length The number of valid values in the array.
     */
    public void setFieldValue(String nodeName, String fieldName, long[] value, int length) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value, length);
                break;
            case HANDLER_STRING:
                sch.fieldValue(toString(value, length));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The boolean representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, boolean value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(value));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The boolean[] representation of the data.
     * @param length The number of valid values in the array.
     */
    public void setFieldValue(String nodeName, String fieldName, boolean[] value, int length) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value, length);
                break;
            case HANDLER_STRING:
                sch.fieldValue(toString(value, length));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The float representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, float value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Float.toString(value));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The float[] representation of the data.
     * @param length The number of valid values in the array.
     */
    public void setFieldValue(String nodeName,
        String fieldName,
        float[] value,
        int length) {

        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value, length);
                break;
            case HANDLER_STRING:
                sch.fieldValue(toString(value, length));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The double representation of the data.
     */
    public void setFieldValue(String nodeName, String fieldName, double value) {
        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Double.toString(value));
                break;
        }
    }

    /**
     * Set the field value into the content handler.
     *
     * @param nodeName The name of the node.
     * @param fieldName The name of the node field.
     * @param value The double[] representation of the data.
     * @param length The number of valid values in the array.
     */
    public void setFieldValue(String nodeName,
        String fieldName,
        double[] value,
        int length) {

        switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(value, length);
                break;
            case HANDLER_STRING:
                sch.fieldValue(toString(value, length));
                break;
        }
    }

    /**
     * Return the field type
     *
     * @param nodeName The name of the node
     * @param fieldName The name of the node field
     * @return The field type
     * @throws InvalidFieldTypeException when the field name doesn't exist
     */
    public int getFieldType(String nodeName, String fieldName)
        throws InvalidFieldTypeException {

        VRMLNode node = nodeMap.get(nodeName);
        if (node == null) {
            node = nodeFactory.createVRMLNode(nodeName, true);
            nodeMap.put(nodeName, node);
        }
        int fieldIndex = node.getFieldIndex(fieldName);
        VRMLFieldDeclaration fieldDef = node.getFieldDeclaration(fieldIndex);

        if(fieldDef == null)
            throw new InvalidFieldException(nodeName + "." + fieldName);

        int fieldType = fieldDef.getFieldType();

        return(fieldType);
    }

    /**
     * Return the default value of a field
     *
     * @param nodeName The name of the node
     * @param fieldName The name of the node field
     * @return The field type
     * @throws InvalidFieldTypeException when the field name doesn't exist
     */
    public VRMLFieldData getFieldDefault(String nodeName, String fieldName)
        throws InvalidFieldTypeException {

        VRMLNode node = nodeMap.get(nodeName);
        if (node == null) {
            node = nodeFactory.createVRMLNode(nodeName, true);
            nodeMap.put(nodeName, node);
        }
        int fieldIndex = node.getFieldIndex(fieldName);
        VRMLFieldData field = ((VRMLNodeType)node).getFieldValue(fieldIndex);

        return field;
    }

    /**
     * Return the data type for the field type
     *
     * @param fieldType The field type identifier
     * @return The data type
     */
    private int getDataType(int fieldType) {

        int dataType = -1;
        switch(fieldType) {

            case FieldConstants.SFBOOL:
                dataType = BOOLEAN;
                break;

            case FieldConstants.MFBOOL:
                dataType = BOOLEAN_ARRAY;
                break;

            case FieldConstants.SFINT32:
                dataType = INT;
                break;

            case FieldConstants.MFINT32:
            case FieldConstants.SFIMAGE:
                dataType = INT_ARRAY;
                break;

            case FieldConstants.SFLONG:
                dataType = LONG;
                break;

            case FieldConstants.MFLONG:
                dataType = LONG_ARRAY;
                break;

            case FieldConstants.SFFLOAT:
                dataType = FLOAT;
                break;

            case FieldConstants.MFFLOAT:
            case FieldConstants.SFROTATION:
            case FieldConstants.MFROTATION:
            case FieldConstants.SFVEC2F:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFVEC4F:
            case FieldConstants.MFVEC2F:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFVEC4F:
            case FieldConstants.SFCOLOR:
            case FieldConstants.MFCOLOR:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.SFMATRIX3F:
            case FieldConstants.SFMATRIX4F:
            case FieldConstants.MFMATRIX3F:
            case FieldConstants.MFMATRIX4F:
                dataType = FLOAT_ARRAY;
                break;

            case FieldConstants.SFTIME:
            case FieldConstants.SFDOUBLE:
                dataType = DOUBLE;
                break;

            case FieldConstants.MFTIME:
            case FieldConstants.MFDOUBLE:
            case FieldConstants.SFVEC3D:
            case FieldConstants.SFVEC4D:
            case FieldConstants.MFVEC3D:
            case FieldConstants.MFVEC4D:
            case FieldConstants.SFMATRIX3D:
            case FieldConstants.SFMATRIX4D:
            case FieldConstants.MFMATRIX3D:
            case FieldConstants.MFMATRIX4D:
                dataType = DOUBLE_ARRAY;
                break;

            case FieldConstants.SFSTRING:
                dataType = STRING;
                break;

            case FieldConstants.MFSTRING:
                dataType = STRING_ARRAY;
                break;

        }
        return(dataType);
    }

    /**
     * Set the data values contained in the argument String for the
     * specified fieldType into the binary content handler
     *
     * @param nodeName The node name
     * @param fieldName The field name
     * @param fieldType The field type
     * @param value The String representation of the data
     */
    private void setData(String nodeName, String fieldName, int fieldType, String value) {

        float[] f_array;

        switch(fieldType) {
            case FieldConstants.SFINT32:
                try {
                    int i = fieldReader.SFInt32(value);
                    bch.fieldValue(i);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.intValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFINT32:
                try {
                    int[] i_array = fieldReader.MFInt32(value);
                    bch.fieldValue(i_array, i_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.intArrayValues, def_data.intArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFIMAGE:
                try {
                    int[] sfi_array = fieldReader.SFImage(value);
                    bch.fieldValue(sfi_array, sfi_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.intValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFFLOAT:
                try {
                    float f = fieldReader.SFFloat(value);
                    bch.fieldValue(f);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFTIME:
                try {
                    double d = fieldReader.SFTime(value);
                    bch.fieldValue(d);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFDOUBLE:
                try {
                    double d = fieldReader.SFDouble(value);
                    bch.fieldValue(d);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                    	I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFTIME:
                try {
                    double[] d_array = fieldReader.MFTime(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                    	I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }
                break;

            case FieldConstants.MFDOUBLE:
                try {
                    double[] d_array = fieldReader.MFDouble(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFLONG:
                try {
                    long l = fieldReader.SFLong(value);
                    bch.fieldValue(l);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.longValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFLONG:
                try {
                    long[] l_array = fieldReader.MFLong(value);
                    bch.fieldValue(l_array, l_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.longArrayValues, def_data.longArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFBOOL:
                try {
                    boolean b = fieldReader.SFBool(value);
                    bch.fieldValue(b);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.booleanValue);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFROTATION:
                try {
                    f_array = fieldReader.SFRotation(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFROTATION:
                try {
                    f_array = fieldReader.MFRotation(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }
                break;

            case FieldConstants.MFBOOL:
                try {
                    boolean[] b_array = fieldReader.MFBool(value);
                    bch.fieldValue(b_array, b_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.booleanArrayValues, def_data.booleanArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFFLOAT:
                try {
                    f_array = fieldReader.MFFloat(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC2F:
                try {
                    f_array = fieldReader.SFVec2f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC3F:
                try {
                    f_array = fieldReader.SFVec3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC4F:
                try {
                    f_array = fieldReader.SFVec4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC2F:
                try {
                    f_array = fieldReader.MFVec2f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC3F:
                try {
                    f_array = fieldReader.MFVec3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC4F:
                try {
                    f_array = fieldReader.MFVec4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC3D:
                try {
                    double[] d_array = fieldReader.SFVec3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC4D:
                try {
                    double[] d_array = fieldReader.SFVec4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC3D:
                try {
                    double[] d_array = fieldReader.MFVec3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC4D:
                try {
                    double[] d_array = fieldReader.MFVec4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFSTRING:
                bch.fieldValue(value);
                break;

            case FieldConstants.MFSTRING:
                try {
                    String[] s_array = fieldReader.MFString(value);
                    bch.fieldValue(s_array, s_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.stringArrayValues, def_data.stringArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFCOLOR:
                try {
                    f_array = fieldReader.SFColor(value);
                    try {
                        FieldValidator.checkColorVector(nodeName + "." + fieldName, f_array);
                    } catch(InvalidFieldValueException ifve) {
                        float[] orig = f_array.clone();

                        for(int i=0; i < 3; i++) {
                            if (f_array[i] < 0) {
                                f_array[i] = 0;
                            } else if (f_array[i] > 1) {
                                f_array[i] = 1;
                            }
                        }
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(orig), java.util.Arrays.toString(f_array)});
                    }

                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                } catch(VRMLException e) {
                    e.printStackTrace(System.err);
                }

                break;

            case FieldConstants.MFCOLOR:
                try {
                    f_array = fieldReader.MFColor(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }
                break;

            case FieldConstants.SFCOLORRGBA:
                try {
                    f_array = fieldReader.SFColorRGBA(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFCOLORRGBA:
                try {
                    f_array = fieldReader.MFColorRGBA(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX3F:
                try {
                    f_array = fieldReader.SFMatrix3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX4F:
                try {
                    f_array = fieldReader.SFMatrix4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX3F:
                try {
                    f_array = fieldReader.MFMatrix3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX4F:
                try {
                    f_array = fieldReader.MFMatrix4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX3D:
                try {
                    double[] d_array = fieldReader.SFMatrix3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX4D:
                try {
                    double[] d_array = fieldReader.SFMatrix4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX3D:
                try {
                    double[] d_array = fieldReader.MFMatrix3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX4D:
                try {
                    double[] d_array = fieldReader.MFMatrix4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG, new String[] {nodeName + "." + fieldName, value});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            // these cases are not primitive types
            case FieldConstants.MFIMAGE:
            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                if (value.equals("NULL")) {
                    // ignore
                    break;
                } else {
                System.out.println("Field Value: " + value);
                throw new IllegalArgumentException(
                        "FieldValueHandler: fieldType: "+ fieldType +" cannot contain an array");
            }
            default:
                throw new IllegalArgumentException(
                    "FieldValueHandler: Unknown fieldType: "+ fieldType);
        }
    }

    /**
     * Set the data values contained in the argument String[] for the
     * specified fieldType into the binary content handler
     *
     * @param fieldType field type
     * @param value String[] representation of the data
     */
    private void setData(String nodeName, String fieldName,int fieldType, String[] value) {

        if(value == null || value.length == 0) {
            // ? is this the right thing to do?
            bch.endField();
        }

        float[] f_array;

        switch(fieldType) {
            case FieldConstants.MFINT32:
                try {
                    int[] i_array = fieldReader.MFInt32(value);
                    bch.fieldValue(i_array, i_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.intArrayValues, def_data.intArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFTIME:
                try {
                    double[] d_array = fieldReader.MFTime(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }
                break;

            case FieldConstants.MFDOUBLE:
                try {
                    double[] d_array = fieldReader.MFDouble(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFLONG:
                try {
                    long[] l_array = fieldReader.MFLong(value);
                    bch.fieldValue(l_array, l_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.longArrayValues, def_data.longArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFROTATION:
                try {
                    f_array = fieldReader.SFRotation(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFROTATION:
                try {
                    f_array = fieldReader.MFRotation(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }
                break;

            case FieldConstants.MFBOOL:
                try {
                    boolean[] b_array = fieldReader.MFBool(value);
                    bch.fieldValue(b_array, b_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.booleanArrayValues, def_data.booleanArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFFLOAT:
                try {
                    f_array = fieldReader.MFFloat(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC2F:
                try {
                    f_array = fieldReader.SFVec2f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC3F:
                try {
                    f_array = fieldReader.SFVec3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC4F:
                try {
                    f_array = fieldReader.SFVec4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC2F:
                try {
                    f_array = fieldReader.MFVec2f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC3F:
                try {
                    f_array = fieldReader.MFVec3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC4F:
                try {
                    f_array = fieldReader.MFVec4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC3D:
                try {
                    double[] d_array = fieldReader.SFVec3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFVEC4D:
                try {
                    double[] d_array = fieldReader.SFVec4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC3D:
                try {
                    double[] d_array = fieldReader.MFVec3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFVEC4D:
                try {
                    double[] d_array = fieldReader.MFVec4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFSTRING:
                try {
                    String[] s_array = fieldReader.MFString(value);
                    bch.fieldValue(s_array, s_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.stringArrayValues, def_data.stringArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFCOLOR:
                try {
                    f_array = fieldReader.SFColor(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFCOLOR:
                try {
                    f_array = fieldReader.MFColor(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }
                break;

            case FieldConstants.SFCOLORRGBA:
                try {
                    f_array = fieldReader.SFColorRGBA(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFCOLORRGBA:
                try {
                    f_array = fieldReader.MFColorRGBA(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX3F:
                try {
                    f_array = fieldReader.SFMatrix3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX4F:
                try {
                    f_array = fieldReader.SFMatrix4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX3F:
                try {
                    f_array = fieldReader.MFMatrix3f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX4F:
                try {
                    f_array = fieldReader.MFMatrix4f(value);
                    bch.fieldValue(f_array, f_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.floatArrayValues, def_data.floatArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX3D:
                try {
                    double[] d_array = fieldReader.SFMatrix3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.SFMATRIX4D:
                try {
                    double[] d_array = fieldReader.SFMatrix4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX3D:
                try {
                    double[] d_array = fieldReader.MFMatrix3d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            case FieldConstants.MFMATRIX4D:
                try {
                    double[] d_array = fieldReader.MFMatrix4d(value);
                    bch.fieldValue(d_array, d_array.length);
                } catch(InvalidFieldFormatException ife) {
                    if (parsingType == ParsingType.TOLERANT) {
                        I18nUtils.printMsg(INVALID_FIELD_VALUE_MSG_PROP, I18nUtils.EXT_MSG,
                        		new String[] {nodeName + "." + fieldName, java.util.Arrays.toString(value)});

                        VRMLFieldData def_data = getFieldDefault(nodeName, fieldName);
                        bch.fieldValue(def_data.doubleArrayValues, def_data.doubleArrayValues.length);
                    } else {
                        throw ife;
                    }
                }

                break;

            // these cases are not primitive array types
            case FieldConstants.SFIMAGE:
            case FieldConstants.MFIMAGE:
            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
            case FieldConstants.SFINT32:
            case FieldConstants.SFFLOAT:
            case FieldConstants.SFTIME:
            case FieldConstants.SFDOUBLE:
            case FieldConstants.SFLONG:
            case FieldConstants.SFBOOL:
            case FieldConstants.SFSTRING:
                throw new IllegalArgumentException(
                    "FieldValueHandler: fieldType: "+ fieldType +" cannot contain an array");

            default:
                throw new IllegalArgumentException(
                    "FieldValueHandler: Unknown fieldType: "+ fieldType );
        }
    }

    /**
     * Return an array of double values for String values
     *
     * @param sArray An array of String values to convert
     * @return A double array of converted values
     */
    public static double[] toDouble(String[] sArray) throws NumberFormatException {
        int num = sArray.length;
        double[] dArray = new double[num];
        for (int i = 0; i < num; i++) {
            dArray[i] = Double.parseDouble(sArray[i]);
        }
        return(dArray);
    }

    /**
     * Return an array of float values for String values
     *
     * @param sArray An array of String values to convert
     * @return A float array of converted values
     */
    public static float[] toFloat(String[] sArray) throws NumberFormatException {
        int num = sArray.length;
        float[] fArray = new float[num];
        for (int i = 0; i < num; i++) {
            fArray[i] = Float.parseFloat(sArray[i]);
        }
        return(fArray);
    }

    /**
     * Return an array of int values for String values
     *
     * @param sArray An array of String values to convert
     * @return An int array of converted values
     */
    public static int[] toInt(String[] sArray) throws NumberFormatException {
        int num = sArray.length;
        int[] iArray = new int[num];
        for (int i = 0; i < num; i++) {
            iArray[i] = Integer.parseInt(sArray[i]);
        }
        return(iArray);
    }

    /**
     * Return an array of double values for a String of
     * whitespace separated values
     *
     * @param s A String containing values to convert
     * @return A double array of converted values
     */
    public static double[] toDouble(String s) throws NumberFormatException {
        String[] sArray = split(s);
        return(toDouble(sArray));
    }

    /**
     * Return an array of float values for a String of
     * whitespace separated values
     *
     * @param s A String containing values to convert
     * @return A float array of converted values
     */
    public static float[] toFloat(String s) throws NumberFormatException {
        String[] sArray = split(s);
        return(toFloat(sArray));
    }

    /**
     * Return an array of int values for a String of
     * whitespace separated values
     *
     * @param s A String containing values to convert
     * @return An int array of converted values
     */
    public static int[] toInt(String s) throws NumberFormatException {
        String[] sArray = split(s);
        return(toInt(sArray));
    }

    /**
     * Return an array of String values for boolean values
     *
     * @param bArray An array of boolean values to convert
     * @return A String array of converted values
     */
    public static String[] toString(boolean[] bArray) {
        int num = bArray.length;
        return(toString(bArray, num));
    }

    /**
     * Return an array of String values for boolean values
     *
     * @param bArray An array of boolean values to convert
     * @param num The number of values from the array to process
     * @return A String array of converted values
     */
    public static String[] toString(boolean[] bArray, int num) {
        String[] sArray = new String[num];
        for (int i = 0; i < num; i++) {
            sArray[i] = bArray[i] ? "TRUE":"FALSE";
        }
        return(sArray);
    }

    /**
     * Return an array of String values for double values
     *
     * @param dArray An array of double values to convert
     * @return A String array of converted values
     */
    public static String[] toString(double[] dArray) {
        int num = dArray.length;
        return(toString(dArray, num));
    }

    /**
     * Return an array of String values for double values
     *
     * @param dArray An array of double values to convert
     * @param num The number of values from the array to process
     * @return A String array of converted values
     */
    public static String[] toString(double[] dArray, int num) {
        String[] sArray = new String[num];
        for (int i = 0; i < num; i++) {
            sArray[i] = Double.toString(dArray[i]);
        }
        return(sArray);
    }

    /**
     * Return an array of String values for float values
     *
     * @param fArray An array of float values to convert
     * @return A String array of converted values
     */
    public static String[] toString(float[] fArray) {
        int num = fArray.length;
        return(toString(fArray, num));
    }

    /**
     * Return an array of String values for float values
     *
     * @param fArray An array of float values to convert
     * @param num The number of values from the array to process
     * @return A String array of converted values
     */
    public static String[] toString(float[] fArray, int num) {
        String[] sArray = new String[num];
        for (int i = 0; i < num; i++) {
            sArray[i] = Float.toString(fArray[i]);
        }
        return(sArray);
    }

    /**
     * Return an array of String values for int values
     *
     * @param iArray An array of int values to convert
     * @return A String array of converted values
     */
    public static String[] toString(int[] iArray) {
        int num = iArray.length;
        return(toString(iArray, num));
    }

    /**
     * Return an array of String values for int values
     *
     * @param iArray An array of int values to convert
     * @param num The number of values from the array to process
     * @return A String array of converted values
     */
    public static String[] toString(int[] iArray, int num) {
        String[] sArray = new String[num];
        for (int i = 0; i < num; i++) {
            sArray[i] = Integer.toString(iArray[i]);
        }
        return(sArray);
    }

    /**
     * Return an array of String values for long values
     *
     * @param lArray An array of long values to convert
     * @return A String array of converted values
     */
    public static String[] toString(long[] lArray) {
        int num = lArray.length;
        return(toString(lArray, num));
    }

    /**
     * Return an array of String values for long values
     *
     * @param lArray An array of long values to convert
     * @param num The number of values from the array to process
     * @return A String array of converted values
     */
    public static String[] toString(long[] lArray, int num) {
        String[] sArray = new String[num];
        for (int i = 0; i < num; i++) {
            sArray[i] = Long.toString(lArray[i]);
        }
        return(sArray);
    }

    /**
     * Split the argument String into an array of Strings.
     *
     * @param s A String
     * @return An array of Strings
     */
    public static String[] split(String s) {
        StringTokenizer st = new StringTokenizer(s);
        String[] sArray = new String[st.countTokens()];
        for (int i = 0; i < sArray.length; i++) {
            sArray[i] = st.nextToken();
        }
        return(sArray);
    }

    /**
     * Flatten the argument array of String arrays into a single String array
     *
     * @param in An array of String arrays.
     * @return A single array
     */
    public static String[] flatten(String[][] in) {
        int num_total = 0;
        int num_arrays = in.length;
        for (int i = 0; i < num_arrays; i++) {
            num_total += in[i].length;
        }
        return(flatten(in, num_total));
    }

    /**
     * Flatten the argument array of String arrays into a single String array
     *
     * @param in An array of String arrays.
     * @param num_total The total number of Strings contained in the in argument
     * @return A single array
     */
    public static String[] flatten(String[][] in, int num_total) {
        String[] out = new String[num_total];
        int num_arrays = in.length;
        int index = 0;
        for (int i = 0; i < num_arrays; i++) {
            int in_length = in[i].length;
            System.arraycopy(in[i], 0, out, index, in_length);
            index += in_length;
        }
        return(out);
    }

    /**
     * Flatten the argument array of int arrays into a single int array
     *
     * @param in An array of int arrays.
     * @return A single array
     */
    public static int[] flatten(int[][] in) {
        int num_total = 0;
        int num_arrays = in.length;
        for (int i = 0; i < num_arrays; i++) {
            num_total += in[i].length;
        }
        return(flatten(in, num_total));
    }

    /**
     * Flatten the argument array of int arrays into a single int array
     *
     * @param in An array of int arrays.
     * @param num_total The total number of ints contained in the in argument
     * @return A single array
     */
    public static int[] flatten(int[][] in, int num_total) {
        int[] out = new int[num_total];
        int num_arrays = in.length;
        int index = 0;
        for (int i = 0; i < num_arrays; i++) {
            int in_length = in[i].length;
            System.arraycopy(in[i], 0, out, index, in_length);
            index += in_length;
        }
        return(out);
    }

    /**
     * Flatten the argument array of float arrays into a single float array
     *
     * @param in An array of float arrays.
     * @return A single array
     */
    public static float[] flatten(float[][] in) {
        int num_total = 0;
        int num_arrays = in.length;
        for (int i = 0; i < num_arrays; i++) {
            num_total += in[i].length;
        }
        return(flatten(in, num_total));
    }

    /**
     * Flatten the argument array of float arrays into a single float array
     *
     * @param in An array of float arrays.
     * @param num_total The total number of floats contained in the in argument
     * @return A single array
     */
    public static float[] flatten(float[][] in, int num_total) {
        float[] out = new float[num_total];
        int num_arrays = in.length;
        int index = 0;
        for (int i = 0; i < num_arrays; i++) {
            int in_length = in[i].length;
            System.arraycopy(in[i], 0, out, index, in_length);
            index += in_length;
        }
        return(out);
    }

    /**
     * Common initialization code called by the constructors.
     *
     * @param handler The content handler instance to send the parsed output to
     */
    private void init(ContentHandler handler) {

        nodeFactory = NRNodeFactory.getNRNodeFactory();
        /////////////////////////////////////////////////////////////////
        // rem: configure the factory to deliver eveything....
        nodeFactory.setSpecVersion(3, 3);
        ComponentInfo[] ci = nodeFactory.getAvailableComponents();
        for (ComponentInfo ci1 : ci) {
            nodeFactory.addComponent(ci1.getName(), ci1.getLevel());
        }
        /////////////////////////////////////////////////////////////////
        nodeMap = new HashMap<>();

        if (handler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)handler;
            sch = null;
            handlerType = HANDLER_BINARY;
        } else if (handler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)handler;
            handlerType = HANDLER_STRING;
        } else {
            bch = null;
            sch = null;
            handlerType = HANDLER_NULL;
        }
    }
}
