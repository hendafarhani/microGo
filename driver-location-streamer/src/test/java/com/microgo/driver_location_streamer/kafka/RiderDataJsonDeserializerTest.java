package com.microgo.driver_location_streamer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.driver_location_streamer.kafka.serialization.RiderDataJsonDeserializer;
import com.microgo.driver_location_streamer.model.Location;
import com.microgo.driver_location_streamer.model.RiderData;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderDataJsonDeserializerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnNullWhenPayloadIsNull() {
        RiderData result;
        try (RiderDataJsonDeserializer deserializer = RiderDataJsonDeserializer.builder()
                .objectMapper(objectMapper)
                .build()) {
            result = deserializer.deserialize("rider.location", null);
        }

        assertNull(result);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldDeserializePayloadIntoRiderData() throws Exception {
        byte[] payload = "{\"identifier\":\"rider-1\"}".getBytes();
        RiderData expected = RiderData.builder()
                .identifier("rider-1")
                .userName("Ada")
                .location(Location.builder().latitude(48.8584).longitude(2.2945).radius(12).build())
                .build();

        RiderData result;
        try (RiderDataJsonDeserializer deserializer = RiderDataJsonDeserializer.builder()
                .objectMapper(objectMapper)
                .build()) {
            when(objectMapper.readValue(payload, RiderData.class)).thenReturn(expected);

            result = deserializer.deserialize("rider.location", payload);
        }

        assertSame(expected, result);
        verify(objectMapper).readValue(payload, RiderData.class);
    }

    @Test
    void shouldWrapIOExceptionInSerializationException() throws Exception {
        byte[] payload = "invalid".getBytes();

        try (RiderDataJsonDeserializer deserializer = RiderDataJsonDeserializer.builder()
                .objectMapper(objectMapper)
                .build()) {
            when(objectMapper.readValue(payload, RiderData.class)).thenThrow(new IOException("boom"));

            assertThrows(SerializationException.class, () -> deserializer.deserialize("rider.location", payload));
        }
    }
}
