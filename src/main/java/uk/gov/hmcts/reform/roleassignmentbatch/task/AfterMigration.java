package uk.gov.hmcts.reform.roleassignmentbatch.task;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.AFTER_CCD_MIGRATION;

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
public class AfterMigration implements Tasklet {

    @Autowired
    private EmailService emailService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
        EmailData emailData = EmailData
                .builder()
                .runId(jobId)
                .emailSubject(AFTER_CCD_MIGRATION)
                .module("Reconciliation")
                .build();
        Response response = emailService.sendEmail(emailData);
        if (response != null) {
            log.info("After CCD Migration - Reconciliation Status mail has been sent to target recipients");
        }
        return RepeatStatus.FINISHED;
    }
}