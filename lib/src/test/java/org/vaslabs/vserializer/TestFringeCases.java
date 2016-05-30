package org.vaslabs.vserializer;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by vnicolaou on 05/05/16.
 */
public class TestFringeCases {

    private VSerializer vSerializer;

    @Before
    public void setUp() {
        vSerializer = new AlphabeticalSerializer();
    }

    @Test
    public void test_serialization_of_internal_strings_with_nulls() {
        TestUtils.InternalStrings internalStrings = new TestUtils.InternalStrings();
        internalStrings.myMessage = "My message";
        internalStrings.myOtherMessage = null;
        internalStrings.myNumber = 0;
        byte[] data = vSerializer.serialize(internalStrings);
        TestUtils.InternalStrings recoveredInternalStrings = vSerializer.deserialise(data, TestUtils.InternalStrings.class);
        assertEquals(internalStrings.myMessage, recoveredInternalStrings.myMessage);
        assertEquals(internalStrings.myOtherMessage, recoveredInternalStrings.myOtherMessage);
        assertEquals(internalStrings.myNumber, recoveredInternalStrings.myNumber);

        internalStrings.myMessage = "";
        internalStrings.myOtherMessage = null;
        internalStrings.myNumber = -1;

        data = vSerializer.serialize(internalStrings);
        recoveredInternalStrings = vSerializer.deserialise(data, TestUtils.InternalStrings.class);
        assertEquals(internalStrings.myMessage, recoveredInternalStrings.myMessage);
        assertEquals(internalStrings.myOtherMessage, recoveredInternalStrings.myOtherMessage);
        assertEquals(internalStrings.myNumber, recoveredInternalStrings.myNumber);

    }

    @Test
    public void test_serialization_of_object_arrays_with_nulls() {
        TestUtils.EncapsulatedData[] encapsulatedDatas = TestUtils.initEncapsulatedDataArray();
        encapsulatedDatas[3] = null;
        encapsulatedDatas[9] = null;

        int arraySize = SerializationUtils.calculateNonPrimitiveArraySize(encapsulatedDatas);
        assertEquals(164 - 15*2, arraySize);

        byte[] data = vSerializer.serialize(encapsulatedDatas);

        TestUtils.EncapsulatedData[] recoveredEncapsulatedData = vSerializer.deserialise(data, TestUtils.EncapsulatedData[].class);
        assertEquals(10, recoveredEncapsulatedData.length);
        for (int i = 0; i <recoveredEncapsulatedData.length; i++) {
            if (i == 3 || i == 9) {
                assertNull(recoveredEncapsulatedData[i]);
                continue;
            }
            assertEquals(encapsulatedDatas[i].a, recoveredEncapsulatedData[i].a);
            assertEquals(i, recoveredEncapsulatedData[i].b);
            assertEquals(0xf, recoveredEncapsulatedData[i].c);
            assertEquals(0xff, recoveredEncapsulatedData[i].d);
        }
    }

    @Test
    public void test_serialization_of_inner_object_arrays_with_nulls() {
        TestUtils.EncapsulatedData[] encapsulatedDatas = TestUtils.initEncapsulatedDataArray();
        encapsulatedDatas[3] = null;
        encapsulatedDatas[9] = null;

        TestUtils.DataStructureWithObjectArray dsObjectArray = new TestUtils.DataStructureWithObjectArray();
        dsObjectArray.encapsulatedDatas = encapsulatedDatas;
        dsObjectArray.somethingElse = false;
        dsObjectArray.value = 48204431L;
        byte[] data = vSerializer.serialize(dsObjectArray);

        assertEquals(143, data.length);

        TestUtils.DataStructureWithObjectArray recoveredDsObjectArray = vSerializer.deserialise(data, TestUtils.DataStructureWithObjectArray.class);

        assertEquals(dsObjectArray.encapsulatedDatas.length, recoveredDsObjectArray.encapsulatedDatas.length);

        assertEquals(dsObjectArray.value, recoveredDsObjectArray.value);
        assertEquals(dsObjectArray.somethingElse, recoveredDsObjectArray.somethingElse);

        for (int i = 0; i < dsObjectArray.encapsulatedDatas.length; i++) {
            if (i == 3 || i == 9) {
                assertNull(recoveredDsObjectArray.encapsulatedDatas[i]);
                continue;
            }
            assertEquals(dsObjectArray.encapsulatedDatas[i].a, recoveredDsObjectArray.encapsulatedDatas[i].a);
            assertEquals(dsObjectArray.encapsulatedDatas[i].b, recoveredDsObjectArray.encapsulatedDatas[i].b);
            assertEquals(dsObjectArray.encapsulatedDatas[i].c, recoveredDsObjectArray.encapsulatedDatas[i].c);
            assertEquals(dsObjectArray.encapsulatedDatas[i].d, recoveredDsObjectArray.encapsulatedDatas[i].d);
        }
    }

    @Test
    public void test_null_inner_arrays() {
        TestUtils.DataStructureWithObjectArray dsObjectArray = new TestUtils.DataStructureWithObjectArray();
        dsObjectArray.encapsulatedDatas = null;
        dsObjectArray.somethingElse = false;
        dsObjectArray.value = 48204431L;
        byte[] data = vSerializer.serialize(dsObjectArray);
        assertEquals(13, data.length);
        TestUtils.DataStructureWithObjectArray recoveredDsObjectArray =
                vSerializer.deserialise(data, TestUtils.DataStructureWithObjectArray.class);
        assertNull(recoveredDsObjectArray.encapsulatedDatas);
        assertEquals(false, recoveredDsObjectArray.somethingElse);
        assertEquals(48204431, recoveredDsObjectArray.value);
    }

    @Test
    public void test_0_length_arrays() {
        TestUtils.DataStructureWithObjectArray dsObjectArray = new TestUtils.DataStructureWithObjectArray();
        dsObjectArray.encapsulatedDatas = new TestUtils.EncapsulatedData[0];
        dsObjectArray.somethingElse = false;
        dsObjectArray.value = 48204431L;
        byte[] data = vSerializer.serialize(dsObjectArray);
        assertEquals(13, data.length);
        TestUtils.DataStructureWithObjectArray recoveredDsObjectArray =
                vSerializer.deserialise(data, TestUtils.DataStructureWithObjectArray.class);
        assertEquals(0, recoveredDsObjectArray.encapsulatedDatas.length);
        assertEquals(false, recoveredDsObjectArray.somethingElse);
        assertEquals(48204431, recoveredDsObjectArray.value);
    }

    @Test
    public void test_null_and_0_length_primitive_arrays() {
        short[] shortValues = new short[0];
        byte[] data = vSerializer.serialize(shortValues);
        assertEquals(0, data.length);
        short[] recoveredShortValues = vSerializer.deserialise(data, short[].class);
        assertEquals(0, recoveredShortValues.length);
        shortValues = null;
        data = vSerializer.serialize(shortValues);
        assertEquals(0, data.length);
        recoveredShortValues = vSerializer.deserialise(data, short[].class);
        assertEquals(0, recoveredShortValues.length);
    }

    @Test
    public void test_inner_primitive_arrays() {
        TestUtils.DataStructureWithArray dataStructureWithArray = new TestUtils.DataStructureWithArray();
        dataStructureWithArray.numbers = null;
        dataStructureWithArray.somethingElse = true;
        dataStructureWithArray.value = -1L;
        byte[] data = vSerializer.serialize(dataStructureWithArray);
        assertEquals(13, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(-1, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());
        assertEquals(-1L, byteBuffer.getLong());

        TestUtils.DataStructureWithArray recoveredDataStructure = vSerializer.deserialise(data, TestUtils.DataStructureWithArray.class);
        assertNull(recoveredDataStructure.numbers);
        assertEquals(true, recoveredDataStructure.somethingElse);
        assertEquals(-1L, recoveredDataStructure.value);

        dataStructureWithArray.numbers = new int[0];

        data = vSerializer.serialize(dataStructureWithArray);
        assertEquals(13, data.length);
        recoveredDataStructure = vSerializer.deserialise(data, TestUtils.DataStructureWithArray.class);

        assertEquals(0, recoveredDataStructure.numbers.length);
        assertEquals(true, recoveredDataStructure.somethingElse);
        assertEquals(-1L, recoveredDataStructure.value);
    }

    @Test
    public void test_transient_fields_not_serialized() {
        TestUtils.TransientData transientData = new TestUtils.TransientData();
        transientData.myNumber = 15;
        transientData.transientNumber = -15;
        byte[] data = vSerializer.serialize(transientData);
        assertEquals(4, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(15, byteBuffer.getInt());
        TestUtils.TransientData recoveredTransientData = vSerializer.deserialise(data, TestUtils.TransientData.class);
        assertEquals(15, recoveredTransientData.myNumber);
        assertEquals(0, recoveredTransientData.transientNumber);
    }

    @Test
    public void test_static_fields_not_serialized() {
        TestUtils.StaticData staticData = new TestUtils.StaticData();
        staticData.myNumber = 4;
        TestUtils.StaticData.staticNumber = -1;
        byte[] data = vSerializer.serialize(staticData);
        assertEquals(4, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(4, byteBuffer.getInt());

        TestUtils.StaticData.staticNumber = -111;
        TestUtils.StaticData recoveredStaticData = vSerializer.deserialise(data, TestUtils.StaticData.class);

        assertEquals(4, recoveredStaticData.myNumber);
        assertEquals(-111, TestUtils.StaticData.staticNumber);

    }

    @Test
    public void test_instantiate_object_with_private_constructor() {
        try {
            PrivateConstructor pc = SerializationUtils.instantiate(PrivateConstructor.class);
            assertNotNull(pc);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    static class PrivateConstructor {
        private PrivateConstructor() {

        }
    }

    @Test
    public void test_array_of_strings() {
        String[] strings = new String[] {"s1", "s2", "s3"};
        byte[] data = vSerializer.serialize(strings);
        String[] recoveredStrings = vSerializer.deserialise(data, String[].class);
        assertEquals(strings.length, recoveredStrings.length);
        assertEquals(strings[0], recoveredStrings[0]);
        assertEquals(strings[1], recoveredStrings[1]);
        assertEquals(strings[2], recoveredStrings[2]);
    }


}
