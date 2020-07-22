package uk.gov.hmcts.reform.roleassignmentbatch;

import static java.lang.String.format;

import java.sql.SQLException;
import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.ClassRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @author romeh
 */
@Configuration
@EnableTransactionManagement
@Profile("DaoTest")
public class DbConfig {

    @ClassRule
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer()
        .withUsername("postgres").withPassword("postgres")
        .withDatabaseName("testdb");

    @Bean
    public DataSource dataSource() {
        postgreSQLContainer.start();
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(format("jdbc:postgresql://%s:%s/%s", postgreSQLContainer.getContainerIpAddress(),
                         postgreSQLContainer.getMappedPort(
                             PostgreSQLContainer.POSTGRESQL_PORT), postgreSQLContainer.getDatabaseName()));
        ds.setUsername(postgreSQLContainer.getUsername());
        ds.setPassword(postgreSQLContainer.getPassword());
        ds.setSchema(postgreSQLContainer.getDatabaseName());
        return ds;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) throws SQLException {
        tryToCreateSchema(dataSource);
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDropFirst(true);
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("test");
        //liquibase.setIgnoreClasspathPrefix(false);
        liquibase.setChangeLog("classpath:/db.changelog/db.changelog-main.xml");
        return liquibase;
    }

    private void tryToCreateSchema(DataSource dataSource) throws SQLException {
        String CREATE_SCHEMA_QUERY = "CREATE SCHEMA IF NOT EXISTS test";
        dataSource.getConnection().createStatement().execute(CREATE_SCHEMA_QUERY);
    }

}
