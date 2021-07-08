package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;

@Component
@Slf4j
public class ValidationTasklet implements Tasklet {

    File downloadedFile;
    FlatFileItemReader<CcdCaseUser> reader;

    @Autowired
    public ValidationTasklet(String fileName, String filePath, FlatFileItemReader<CcdCaseUser> fileItemReader) {
        downloadedFile = new File(filePath + fileName);
        reader = fileItemReader;

        reader.open(new ExecutionContext());
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        CcdCaseUser ccdUser;
        do {
            ccdUser = reader.read();
            if (ccdUser != null) {
                validate(ccdUser);
            }
        } while (ccdUser != null);

        return RepeatStatus.FINISHED;
    }

    protected void validate(CcdCaseUser ccdCaseUser) {
        validateCaseId(ccdCaseUser.getCaseDataId());
        validateUserId(ccdCaseUser.getUserId());
    }

    protected void validateCaseId(String caseId) {
        if (caseId.length() > 16) {
            log.info("caseIdlength is invalid.");
            //TODO: Log it in audit_exception table
            //throw new RuntimeException("caseId invalid")
        }
    }

    protected void validateUserId(String userId) {
        //TODO
    }
}
