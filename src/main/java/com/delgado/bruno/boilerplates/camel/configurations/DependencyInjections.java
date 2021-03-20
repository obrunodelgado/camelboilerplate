package com.delgado.bruno.boilerplates.camel.configurations;

import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DependencyInjections {

    @Bean
    public KafkaBrokers kafkaBrokers() {
        final String[] brokers = new String[] { "camel_boilerplate_kafka:9092" };
//        final String[] brokers = new String[] { "localhost:9092" };
        return new KafkaBrokers(brokers);
    }

    @Bean
    public DataSource dataSource() {
        String driver = "org.postgresql.Driver";
        String username = "postgres";
        String password = "postgres";
        String dataBaseConnectionString = "jdbc:postgresql://camel_boilerplate_postgres:5432/camelboilerplate";
//        String dataBaseConnectionString = "jdbc:postgresql://localhost:5432/camelboilerplate";

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(dataBaseConnectionString);

        return dataSource;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        FluentConfiguration configuration = Flyway.configure();
        configuration.schemas("public");
        configuration.dataSource(dataSource);
        return configuration.load();
    }
}
