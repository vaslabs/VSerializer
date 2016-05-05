package org.vaslabs.vserializer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by vnicolaou on 05/05/16.
 */
public class TestFringeCases {

    private VSerializer vSerializer;

    @Before
    public void setUp() {
        vSerializer = new AlphabeticalSerializer();
    }

    @Test
    public void test_serialization_of_internal_strings_with_nulls() {
        TestUtils.InternalStrings internalStrings = new TestUtils.InternalStrings();
        internalStrings.myMessage = "My message";
        internalStrings.myOtherMessage = null;
        internalStrings.myNumber = 0;
        byte[] data = vSerializer.serialize(internalStrings);
        TestUtils.InternalStrings recoveredInternalStrings = vSerializer.deserialise(data, TestUtils.InternalStrings.class);
        assertEquals(internalStrings.myMessage, recoveredInternalStrings.myMessage);
        assertEquals(internalStrings.myOtherMessage, recoveredInternalStrings.myOtherMessage);
        assertEquals(internalStrings.myNumber, recoveredInternalStrings.myNumber);

        internalStrings.myMessage = "";
        internalStrings.myOtherMessage = null;
        internalStrings.myNumber = -1;

        data = vSerializer.serialize(internalStrings);
        recoveredInternalStrings = vSerializer.deserialise(data, TestUtils.InternalStrings.class);
        assertEquals(internalStrings.myMessage, recoveredInternalStrings.myMessage);
        assertEquals(internalStrings.myOtherMessage, recoveredInternalStrings.myOtherMessage);
        assertEquals(internalStrings.myNumber, recoveredInternalStrings.myNumber);

    }

}
