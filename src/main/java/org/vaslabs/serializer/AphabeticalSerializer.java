package org.vaslabs.serializer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by vnicolaou on 02/05/16.
 */
public class AphabeticalSerializer implements VSerializer {
    public <T> byte[] serialize(T object) {
        Field[] fields = object.getClass().getDeclaredFields();
        Arrays.sort(fields, new Comparator<Field>() {
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        int dataSize = calculateSize(fields);
        byte[] data = new byte[dataSize];
        ByteBuffer byteBuffer = new ByteBuffer(data);

        return new byte[0];
    }

    private int calculateSize(Field[] fields) {
        int size = 0;
        for (Field field : fields) {
            if (!field.getType().isPrimitive()) {
                throw new IllegalArgumentException("Only primitives are supported");
            }
            field.getType().equals()
        }
    }

    public <T, E extends Class<T>> T deserialize(byte[] byteStream, E clazz) {
        return null;
    }
}
