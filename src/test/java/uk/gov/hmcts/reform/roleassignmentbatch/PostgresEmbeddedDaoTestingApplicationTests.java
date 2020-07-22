package uk.gov.hmcts.reform.roleassignmentbatch;

import java.time.Duration;
import java.util.Random;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbConfig.class})
@ActiveProfiles("DaoTest")
//@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:dao/TestData.sql")
public class PostgresEmbeddedDaoTestingApplicationTests {

    @Autowired
    SpringLiquibase springLiquibase;
    @Test
    //@Transactional
    public void contextLoads() {
        springLiquibase.getChangeLog();
    }
}
