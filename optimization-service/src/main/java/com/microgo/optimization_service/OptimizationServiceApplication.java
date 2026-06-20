package com.microgo.optimization_service;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OptimizationServiceProperties.class)
public class OptimizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OptimizationServiceApplication.class, args);
    }
}
