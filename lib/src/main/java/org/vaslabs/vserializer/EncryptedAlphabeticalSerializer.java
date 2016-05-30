package org.vaslabs.vserializer;

/**
 * Created by vnicolaou on 06/05/16.
 */
public abstract class EncryptedAlphabeticalSerializer extends AlphabeticalSerializer {

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

    protected abstract byte[] encrypt(byte[] data) throws Exception;

    public <T> byte[] serialize(T[] objects) {
        byte[] data = super.serialize(objects);
        try {
            data = encrypt(data);
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

    protected abstract byte[] decrypt(byte[] data) throws Exception;
}
