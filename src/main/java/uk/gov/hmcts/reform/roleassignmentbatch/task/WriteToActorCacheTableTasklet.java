package uk.gov.hmcts.reform.roleassignmentbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WriteToActorCacheTableTasklet implements Tasklet {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Writing to actor cache table");
        jdbcTemplate.update("insert into replica_actor_cache_control (actor_id, etag,json_response)"
                            + " select distinct(actor_id),0, '{}'::jsonb from replica_role_assignment;");
        log.info("Writing to actor cache table is complete");
        return RepeatStatus.FINISHED;
    }
}
