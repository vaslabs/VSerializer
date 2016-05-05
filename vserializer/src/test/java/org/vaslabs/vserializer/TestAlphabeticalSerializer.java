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

    VSerializer vSerializer = new AlphabeticalSerializer();


    @Test
    public void test_serializing_deserializing_by_alphabetical_order() throws Exception {

        TestUtils.EncapsulatedData myTestObject = new TestUtils.EncapsulatedData();

        initWithData(myTestObject);
        byte[] data = vSerializer.serialize(myTestObject);
        assertEquals(15, data.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);


        assertEquals(myTestObject.a, byteBuffer.getLong());
        assertEquals(myTestObject.b, byteBuffer.getInt());
        assertEquals(myTestObject.c, byteBuffer.get());
        assertEquals(myTestObject.d, byteBuffer.getShort());

        TestUtils.EncapsulatedData encapsulatedData = vSerializer.deserialise(data, TestUtils.EncapsulatedData.class);
        assertNotNull(encapsulatedData);
        assertEquals(myTestObject.a, encapsulatedData.a);
        assertEquals(myTestObject.b, encapsulatedData.b);
        assertEquals(myTestObject.c, encapsulatedData.c);
        assertEquals(myTestObject.d, encapsulatedData.d);
    }

    @Test
    public void test_complex_serialisation_deserialisation() {
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new TestUtils.ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;

        int size = SerializationUtils.calculateSize(cds.getClass().getDeclaredFields(), cds);

        assertEquals(26, size);

        byte[] data = vSerializer.serialize(cds);
        assertEquals(26, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(cds.a, byteBuffer.getLong());
        assertEquals(cds.b, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());

        cds = vSerializer.deserialise(data, TestUtils.ComplexDataStructure.class);

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
        byte[] data = vSerializer.serialize(finalEncapsulatedData);

        TestUtils.FinalEncapsulatedData finalEncapsulatedData1 = vSerializer.deserialise(data, TestUtils.FinalEncapsulatedData.class);
        assertEquals(1L, finalEncapsulatedData1.a);
    }

    @Test
    public void test_string_serialization_deserialization() {
        String someString = "Hello world";
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
        TestUtils.EncapsulatedData[] encapsulatedDatas = TestUtils.initEncapsulatedDataArray();

        byte[] data = vSerializer.serialize(encapsulatedDatas);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        int size = byteBuffer.getInt();
        assertEquals(10, size);
        assertEquals(164, data.length);
        for (int i = 0; i <encapsulatedDatas.length; i++) {
            assertEquals(1, byteBuffer.get());
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

    @Test
    public void test_serialization_of_inner_object_arrays() {
        TestUtils.EncapsulatedData[] encapsulatedDatas = TestUtils.initEncapsulatedDataArray();
        TestUtils.DataStructureWithObjectArray dsObjectArray = new TestUtils.DataStructureWithObjectArray();
        dsObjectArray.encapsulatedDatas = encapsulatedDatas;
        dsObjectArray.somethingElse = false;
        dsObjectArray.value = 48204431L;
        byte[] data = vSerializer.serialize(dsObjectArray);
        assertEquals(173, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(10, byteBuffer.getInt());
        for (int i = 0; i < encapsulatedDatas.length; i++) {
            assertEquals(1, byteBuffer.get());
            assertEquals(encapsulatedDatas[i].a, byteBuffer.getLong());
            assertEquals(i, byteBuffer.getInt());
            assertEquals(0xf, byteBuffer.get());
            assertEquals(0xff, byteBuffer.getShort());
        }
        assertEquals(0, byteBuffer.get());
        assertEquals(dsObjectArray.value, byteBuffer.getLong());

        TestUtils.DataStructureWithObjectArray recoveredDsObjectArray = vSerializer.deserialise(data, TestUtils.DataStructureWithObjectArray.class);
        assertEquals(dsObjectArray.encapsulatedDatas.length, recoveredDsObjectArray.encapsulatedDatas.length);
        for (int i = 0; i < dsObjectArray.encapsulatedDatas.length; i++) {
            assertEquals(dsObjectArray.encapsulatedDatas[i].a, recoveredDsObjectArray.encapsulatedDatas[i].a);
            assertEquals(dsObjectArray.encapsulatedDatas[i].b, recoveredDsObjectArray.encapsulatedDatas[i].b);
            assertEquals(dsObjectArray.encapsulatedDatas[i].c, recoveredDsObjectArray.encapsulatedDatas[i].c);
            assertEquals(dsObjectArray.encapsulatedDatas[i].d, recoveredDsObjectArray.encapsulatedDatas[i].d);
        }
    }

    @Test
    public void test_serialization_of_internal_strings() {
        TestUtils.InternalStrings internalStrings = new TestUtils.InternalStrings();
        internalStrings.myMessage = "My Message";
        internalStrings.myOtherMessage = "My Other Message";
        internalStrings.myNumber = -255;

        assertEquals(internalStrings.myMessage.length()*2 + internalStrings.myOtherMessage.length()*2 + 4 + 8,
                SerializationUtils.calculateSize(TestUtils.InternalStrings.class.getDeclaredFields(), internalStrings));

        byte[] data = vSerializer.serialize(internalStrings);
        assertEquals(internalStrings.myMessage.length()*2 + internalStrings.myOtherMessage.length()*2 + 4 + 8, data.length);
        TestUtils.InternalStrings recoveredInternalStrings = vSerializer.deserialise(data, TestUtils.InternalStrings.class);
        assertEquals(internalStrings.myMessage, recoveredInternalStrings.myMessage);
        assertEquals(internalStrings.myOtherMessage, recoveredInternalStrings.myOtherMessage);
        assertEquals(internalStrings.myNumber, recoveredInternalStrings.myNumber);
    }

    @Test
    public void test_serialization_of_primitive_arrays() {
        int[] array = new int[5];
        for (int i = 0; i < array.length; i++) {
            array[i]= i;
        }
        byte[] data = vSerializer.serialize(array);
        assertEquals(20, data.length);
        int[] recoveredArray = vSerializer.deserialise(data, int[].class);
        assertEquals(array.length, recoveredArray.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], recoveredArray[i]);
        }
    }

    @Test
    public void test_serialization_of_all_primitive_types() {
        TestUtils.AllEncapsulatedData allEncapsulatedData = new TestUtils.AllEncapsulatedData();
        allEncapsulatedData.a = -1L;
        allEncapsulatedData.b = 1;
        allEncapsulatedData.c = 127;
        allEncapsulatedData.d = -32768;
        allEncapsulatedData.e = true;
        allEncapsulatedData.f = 'h';

        byte[] data = vSerializer.serialize(allEncapsulatedData);

        TestUtils.AllEncapsulatedData recoveredData = vSerializer.deserialise(data, TestUtils.AllEncapsulatedData.class);
        assertEquals(allEncapsulatedData.a, recoveredData.a);
        assertEquals(allEncapsulatedData.b, recoveredData.b);
        assertEquals(allEncapsulatedData.c, recoveredData.c);
        assertEquals(allEncapsulatedData.d, recoveredData.d);
        assertEquals(allEncapsulatedData.e, recoveredData.e);
        assertEquals(allEncapsulatedData.f, recoveredData.f);

    }

    @Test
    public void test_serialization_of_all_primitive_array_types() {
        TestUtils.AllEncapsulatedArrayData allEncapsulatedData = new TestUtils.AllEncapsulatedArrayData();
        allEncapsulatedData.a = new long[] {-1L, 1L};
        allEncapsulatedData.b = new int[] {1, -1};
        allEncapsulatedData.c = new byte[] {127, -128};
        allEncapsulatedData.d = new short[] {-32768, 32767};
        allEncapsulatedData.e = new boolean[] {true, false, true};
        allEncapsulatedData.f = new char[]{'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd', '!'};

        byte[] data = vSerializer.serialize(allEncapsulatedData);

        TestUtils.AllEncapsulatedArrayData recoveredData = vSerializer.deserialise(data, TestUtils.AllEncapsulatedArrayData.class);
        assertTrue(Arrays.equals(allEncapsulatedData.a, recoveredData.a));
        assertTrue(Arrays.equals(allEncapsulatedData.b, recoveredData.b));
        assertTrue(Arrays.equals(allEncapsulatedData.c, recoveredData.c));
        assertTrue(Arrays.equals(allEncapsulatedData.d, recoveredData.d));
        assertTrue(Arrays.equals(allEncapsulatedData.e, recoveredData.e));
        assertTrue(Arrays.equals(allEncapsulatedData.f, recoveredData.f));

    }

}