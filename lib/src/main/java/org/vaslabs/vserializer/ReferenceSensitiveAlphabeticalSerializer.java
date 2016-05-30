package org.vaslabs.vserializer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.vaslabs.vserializer.SerializationUtils.arrangeField;
import static org.vaslabs.vserializer.SerializationUtils.getAllFields;
import static org.vaslabs.vserializer.SerializationUtils.skipField;

/**
 * Created by vnicolaou on 22/05/16.
 */
public class ReferenceSensitiveAlphabeticalSerializer extends AlphabeticalSerializer{

    private final ThreadLocal<ByteBufferPutter> byteBufferPutterThreadLocal;
    private final ThreadLocal<Map<Integer, Object>> mappingThreadLocal;

    protected ReferenceSensitiveAlphabeticalSerializer() {
        byteBufferPutterThreadLocal = new ThreadLocal<>();
        mappingThreadLocal = new ThreadLocal<>();
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
        byteBufferPutterThreadLocal.remove();
        return byteBuffer.array();
    }

    @Override
    public <T> T deserialise(byte[] data, Class<T> clazz) {
        if (clazz.equals(String.class))
            return super.deserialise(data, clazz);
        Field[] fields = getAllFields(clazz);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        T obj = null;
        int instanceSignature = byteBuffer.getInt();
        if (instanceSignature == 0)
            return null;
        try {
            obj = SerializationUtils.instantiate(clazz);
            setUpThreadLocalIfMappingDoesNotExist();
            seen(instanceSignature, obj);
            obj = convert(byteBuffer, fields, obj);
        } catch (Exception e) {
            return obj;
        }
        mappingThreadLocal.remove();
        return obj;
    }

    @Override
    protected <T> void convert(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Class fieldType = field.getType();
        if (skipField(field))
            return;
        if (fieldType.isArray()) {
            super.convertArray(byteBuffer, field, obj);
            return;
        }

        PrimitiveType primitiveType = SerializationUtils.enumTypes.get(fieldType);
        if (primitiveType == null) {
            if (String.class.equals(field.getType())) {
                this.convertString(byteBuffer, field, obj);
            }
            int signature = byteBuffer.getInt();
            if (signature == 0) {
                field.set(obj, null);
            } else {
                final Object innerObject;
                if (seen(signature))
                    innerObject = getFromMapping(signature);
                else {
                    innerObject = SerializationUtils.instantiate(field.getType());
                    seen(signature, innerObject);
                }
                field.set(obj, innerObject);
                convert(byteBuffer, getAllFields(obj), innerObject);
            }
            return;
        }
        super.convert(byteBuffer, field, obj);
    }

    private void seen(int signature, Object innerObject) {
        Map<Integer, Object> signatureToObjectMap = mappingThreadLocal.get();
        signatureToObjectMap.put(signature, innerObject);
    }

    private Object getFromMapping(int signature) {
        Map<Integer, Object> signatureToObjectMap = mappingThreadLocal.get();
        return signatureToObjectMap.get(signature);
    }

    private boolean seen(int signature) {
        setUpThreadLocalIfMappingDoesNotExist();
        Map<Integer, Object> signatureToObjectMap = mappingThreadLocal.get();
        if (signatureToObjectMap.containsKey(signature))
            return true;
        return false;
    }

    private void setUpThreadLocalIfMappingDoesNotExist() {
        Map<Integer, Object> signatureToObjectMap = mappingThreadLocal.get();
        if (signatureToObjectMap == null) {
            signatureToObjectMap = new HashMap<>();
            mappingThreadLocal.set(signatureToObjectMap);
        }
    }

    @Override
    protected int computeSize(Field[] fields, Object obj) {
        SizeComputer sizeComputer = new SizeComputer(obj);
        return sizeComputer.calculateSize(fields, obj);
    }

    @Override
    protected void putIn(ByteBuffer byteBuffer, Field[] fields, Object obj) throws IllegalAccessException {
        ByteBufferPutter byteBufferPutter = new ByteBufferPutter(obj);
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
