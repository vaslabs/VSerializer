package org.vaslabs.vserializer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by vnicolaou on 06/05/16.
 */
public class EncryptedAlphabeticalSerializer extends AlphabeticalSerializer {
    private final PublicKey remotePublicKey;
    private final PrivateKey privateKey;

    public EncryptedAlphabeticalSerializer(PrivateKey localPrivateKey, PublicKey remotePublicKey) {
        this.privateKey = localPrivateKey;
        this.remotePublicKey = remotePublicKey;
    }

    @Override
    public <T> byte[] serialize(T obj) {
        byte[] data = super.serialize(obj);

        try {
            data = encrypt(data);
        } catch (Exception e) {
            return null;
        }

        return data;
    }

    private byte[] encrypt(byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, remotePublicKey);
        byte[] cipherData = cipher.doFinal(data);
        return cipherData;
    }

    public <T> byte[] serialize(T[] objects) {
        byte[] data = super.serialize(objects);
        try {
            encrypt(data);
        } catch (Exception e) {
            return null;
        }
        return data;
    }

    @Override
    public <T> T deserialise(byte[] data, Class<T> clazz) {
        try {
            data = decrypt(data);
        } catch (Exception e) {
            return null;
        }
        return super.deserialise(data, clazz);
    }

    private byte[] decrypt(byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] cipherData = cipher.doFinal(data);
        return cipherData;
    }
}
