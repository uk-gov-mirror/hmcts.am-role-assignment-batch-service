package uk.gov.hmcts.reform.roleassignment;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = BaseTest.class)
public class RoleAssignmentBatchJobIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentBatchJobIntegrationTest.class);

    private static final String COUNT_RECORDS = "SELECT count(1) as n FROM role_assignment_request";

    @Autowired
    private DataSource ds;

    private JdbcTemplate template;

    @Before
    public void setUp() {
        template = new JdbcTemplate(ds);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/role_assignment_clean_up.sql",
                    "classpath:sql/insert_role_assignment_request.sql",
                    "classpath:sql/insert_role_assignment_history.sql",
                    "classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRecordCountFromRequestTable() {
        final Integer count = template.queryForObject(COUNT_RECORDS, Integer.class);
        logger.info(" Total number of records fetched from role assignment request table...{}", count);
        assertEquals(
            "role_assignment_request record count ", 5, count.intValue());
    }

}
