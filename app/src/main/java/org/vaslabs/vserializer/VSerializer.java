package org.vaslabs.vserializer;

/**
 * Created by vnicolaou on 02/05/16.
 */
public interface VSerializer {

    <T> byte[] serialize(T myTestObject);

    <T, E extends Class<T>> T deserialise(byte[] data, E clazz);
}
