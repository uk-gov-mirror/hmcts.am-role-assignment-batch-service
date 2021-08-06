package uk.gov.hmcts.reform.roleassignmentbatch.config;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.ApplicationParams;
import uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;

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

    public JobRunnableDecider(boolean migrationFlag, boolean renameTables) {
        fluxMigrationFlag = migrationFlag;
        fluxRenameTablesFlag = renameTables;
    }

    @Override
    public FlowExecutionStatus decide(JobExecution job, StepExecution stepExecution) {
        var isFlowEnabled = false;
        boolean isServiceEnabled = featureConditionEvaluator.isFlagEnabled(SERVICE_NAME,
                job.getJobInstance().getJobName());
        log.info("***** LdFlag for {} is enabled {} *****", job.getJobInstance().getJobName(), isServiceEnabled);
        log.info("***** The Application params for Job {} run are {} *****", job.getJobInstance().getJobName(),
                 applicationParams.toString());

        if (isServiceEnabled) {
            isFlowEnabled = processExecutionDecider(job);
        }

        return new FlowExecutionStatus(isFlowEnabled ? ENABLED : DISABLED);
    }

    public boolean processExecutionDecider(JobExecution job) {
        var isEnabled = true;
        var ldMigrationFlag = featureConditionEvaluator.isFlagEnabled(SERVICE_NAME,
                "ccd-am-migration");
        var ldRenameTablesFlag = featureConditionEvaluator.isFlagEnabled(SERVICE_NAME,
                "ccd-am-migration-rename-tables");

        if (fluxMigrationFlag != ldMigrationFlag || fluxRenameTablesFlag != ldRenameTablesFlag) {
            log.info("LaunchDarkly and Flux flags do not match");
            job.setExitStatus(new ExitStatus(ExitStatus.STOPPED.toString(),
                    "LaunchDarkly and Flux flags do not match"));
            isEnabled = false;
        } else if (fluxMigrationFlag && fluxRenameTablesFlag) {
            log.info("migrationFlag and renameTablesFlag are true");
            job.setExitStatus(new ExitStatus(ExitStatus.STOPPED.toString(),
                    "migrationFlag and renameTablesFlag are true"));
            isEnabled = false;
        }
        if (!fluxMigrationFlag && !fluxRenameTablesFlag) {
            log.info("migrationFlag and renameTablesFlag are false - Process will terminate naturally.");
        }

        return isEnabled;
    }
}
