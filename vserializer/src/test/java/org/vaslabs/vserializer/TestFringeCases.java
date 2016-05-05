package org.vaslabs.vserializer;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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



}
