package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.service.DriverAvailabilityRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAvailabilityRegistryImpl implements DriverAvailabilityRegistry {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${simulation-service.redis.available-drivers-key:available_drivers}")
    private String availableDriversKey;

    @Override
    public void markAvailable(String driverId) {
        if (!StringUtils.hasText(driverId)) {
            return;
        }
        stringRedisTemplate.opsForSet().add(availableDriversKey, driverId);
        log.debug("Driver {} marked available for dispatch", driverId);
    }

    @Override
    public void markBusy(String driverId) {
        if (!StringUtils.hasText(driverId)) {
            return;
        }
        stringRedisTemplate.opsForSet().remove(availableDriversKey, driverId);
        log.debug("Driver {} marked busy (removed from dispatch availability)", driverId);
    }
}
