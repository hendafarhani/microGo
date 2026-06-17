package com.microgo.driver_location_streamer.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.driver_location_streamer.model.RiderData;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

@Builder
@RequiredArgsConstructor
public class RiderDataJsonDeserializer implements Deserializer<RiderData> {

    private final ObjectMapper objectMapper;

    public RiderDataJsonDeserializer() {
        this(new ObjectMapper());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no-op
    }

    @Override
    public RiderData deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, RiderData.class);
        } catch (IOException ex) {
            throw new SerializationException("Failed to deserialize RiderData", ex);
        }
    }

    @Override
    public void close() {
        // no resources to close
    }
}
