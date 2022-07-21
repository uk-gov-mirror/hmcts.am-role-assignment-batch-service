package uk.gov.hmcts.reform.roleassignmentbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

@Slf4j
@Component
public class SetupDbFlags implements Tasklet {

    @Autowired
    private JdbcTemplate template;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        template.execute(Constants.SETUP_MIGRATION_CONTROL_TABLE);
        template.execute(Constants.SETUP_MIGRATION_MAIN);
        template.execute(Constants.SETUP_MIGRATION_RENAME);
        return RepeatStatus.FINISHED;
    }
}