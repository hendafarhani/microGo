package com.microgo.driver_location_streamer.kafka.configuration;

import com.microgo.driver_location_streamer.kafka.serialization.DriverLocationUpdatedEventJsonDeserializer;
import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaListenerConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DriverLocationUpdatedEvent> riderLocationListenerFactory(
            ConsumerFactory<String, DriverLocationUpdatedEvent> riderConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, DriverLocationUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(riderConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DriverLocationUpdatedEvent> riderConsumerFactory(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ErrorHandlingDeserializer<DriverLocationUpdatedEvent> valueDeserializer =
                new ErrorHandlingDeserializer<>(new DriverLocationUpdatedEventJsonDeserializer());
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }
}
