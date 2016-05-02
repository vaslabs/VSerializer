package org.vaslabs.vserializer;


import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TestAlphabeticalSerializer {


    @Test
    public void test_serializing_deserializing_by_alphabetical_order() throws Exception {
        VSerializer serializer = new AlphabeticalSerializer();

        EncapsulatedData myTestObject = new EncapsulatedData();

        initWithData(myTestObject);
        byte[] data = serializer.serialize(myTestObject);
        assertEquals(15, data.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);


        assertEquals(myTestObject.a, byteBuffer.getLong());
        assertEquals(myTestObject.b, byteBuffer.getInt());
        assertEquals(myTestObject.c, byteBuffer.get());
        assertEquals(myTestObject.d, byteBuffer.getShort());

        EncapsulatedData encapsulatedData = serializer.deserialise(data, EncapsulatedData.class);
        assertNotNull(encapsulatedData);
        assertEquals(myTestObject.a, encapsulatedData.a);
        assertEquals(myTestObject.b, encapsulatedData.b);
        assertEquals(myTestObject.c, encapsulatedData.c);
        assertEquals(myTestObject.d, encapsulatedData.d);

    }

    private void initWithData(EncapsulatedData myTestObject) {
        myTestObject.a = 0xff121212;
        myTestObject.b = 0x1111;
        myTestObject.c = 0xf;
        myTestObject.d = 0xff;
    }


    public static class EncapsulatedData {
        private long a;// = 0xff121212;
        private int b;// = 0x1111;
        private short d;// = 0xff;
        private byte c;// = 0xf;
    }
}