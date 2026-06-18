package com.microgo.dashboard_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.service")
public record DashboardServiceProperties(
        String eventTopic,
        String ackTopic,
        String consumerGroupId,
        String listenerId,
        Integer eventTopicPartitions,
        Integer ackTopicPartitions,
        Short replicationFactor
) {

    public DashboardServiceProperties {
        eventTopicPartitions = eventTopicPartitions == null ? 3 : eventTopicPartitions;
        ackTopicPartitions = ackTopicPartitions == null ? 3 : ackTopicPartitions;
        replicationFactor = replicationFactor == null ? 1 : replicationFactor;
    }
}
