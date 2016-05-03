package org.vaslabs.vserializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vnicolaou on 02/05/16.
 */
public class AlphabeticalSerializer implements VSerializer {
    private static Map<Class, Integer> sizes = new HashMap<>();

    static {
        sizes.put(Integer.TYPE, 4);
        sizes.put(Long.TYPE, 8);
        sizes.put(Boolean.TYPE, 1);
        sizes.put(Byte.TYPE, 1);
        sizes.put(Short.TYPE, 2);
        sizes.put(Character.TYPE, 2);
        sizes.put(int[].class, 4);
        sizes.put(long[].class, 8);
        sizes.put(boolean[].class, 1);
        sizes.put(byte[].class, 1);
        sizes.put(short[].class, 2);
        sizes.put(char[].class, 2);
    }

    private static Map<Class, PrimitiveType> enumTypes = new HashMap<Class, PrimitiveType>();

    static {
        enumTypes.put(Integer.TYPE, PrimitiveType.INT);
        enumTypes.put(Long.TYPE, PrimitiveType.LONG);
        enumTypes.put(Boolean.TYPE, PrimitiveType.BOOLEAN);
        enumTypes.put(Byte.TYPE, PrimitiveType.BYTE);
        enumTypes.put(Short.TYPE, PrimitiveType.SHORT);
        enumTypes.put(Character.TYPE, PrimitiveType.CHAR);
        enumTypes.put(int[].class, PrimitiveType.INT);
        enumTypes.put(long[].class, PrimitiveType.LONG);
        enumTypes.put(boolean[].class, PrimitiveType.BOOLEAN);
        enumTypes.put(byte[].class, PrimitiveType.BYTE);
        enumTypes.put(short[].class, PrimitiveType.SHORT);
        enumTypes.put(char[].class, PrimitiveType.CHAR);
    }


    @Override
    public <T> byte[] serialize(T obj) {
        final Field[] fields = obj.getClass().getDeclaredFields();
        final int size = calculateSize(fields, obj);

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
    public <T, E extends Class<T>> T deserialise(byte[] data, E clazz) {
        Field[] fields = clazz.getDeclaredFields();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        T obj = null;
        try {
            obj = instantiate(clazz);
            obj = convert(byteBuffer, fields, obj);
        } catch (Exception e) {
            return obj;
        }
        return obj;
    }

    public static <T, E extends Class<T>> T instantiate(E clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(null);
    }

    public static <T> T convert(ByteBuffer byteBuffer, Field[] fields, T obj) throws NoSuchMethodException, InvocationTargetException, InstantiationException {
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                convert(byteBuffer, field, obj);
            } catch (IllegalAccessException e) {
                return null;
            }
            field.setAccessible(false);
        }
        return obj;
    }

    public static <T> void convert(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Class fieldType = field.getType();
        if (fieldType.isArray()) {
            convertArray(byteBuffer, field, obj);
            return;
        }

        PrimitiveType primitiveType = enumTypes.get(fieldType);
        if (primitiveType == null) {
            boolean isNull = 0 == byteBuffer.get();
            if (isNull) {
                field.set(obj, null);
                return;
            } else {
                Object innerObject = instantiate(field.getType());
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

    private static <T> void convertArray(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException {
        int arrayLength = byteBuffer.getInt();
        Class fieldType = field.getType();
        if (!enumTypes.containsKey(fieldType))
            return;
        switch (enumTypes.get(fieldType)) {
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

    private void putIn(ByteBuffer byteBuffer, Field[] fields, Object obj) throws IllegalAccessException {
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                putIn(byteBuffer, field, obj);
            } catch (IllegalAccessException iae) {

            }
            field.setAccessible(false);
        }
    }

    private void putIn(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        Class type = field.getType();
        if (type.isArray()) {
            putArrayIn(byteBuffer, field, obj);
            return;
        }
        PrimitiveType primitiveType = enumTypes.get(type);
        if (primitiveType == null) {
            Object fieldObject = field.get(obj);
            if (fieldObject == null) {
                byteBuffer.put((byte) 0);
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
            int arrayLength = findArrayLength(field, obj);
            byteBuffer.putInt(arrayLength);
            insertArrayValues(byteBuffer, field, obj);
        } catch (Exception e) {
            return;
        }

    }

    private void insertArrayValues(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        Class fieldType = field.getType();
        if (!enumTypes.containsKey(fieldType))
            return;
        switch (enumTypes.get(fieldType)) {
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

    public static int calculateSize(Field[] fields, Object obj) {
        int size = 0;
        for (Field field : fields) {
            if (Modifier.isTransient(field.getModifiers()))
                continue;
            if (field.getType().isArray()) {
                size += 4 + sizeOfArray(field, obj);
                continue;
            }
            else if (!field.getType().isPrimitive()) {
                size += 1;
                try {
                    field.setAccessible(true);
                    final Object newObj = field.get(obj);
                    if (newObj != null)
                        size += calculateSize(newObj.getClass().getDeclaredFields(), newObj);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {

                }
            }
            size += sizeOf(field);
        }
        return size;
    }

    private static int sizeOfArray(Field field, Object obj) {
        try {

            field.setAccessible(true);
            Class type = field.getType();
            int consistentTypeSize = sizes.get(type);
            int arraySize = findArrayLength(field, obj);
            field.setAccessible(false);
            return arraySize*consistentTypeSize;
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private static int findArrayLength(Field field, Object obj) throws IllegalAccessException {
        switch (enumTypes.get(field.getType())) {
            case INT: {
                int[] array = (int[]) field.get(obj);
                return array.length;
            }
            case LONG: {
                long[] array = (long[])field.get(obj);
                return array.length;
            }
            case BYTE: {
                byte[] array = (byte[])field.get(obj);
                return array.length;
            }
            case CHAR: {
                char[] array = (char[])field.get(obj);
                return array.length;
            }
            case BOOLEAN: {
                boolean[] array = (boolean[]) field.get(obj);
                return array.length;
            }
            case SHORT: {
                short[] array = (short[]) field.get(obj);
                return array.length;
            }
            default:
                return 0;
        }
    }

    public static int sizeOf(Field field) {
        Class type = field.getType();
        if (!sizes.containsKey(type))
            return 0;
        int size = sizes.get(type);
        return size;
    }
}
