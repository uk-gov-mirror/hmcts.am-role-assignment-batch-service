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

@Component
@Slf4j
public class InsertDataPostMigrationTasklet implements Tasklet {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Writing to actor cache table");
        jdbcTemplate.update(Constants.INSERT_INTO_ACTOR_CACHE);
        log.info("Writing to actor cache table is complete");

        log.info("Insert data from current tables to Replicas");
        jdbcTemplate.update(Constants.COPY_DATA_INTO_ACTOR_CACHE);
        jdbcTemplate.update(Constants.COPY_DATA_INTO_ROLE_ASSIGNMENT);
        jdbcTemplate.update(Constants.COPY_DATA_INTO_HISTORY);
        jdbcTemplate.update(Constants.COPY_DATA_INTO_REQUEST);
        log.info("Data insertion from Current tables to replicas is complete");

        return RepeatStatus.FINISHED;
    }
}
