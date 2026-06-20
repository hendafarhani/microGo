package com.microgo.driver_location_streamer.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

@Builder
@RequiredArgsConstructor
public class DriverLocationUpdatedEventJsonDeserializer implements Deserializer<DriverLocationUpdatedEvent> {

    private final ObjectMapper objectMapper;

    public DriverLocationUpdatedEventJsonDeserializer() {
        this(new ObjectMapper().findAndRegisterModules());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no-op
    }

    @Override
    public DriverLocationUpdatedEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, DriverLocationUpdatedEvent.class);
        } catch (IOException ex) {
            throw new SerializationException("Failed to deserialize DriverLocationUpdatedEvent", ex);
        }
    }

    @Override
    public void close() {
        // no resources to close
    }
}
