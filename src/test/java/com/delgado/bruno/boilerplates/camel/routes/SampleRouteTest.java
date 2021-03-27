package com.delgado.bruno.boilerplates.camel.routes;

import com.delgado.bruno.boilerplates.camel.configurations.DependencyInjection;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static com.delgado.bruno.boilerplates.camel.routes.SampleRoute.TOPIC_ID;

@UseAdviceWith
@CamelSpringBootTest
@SpringBootTest(classes = { SampleRoute.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { DependencyInjection.class, CamelAutoConfiguration.class})
public class SampleRouteTest {

    @Autowired
    protected CamelContext camelContext;

    @Produce("direct:" + TOPIC_ID)
    private ProducerTemplate sampleTopic;

    @EndpointInject("mock:jdbc:dataSource")
    private MockEndpoint jdbc;

    @BeforeEach
    public void init() throws Exception {
        AdviceWith.adviceWith(camelContext, "sample", route -> {
            route.replaceFromWith("direct:" + TOPIC_ID);
            route.mockEndpointsAndSkip("jdbc:dataSource");
        });

        camelContext.start();
    }

    @Test
    public void test_sample_route() throws Exception {
        String event = "{\"name\":\"Bruno\",\"quantity\":12,\"price\":12.5}";
        String expected = "INSERT INTO sample_table (name, quantity, price) VALUES ('Bruno', 12, 12.5)";

        sampleTopic.sendBody(event);

        jdbc.expectedBodiesReceived(expected);
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    public void test_exception_route() throws Exception {
        String event = "malformed_json";
        String expectedQuery = "INSERT INTO public.errors_issued (system_time, route, message, cause, event) VALUES(now(), 'sample', 'java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $', 'Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $', 'malformed_json')";

        sampleTopic.sendBody(event);

        jdbc.expectedBodiesReceived(expectedQuery);
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}