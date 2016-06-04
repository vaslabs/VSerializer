package org.vaslabs.vserializer;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.vaslabs.vserializer.TestUtils.serializeObject;

/**
 * Created by vnicolaou on 05/05/16.
 */
public class TestPerformance {

    VSerializer vSerializer = new AlphabeticalSerializer();

    @Test
    public void measureDifferenceFromVM() {
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new TestUtils.ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;
        byte[] data = vSerializer.serialize(cds);

        byte[] jvmData = serializeObject(cds);

        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
        assertTrue(data.length < jvmData.length);

        TestUtils.DataStructureWithArray dataStructureWithArray = new TestUtils.DataStructureWithArray();
        dataStructureWithArray.numbers = new int[5];
        dataStructureWithArray.numbers[0] = 1;
        dataStructureWithArray.numbers[1] = -1;
        dataStructureWithArray.numbers[2] = 2;
        dataStructureWithArray.numbers[3] = -2;
        dataStructureWithArray.numbers[4] = 3;

        dataStructureWithArray.somethingElse = true;
        dataStructureWithArray.value = 0xff11223344L;
        data = vSerializer.serialize(dataStructureWithArray);
        jvmData = serializeObject(dataStructureWithArray);

        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
        assertTrue(data.length < jvmData.length);

        System.out.println("Compare string serialization");
        String someString = "Hello world";
        data = vSerializer.serialize(someString);
        jvmData = serializeObject(someString);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);


        System.out.println("Compare string serialization all characters");
        char[] chars = new char[65000];
        for (char c = 0; c < chars.length; c++) {
            chars[c] = c;
        }
        someString = new String(chars);

        data = vSerializer.serialize(someString);
        jvmData = serializeObject(someString);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);

        System.out.println("Compare simple object array serialization");
        TestUtils.EncapsulatedData[] encapsulatedDatas = TestUtils.initEncapsulatedDataArray();
        data = vSerializer.serialize(encapsulatedDatas);
        jvmData = serializeObject(encapsulatedDatas);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
    }

    @Test
    public void test_encapsulated_strings_performance() {
        TestUtils.InternalStrings internalStrings = new TestUtils.InternalStrings();
        internalStrings.myMessage = "My Message";
        internalStrings.myOtherMessage = "My Other Message";
        internalStrings.myNumber = -255;
        byte[] data = vSerializer.serialize(internalStrings);
        byte[] jvmData = serializeObject(internalStrings);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
    }

    @Test
    public void test_enum_performance() {
        TestEnumSupport.EnumEncapsulator enumEncapsulator = new TestEnumSupport.EnumEncapsulator();
        enumEncapsulator.timeUnitMinutes = TimeUnit.MINUTES;
        enumEncapsulator.timeUnitSeconds = TimeUnit.SECONDS;
        enumEncapsulator.timeUnitDays = TimeUnit.DAYS;
        byte[] data = vSerializer.serialize(enumEncapsulator);
        byte[] jvmData = serializeObject(enumEncapsulator);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);

    }
}
