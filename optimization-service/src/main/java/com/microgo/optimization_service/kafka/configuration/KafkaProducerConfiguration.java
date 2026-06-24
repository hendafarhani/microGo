package com.microgo.optimization_service.kafka.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    @Bean
    public ProducerFactory<String, Object> optimizationProducerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                jsonSerializer(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, Object> optimizationKafkaTemplate(
            ProducerFactory<String, Object> optimizationProducerFactory) {
        return new KafkaTemplate<>(optimizationProducerFactory);
    }

    private Serializer<Object> jsonSerializer(ObjectMapper objectMapper) {
        return (topic, data) -> {
            if (data == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException exception) {
                throw new SerializationException("Failed to serialize Kafka payload", exception);
            }
        };
    }
}
