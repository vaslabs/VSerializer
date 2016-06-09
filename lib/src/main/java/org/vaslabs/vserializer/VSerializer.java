package org.vaslabs.vserializer;

import java.util.List;

/**
 * Created by vnicolaou on 02/05/16.
 */
public interface VSerializer {

    <T> byte[] serialize(T obj);

    <T> byte[] serialize(List<T> list);

    <T> byte[] serialize(T[] objects);

    <T> T deserialise(byte[] data, Class<T> clazz);


    <T> List<T> deserialise(byte[] data, Class<List> listClass, Class<T> parametarizedClass);
}
