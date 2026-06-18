package com.microgo.dashboard_service.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketConfigurationTest {

    @Test
    void exposesExpectedWebSocketContract() {
        assertThat(WebSocketConfiguration.STOMP_ENDPOINT).isEqualTo("/ws");
        assertThat(WebSocketConfiguration.TOPIC_PREFIX).isEqualTo("/topic");
        assertThat(WebSocketConfiguration.APPLICATION_PREFIX).isEqualTo("/app");
    }
}
