package uk.gov.hmcts.reform.roleassignmentbatch;

import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;

import javax.sql.DataSource;

@SpringBootTest
@ExtendWith(SerenityJUnit5Extension.class)
@ContextConfiguration(classes = BaseTest.class)
public class RoleAssignmentBatchJobIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentBatchJobIntegrationTest.class);

    private DeleteExpiredRecords sut;
    private static final String COUNT_RECORDS_FROM_LIVE_TABLE = "SELECT count(*) as n FROM role_assignment";
    private static final String COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE =
        "SELECT count(*) as n FROM role_assignment_history where STATUS='EXPIRED'";

    @Autowired
    @Qualifier("rasDataSource")
    private DataSource ds;

    private JdbcTemplate template;

    @Mock
    private EmailService emailService;

    @Mock
    StepExecution stepExecution = Mockito.mock(StepExecution.class);

    @Mock
    JobExecution jobExecution = Mockito.mock(JobExecution.class);

    @Mock
    StepContribution stepContribution = new StepContribution(stepExecution);

    @Mock
    StepContext stepContext = new StepContext(stepExecution);

    @Mock
    ChunkContext chunkContext = new ChunkContext(stepContext);

    @BeforeEach
    public void setUp() {
        template = new JdbcTemplate(ds);
        sut = new DeleteExpiredRecords(emailService, template, 2);
        Mockito.when(stepContribution.getStepExecution()).thenReturn(stepExecution);
        Mockito.when(stepContribution.getStepExecution().getJobExecution()).thenReturn(jobExecution);
        Mockito.when(stepContribution.getStepExecution().getJobExecution().getId()).thenReturn(Long.valueOf(1));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(dataSource = "rasDataSource"),
         scripts = {"classpath:sql/ras/role_assignment_clean_up.sql",
                    "classpath:sql/ras/insert_role_assignment_request.sql",
                    "classpath:sql/ras/insert_role_assignment_history.sql",
                    "classpath:sql/ras/insert_role_assignment.sql"})
    public void shouldGetRecordCountFromLiveTable() {
        final Integer count = template.queryForObject(COUNT_RECORDS_FROM_LIVE_TABLE, Integer.class);
        logger.info(" Total number of records fetched from role assignment Live table...{}", count);
        Assertions.assertNotNull(count);
        Assertions.assertEquals(5, count.intValue(), "role_assignment record count ");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,config = @SqlConfig(dataSource = "rasDataSource"),
         scripts = {"classpath:sql/ras/role_assignment_clean_up.sql",
                    "classpath:sql/ras/insert_role_assignment_request.sql",
                    "classpath:sql/ras/insert_role_assignment_history.sql",
                    "classpath:sql/ras/insert_role_assignment.sql"})
    public void shouldDeleteRecordsFromLiveTable() {
        Integer count = template.queryForObject(COUNT_RECORDS_FROM_LIVE_TABLE, Integer.class);
        logger.info(" Total number of records fetched from role assignment Live table...{}", count);
        logger.info(" Deleting the records from Live table.");
        sut.execute(stepContribution, chunkContext);
        count = template.queryForObject(COUNT_RECORDS_FROM_LIVE_TABLE, Integer.class);
        logger.info(" Total number of records fetched from role assignment Live table...{}", count);
        Assertions.assertEquals(Integer.valueOf(0), count, "The live records were not deleted");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(dataSource = "rasDataSource"),
         scripts = {"classpath:sql/ras/role_assignment_clean_up.sql",
                    "classpath:sql/ras/insert_role_assignment_request.sql",
                    "classpath:sql/ras/insert_role_assignment_history.sql",
                    "classpath:sql/ras/insert_role_assignment.sql"})
    public void shouldInsertRecordsInHistoryTable() {
        Integer count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of expired records fetched from History table...{}", count);
        Assertions.assertEquals(Integer.valueOf(0), count, "The live records were not deleted");
        logger.info(" Deleting the records from Live table. Insert the records in History table.");
        sut.execute(stepContribution, chunkContext);
        count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of Expired records fetched from History table...{}", count);
        Assertions.assertEquals(Integer.valueOf(5), count, "The EXPIRED records were not inserted");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(dataSource = "rasDataSource"),
         scripts = {"classpath:sql/ras/role_assignment_clean_up.sql",
                    "classpath:sqlcomplexscenarios/insert_role_assignment_request.sql",
                    "classpath:sqlcomplexscenarios/insert_role_assignment_history.sql",
                    "classpath:sqlcomplexscenarios/insert_role_assignment.sql"})
    public void shouldDeleteLiveRecordsComplexScenario() {
        Integer count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of expired records fetched from History table...{}", count);
        Assertions.assertEquals(Integer.valueOf(0), count, "The live records were not deleted");
        logger.info(" Deleting the records from Live table. Insert the records in History table.");
        sut.execute(stepContribution, chunkContext);
        count = template.queryForObject(COUNT_EXPIRED_RECORDS_FROM_HISTORY_TABLE, Integer.class);
        logger.info(" Total number of Expired records fetched from History table...{}", count);
        Assertions.assertEquals(Integer.valueOf(3), count, "The EXPIRED records were not inserted");
    }

}
