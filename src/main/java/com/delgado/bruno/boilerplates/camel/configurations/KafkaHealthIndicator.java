package com.delgado.bruno.boilerplates.camel.configurations;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@DependsOn("kafkaBrokers")
public class KafkaHealthIndicator implements HealthIndicator {

    private static final String HEALTH_CHECK_TOPIC = "health_check";
    public static final int CHECK_TIMEOUT = 2;

    private final KafkaAdmin admin;
    private final List<String> brokers;
    private final ApplicationEventPublisher eventPublisher;

    private Map<String, Object> kafkaConfig;

    @Autowired
    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin, KafkaBrokers kafkaBrokers, ApplicationEventPublisher eventPublisher) {
        this.admin = kafkaAdmin;
        this.brokers = kafkaBrokers.getBrokers();
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void setUpAdminClient() {
        kafkaConfig = new HashMap<>();
        kafkaConfig.putAll(admin.getConfigurationProperties());
        kafkaConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
    }

    @Override
    public Health health() {
        AdminClient adminClient = null;

        try {
            adminClient = AdminClient.create(kafkaConfig);
        } catch (Exception e) {
            return Health.down()
                    .withDetail("Cause", e.getCause()).build();
        }

        try {
            DescribeClusterOptions describeClusterOptions = new DescribeClusterOptions().timeoutMs(2000);
            adminClient.describeCluster(describeClusterOptions);

            adminClient
                    .describeConsumerGroups(Arrays.asList("topic"))
                    .all()
                    .get(CHECK_TIMEOUT, TimeUnit.SECONDS);

            Map<String, TopicDescription> topicDescriptionMap = adminClient
                    .describeTopics(Arrays.asList(HEALTH_CHECK_TOPIC))
                    .all()
                    .get(CHECK_TIMEOUT, TimeUnit.SECONDS);

            List<TopicPartitionInfo> partitions = topicDescriptionMap.get(HEALTH_CHECK_TOPIC)
                    .partitions();

            if (partitions == null || partitions.isEmpty()) {
                return Health.down()
                        .withDetail("Reason", "No partition found for topic: " + HEALTH_CHECK_TOPIC)
                        .build();
            }

            if (partitions.stream().anyMatch(p -> p.leader() == null)) {
                return Health.down().withDetail("Reason",
                        "No partition leader found for topic: " + HEALTH_CHECK_TOPIC).build();
            }

        } catch (Exception e) {
            AvailabilityChangeEvent.publish(this.eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);

            return Health.down()
                    .withDetail("Reason", "Kafka may be offline").build();
        } finally {
            Duration duration = Duration.ofMillis(10);
            adminClient.close(duration);
        }

        return Health.up().build();
    }
}