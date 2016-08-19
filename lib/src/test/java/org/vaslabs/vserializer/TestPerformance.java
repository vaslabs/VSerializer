package org.vaslabs.vserializer;

import org.junit.Test;
import static java.lang.System.out;

/**
 * Created by vnicolaou on 19/08/16.
 */
public class TestPerformance {

    private static final int SAMPLE = 1000;
    VSerializer vSerializer = new ReferenceSensitiveAlphabeticalSerializer();

    @Test
    public void test_performance() {
        out.println("Measuring serialization performance");
        long started = System.currentTimeMillis();
        for (int i = 0; i < SAMPLE; i++)
            runSerialization(100);
        long ended = System.currentTimeMillis();
        System.out.println(ended-started);
    }

    private void runSerialization(int depth) {
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        TestUtils.ComplexDataStructure addingCds = cds;

        for (int i = 0; i < depth; i++) {
            addingCds.a = 0xff;
            addingCds.b = -1;
            addingCds.somethingElse = new TestUtils.ComplexDataStructure();
            addingCds.somethingElse.a = -2;
            addingCds.somethingElse.b = 5;
            addingCds = addingCds.somethingElse;
        }
        byte[] data = vSerializer.serialize(cds);
        TestUtils.ComplexDataStructure recoveredCds = vSerializer.deserialise(data, TestUtils.ComplexDataStructure.class);

    }

}
