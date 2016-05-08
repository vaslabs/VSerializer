package org.vaslabs.vserializer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by vnicolaou on 05/05/16.
 */
public class TestUtils {

    protected static void initWithData(EncapsulatedData myTestObject) {
        myTestObject.a = 0xff121212;
        myTestObject.b = 0x1111;
        myTestObject.c = 0xf;
        myTestObject.d = 0xff;
    }

    protected static byte[] serializeObject(Serializable cds) {
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

    public static EncapsulatedData[] initEncapsulatedDataArray() {
        EncapsulatedData[] encapsulatedDatas = new TestUtils.EncapsulatedData[10];
        for (int i = 0; i < encapsulatedDatas.length; i++) {
            encapsulatedDatas[i] = new TestUtils.EncapsulatedData();
            initWithData(encapsulatedDatas[i]);
            encapsulatedDatas[i].b = i;
        }
        return encapsulatedDatas;
    }

    public static class EncapsulatedData implements Serializable {
        protected long a;// = 0xff121212;
        protected int b;// = 0x1111;
        protected short d;// = 0xff;
        protected byte c;// = 0xf;
    }

    public static class FinalEncapsulatedData {
        protected final long a;// = 0xff121212;
        protected final int b;// = 0x1111;
        protected final short d;// = 0xff;
        protected final byte c;// = 0xf;

        public FinalEncapsulatedData() {
            a = 0;
            b = 0;
            d = 0;
            c = 0;
        }
        public FinalEncapsulatedData(long a, int b, short d, byte c) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }

    public static class ComplexDataStructure implements Serializable {
        protected long a;
        protected int b;
        protected ComplexDataStructure somethingElse;
    }

    public static class DataStructureWithArray implements Serializable{
        protected int[] numbers;
        protected long value;
        protected boolean somethingElse;
    }

    public static class DataStructureWithObjectArray implements Serializable{
        protected EncapsulatedData[] encapsulatedDatas;
        protected long value;
        protected boolean somethingElse;
    }

    public static class InternalStrings implements Serializable{
        protected String myMessage;
        protected int myNumber;
        protected String myOtherMessage;
    }

    public static class AllEncapsulatedData implements Serializable {
        protected long a;// = 0xff121212;
        protected int b;// = 0x1111;
        protected short d;// = 0xff;
        protected byte c;// = 0xf;
        protected boolean e;
        protected char f;
    }

    public static class AllEncapsulatedArrayData implements Serializable {
        protected long[] a;// = 0xff121212;
        protected int[] b;// = 0x1111;
        protected short[] d;// = 0xff;
        protected byte[] c;// = 0xf;
        protected boolean[] e;
        protected char[] f;
    }

    public static class TransientData implements Serializable{
        protected int myNumber;
        protected transient int transientNumber;
    }

    public static class StaticData implements Serializable{
        protected int myNumber;
        protected static int staticNumber;
    }

}
