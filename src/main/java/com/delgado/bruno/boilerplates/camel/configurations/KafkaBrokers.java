package com.delgado.bruno.boilerplates.camel.configurations;

import java.util.Arrays;
import java.util.List;

public class KafkaBrokers {

    private List<String> brokers;

    public KafkaBrokers(String[] brokers) {
        this.brokers = Arrays.asList(brokers);
    }

    public List<String> getBrokers() {
        return brokers;
    }

    public String getKafkaUri(String topic, String offset) {
        return String.format(
                "kafka:%s?brokers=%s&autoOffsetReset=%s",
                topic,
                String.join(",", this.brokers),
                offset
        );
    }
}
