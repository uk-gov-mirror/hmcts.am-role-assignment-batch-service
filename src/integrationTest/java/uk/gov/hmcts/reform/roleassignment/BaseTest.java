package uk.gov.hmcts.reform.roleassignment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.BeforeClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class BaseTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres
            .builder()
            .setPort(0)
            .start();
    }

    @Bean
    public DataSource dataSource() throws Exception {
        final EmbeddedPostgres pg = embeddedPostgres();

        final Properties props = new Properties();
        props.setProperty("stringtype", "unspecified");
        final Connection connection = DriverManager.getConnection(pg.getJdbcUrl("postgres", "postgres"), props);

        DataSource datasource = new SingleConnectionDataSource(connection, true);
        Database database =
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase =
            new Liquibase("db/changelog/db.changelog-main.xml", new ClassLoaderResourceAccessor(), database);

        liquibase.update(new Contexts());
        return datasource;
    }

    @PreDestroy
    public void contextDestroyed() throws IOException {
        embeddedPostgres().close();
    }
}
