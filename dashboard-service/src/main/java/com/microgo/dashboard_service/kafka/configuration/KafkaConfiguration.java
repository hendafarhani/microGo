package com.microgo.dashboard_service.kafka.configuration;

import com.microgo.dashboard_service.configuration.DashboardServiceProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfiguration {

    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaAdmin(bootstrapServerConfig(bootstrapServers));
    }

    @Bean
    public NewTopic dashboardEventsTopic(DashboardServiceProperties properties) {
        return new NewTopic(properties.eventTopic(), properties.eventTopicPartitions(), properties.replicationFactor());
    }

    @Bean
    public NewTopic dashboardAckTopic(DashboardServiceProperties properties) {
        return new NewTopic(properties.ackTopic(), properties.ackTopicPartitions(), properties.replicationFactor());
    }

    @Bean
    public ProducerFactory<String, String> dashboardProducerFactory(@Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return new DefaultKafkaProducerFactory<>(producerConfig(bootstrapServers));
    }

    @Bean
    public KafkaTemplate<String, String> dashboardKafkaTemplate(ProducerFactory<String, String> dashboardProducerFactory) {
        return new KafkaTemplate<>(dashboardProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> dashboardEventConsumerFactory(@Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return new DefaultKafkaConsumerFactory<>(consumerConfig(bootstrapServers));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> dashboardEventListenerContainerFactory(
            ConsumerFactory<String, String> dashboardEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dashboardEventConsumerFactory);
        return factory;
    }

    private Map<String, Object> producerConfig(String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.putAll(bootstrapServerConfig(bootstrapServers));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return config;
    }

    private Map<String, Object> consumerConfig(String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return config;
    }

    private Map<String, Object> bootstrapServerConfig(String bootstrapServers) {
        return Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    }
}
