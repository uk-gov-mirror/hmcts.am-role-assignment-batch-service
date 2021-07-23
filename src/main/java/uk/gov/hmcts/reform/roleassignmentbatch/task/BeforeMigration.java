package uk.gov.hmcts.reform.roleassignmentbatch.task;

import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.BEFORE_CCD_MIGRATION;

@Slf4j
@Component
public class BeforeMigration implements Tasklet {

    @Autowired
    private EmailService emailService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
            Response response = emailService.sendEmail(jobId, BEFORE_CCD_MIGRATION);
            if(response !=null){
                log.info("Before CCD Migration - Reconciliation Status mail has been sent to target recipients");
            }
            return RepeatStatus.FINISHED;
    }
}