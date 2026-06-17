package com.microgo.driver_location_streamer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class DriverLocationStreamerApplicationTests {

    @Test
    void contextLoads() {
    }
}
