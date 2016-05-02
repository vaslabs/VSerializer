package org.vaslabs.serializer;

/**
 * Created by vnicolaou on 02/05/16.
 */
public interface VSerializer {

    <T> byte[] serialize(T object);

    <T, E extends Class<T>> T deserialize(byte[] byteStream, E clazz);

}
