package uk.gov.hmcts.reform.roleassignmentbatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly.FeatureConditionEvaluator;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = {BaseTest.class})
@ComponentScan(basePackages = "uk.gov.hmcts.reform.roleassignmentbatch")
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@TestPropertySource(properties = {"csv-file-path=src/integrationTest/resources/"})
public class CcdToRasBatchJobIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CcdToRasBatchJobIntegrationTest.class);

    private static final String COUNT_RECORDS_FROM_AUDIT_TABLE = "SELECT count(*) as n FROM audit_faults";

    private static final String AUDIT_TABLE = "SELECT id,failed_at,reason ,ccd_users,request,history,live "
            + "FROM audit_faults";

    private static final String BATCH_JOB_TABLE = "SELECT job_execution_id, job_name, status, exit_code, exit_message "
            + "FROM public.batch_job_execution j,public.batch_job_instance i where j.job_instance_id=i.job_instance_id "
            + "order by start_time desc";

    private static final String BATCH_STEP_TABLE = "SELECT step_name, status, commit_count, read_count, write_count,"
            + " read_skip_count, write_skip_count, process_skip_count, exit_code, exit_message "
            + "FROM public.batch_step_execution where job_execution_id=?";

    private static final String CCD_AM_JOB = "ccd-am-migration";

    @Autowired
    private DataSource ds;

    private JdbcTemplate template;

    @MockBean
    JdbcBatchItemWriter<HistoryEntity> roleAssignmentHistoryWriter;

    @MockBean
    FeatureConditionEvaluator featureConditionEvaluator;

    @MockBean
    ApplicationParams applicationParams;

    @Before
    public void setUp() {
        template = new JdbcTemplate(ds);
        MockitoAnnotations.initMocks(this);
        doReturn(true).when(featureConditionEvaluator).isFlagEnabled(any(),any());
    }

    @Test
    public void shouldStopProcess_LdFlagFalse() {
        logger.info("Process should stop as LD flag set as false");
        doReturn(false).when(featureConditionEvaluator).isFlagEnabled(anyString(), anyString());
        Map<String, String> job = getRecordsFromBatchJobTable(CCD_AM_JOB);
        Map<String, Map<String, String>> step = getRecordsFromBatchStepTable(job.get("job_execution_id"));

        assertEquals("COMPLETED", job.get("status"));
        assertEquals("STOPPED", job.get("exit_code"));
        assertEquals("COMPLETED", step.get("LdValidation").get("status"));
        assertEquals("COMPLETED", step.get("LdValidation").get("exit_code"));
    }

    @Test
    public void shouldStopProcess_LdFlagTrue_MergeFlagsFalse() {
        logger.info("Process should stop as migration flags are set to false");
        Mockito.when(featureConditionEvaluator.isFlagEnabled(anyString(),anyString())).thenReturn(true);
        doReturn(true).when(featureConditionEvaluator).isFlagEnabled(anyString(), anyString());
        doReturn(DISABLED).when(applicationParams).getProcessCcdDataEnabled();
        doReturn(DISABLED).when(applicationParams).getRenamingPostMigrationTablesEnabled();

        Map<String, String> job = getRecordsFromBatchJobTable(CCD_AM_JOB);
        Map<String, Map<String, String>> step = getRecordsFromBatchStepTable(job.get("job_execution_id"));
        assertEquals("COMPLETED", job.get("status"));
        //assertEquals("COMPLETED", job.get("exit_code"));
        assertEquals("COMPLETED", step.get("LdValidation").get("status"));
        assertEquals("COMPLETED", step.get("LdValidation").get("exit_code"));
    }

    @Test
    public void shouldProcess_LdFlagTrue_OnlyMergeProcessFlagsTrue() {
        logger.info("Process data from CCD to RAS temp tables as migration.masterFlag set to true and "
                + "rename should not happen");
        //doThrow(new Exception("proc ess failed")).when(roleAssignmentHistoryWriter).write(any());
        doReturn(true).when(featureConditionEvaluator).isFlagEnabled(anyString(), anyString());
        doReturn(ENABLED).when(applicationParams).getProcessCcdDataEnabled();
        doReturn(DISABLED).when(applicationParams).getRenamingPostMigrationTablesEnabled();
        Map<String, String> job = getRecordsFromBatchJobTable(CCD_AM_JOB);
        Map<String, Map<String, String>> step = getRecordsFromBatchStepTable(job.get("job_execution_id"));
        assertEquals("COMPLETED", job.get("status"));
        //assertEquals("COMPLETED", job.get("exit_code"));
        assertEquals("COMPLETED", step.get("LdValidation").get("status"));
        assertEquals("COMPLETED", step.get("LdValidation").get("exit_code"));
    }

    /*@ParameterizedTest
    @CsvSource({
            "/welcome,GET,orm-base-flag",
            "/am/role-mapping/refresh,POST,orm-refresh-role",
    })
    void getLdFlagGetCase(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        String flagName = featureConditionEvaluator.isFlagEnabled(request);
        Assertions.assertEquals(flag, flagName);
    }
    */

    public List<AuditFaults> getRecordsFromAuditTable() {

        var rm = (RowMapper<AuditFaults>) (ResultSet result, int rowNum) -> {
            var entity = new AuditFaults();
            entity.setId(result.getLong("id"));
            entity.setFailedAt(result.getString("failed_at"));
            entity.setReason(result.getString("reason"));
            entity.setCcdUsers(result.getString("ccd_users"));
            entity.setHistory(result.getString("history"));
            entity.setRequest(result.getString("request"));
            entity.setLive(result.getString("live"));
            return entity;
        };
        return template.query(AUDIT_TABLE, rm);
    }

    public Map<String, String> getRecordsFromBatchJobTable(String jobName) {
        return template.queryForList(BATCH_JOB_TABLE).stream().filter(e -> e.get("job_name").equals(jobName))
                .findFirst().get().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }

    public Map<String, Map<String, String>> getRecordsFromBatchStepTable(String exeId) {
        return template.queryForList(BATCH_STEP_TABLE, exeId).stream()
                .collect(Collectors.toMap(m -> m.get("step_name").toString(), m -> m.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()))));
    }
}
