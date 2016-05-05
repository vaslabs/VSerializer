package org.vaslabs.vserializer;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.vaslabs.vserializer.TestUtils.initWithData;
import static org.vaslabs.vserializer.TestUtils.serializeObject;

/**
 * Created by vnicolaou on 05/05/16.
 */
public class TestPerformance {

    @Test
    public void measureDifferenceFromVM() {
        VSerializer serializer = new AlphabeticalSerializer();
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new TestUtils.ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;
        byte[] data = serializer.serialize(cds);

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
        data = serializer.serialize(dataStructureWithArray);
        jvmData = serializeObject(dataStructureWithArray);

        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
        assertTrue(data.length < jvmData.length);

        System.out.println("Compare string serialization");
        String someString = "Hello world";
        VSerializer vSerializer = new AlphabeticalSerializer();
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

        vSerializer = new AlphabeticalSerializer();
        data = vSerializer.serialize(someString);
        jvmData = serializeObject(someString);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);

        System.out.println("Compare simple object array serialization");
        TestUtils.EncapsulatedData[] encapsulatedDatas = new TestUtils.EncapsulatedData[10];
        for (int i = 0; i < encapsulatedDatas.length; i++) {
            encapsulatedDatas[i] = new TestUtils.EncapsulatedData();
            initWithData(encapsulatedDatas[i]);
            encapsulatedDatas[i].b = i;
        }

        data = vSerializer.serialize(encapsulatedDatas);
        jvmData = serializeObject(encapsulatedDatas);
        System.out.println("VSerializer: " + data.length);
        System.out.println("JVM Serializer: " + jvmData.length);
    }
}
