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
        CircularDS recoveredCircularDS = vSerializer.deserialise(bytes, CircularDS.class);
        assertEquals(circularDS.justANumber, recoveredCircularDS.justANumber);
        assertEquals(circularDS.pointsTo.justANumber, recoveredCircularDS.pointsTo.justANumber);
    }

    @Test
    public void test_do_simple_regression() {
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new TestUtils.ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;
        SizeComputer sizeComputer = new SizeComputer(cds);
        int computedSize = sizeComputer.calculateSize(SerializationUtils.getAllFields(cds), cds);
        assertEquals(4 + (8+4)*2+ 4 + 4, computedSize);
        byte[] data = vSerializer.serialize(cds);
        assertEquals(4 + (8+4)*2+ 4 + 4, data.length );
        TestUtils.ComplexDataStructure recoveredCds = vSerializer.deserialise(data, TestUtils.ComplexDataStructure.class);
        assertEquals(cds.a, recoveredCds.a);
        assertEquals(cds.b, recoveredCds.b);
        assertEquals(cds.somethingElse.a, recoveredCds.somethingElse.a);
        assertEquals(cds.somethingElse.b, recoveredCds.somethingElse.b);
        assertEquals(cds.somethingElse.somethingElse, recoveredCds.somethingElse.somethingElse);
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
