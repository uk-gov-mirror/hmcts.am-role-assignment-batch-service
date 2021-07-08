package uk.gov.hmcts.reform.roleassignmentbatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.sql.DataSource;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Assert;
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
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = BaseTest.class)
public class RoleAssignmentBatchJobIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentBatchJobIntegrationTest.class);

    private DeleteExpiredRecords sut;
    private static final String COUNT_RECORDS_FROM_LIVE_TABLE = "SELECT count(*) as n FROM role_assignment";
    private static final String COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE =
        "SELECT count(*) as n FROM role_assignment_history where STATUS='EXPIRED'";

    @Autowired
    private DataSource ds;

    private JdbcTemplate template;

    @Before
    public void setUp() {
        template = new JdbcTemplate(ds);
        sut = new DeleteExpiredRecords(template, 2);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/role_assignment_clean_up.sql",
                    "classpath:sql/insert_role_assignment_request.sql",
                    "classpath:sql/insert_role_assignment_history.sql",
                    "classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRecordCountFromLiveTable() {
        final Integer count = template.queryForObject(COUNT_RECORDS_FROM_LIVE_TABLE, Integer.class);
        logger.info(" Total number of records fetched from role assignment Live table...{}", count);
        assertNotNull(count);
        assertEquals(
            "role_assignment record count ", 5, count.intValue());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/role_assignment_clean_up.sql",
                    "classpath:sql/insert_role_assignment_request.sql",
                    "classpath:sql/insert_role_assignment_history.sql",
                    "classpath:sql/insert_role_assignment.sql"})
    public void shouldDeleteRecordsFromLiveTable() {
        Integer count = template.queryForObject(COUNT_RECORDS_FROM_LIVE_TABLE, Integer.class);
        logger.info(" Total number of records fetched from role assignment Live table...{}", count);
        logger.info(" Deleting the records from Live table.");
        sut.execute(null, null);
        count = template.queryForObject(COUNT_RECORDS_FROM_LIVE_TABLE, Integer.class);
        logger.info(" Total number of records fetched from role assignment Live table...{}", count);
        Assert.assertEquals("The live records were not deleted", Integer.valueOf(0), count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/role_assignment_clean_up.sql",
                    "classpath:sql/insert_role_assignment_request.sql",
                    "classpath:sql/insert_role_assignment_history.sql",
                    "classpath:sql/insert_role_assignment.sql"})
    public void shouldInsertRecordsInHistoryTable() {
        Integer count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of expired records fetched from History table...{}", count);
        Assert.assertEquals("The live records were not deleted", Integer.valueOf(0), count);
        logger.info(" Deleting the records from Live table. Insert the records in History table.");
        sut.execute(null, null);
        count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of Expired records fetched from History table...{}", count);
        Assert.assertEquals("The EXPIRED records were not inserted", Integer.valueOf(5), count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/role_assignment_clean_up.sql",
                    "classpath:sqlcomplexscenarios/insert_role_assignment_request.sql",
                    "classpath:sqlcomplexscenarios/insert_role_assignment_history.sql",
                    "classpath:sqlcomplexscenarios/insert_role_assignment.sql"})
    public void shouldDeleteLiveRecordsComplexScenario() {
        Integer count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of expired records fetched from History table...{}", count);
        Assert.assertEquals("The live records were not deleted", Integer.valueOf(0), count);
        logger.info(" Deleting the records from Live table. Insert the records in History table.");
        sut.execute(null, null);
        count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of Expired records fetched from History table...{}", count);
        Assert.assertEquals("The EXPIRED records were not inserted", Integer.valueOf(3), count);
    }

}
