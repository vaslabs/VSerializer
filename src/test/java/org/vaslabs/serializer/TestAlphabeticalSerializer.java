package org.vaslabs.serializer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by vnicolaou on 02/05/16.
 */
public class TestAlphabeticalSerializer {

    @Test
    public void test_encapsulated_primitives_serialized_deserialized() {
        VSerializer serializer = new AphabeticalSerializer();
        EncapsulatedData encapsulatedData = new EncapsulatedData();
        encapsulatedData.a = 24L;
        encapsulatedData.c = 1;
        encapsulatedData.b = 2.4f;
        encapsulatedData.d = 1024;
        byte[] bytes = serializer.serialize(encapsulatedData);

        assertEquals(17, bytes.length);
    }


    private class EncapsulatedData {
        private long a;
        private byte c;
        private int d;
        private float b;
    }

}
