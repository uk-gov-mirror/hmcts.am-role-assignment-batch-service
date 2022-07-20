package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.roleassignmentbatch.ApplicationParams;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.PROCESS_FLAGS;

@Slf4j
public class JobRunnableDecider implements JobExecutionDecider {

    private static final String SERVICE_NAME = "am_role_assignment_batch_service";

    boolean fluxMigrationFlag;
    boolean fluxRenameTablesFlag;

    @Autowired
    FeatureConditionEvaluator featureConditionEvaluator;

    @Autowired
    private EmailService emailService;

    @Autowired
    ApplicationParams applicationParams;

    @Autowired
    JdbcTemplate template;

    public JobRunnableDecider(boolean migrationFlag, boolean renameTables) {
        fluxMigrationFlag = migrationFlag;
        fluxRenameTablesFlag = renameTables;
    }

    public String buildFlagStatusString(boolean ldmf, boolean ldrf,
                                        boolean isDbMigrationFlagEnabled, boolean isRenameFlagEnabled) {
        return String.format("LD Migration Flag: %s <br />"
                        + "LD Rename Flag: %s <br />"
                        + "<hr>"
                        + "Flux Migration Flag: %s <br />"
                        + "Flux Rename Flag: %s <br />"
                        + "<hr>"
                        + "DB Migration Flag: %s <br />"
                        + "DB Rename Flag: %s <br />",
                ldmf, ldrf, fluxMigrationFlag, fluxRenameTablesFlag, isDbMigrationFlagEnabled, isRenameFlagEnabled);
    }

    public EmailData buildEmailData(String flagStatus, String jobId) {
        return EmailData
                .builder()
                .runId(jobId)
                .emailSubject(PROCESS_FLAGS)
                .module("ProcessFlags")
                .flags(flagStatus)
                .build();
    }

    public boolean getFluxFlagStatus(String flagName) {
        return featureConditionEvaluator.isFlagEnabled(SERVICE_NAME, flagName);
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        return null;
    }
}
