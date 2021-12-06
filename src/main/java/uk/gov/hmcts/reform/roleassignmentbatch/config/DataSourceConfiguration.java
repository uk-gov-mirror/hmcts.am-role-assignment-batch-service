package uk.gov.hmcts.reform.roleassignmentbatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean(name = "rasDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource rasDataSource() {
        return DataSourceBuilder.create().build();

    }

    @Bean(name = "judicialDataSource")
    @ConfigurationProperties(prefix = "spring.judicial.datasource")
    public DataSource judicialDataSource() {
        return DataSourceBuilder.create().build();
    }
}
