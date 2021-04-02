package com.delgado.bruno.boilerplates.camel.configurations;

import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.info.EnvironmentInfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DependencyInjection {

    @Bean
    public KafkaBrokers kafkaBrokers(Environment environment) {
        final String[] brokers = new String[] { environment.getProperty("kafka.url") };
        return new KafkaBrokers(brokers);
    }

    @Bean
    public DataSource dataSource(Environment environment) {
        String driver = environment.getProperty("database.driver");
        String username = environment.getProperty("database.username");
        String password = environment.getProperty("database.password");
        String dataBaseConnectionString = environment.getProperty("database.url");

        Logger logger = LoggerFactory.getLogger(DependencyInjection.class);
        logger.info("Database Connection String: " + dataBaseConnectionString);

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(dataBaseConnectionString);

        return dataSource;
    }
}
