package org.vaslabs.vserializer;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by vnicolaou on 06/05/16.
 */
public class TestEncryptedSerializer {

    private PublicKey remotePublicKey;

    private PrivateKey remotePrivateKey;
    private PrivateKey localPrivateKey;
    private PublicKey localPublicKey;

    private EncryptedAlphabeticalSerializer remoteEncryptedAlphabeticalSerializer;
    private EncryptedAlphabeticalSerializer localEncryptedAlphabeticalSerializer;

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        remotePrivateKey = kp.getPrivate();
        remotePublicKey = kp.getPublic();

        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kpLocal = kpg.genKeyPair();
        localPrivateKey = kpLocal.getPrivate();
        localPublicKey = kpLocal.getPublic();

        remoteEncryptedAlphabeticalSerializer = new EncryptedAlphabeticalSerializer(remotePrivateKey, localPublicKey);
        localEncryptedAlphabeticalSerializer = new EncryptedAlphabeticalSerializer(localPrivateKey, remotePublicKey);
    }

    @Test
    public void test_encryption_decryption_on_serialization() {
        TestUtils.ComplexDataStructure cds = new TestUtils.ComplexDataStructure();
        cds.a = 0xff;
        cds.b = -1;
        cds.somethingElse = new TestUtils.ComplexDataStructure();
        cds.somethingElse.a = -2;
        cds.somethingElse.b = 5;

        int size = SerializationUtils.calculateSize(cds.getClass().getDeclaredFields(), cds);
        assertEquals(26, size);

        byte[] data = localEncryptedAlphabeticalSerializer.serialize(cds);
        assertTrue(26 < data.length);

        cds = remoteEncryptedAlphabeticalSerializer.deserialise(data, TestUtils.ComplexDataStructure.class);

        assertEquals(0xff, cds.a);
        assertEquals(-1, cds.b);
        assertNotNull(cds.somethingElse);
        assertEquals(-2, cds.somethingElse.a);
        assertEquals(5, cds.somethingElse.b);

        TestUtils.EncapsulatedData[] encapsulatedDatas = TestUtils.initEncapsulatedDataArray();

        data = localEncryptedAlphabeticalSerializer.serialize(encapsulatedDatas);

        TestUtils.EncapsulatedData[] recoveredEncapsulatedData = remoteEncryptedAlphabeticalSerializer.deserialise(data, TestUtils.EncapsulatedData[].class);

        assertEquals(10, recoveredEncapsulatedData.length);
        for (int i = 0; i <recoveredEncapsulatedData.length; i++) {
            assertEquals(encapsulatedDatas[i].a, recoveredEncapsulatedData[i].a);
            assertEquals(i, recoveredEncapsulatedData[i].b);
            assertEquals(0xf, recoveredEncapsulatedData[i].c);
            assertEquals(0xff, recoveredEncapsulatedData[i].d);
        }

    }



}
