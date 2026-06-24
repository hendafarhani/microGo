package com.microgo.simulation_service.service.serviceimpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DriverAvailabilityRegistryImplTest {

    private StringRedisTemplate redisTemplate;
    private SetOperations<String, String> setOperations;
    private DriverAvailabilityRegistryImpl registry;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        registry = new DriverAvailabilityRegistryImpl(redisTemplate);
        ReflectionTestUtils.setField(registry, "availableDriversKey", "available_drivers");
    }

    @Test
    void markAvailableAddsDriverToSet() {
        registry.markAvailable("driver-1");

        verify(setOperations).add("available_drivers", "driver-1");
    }

    @Test
    void markBusyRemovesDriverFromSet() {
        registry.markBusy("driver-1");

        verify(setOperations).remove("available_drivers", "driver-1");
    }

    @Test
    void ignoresBlankDriverIds() {
        registry.markAvailable(" ");
        registry.markBusy(null);

        verifyNoInteractions(setOperations);
    }
}
