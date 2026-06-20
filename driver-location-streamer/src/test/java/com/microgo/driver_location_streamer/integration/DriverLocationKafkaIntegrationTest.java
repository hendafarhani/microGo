package com.microgo.driver_location_streamer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class DriverLocationKafkaIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${kafka.topics.rider-location}")
    private String riderLocationTopic;

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void consumesDriverLocationFromKafkaAndBroadcastsIt() throws Exception {
        DriverLocationUpdatedEvent payload = DriverLocationUpdatedEvent.builder()
                .driverId("driver-kafka-1")
                .providerIdentifier("driver-kafka-1")
                .status("CRUISING")
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        kafkaTemplate.send(
                        riderLocationTopic,
                        payload.getDriverId(),
                        objectMapper.writeValueAsBytes(payload))
                .get(10, TimeUnit.SECONDS);

        verify(messagingTemplate, timeout(Duration.ofSeconds(10).toMillis()))
                .convertAndSend(DriverLocationStreamingService.DRIVER_LOCATIONS_DESTINATION, payload);
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        ProducerFactory<String, byte[]> testProducerFactory(
                @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
            return new DefaultKafkaProducerFactory<>(Map.of(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
            ));
        }

        @Bean
        KafkaTemplate<String, byte[]> testKafkaTemplate(ProducerFactory<String, byte[]> testProducerFactory) {
            return new KafkaTemplate<>(testProducerFactory);
        }

        @Bean
        @Primary
        SimpMessagingTemplate testMessagingTemplate() {
            return mock(SimpMessagingTemplate.class);
        }
    }
}
