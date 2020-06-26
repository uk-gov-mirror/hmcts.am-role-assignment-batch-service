package uk.gov.hmcts.reform.roleassignmentbatch.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class FetchExpiredRecords implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(FetchExpiredRecords.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("::ParentRouteTask starts::");
        log.info("Select Records - START");
        jdbcTemplate.execute("select * from role_assignment_history rah  WHERE id in (SELECT id FROM role_assignment WHERE end_time <= now()) and status='LIVE'");

        jdbcTemplate.query(
                "select id,request_id,actor_id from role_assignment_history rah  WHERE id in (SELECT id FROM role_assignment WHERE end_time >= now()) and status='LIVE'",
                (rs, rowNum) -> new RoleAssignmentHistory(rs.getObject("id", java.util.UUID.class), rs.getObject("request_id", java.util.UUID.class), rs.getObject("actor_id", java.util.UUID.class))
        ).forEach(roleassignmenthistory -> log.info(roleassignmenthistory.toString()));
        log.info("Select Records - END");
        log.info("::ParentRouteTask completes with {}::", "status");
        return RepeatStatus.FINISHED;
    }
}
