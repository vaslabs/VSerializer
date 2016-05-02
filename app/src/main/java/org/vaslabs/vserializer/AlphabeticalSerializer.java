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
    }

    private static Map<Class, PrimitiveType> enumTypes = new HashMap<Class, PrimitiveType>();

    static {
        enumTypes.put(Integer.TYPE, PrimitiveType.INT);
        enumTypes.put(Long.TYPE, PrimitiveType.LONG);
        enumTypes.put(Boolean.TYPE, PrimitiveType.BOOLEAN);
        enumTypes.put(Byte.TYPE, PrimitiveType.BYTE);
        enumTypes.put(Short.TYPE, PrimitiveType.SHORT);
        enumTypes.put(Character.TYPE, PrimitiveType.CHAR);
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
        PrimitiveType primitiveType = enumTypes.get(field.getType());
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
            default:
                throw new IllegalArgumentException(field.getType().toString());
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
        PrimitiveType primitiveType = enumTypes.get(field.getType());
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
            default:
                throw new IllegalArgumentException(field.getType().toString());
        }
    }

    public static int calculateSize(Field[] fields, Object obj) {
        int size = 0;
        for (Field field : fields) {
            if (Modifier.isTransient(field.getModifiers()))
                continue;
            if (!field.getType().isPrimitive()) {
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

    public static int sizeOf(Field field) {
        Class type = field.getType();
        if (!sizes.containsKey(type))
            return 0;
        int size = sizes.get(type);
        return size;
    }
}
