package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly.FeatureConditionEvaluator;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;

@Slf4j
public class JobRunnableDecider implements JobExecutionDecider {

    private static final String SERVICE_NAME = "am_role_assignment_batch_service";

    @Autowired
    FeatureConditionEvaluator featureConditionEvaluator;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        boolean isEnabled = featureConditionEvaluator.isFlagEnabled(SERVICE_NAME, "ccd-am-migration");
        log.info("***** LdFlag for CCD to AM migration is enabled {} *****", isEnabled);
        return new FlowExecutionStatus(isEnabled ? ENABLED : DISABLED);
    }
}
