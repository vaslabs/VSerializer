package org.vaslabs.vserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vnicolaou on 03/05/16.
 */
public class SerializationUtils {
    static Map<Class, Integer> sizes = new HashMap<>();
    static Map<Class, PrimitiveType> enumTypes = new HashMap<Class, PrimitiveType>();


    static {
        SerializationUtils.sizes.put(Integer.TYPE, 4);
        SerializationUtils.sizes.put(Long.TYPE, 8);
        SerializationUtils.sizes.put(Boolean.TYPE, 1);
        SerializationUtils.sizes.put(Byte.TYPE, 1);
        SerializationUtils.sizes.put(Short.TYPE, 2);
        SerializationUtils.sizes.put(Character.TYPE, 2);
        SerializationUtils.sizes.put(int[].class, 4);
        SerializationUtils.sizes.put(long[].class, 8);
        SerializationUtils.sizes.put(boolean[].class, 1);
        SerializationUtils.sizes.put(byte[].class, 1);
        SerializationUtils.sizes.put(short[].class, 2);
        SerializationUtils.sizes.put(char[].class, 2);
    }

    static {
        SerializationUtils.enumTypes.put(Integer.TYPE, PrimitiveType.INT);
        SerializationUtils.enumTypes.put(Long.TYPE, PrimitiveType.LONG);
        SerializationUtils.enumTypes.put(Boolean.TYPE, PrimitiveType.BOOLEAN);
        SerializationUtils.enumTypes.put(Byte.TYPE, PrimitiveType.BYTE);
        SerializationUtils.enumTypes.put(Short.TYPE, PrimitiveType.SHORT);
        SerializationUtils.enumTypes.put(Character.TYPE, PrimitiveType.CHAR);
        SerializationUtils.enumTypes.put(int[].class, PrimitiveType.INT);
        SerializationUtils.enumTypes.put(long[].class, PrimitiveType.LONG);
        SerializationUtils.enumTypes.put(boolean[].class, PrimitiveType.BOOLEAN);
        SerializationUtils.enumTypes.put(byte[].class, PrimitiveType.BYTE);
        SerializationUtils.enumTypes.put(short[].class, PrimitiveType.SHORT);
        SerializationUtils.enumTypes.put(char[].class, PrimitiveType.CHAR);
    }

    public static <T, E extends Class<T>> T instantiate(E clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance(null);
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
                if (field.getType().equals(String.class)) {
                    try {
                        String string = (String) field.get(obj);
                        if (string == null)
                            size += 4;
                        else
                            size += 4 + string.length()*2;
                        continue;
                    } catch (IllegalAccessException e) {
                        return 0;
                    }
                }
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
            final int consistentTypeSize;
            if (!sizes.containsKey(type)) {
                return sizeOfArrayType(field, obj);
            }
            else {
                consistentTypeSize = sizes.get(type);
            }
            int arraySize = findArrayLength(field, obj);
            field.setAccessible(false);
            return arraySize*consistentTypeSize;
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private static int sizeOfArrayType(Field field, Object obj) throws IllegalAccessException {
        Object[] objects = (Object[]) field.get(obj);
        if (objects == null || objects.length == 0) {
            return 0;
        }
        int sizeSum = 0;
        for (Object object : objects) {
            if (object == null) {
                continue;
            }
            Class type = object.getClass();
            sizeSum += calculateSize(type.getDeclaredFields(), object);
        }
        return sizeSum + objects.length;
    }


    protected static int findArrayLength(Field field, Object obj) throws IllegalAccessException {
        if (!enumTypes.containsKey(field.getType())) {
            return findArrayLengthNonPrimitive(field, obj);
        }
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

    private static int findArrayLengthNonPrimitive(Field field, Object obj) throws IllegalAccessException {
        Object[] objects = (Object[]) field.get(obj);
        return objects.length;
    }

    public static int sizeOf(Field field) {
        Class type = field.getType();
        if (!sizes.containsKey(type))
            return 0;
        int size = sizes.get(type);
        return size;
    }

    public static <T> void arrangeField(Field field, T obj) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    public static void houseKeeping(Field field) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(false);
    }

    public static <T> int calculateNonPrimitiveArraySize(T[] objects) {
        final Field[] fields = objects[0].getClass().getDeclaredFields();
        final int sizeOfSingleObject = SerializationUtils.calculateSize(fields, objects[0]);

        int totalSize = objects.length + 4;
        for (T object : objects) {
            if (object != null)
                totalSize += sizeOfSingleObject;
        }
        return totalSize;
    }
}
