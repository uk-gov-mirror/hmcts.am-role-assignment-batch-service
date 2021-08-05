package uk.gov.hmcts.reform.roleassignmentbatch.task;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.BEFORE_CCD_MIGRATION;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.RECONCILIATION;

import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;

@Slf4j
@Component
public class BeforeMigration implements Tasklet {

    @Autowired
    private EmailService emailService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
        EmailData emailData = EmailData
                .builder()
                .runId(jobId)
                .emailSubject(BEFORE_CCD_MIGRATION)
                .module(RECONCILIATION)
                .build();
        Response response = emailService.sendEmail(emailData);
        if (response != null) {
            log.info("Before CCD Migration - Reconciliation Status mail has been sent to target recipients");
        }
        return RepeatStatus.FINISHED;
    }
}