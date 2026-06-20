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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false"
        }
)
@Testcontainers(disabledWithoutDocker = true)
class DriverLocationWebSocketContainerTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @LocalServerPort
    private int port;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.rider-location}")
    private String riderLocationTopic;

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void streamsKafkaLocationEventToSubscribedWebSocketClient() throws Exception {
        BlockingQueue<DriverLocationUpdatedEvent> receivedMessages = new LinkedBlockingQueue<>();
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(List.of(
                new WebSocketTransport(new StandardWebSocketClient())
        )));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient
                .connectAsync("http://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        session.subscribe(DriverLocationStreamingService.DRIVER_LOCATIONS_DESTINATION, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return DriverLocationUpdatedEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((DriverLocationUpdatedEvent) payload);
            }
        });

        DriverLocationUpdatedEvent payload = DriverLocationUpdatedEvent.builder()
                .driverId("driver-ws-1")
                .providerIdentifier("driver-ws-1")
                .status("CRUISING")
                .latitude(51.5090)
                .longitude(-0.1180)
                .build();

        kafkaTemplate.send(
                        riderLocationTopic,
                        payload.getDriverId(),
                        objectMapper.writeValueAsBytes(payload))
                .get(10, TimeUnit.SECONDS);

        DriverLocationUpdatedEvent received = receivedMessages.poll(10, TimeUnit.SECONDS);

        assertThat(received).isEqualTo(payload);
        session.disconnect();
        stompClient.stop();
    }

    @TestConfiguration
    static class TestKafkaProducerConfiguration {

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
    }
}
