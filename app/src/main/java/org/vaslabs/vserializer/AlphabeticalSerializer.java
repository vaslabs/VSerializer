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
import java.util.Objects;

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
        final int size = calculateSize(fields);

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);


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

        return byteBuffer.array();
    } 

    @Override
    public <T, E extends Class<T>> T deserialise(byte[] data, E clazz) {
        Field[] fields = clazz.getDeclaredFields();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        T obj = null;
        try {
            obj = instantiate(clazz);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
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

    private <T, E extends Class<T>> T instantiate(E clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(null);
    }

    private <T> void convert(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException {
        PrimitiveType primitiveType = enumTypes.get(field.getType());
        if (primitiveType == null)
            return;
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

    private void putIn(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        PrimitiveType primitiveType = enumTypes.get(field.getType());
        if (primitiveType == null)
            return;
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

    private int calculateSize(Field[] fields) {
        int size = 0;
        for (Field field : fields) {
            if (Modifier.isTransient(field.getModifiers()))
                continue;
            size += sizeOf(field);
        }
        return size;
    }

    private int sizeOf(Field field) {

        Class type = field.getType();

        int size = -1;
        if (sizes.containsKey(type)) {
            size = sizes.get(type);
        }
        if (size == -1) {
            return 0;
        }

        return size;
    }
}
