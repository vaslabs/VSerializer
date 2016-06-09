package org.vaslabs.vserializer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by vnicolaou on 09/06/16.
 */
public class TestJavaCollectionsSerialization {

    VSerializer vSerializer = new AlphabeticalSerializer();
    private List<Object> aListOfIntegers;
    byte[] data;

    @Test
    public void test_that_integer_lists_are_serialized() {
        whenCreatingAnIntegerList();
        data = vSerializer.serialize(aListOfIntegers);
        evaluateLengthAndData();
    }

    @Test
    public void test_that_can_get_recover_integer_list() {
        whenCreatingAnIntegerList();
        data = vSerializer.serialize(aListOfIntegers);
        Integer[] recoveredArray = vSerializer.deserialise(data, Integer[].class);
        assertEquals(aListOfIntegers.get(0), recoveredArray[0]);
        List<Integer> recoveredList = vSerializer.deserialise(data, List.class, Integer.class);
        assertEquals(aListOfIntegers.get(0), recoveredList.get(0));
    }

    @Test
    public void test_serialization_of_list_of_objects_simple() {
        List<TestUtils.EncapsulatedData> encapsulatedDataList = new ArrayList<>();
        TestUtils.EncapsulatedData encapsulatedData = new TestUtils.EncapsulatedData();
        TestUtils.initWithData(encapsulatedData);
        encapsulatedDataList.add(vSerializer.deserialise(vSerializer.serialize(encapsulatedData), TestUtils.EncapsulatedData.class));
        encapsulatedData.a = 2;
        encapsulatedDataList.add(vSerializer.deserialise(vSerializer.serialize(encapsulatedData), TestUtils.EncapsulatedData.class));
        byte[] data = vSerializer.serialize(encapsulatedDataList);
        assertEquals(4 + 2 + 15*2, data.length);
        List<TestUtils.EncapsulatedData> recoveredList = vSerializer.deserialise(data, List.class, TestUtils.EncapsulatedData.class);
        assertEquals(encapsulatedDataList.get(0).a, recoveredList.get(0).a);
        assertEquals(encapsulatedDataList.get(1).a, recoveredList.get(1).a);
    }

    private void evaluateLengthAndData() {
        assertEquals(19, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(3, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());
        assertEquals(1, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());
        assertEquals(2, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());
        assertEquals(3, byteBuffer.getInt());
    }

    private void whenCreatingAnIntegerList() {
        aListOfIntegers = new ArrayList<>();
        aListOfIntegers.add(1);
        aListOfIntegers.add(2);
        aListOfIntegers.add(3);
    }

}
