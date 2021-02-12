package uk.gov.hmcts.reform.roleassignment;

import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BaseTest {

    protected static final ObjectMapper mapper = new ObjectMapper();
    public static final String POSTGRES = "postgres";

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    @Primary
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres
            .builder()
            .setConnectConfig("stringtype", "unspecified")
            .setPort(0)
            .start();
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("embeddedPostgres") final EmbeddedPostgres pg) {

        return DataSourceBuilder
            .create()
            .username(POSTGRES)
            .password(POSTGRES)
            .url(pg.getJdbcUrl(POSTGRES, POSTGRES))
            .driverClassName("org.postgresql.Driver")
            .build();
    }

    @PreDestroy
    public void contextDestroyed() throws IOException {
        embeddedPostgres().close();
    }
}
