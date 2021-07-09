package uk.gov.hmcts.reform.roleassignmentbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@SuppressWarnings("all")
public class RenameTablesPostMigration implements Tasklet {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        dropTables();
        renameTables();
        createIndexes();
        return RepeatStatus.FINISHED;
    }

    private void createIndexes() {
        log.info("Rebuilding the indexes");

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_process_reference ON"
                             + " role_assignment_history USING btree (process, reference);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_actor_id ON role_assignment USING btree (actor_id);");

        log.info("Index rebuild is complete");
    }

    private void renameTables() {
        log.info("Starting renaming the tables.");
        log.info("Renaming the Live table.");
        jdbcTemplate.update("ALTER TABLE IF EXISTS role_assignment RENAME TO temp_role_assignment;");
        jdbcTemplate.update("ALTER TABLE IF EXISTS replica_role_assignment RENAME TO role_assignment;");
        log.info("Rename Live table is Successful");

        log.info("Renaming the History table.");
        jdbcTemplate.update("ALTER TABLE IF EXISTS role_assignment_history RENAME TO temp_role_assignment_history;");
        jdbcTemplate.update("ALTER TABLE IF EXISTS replica_role_assignment_history RENAME TO role_assignment_history;");
        log.info("Rename history table is Successful");

        log.info("Renaming the Request table.");
        jdbcTemplate.update("ALTER TABLE IF EXISTS role_assignment_request RENAME TO temp_role_assignment_request;");
        jdbcTemplate.update("ALTER TABLE IF EXISTS replica_role_assignment_request RENAME TO role_assignment_request;");
        log.info("Rename Request table is Successful");

        log.info("Renaming the Actor Cache table.");
        jdbcTemplate.update("ALTER TABLE IF EXISTS actor_cache_control RENAME TO temp_actor_cache_control;");
        jdbcTemplate.update("ALTER TABLE IF EXISTS replica_actor_cache_control RENAME TO actor_cache_control;");
        log.info("Rename Actor Cache is Successful");

        log.info("End Table renaming");
    }

    private void dropTables() {
        log.info("Dropping the existing temp tables.");
        jdbcTemplate.update("DROP TABLE IF EXISTS temp_actor_cache_control CASCADE;");
        jdbcTemplate.update("DROP TABLE IF EXISTS temp_role_assignment CASCADE;");
        jdbcTemplate.update("DROP TABLE IF EXISTS temp_role_assignment_history CASCADE;");
        jdbcTemplate.update("DROP TABLE IF EXISTS temp_role_assignment_request CASCADE;");

        log.info("Drop temp table is Successful");
    }
}
