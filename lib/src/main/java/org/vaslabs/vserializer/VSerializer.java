package org.vaslabs.vserializer;

/**
 * Created by vnicolaou on 02/05/16.
 */
public interface VSerializer {

    <T> byte[] serialize(T obj);

    <T> byte[] serialize(T[] objects);

    <T> T deserialise(byte[] data, Class<T> clazz);
}
