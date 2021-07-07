package uk.gov.hmcts.reform.roleassignment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public abstract class BaseTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void init() {
        //mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @TestConfiguration
    static class Configuration {
        Connection connection;

        @Bean
        public EmbeddedPostgres embeddedPostgres() throws IOException {
            return EmbeddedPostgres
                    .builder()
                    .setPort(0)
                    .start();
        }

        @Bean
        public DataSource dataSource(@Autowired EmbeddedPostgres pg) throws Exception {

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres", "postgres"), props);
            DataSource datasource = new SingleConnectionDataSource(connection, true);
            Flyway.configure().dataSource(datasource)
                    .locations("db/migration/").load().migrate();
            return datasource;

        }



        @PreDestroy
        public void contextDestroyed() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }
    }
}