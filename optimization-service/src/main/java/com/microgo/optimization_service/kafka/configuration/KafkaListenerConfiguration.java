package com.microgo.optimization_service.kafka.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.microgo.optimization_service.kafka.model.DriverLocationUpdatedEvent;
import com.microgo.optimization_service.kafka.model.RideAssignedEvent;
import com.microgo.optimization_service.kafka.model.RideCancelledEvent;
import com.microgo.optimization_service.kafka.model.ScenarioStartedEvent;
import com.microgo.optimization_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.optimization_service.kafka.model.SimulationCompletedEvent;
import com.microgo.optimization_service.kafka.model.SimulationMetricsUpdatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaListenerConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ScenarioStartedEvent> scenarioStartedEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, ScenarioStartedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SimulatedRideRequestedEvent> simulatedRideRequestedEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, SimulatedRideRequestedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SimulationMetricsUpdatedEvent> simulationMetricsUpdatedEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, SimulationMetricsUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SimulationCompletedEvent> simulationCompletedEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, SimulationCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DriverLocationUpdatedEvent> driverLocationUpdatedEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, DriverLocationUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RideAssignedEvent> rideAssignedEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, RideAssignedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RideCancelledEvent> rideCancelledEventListenerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return listenerFactory(bootstrapServers, RideCancelledEvent.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(
            String bootstrapServers,
            Class<T> valueType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(bootstrapServers, valueType, eventObjectMapper()));
        return factory;
    }

    private ObjectMapper eventObjectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private <T> ConsumerFactory<String, T> consumerFactory(
            String bootstrapServers,
            Class<T> valueType,
            ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ErrorHandlingDeserializer<T> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer(valueType, objectMapper));
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }

    private <T> Deserializer<T> jsonDeserializer(Class<T> valueType, ObjectMapper objectMapper) {
        return new Deserializer<>() {
            @Override
            public T deserialize(String topic, byte[] data) {
                if (data == null || data.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(data, valueType);
                } catch (IOException exception) {
                    throw new SerializationException("Failed to deserialize Kafka payload for topic " + topic, exception);
                }
            }
        };
    }
}
