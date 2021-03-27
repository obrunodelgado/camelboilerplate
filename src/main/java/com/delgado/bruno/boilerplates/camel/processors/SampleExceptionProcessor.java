package com.delgado.bruno.boilerplates.camel.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleExceptionProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleExceptionProcessor.class);

    private final String INSERT_QUERY = "INSERT INTO public.errors_issued (system_time, route, message, cause, event) VALUES(now(), '%s', '%s', '%s', '%s')";

    @Override
    public void process(Exchange exchange) throws Exception {
        String route = (String) exchange.getProperty(Exchange.FAILURE_ROUTE_ID);
        String event = exchange.getIn().getBody(String.class);

        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String cause = exception.getCause().getLocalizedMessage();
        String message = exception.getLocalizedMessage();

        LOGGER.error("An error ocurred while processing route " + route + "."
                + " | Event: " + event
                + " | Exception: " + exception.getClass().getSimpleName()
                + " | Message: "+ exception.getMessage()
                + " | Cause: " + exception.getCause());

        String insertQuery = String.format(INSERT_QUERY, route, message, cause, event);
        exchange.getIn().setBody(insertQuery);
    }
}
