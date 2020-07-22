package uk.gov.hmcts.reform.roleassignmentbatch;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {RoleAssignmentBatchApplicationTests.Initializer.class})
public class RoleAssignmentBatchApplicationTests {

    @ClassRule
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer()
        .withUsername("postgres").withPassword("postgres")
        .withDatabaseName("testdb");

    @ClassRule
    public static FixedHostPortGenericContainer postgreSQLContainer1 =
        new FixedHostPortGenericContainer<>("postgres:latest")
            .withEnv("POSTGRES_USER","testUser")
            .withEnv("POSTGRES_PASSWORD","testPassword")
            .withEnv("POSTGRES_DB","testDb")
            .withFixedExposedPort(60015, 60015);

    static class Initializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                "spring.datasource.password=" + postgreSQLContainer.getPassword()
                                 ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}