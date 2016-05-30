package org.vaslabs.vserializer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.vaslabs.vserializer.SerializationUtils.arrangeField;
import static org.vaslabs.vserializer.SerializationUtils.getAllFields;
import static org.vaslabs.vserializer.SerializationUtils.sizeOf;

/**
 * Created by vnicolaou on 22/05/16.
 */
public class SizeComputer {

    private Set<Integer> seenSet;
    int size = 4;
    public SizeComputer(Object obj) {
        seenSet = new HashSet<Integer>();
        seenSet.add(System.identityHashCode(obj));
    }

    private SizeComputer() {
        size = 0;
        seenSet = new HashSet<Integer>();
    }

    public int calculateSize(Field[] fields, Object obj) {
        for (Field field : fields) {
            if (SerializationUtils.skipField(field))
                continue;
            if (field.getType().isArray()) {
                size += 4 + SerializationUtils.sizeOfArray(field, obj);
                continue;
            }
            else if (!field.getType().isPrimitive()) {
                if (seen(field, obj)) {
                    size += 4;
                    continue;
                }
                if (field.getType().equals(String.class)) {
                    try {
                        try {
                            arrangeField(field, obj);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
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
                size += 4;
                try {
                    arrangeField(field, obj);
                    final Object newObj = field.get(obj);
                    if (newObj != null) {
                        SizeComputer sizeComputer = new SizeComputer();
                        size += sizeComputer.calculateSize(getAllFields(newObj), newObj);
                    }
                } catch (Exception e) {

                }
            } else {
                size += sizeOf(field);
            }
        }
        return size;
    }

    private boolean seen(Field field, Object obj) {
        Object fieldObject = null;
        try {
            arrangeField(field, obj);
            fieldObject = field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final int objectReference;
        if (fieldObject == null)
            objectReference = 0;
        else
            objectReference = System.identityHashCode(fieldObject);

        if (this.seenSet.contains(objectReference))
            return true;
        seenSet.add(objectReference);
        return false;
    }


}
