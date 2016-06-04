package org.vaslabs.vserializer;

import org.junit.Test;

import java.nio.ByteBuffer;
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
    private EnumArrayEncapsulator enumArrayEncapsulator;

    @Test
    public void test_enum_serialization_deserialization() {
        whenInstantiatingClassWithEnums();
        serializeIt();
        shouldHaveSize(3);
        andDeserialize();
        shouldBeEquals();
    }

    @Test
    public void test_serialization_of_enum_arrays() {
        enumArrayEncapsulator = new EnumArrayEncapsulator();
        data = vSerializer.serialize(enumArrayEncapsulator);
        shouldHaveSize(8);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        assertEquals(2, byteBuffer.getInt());
        assertEquals(1, byteBuffer.get());
        assertEquals(TimeUnit.DAYS.ordinal(), byteBuffer.get());
        assertEquals(1, byteBuffer.get());
        assertEquals(TimeUnit.SECONDS.ordinal(), byteBuffer.get());

        EnumArrayEncapsulator recoveredEnumArrayEncapsulator = vSerializer.deserialise(data, EnumArrayEncapsulator.class);
        assertEquals(enumArrayEncapsulator.timeUnits.length, recoveredEnumArrayEncapsulator.timeUnits.length);
        assertEquals(enumArrayEncapsulator.timeUnits[0], recoveredEnumArrayEncapsulator.timeUnits[0]);
        assertEquals(enumArrayEncapsulator.timeUnits[1], recoveredEnumArrayEncapsulator.timeUnits[1]);
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
    private static class EnumArrayEncapsulator {
        private TimeUnit[] timeUnits = new TimeUnit[] {TimeUnit.DAYS, TimeUnit.SECONDS};
    }
}
