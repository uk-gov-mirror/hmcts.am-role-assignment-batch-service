package uk.gov.hmcts.reform.roleassignmentbatch;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.hmcts.reform.roleassignmentbatch.exception.BadDayConfigForJudicialRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteJudicialExpiredRecords;

import javax.sql.DataSource;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = BaseTest.class)
public class JudicialBookingBatchJobIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(JudicialBookingBatchJobIntegrationTest.class);
    private DeleteJudicialExpiredRecords sut;

    @Autowired
    @Qualifier("judicialDataSource")
    private DataSource judicialDataSource;

    @Mock
    EmailService emailService = Mockito.mock(EmailService.class);

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

    @Before
    public void setUp() {
        sut = new DeleteJudicialExpiredRecords(emailService, judicialDataSource, 0);
        Mockito.when(stepContribution.getStepExecution()).thenReturn(stepExecution);
        Mockito.when(stepContribution.getStepExecution().getJobExecution()).thenReturn(jobExecution);
        Mockito.when(stepContribution.getStepExecution().getJobExecution().getId()).thenReturn(Long.valueOf(1));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(dataSource = "judicialDataSource"),
            scripts = {"classpath:sql/judicial/insert_judicial_database.sql"})
    public void shouldDeleteLiveJudicialRecords() {
        Integer totalRecords = sut.getTotalJudicialRecords();
        logger.info("Total number of Judicial records in Database::{}", totalRecords);
        Integer countEligibleRecordsForDeletion = sut.getCountEligibleJudicialRecords();
        logger.info("Records Eligible for Deletion in Judicial records in Database::{}",
                countEligibleRecordsForDeletion);
        Assert.assertEquals("Total records", Integer.valueOf(5), totalRecords);
        Assert.assertEquals("Records eligible for deletion", Integer.valueOf(3),
                countEligibleRecordsForDeletion);
        logger.info("Deleting the Judicial records");
        sut.execute(stepContribution, chunkContext);

        int totalRecordsInDbPostDelete = sut.getTotalJudicialRecords();
        logger.info("Total number of Judicial records in Database::{} ", totalRecordsInDbPostDelete);

        Assert.assertEquals("Total records post delete", Integer.valueOf(2),
                Integer.valueOf(totalRecordsInDbPostDelete));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(dataSource = "judicialDataSource"),
            scripts = {"classpath:sql/judicial/insert_judicial_database.sql"})
    public void shouldNotDeleteLiveJudicialRecordsForDaysParam() {
        sut = new DeleteJudicialExpiredRecords(emailService, judicialDataSource, 50);

        Integer totalRecords = sut.getTotalJudicialRecords();
        logger.info("Total number of Judicial records in Database::{}", totalRecords);

        Integer countEligibleRecordsForDeletion = sut.getCountEligibleJudicialRecords();
        logger.info("Records Eligible for Deletion in Judicial records in Database::{}",
                countEligibleRecordsForDeletion);

        Assert.assertEquals("Total records", Integer.valueOf(5), totalRecords);
        Assert.assertEquals("Records eligible for deletion", Integer.valueOf(0),
                countEligibleRecordsForDeletion);
        logger.info("Deleting the Judicial records");
        sut.execute(stepContribution, chunkContext);

        int totalRecordsInDbPostDelete = sut.getTotalJudicialRecords();
        logger.info("Total number of Judicial records in Database::{} ", totalRecordsInDbPostDelete);

        Assert.assertEquals("Total records post delete", Integer.valueOf(5),
                Integer.valueOf(totalRecordsInDbPostDelete));
    }

    @Test(expected = BadDayConfigForJudicialRecords.class)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(dataSource = "judicialDataSource"),
            scripts = {"classpath:sql/judicial/insert_judicial_database.sql"})
    public void shouldThrowExceptionForBadDayInput() {

        sut = new DeleteJudicialExpiredRecords(emailService, judicialDataSource, -5);
        sut.execute(stepContribution, chunkContext);

    }

}
