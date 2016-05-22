package org.vaslabs.vserializer;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Created by vnicolaou on 22/05/16.
 */
public class TestCircularDataStructures {

    CircularDS circularDS;
    VSerializer vSerializer = new ReferenceSensitiveAlphabeticalSerializer();

    @Before
    public void setUp() {

    }

    @Test
    public void test_that_circlular_ds_is_serialized_with_reference_sensitive_serializer() {
        whenCreatingCircularDS();
        withNumber(5);
        withInternalItself();
        byte[] bytes = thenRunSerializer();
        //4 bytes reference, 4 bytes internal int, 4 bytes to circle itself
        assertEquals(12, bytes.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        assertEquals(System.identityHashCode(circularDS), byteBuffer.getInt());
        assertEquals(5, byteBuffer.getInt());
        assertEquals(System.identityHashCode(circularDS), byteBuffer.getInt());
    }

    private byte[] thenRunSerializer() {
        return vSerializer.serialize(circularDS);
    }

    private void withInternalItself() {
        circularDS.pointsTo = circularDS;
    }


    private void withNumber(int n) {
        circularDS.justANumber = n;
    }

    private void whenCreatingCircularDS() {
        circularDS = new CircularDS();
    }


    public static class CircularDS {
        private CircularDS pointsTo;

        private int justANumber;

    }
}
