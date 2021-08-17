package uk.gov.hmcts.reform.roleassignmentbatch.config;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.PROCESS_FLAGS;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.ApplicationParams;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.EmailData;
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

        var ldMigrationFlag = getFluxFlagStatus("ccd-am-migration");
        var ldRenameTablesFlag = getFluxFlagStatus("ccd-am-migration-rename-tables");

        var flagStatus = buildFlagStatusString(ldMigrationFlag, ldRenameTablesFlag);
        var emailData = buildEmailData(flagStatus, job.getJobId().toString());

        if (fluxMigrationFlag != ldMigrationFlag || fluxRenameTablesFlag != ldRenameTablesFlag) {
            log.info("LaunchDarkly and Flux flags do not match");
            emailService.sendEmail(emailData);
            job.setExitStatus(new ExitStatus(ExitStatus.STOPPED.toString(),
                    "LaunchDarkly and Flux flags do not match"));
            isEnabled = false;
        } else if (fluxMigrationFlag && fluxRenameTablesFlag) {
            log.info("MigrationFlag and renameTablesFlag are true. The application will exit.");
            emailService.sendEmail(emailData);
            job.setExitStatus(new ExitStatus(ExitStatus.STOPPED.toString(),
                    "migrationFlag and renameTablesFlag are true"));
            isEnabled = false;
        }
        if (!fluxMigrationFlag && !fluxRenameTablesFlag) {
            log.info("migrationFlag and renameTablesFlag are false - Process will terminate naturally.");
        }

        return isEnabled;
    }

    public String buildFlagStatusString(boolean ldmf, boolean ldrf) {
        return String.format("LD Migration Flag: %s <br />"
                        + "LD Rename Flag: %s <br />"
                        + "Flux Migration Flag: %s <br />"
                        + "Flux Rename Flag: %s <br />",
                ldmf, ldrf, fluxMigrationFlag, fluxRenameTablesFlag);
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
}
