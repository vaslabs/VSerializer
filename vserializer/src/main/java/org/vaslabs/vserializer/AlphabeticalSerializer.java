package org.vaslabs.vserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by vnicolaou on 02/05/16.
 */
public class AlphabeticalSerializer extends StringSerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj instanceof String)
            return super.serialize(obj);
        final Field[] fields = obj.getClass().getDeclaredFields();
        final int size = SerializationUtils.calculateSize(fields, obj);

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);


        try {
            putIn(byteBuffer, fields, obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }

        return byteBuffer.array();
    }

    @Override
    public <T> byte[] serialize(T[] objects) {
        if (objects.length == 0)
            return new byte[0];
        final int totalSize = SerializationUtils.calculateNonPrimitiveArraySize(objects);
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize);
        byteBuffer.putInt(objects.length);
        for (T obj : objects) {
            try {
                if (obj != null) {
                    byteBuffer.put((byte) 1);
                    putIn(byteBuffer, obj.getClass().getDeclaredFields(), obj);
                } else {
                    byteBuffer.put((byte) -1);
                }
            } catch (IllegalAccessException e) {
                return new byte[0];
            }
        }
        return byteBuffer.array();
    }

    @Override
    public <T> T deserialise(byte[] data, Class<T> clazz) {
        if (clazz.equals(String.class))
            return super.deserialise(data, clazz);
        if (clazz.isArray())
            return deserialiseArray(data, clazz);
        Field[] fields = clazz.getDeclaredFields();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        T obj = null;
        try {
            obj = SerializationUtils.instantiate(clazz);
            obj = convert(byteBuffer, fields, obj);
        } catch (Exception e) {
            return obj;
        }
        return obj;
    }

    private <T> T deserialiseArray(byte[] data, Class<T> clazz) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        final int arraySize = byteBuffer.getInt();
        Class type = clazz.getComponentType();
        T[] objects = (T[]) Array.newInstance(type, arraySize);
        final Field[] fields = type.getDeclaredFields();
        for (int i = 0; i < objects.length; i++) {
            boolean objectIsNull = byteBuffer.get() == -1;
            if (objectIsNull) {
                objects[i] = null;
                continue;
            }
            T obj = null;
            try {
                obj = (T) SerializationUtils.instantiate(type);
                obj = convert(byteBuffer, fields, obj);
                objects[i] = obj;
            } catch (Exception e) {

            }
        }
        return (T) objects;
    }

    public  <T> T convert(ByteBuffer byteBuffer, Field[] fields, T obj) throws NoSuchMethodException, InvocationTargetException, InstantiationException {
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            try {
                SerializationUtils.arrangeField(field, obj);
                convert(byteBuffer, field, obj);
            } catch (Exception e) {
                return null;
            } finally {
                try {
                    SerializationUtils.houseKeeping(field);
                } catch (Exception e) {

                }
            }
        }
        return obj;
    }

    public <T> void convert(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Class fieldType = field.getType();
        if (Modifier.isStatic(field.getModifiers()))
            return;
        if (fieldType.isArray()) {
            convertArray(byteBuffer, field, obj);
            return;
        }

        PrimitiveType primitiveType = SerializationUtils.enumTypes.get(fieldType);
        if (primitiveType == null) {
            if (String.class.equals(field.getType())) {
                this.convertString(byteBuffer, field, obj);
                return;
            }
            boolean isNull = -1 == byteBuffer.get();
            if (isNull) {
                field.set(obj, null);
                return;
            } else {

                Object innerObject = SerializationUtils.instantiate(field.getType());
                field.set(obj, innerObject);
                convert(byteBuffer, obj.getClass().getDeclaredFields(), innerObject);
                return;
            }
        }
        switch (primitiveType) {
            case INT: {
                int value = byteBuffer.getInt();
                field.setInt(obj, value);
                return;
            }
            case LONG: {
                long value = byteBuffer.getLong();
                field.setLong(obj, value);
                return;
            }
            case SHORT: {
                short value = byteBuffer.getShort();
                field.setShort(obj, value);
                return;
            }
            case CHAR: {
                char value = byteBuffer.getChar();
                field.setChar(obj, value);
                return;
            }
            case BYTE: {
                byte value = byteBuffer.get();
                field.setByte(obj, value);
                return;
            }
            case BOOLEAN: {
                byte value = byteBuffer.get();
                field.setBoolean(obj, value == 1);
                return;
            }
            default:
                throw new IllegalArgumentException(field.getType().toString());
        }
    }


    private <T> void convertArray(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException, NoSuchFieldException {
        Class fieldType = field.getType();
        if (Modifier.isStatic(field.getModifiers()))
            return;
        if (!SerializationUtils.enumTypes.containsKey(fieldType)) {
            convertNonPrimitiveArray(byteBuffer, field, obj);
            return;
        }
        final int arrayLength = byteBuffer.getInt();
        switch (SerializationUtils.enumTypes.get(fieldType)) {
            case INT: {
                int[] array = new int[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getInt();}
                field.set(obj, array);
                return;
            }
            case LONG: {
                long[] array = new long[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getLong();}
                field.set(obj, array);
                return;
            }
            case SHORT: {
                short[] array = new short[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getShort();}
                field.set(obj, array);
                return;
            }
            case CHAR: {
                char[] array = new char[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getChar();}
                field.set(obj, array);
                return;
            }case BOOLEAN: {
                boolean[] array = new boolean[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.get() == 1;}
                field.set(obj, array);
                return;
            }
            case BYTE: {
                byte[] array = new byte[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.get();}
                field.set(obj, array);
                return;
            }
        }
    }

    private <T> void convertNonPrimitiveArray(ByteBuffer byteBuffer, final Field field, T object) throws IllegalAccessException, NoSuchFieldException {
        if (Modifier.isStatic(field.getModifiers()))
            return;
        final int arraySize = byteBuffer.getInt();
        Class type = field.getType().getComponentType();
        T[] objects = (T[]) Array.newInstance(type, arraySize);
        final Field[] fields = type.getDeclaredFields();
        for (int i = 0; i < objects.length; i++) {
            if (byteBuffer.get() == -1) {
                objects[i] = null;
                continue;
            }
            T obj = null;
            try {
                obj = (T) SerializationUtils.instantiate(type);
                obj = convert(byteBuffer, fields, obj);
                objects[i] = obj;
            } catch (Exception e) {

            }
        }
        SerializationUtils.arrangeField(field, object);
        field.set(object, objects);
        SerializationUtils.houseKeeping(field);

    }

    private void putIn(ByteBuffer byteBuffer, Field[] fields, Object obj) throws IllegalAccessException {
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            try {
                SerializationUtils.arrangeField(field, obj);
                putIn(byteBuffer, field, obj);
            } catch (NoSuchFieldException nsfe) {

            }
            field.setAccessible(false);
        }
    }

    private void putIn(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        if (obj == null)
            return;
        Class type = field.getType();
        if (type.isArray()) {
            putArrayIn(byteBuffer, field, obj);
            return;
        }
        PrimitiveType primitiveType = SerializationUtils.enumTypes.get(type);
        if (primitiveType == null) {
            if (String.class.equals(type)) {
                this.insertString(byteBuffer, field, obj);
                return;
            }
            Object fieldObject = field.get(obj);
            if (fieldObject == null) {
                byteBuffer.put((byte) -1);
                return;
            } else {
                byteBuffer.put((byte) 1);
                putIn(byteBuffer, fieldObject.getClass().getDeclaredFields(), fieldObject);
                return;
            }
        }
        switch (primitiveType) {
            case INT:
                byteBuffer.putInt(field.getInt(obj));
                return;
            case LONG:
                byteBuffer.putLong(field.getLong(obj));
                return;
            case SHORT:
                byteBuffer.putShort(field.getShort(obj));
                return;
            case CHAR:
                byteBuffer.putChar(field.getChar(obj));
                return;
            case BYTE:
                byteBuffer.put(field.getByte(obj));
                return;
            case BOOLEAN:
                byteBuffer.put((byte) (field.getBoolean(obj) ? 1 : 0));
                return;
            default:
                throw new IllegalArgumentException(field.getType().toString());
        }
    }

    private void putArrayIn(ByteBuffer byteBuffer, Field field, Object obj) {
        try {
            int arrayLength = SerializationUtils.findArrayLength(field, obj);
            byteBuffer.putInt(arrayLength);
            insertArrayValues(byteBuffer, field, obj);
        } catch (Exception e) {
            return;
        }

    }

    private void insertArrayValues(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        Class fieldType = field.getType();
        if (!SerializationUtils.enumTypes.containsKey(fieldType)) {
            insertArrayValuesNonPrimitive(byteBuffer, field, obj);
            return;
        }
        switch (SerializationUtils.enumTypes.get(fieldType)) {
            case INT: {
                int[] array = (int[]) field.get(obj);
                for (int i : array) { byteBuffer.putInt(i); }
                return;
            }
            case LONG: {
                long[] array = (long[]) field.get(obj);
                for (long l : array) {
                    byteBuffer.putLong(l);
                }
                return;
            }
            case SHORT: {
                short[] array = (short[]) field.get(obj);
                for (short s : array) { byteBuffer.putShort(s); }
                return;
            }
            case BOOLEAN: {
                boolean[] array = (boolean[]) field.get(obj);
                for (boolean b : array) {
                    byteBuffer.put((byte) (b ? 1 : 0));
                }
                return;
            }
            case BYTE: {
                byte[] array = (byte[])field.get(obj);
                for (byte b : array) { byteBuffer.put(b);}
                return;
            }
            case CHAR: {
                char[] array = (char[]) field.get(obj);
                for (char c : array) {byteBuffer.putChar(c);}
                return;
            }
            default:
                return;
        }
    }

    private void insertArrayValuesNonPrimitive(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        Object[] objects = (Object[]) field.get(obj);
        if (objects == null || objects.length == 0)
            return;

        Field[] fields = objects[0].getClass().getDeclaredFields();
        for (Object object : objects) {
            if (object == null) {
                byteBuffer.put((byte) -1);
            } else {
                byteBuffer.put((byte) 1);
                putIn(byteBuffer, fields, object);
            }
        }
    }

}

abstract class StringSerializer implements VSerializer {
    @Override
    public <T> byte[] serialize(T myTestObject) {
        if (!(myTestObject instanceof String))
            throw new IllegalArgumentException("Only Strings are supported");
        char[] chars = ((String)myTestObject).toCharArray();
        return toBytes(chars);
    }

    private byte[] toBytes(char[] chars) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(chars.length*2);
        for (char c : chars) {
            byteBuffer.putChar(c);
        }
        return byteBuffer.array();
    }

    private char[] toChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = byteBuffer.getChar();
        }
        return chars;
    }

    @Override
    public <T> T deserialise(byte[] data, Class<T> clazz) {
        if (!clazz.equals(String.class)) {
            throw new IllegalArgumentException("Only Strings are supported");
        }
        return (T) new String(toChars(data));
    }

    protected <T> void convertString(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException {
        final int stringLength = byteBuffer.getInt();
        if (stringLength == -1) {
            field.set(obj, null);
            return;
        }
        final char[] stringChars = new char[stringLength];
        for (int i = 0; i < stringLength; i++) {
            stringChars[i] = byteBuffer.getChar();
        }
        String string = new String(stringChars);
        field.set(obj, string);
    }

    protected <T> void insertString(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException {
        String string = (String) field.get(obj);
        if (string == null) {
            byteBuffer.putInt(-1);
            return;
        }
        byteBuffer.putInt(string.length());
        char[] characters = string.toCharArray();
        byte[] bytes = toBytes(characters);
        byteBuffer.put(bytes);
    }
}