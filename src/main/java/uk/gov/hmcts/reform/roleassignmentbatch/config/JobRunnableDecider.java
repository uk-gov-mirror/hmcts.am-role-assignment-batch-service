package uk.gov.hmcts.reform.roleassignmentbatch.config;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.ApplicationParams;
import uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly.FeatureConditionEvaluator;

@Slf4j
public class JobRunnableDecider implements JobExecutionDecider {

    private static final String SERVICE_NAME = "am_role_assignment_batch_service";

    @Autowired
    FeatureConditionEvaluator featureConditionEvaluator;

    @Autowired
    ApplicationParams applicationParams;

    @Override
    public FlowExecutionStatus decide(JobExecution job, StepExecution stepExecution) {
        boolean isEnabled = featureConditionEvaluator.isFlagEnabled(SERVICE_NAME, job.getJobInstance().getJobName());
        log.info("***** LdFlag for {} is enabled {} *****", job.getJobInstance().getJobName(), isEnabled);
        log.info("***** The Application params for Job {} run are {} *****", job.getJobInstance().getJobName(),
                 applicationParams.toString());
        return new FlowExecutionStatus(isEnabled ? ENABLED : DISABLED);
    }
}
