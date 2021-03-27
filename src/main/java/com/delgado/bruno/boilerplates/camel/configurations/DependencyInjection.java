package com.delgado.bruno.boilerplates.camel.configurations;

import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DependencyInjection {

    // USE IF YOU ARE RUNNING THIS PROJECT IN A CONTAINER
    private final String KAFKA_HOST = "camel_boilerplate_kafka";
    private final String POSTGRESQL_HOST = "camel_boilerplate_postgres";

    // USE IF YOU ARE RUNNING THIS PROJECT USING COMMAND LINE
//    private final String POSTGRESQL_HOST = "localhost";
//    private final String KAFKA_HOST = "localhost";

    @Bean
    public KafkaBrokers kafkaBrokers() {
        final String[] brokers = new String[] { KAFKA_HOST + ":9092" };
        return new KafkaBrokers(brokers);
    }

    @Bean
    public DataSource dataSource() {
        String driver = "org.postgresql.Driver";
        String username = "postgres";
        String password = "postgres";
        String dataBaseConnectionString = "jdbc:postgresql://" + POSTGRESQL_HOST + ":5432/camel_boilerplate";

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(dataBaseConnectionString);

        return dataSource;
    }
}
