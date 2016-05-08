package org.vaslabs.vserializer;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Created by vnicolaou on 08/05/16.
 */
public class TestInheritedSerialization {

    private VSerializer vSerializer;

    @Before
    public void setUp() {
        vSerializer = new AlphabeticalSerializer();
    }


    @Test
    public void test_that_superclass_fields_are_serialized() {
        EncapsulatedDataSubclass encapsulatedDataSubclass = new EncapsulatedDataSubclass();
        encapsulatedDataSubclass.myMessage = "MyMessage";
        TestUtils.initWithData(encapsulatedDataSubclass);
        byte[] data = vSerializer.serialize(encapsulatedDataSubclass);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(encapsulatedDataSubclass.myMessage.length()*2 + 4 + 15, data.length);
        assertEquals(encapsulatedDataSubclass.a, byteBuffer.getLong());

        EncapsulatedDataSubclass recoveredEncapsulatedDataSubclass = vSerializer.deserialise(data, EncapsulatedDataSubclass.class);
        assertEquals(encapsulatedDataSubclass.myMessage, recoveredEncapsulatedDataSubclass.myMessage);
        assertEquals(encapsulatedDataSubclass.a, recoveredEncapsulatedDataSubclass.a);
        assertEquals(encapsulatedDataSubclass.b, recoveredEncapsulatedDataSubclass.b);
        assertEquals(encapsulatedDataSubclass.c, recoveredEncapsulatedDataSubclass.c);
        assertEquals(encapsulatedDataSubclass.d, recoveredEncapsulatedDataSubclass.d);
    }

    public static class EncapsulatedDataSubclass extends TestUtils.EncapsulatedData {
        private String myMessage = "";
    }

}
