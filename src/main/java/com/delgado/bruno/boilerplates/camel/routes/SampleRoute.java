package com.delgado.bruno.boilerplates.camel.routes;

import com.delgado.bruno.boilerplates.camel.configurations.KafkaBrokers;
import com.delgado.bruno.boilerplates.camel.models.SampleEvent;
import com.delgado.bruno.boilerplates.camel.processors.SampleExceptionProcessor;
import com.delgado.bruno.boilerplates.camel.processors.SampleProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.gson.GsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SampleRoute extends RouteBuilder {

    public static final String OFFSET = "earliest";
    public static final String TOPIC_ID = "sample_topic";
    public static final String GROUP_ID = "sample_consumer";

    private final KafkaBrokers kafkaBrokers;

    @Autowired
    public SampleRoute(KafkaBrokers kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    @Override
    public void configure() {
        final String kafkaUri = this.kafkaBrokers.getKafkaUri(TOPIC_ID, OFFSET);
        final GsonDataFormat dataFormat = new GsonDataFormat(SampleEvent.class);

        onException(Exception.class)
                .handled(true)
                .process(new SampleExceptionProcessor())
                .to("jdbc:dataSource");

        from(kafkaUri + "&groupId=" + GROUP_ID)
                .routeId("sample")
                .unmarshal(dataFormat)
                .process(new SampleProcessor())
                .to("jdbc:dataSource");
    }
}
