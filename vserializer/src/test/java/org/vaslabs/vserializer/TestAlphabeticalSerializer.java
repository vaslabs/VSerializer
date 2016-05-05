package org.vaslabs.vserializer;


import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.vaslabs.vserializer.TestUtils.initWithData;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TestAlphabeticalSerializer {


    @Test
    public void test_serializing_deserializing_by_alphabetical_order() throws Exception {
        VSerializer serializer = new AlphabeticalSerializer();

        TestUtils.EncapsulatedData myTestObject = new TestUtils.EncapsulatedData();

        initWithData(myTestObject);
        byte[] data = serializer.serialize(myTestObject);
        assertEquals(15, data.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);


        assertEquals(myTestObject.a, byteBuffer.getLong());
        assertEquals(myTestObject.b, byteBuffer.getInt());
        assertEquals(myTestObject.c, byteBuffer.get());
        assertEquals(myTestObject.d, byteBuffer.getShort());

        TestUtils.EncapsulatedData encapsulatedData = serializer.deserialise(data, TestUtils.EncapsulatedData.class);
        assertNotNull(encapsulatedData);
        assertEquals(myTestObject.a, encapsulatedData.a);
        assertEquals(myTestObject.b, encapsulatedData.b);
        assertEquals(myTestObject.c, encapsulatedData.c);
        assertEquals(myTestObject.d, encapsulatedData.d);
    }

    @Test
    public void test_complex_serialisation_deserialisation() {
        VSerializer serializer = new AlphabeticalSerializer();
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new TestUtils.ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;

        int size = SerializationUtils.calculateSize(cds.getClass().getDeclaredFields(), cds);

        assertEquals(26, size);

        byte[] data = serializer.serialize(cds);
        assertEquals(26, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(cds.a, byteBuffer.getLong());
        assertEquals(cds.b, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());

        cds = serializer.deserialise(data, TestUtils.ComplexDataStructure.class);

        assertEquals(0xff, cds.a);
        assertEquals(-1, cds.b);
        assertNotNull(cds.somethingElse);
        assertEquals(-2, cds.somethingElse.a);
        assertEquals(5, cds.somethingElse.b);
    }

    @Test
    public void test_serialization_deserialization_of_objects_that_encapsulate_primitive_arrays() {
        TestUtils.DataStructureWithArray dataStructureWithArray = new TestUtils.DataStructureWithArray();
        dataStructureWithArray.numbers = new int[5];
        dataStructureWithArray.numbers[0] = 1;
        dataStructureWithArray.numbers[1] = -1;
        dataStructureWithArray.numbers[2] = 2;
        dataStructureWithArray.numbers[3] = -2;
        dataStructureWithArray.numbers[4] = 3;

        dataStructureWithArray.somethingElse = true;
        dataStructureWithArray.value = 0xff11223344L;

        assertEquals(33, SerializationUtils.calculateSize(dataStructureWithArray.getClass().getDeclaredFields(), dataStructureWithArray));
        VSerializer vSerializer = new AlphabeticalSerializer();
        byte[] data = vSerializer.serialize(dataStructureWithArray);
        assertEquals(33, data.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        assertEquals(5, byteBuffer.getInt());
        assertEquals(1, byteBuffer.getInt());
        assertEquals(-1, byteBuffer.getInt());
        assertEquals(2, byteBuffer.getInt());
        assertEquals(-2, byteBuffer.getInt());
        assertEquals(3, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());
        assertEquals(dataStructureWithArray.value, byteBuffer.getLong());

        TestUtils.DataStructureWithArray dataStructureWithArrayDeserialized = vSerializer.deserialise(data, TestUtils.DataStructureWithArray.class);

        assertTrue(Arrays.equals(dataStructureWithArray.numbers, dataStructureWithArrayDeserialized.numbers));
        assertEquals(dataStructureWithArray.somethingElse, dataStructureWithArrayDeserialized.somethingElse);
        assertEquals(dataStructureWithArray.value, dataStructureWithArrayDeserialized.value);
    }

    @Test
    public void test_serialization_deserialization_with_final_values() {
        TestUtils.FinalEncapsulatedData finalEncapsulatedData = new TestUtils.FinalEncapsulatedData(1L, 2, (short)3, (byte)4);
        VSerializer vSerializer = new AlphabeticalSerializer();
        byte[] data = vSerializer.serialize(finalEncapsulatedData);

        TestUtils.FinalEncapsulatedData finalEncapsulatedData1 = vSerializer.deserialise(data, TestUtils.FinalEncapsulatedData.class);
        assertEquals(1L, finalEncapsulatedData1.a);
    }

    @Test
    public void test_string_serialization_deserialization() {
        String someString = "Hello world";
        VSerializer vSerializer = new AlphabeticalSerializer();
        byte[] data = vSerializer.serialize(someString);
        String recoveredString = vSerializer.deserialise(data, String.class);
        assertEquals(someString, recoveredString);

        someString = "" + (char)5384 + (char)4382;
        data = vSerializer.serialize(someString);
        recoveredString = vSerializer.deserialise(data, String.class);
        assertEquals(someString, recoveredString);

        char[] chars = new char[65000];
        for (char c = 0; c < chars.length; c++) {
            chars[c] = c;
        }
        someString = new String(chars);
        data = vSerializer.serialize(someString);
        recoveredString = vSerializer.deserialise(data, String.class);
        assertEquals(someString, recoveredString);

    }

    @Test
    public void test_serialization_of_object_arrays() {
        TestUtils.EncapsulatedData[] encapsulatedDatas = new TestUtils.EncapsulatedData[10];
        for (int i = 0; i < encapsulatedDatas.length; i++) {
            encapsulatedDatas[i] = new TestUtils.EncapsulatedData();
            initWithData(encapsulatedDatas[i]);
            encapsulatedDatas[i].b = i;
        }

        VSerializer vSerializer = new AlphabeticalSerializer();
        byte[] data = vSerializer.serialize(encapsulatedDatas);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        int size = byteBuffer.getInt();
        assertEquals(10, size);
        for (int i = 0; i <encapsulatedDatas.length; i++) {
            assertEquals(encapsulatedDatas[i].a, byteBuffer.getLong());
            assertEquals(i, byteBuffer.getInt());
            assertEquals(0xf, byteBuffer.get());
            assertEquals(0xff, byteBuffer.getShort());
        }

        TestUtils.EncapsulatedData[] recoveredEncapsulatedData = vSerializer.deserialise(data, TestUtils.EncapsulatedData[].class);
        assertEquals(10, recoveredEncapsulatedData.length);
        for (int i = 0; i <recoveredEncapsulatedData.length; i++) {
            assertEquals(encapsulatedDatas[i].a, recoveredEncapsulatedData[i].a);
            assertEquals(i, recoveredEncapsulatedData[i].b);
            assertEquals(0xf, recoveredEncapsulatedData[i].c);
            assertEquals(0xff, recoveredEncapsulatedData[i].d);
        }
    }

}