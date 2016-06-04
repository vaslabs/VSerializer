package org.vaslabs.vserializer;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by vnicolaou on 04/06/16.
 */
public class TestEnumSupport {

    VSerializer vSerializer = new AlphabeticalSerializer();
    byte[] data;
    EnumEncapsulator enumEncapsulator;
    private EnumEncapsulator recoveredEnumEncapsulator;

    @Test
    public void test_enum_serialization_deserialization() {
        whenInstantiatingClassWithEnums();
        serializeIt();
        shouldHaveSize(3);
        andDeserialize();
        shouldBeEquals();
    }

    private void shouldBeEquals() {
        assertEquals(enumEncapsulator.timeUnitDays, recoveredEnumEncapsulator.timeUnitDays);
        assertEquals(enumEncapsulator.timeUnitMinutes, recoveredEnumEncapsulator.timeUnitMinutes);
        assertEquals(enumEncapsulator.timeUnitSeconds, recoveredEnumEncapsulator.timeUnitSeconds);
    }

    private void andDeserialize() {
        recoveredEnumEncapsulator = vSerializer.deserialise(data, EnumEncapsulator.class);
    }

    private void shouldHaveSize(int expectedSize) {
        assertEquals(expectedSize, data.length);
    }

    private void serializeIt() {
        data = vSerializer.serialize(enumEncapsulator);
    }

    private void whenInstantiatingClassWithEnums() {
        enumEncapsulator = new EnumEncapsulator();
    }


    private static class EnumEncapsulator {
        private TimeUnit timeUnitDays = TimeUnit.DAYS;
        private TimeUnit timeUnitSeconds = TimeUnit.SECONDS;
        private TimeUnit timeUnitMinutes = TimeUnit.MINUTES;
    }
}
