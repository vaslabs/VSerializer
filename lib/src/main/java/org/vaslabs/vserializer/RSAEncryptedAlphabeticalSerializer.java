package org.vaslabs.vserializer;

import org.vaslabs.vserializer.EncryptedAlphabeticalSerializer;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

/**
 * Created by vnicolaou on 30/05/16.
 */
public class RSAEncryptedAlphabeticalSerializer extends EncryptedAlphabeticalSerializer {

    private final PublicKey remotePublicKey;
    private final PrivateKey privateKey;

    public RSAEncryptedAlphabeticalSerializer(PrivateKey localPrivateKey, PublicKey remotePublicKey) {
        this.privateKey = localPrivateKey;
        this.remotePublicKey = remotePublicKey;
    }

    @Override
    protected byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, remotePublicKey);
        byte[] cipherData = cipher.doFinal(data);
        return cipherData;
    }

    @Override
    protected byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] cipherData = cipher.doFinal(data);
        return cipherData;
    }

}
