package com.microgo.simulation_service;

import com.microgo.simulation_service.config.SimulationServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SimulationServiceProperties.class)
public class SimulationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulationServiceApplication.class, args);
    }
}
