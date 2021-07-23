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
        jdbcTemplate.update("INSERT into replica_actor_cache_control(SELECT * FROM actor_cache_control) on conflict do nothing;");
        jdbcTemplate.update("INSERT into replica_role_assignment(SELECT * FROM role_assignment) on conflict do nothing;");
        jdbcTemplate.update("INSERT into replica_role_assignment_request(SELECT * FROM role_assignment_request) on conflict do nothing;");
        jdbcTemplate.update("INSERT into replica_role_assignment_history(SELECT * FROM role_assignment_history) on conflict do nothing;");
        log.info("Data insertion from Current tables to replicas is complete");

        return RepeatStatus.FINISHED;
    }
}
