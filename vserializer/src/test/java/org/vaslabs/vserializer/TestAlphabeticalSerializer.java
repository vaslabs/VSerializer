package org.vaslabs.vserializer;


import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void test_complex_serialisation_deserialisation() {
        VSerializer serializer = new AlphabeticalSerializer();
        ComplexDataStructure cds = new ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;

        int size = AlphabeticalSerializer.calculateSize(cds.getClass().getDeclaredFields(), cds);

        assertEquals(26, size);

        byte[] data = serializer.serialize(cds);
        assertEquals(26, data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(cds.a, byteBuffer.getLong());
        assertEquals(cds.b, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());

        cds = serializer.deserialise(data, ComplexDataStructure.class);

        assertEquals(0xff, cds.a);
        assertEquals(-1, cds.b);
        assertNotNull(cds.somethingElse);
        assertEquals(-2, cds.somethingElse.a);
        assertEquals(5, cds.somethingElse.b);
    }

    @Test
    public void measureDifferenceFromVM() {
        VSerializer serializer = new AlphabeticalSerializer();
        ComplexDataStructure cds = new ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;
        byte[] data = serializer.serialize(cds);

        byte[] jvmData = serializeObject(cds);

        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
        assertTrue(data.length < jvmData.length);
    }

    private void initWithData(EncapsulatedData myTestObject) {
        myTestObject.a = 0xff121212;
        myTestObject.b = 0x1111;
        myTestObject.c = 0xf;
        myTestObject.d = 0xff;
    }

    private byte[] serializeObject(ComplexDataStructure cds) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = null;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(cds);
            data = baos.toByteArray();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;

    }


    public static class EncapsulatedData {
        private long a;// = 0xff121212;
        private int b;// = 0x1111;
        private short d;// = 0xff;
        private byte c;// = 0xf;
    }

    public static class ComplexDataStructure implements Serializable {
        private long a;
        private int b;
        private ComplexDataStructure somethingElse;
    }
}