package com.delgado.bruno.boilerplates.camel.processors;

import com.delgado.bruno.boilerplates.camel.models.SampleEvent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class SampleProcessor implements Processor {

    private final String INSERT_QUERY = "INSERT INTO sample_table (name, quantity, price) VALUES ('%s', %s, %s)";

    @Override
    public void process(Exchange exchange) {
        var event = (SampleEvent) exchange.getIn().getBody();

        var insertQuery = String.format(INSERT_QUERY, event.getName(), event.getQuantity(), event.getPrice());

        exchange.getIn().setBody(insertQuery);
    }
}