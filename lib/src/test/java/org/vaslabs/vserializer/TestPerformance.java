package org.vaslabs.vserializer;

import org.junit.Test;
import static java.lang.System.out;

/**
 * Created by vnicolaou on 19/08/16.
 */
public class TestPerformance {

    private static final int SAMPLE = 1000;
    VSerializer vSerializer = new ReferenceSensitiveAlphabeticalSerializer();
    VSerializer vDefaultSerializer = new ReferenceSensitiveAlphabeticalSerializer();

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

    @Test
    public void test_performance_jvm() {
        out.println("Measuring serialization performance jvm");
        long started = System.currentTimeMillis();
        for (int i = 0; i < SAMPLE; i++)
            runJVMSerialization(100);
        long ended = System.currentTimeMillis();
        System.out.println(ended-started);
    }

    private void runJVMSerialization(int depth) {
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
        byte[] data = TestUtils.serializeObject(cds);
        TestUtils.ComplexDataStructure recoveredCds = TestUtils.deserializeObject(data);
    }

    @Test
    public void test_string_performance() {
        out.println("Measuring string serialization performance");
        long started = System.currentTimeMillis();
        for (int i = 0; i < SAMPLE; i++)
            runStringSerialization();
        long ended = System.currentTimeMillis();
        System.out.println(ended-started);
    }

    @Test
    public void test_string_jvm_performance() {
        out.println("Measuring jvm string serialization performance");
        long started = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++)
            runStringSerialization();
        long ended = System.currentTimeMillis();
        System.out.println(ended-started);
    }

    private void runStringSerialization() {
        String someString = String.valueOf(Math.random());
        byte[] data = TestUtils.serializeObject(someString);
        TestUtils.deserializeObject(data);
    }


}
