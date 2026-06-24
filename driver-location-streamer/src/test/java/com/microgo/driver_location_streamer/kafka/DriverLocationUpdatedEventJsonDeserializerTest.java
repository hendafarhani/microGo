package com.microgo.driver_location_streamer.kafka;

import com.microgo.driver_location_streamer.kafka.serialization.DriverLocationUpdatedEventJsonDeserializer;
import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DriverLocationUpdatedEventJsonDeserializerTest {

    @Test
    void shouldReturnNullWhenPayloadIsNull() {
        DriverLocationUpdatedEvent result;
        try (DriverLocationUpdatedEventJsonDeserializer deserializer = new DriverLocationUpdatedEventJsonDeserializer()) {
            result = deserializer.deserialize("driver.location.updated", null);
        }

        assertNull(result);
    }

    @Test
    void shouldDeserializePayloadIntoDriverLocationUpdatedEvent() throws Exception {
        DriverLocationUpdatedEvent result;
        try (DriverLocationUpdatedEventJsonDeserializer deserializer = new DriverLocationUpdatedEventJsonDeserializer()) {
            result = deserializer.deserialize("driver.location.updated",
                    "{\"driverId\":\"driver-1\",\"providerIdentifier\":\"driver-1\",\"status\":\"CRUISING\",\"latitude\":51.5074,\"longitude\":-0.1278}".getBytes());
        }

        assertThat(result.getDriverId()).isEqualTo("driver-1");
        assertThat(result.getProviderIdentifier()).isEqualTo("driver-1");
        assertThat(result.getStatus()).isEqualTo("CRUISING");
    }

    @Test
    void shouldWrapInvalidPayloadInSerializationException() {
        try (DriverLocationUpdatedEventJsonDeserializer deserializer = new DriverLocationUpdatedEventJsonDeserializer()) {
            assertThrows(SerializationException.class,
                    () -> deserializer.deserialize("driver.location.updated", "invalid".getBytes()));
        }
    }
}
