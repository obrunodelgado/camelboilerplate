package com.delgado.bruno.boilerplates.camel.configurations;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfiguration {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        FluentConfiguration configuration = Flyway.configure();
        configuration.schemas("public");
        configuration.dataSource(dataSource);
        return configuration.load();
    }

}
