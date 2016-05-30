package org.vaslabs.vserializer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.vaslabs.vserializer.SerializationUtils.arrangeField;
import static org.vaslabs.vserializer.SerializationUtils.getAllFields;
import static org.vaslabs.vserializer.SerializationUtils.skipField;

/**
 * Created by vnicolaou on 22/05/16.
 */
public class ReferenceSensitiveAlphabeticalSerializer extends AlphabeticalSerializer{

    private ThreadLocal<ByteBufferPutter> byteBufferPutterThreadLocal;

    protected ReferenceSensitiveAlphabeticalSerializer() {

    }

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null)
            return new byte[0];
        if (obj.getClass().isArray()) {
            boolean isPrimitive = SerializationUtils.enumTypes.containsKey(obj.getClass());
            if (isPrimitive) {
                return SerializationUtils.toBytes(obj);
            }
        }
        if (obj instanceof String)
            return super.serialize(obj);
        final Field[] fields = getAllFields(obj);
        final int size = computeSize(fields, obj);

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);

        try {
            byteBuffer.putInt(System.identityHashCode(obj));
            putIn(byteBuffer, fields, obj);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

        return byteBuffer.array();
    }

    @Override
    protected int computeSize(Field[] fields, Object obj) {
        SizeComputer sizeComputer = new SizeComputer(obj);
        return sizeComputer.calculateSize(fields, obj);
    }

    @Override
    protected void putIn(ByteBuffer byteBuffer, Field[] fields, Object obj) throws IllegalAccessException {
        ByteBufferPutter byteBufferPutter = new ByteBufferPutter(obj);
        byteBufferPutterThreadLocal = new ThreadLocal<>();
        byteBufferPutterThreadLocal.set(byteBufferPutter);
        super.putIn(byteBuffer, fields, obj);
    }

    @Override
    protected void putIn(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        if (obj == null)
            return;
        Class type = field.getType();
        if (type.isPrimitive()) {
            super.putIn(byteBuffer, field, obj);
            return;
        }
        ByteBufferPutter byteBufferPutter = byteBufferPutterThreadLocal.get();
        try {
            arrangeField(field, obj);
        } catch (Exception e) {
        }
        byteBufferPutter.put(byteBuffer, field, obj);
    }



        private class ByteBufferPutter {

            private Set<Integer> seenObject;

            private ByteBufferPutter(Object parent) {
                seenObject = new HashSet<>();
                seenObject.add(System.identityHashCode(parent));
            }

            public void put(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {


                Object fieldObject = field.get(obj);
                if (fieldObject != null)
                    byteBuffer.putInt(System.identityHashCode(fieldObject));
                if (seen(fieldObject) || fieldObject == null) {
                    return;
                } else {
                    putIn(byteBuffer, getAllFields(fieldObject), fieldObject);
                    return;
                }

            }

            private boolean seen(Object fieldObject) {
                final int signature;
                if (fieldObject == null)
                    signature = 0;
                else
                    signature = System.identityHashCode(fieldObject);
                if (seenObject.contains(signature))
                    return true;
                seenObject.add(signature);
                return false;
            }
        }
}
