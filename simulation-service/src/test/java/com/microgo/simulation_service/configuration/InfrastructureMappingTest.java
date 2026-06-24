package com.microgo.simulation_service.configuration;

import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.kafka.configuration.KafkaListenerConfiguration;
import com.microgo.simulation_service.kafka.model.DriverLocationUpdatedEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.annotation.EnableKafka;

import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureMappingTest {

    @Test
    void kafkaListenersAreEnabled() {
        assertThat(KafkaListenerConfiguration.class).hasAnnotation(EnableKafka.class);
    }

    @Test
    void simulationResultJsonFieldsUseHibernateJsonType() throws NoSuchFieldException {
        assertJsonField("predictedDemandByZone");
        assertJsonField("metricsSnapshot");
    }

    @Test
    void locationEventsAllowAdditiveCompatibilityFields() throws Exception {
        DriverLocationUpdatedEvent event = eventObjectMapper().readValue(
                """
                {
                  "driverId": "driver-1",
                  "driverIdentifier": "driver-1",
                  "latitude": 51.5,
                  "longitude": -0.12,
                  "tickSequence": 1
                }
                """,
                DriverLocationUpdatedEvent.class);

        assertThat(event.getDriverId()).isEqualTo("driver-1");
    }

    private ObjectMapper eventObjectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void assertJsonField(String fieldName) throws NoSuchFieldException {
        JdbcTypeCode annotation = SimulationResultEntity.class
                .getDeclaredField(fieldName)
                .getAnnotation(JdbcTypeCode.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(SqlTypes.JSON);
    }
}
