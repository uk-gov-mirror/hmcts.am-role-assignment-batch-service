package uk.gov.hmcts.reform.roleassignmentbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.AM_TABLES;

@Component
@Slf4j
public class RenameTablesPostMigration implements Tasklet {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        if (jdbcTemplate.queryForObject("SELECT to_regclass('replica_role_assignment');",String.class) == null) {
            contribution.setExitStatus(new ExitStatus("FAILED", "Replica Tables not exist"));
        } else {
            dropTables();
            renameTables();
            createIndexes();
        }
        return RepeatStatus.FINISHED;
    }

    private void createIndexes() {
        log.info("Rebuilding the indexes");

        jdbcTemplate.execute("ALTER INDEX IF EXISTS idx_actor_id RENAME TO temp_idx_actor_id;");
        jdbcTemplate.execute("ALTER INDEX IF EXISTS idx_process_reference RENAME TO temp_idx_process_reference;");

        jdbcTemplate.execute("ALTER INDEX IF EXISTS replica_role_assignment_actor_id_idx RENAME TO idx_actor_id;");
        jdbcTemplate.execute("ALTER INDEX IF EXISTS replica_role_assignment_history_process_reference_idx RENAME TO"
                + " idx_process_reference;");

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_process_reference ON"
                                    + " role_assignment_history USING btree (process, reference);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_actor_id ON role_assignment USING btree (actor_id);");

        log.info("Index rebuild is complete");
    }

    private void renameTables() {
        log.info("Starting renaming the tables.");
        jdbcTemplate.update("ALTER INDEX IF EXISTS pk_role_assignment_history RENAME TO role_assignment_history_pkey;");
        AM_TABLES.forEach(table -> {
            log.info("Renaming the {} table...", table);
            jdbcTemplate.update(String.format("ALTER TABLE IF EXISTS %s RENAME TO temp_%s;", table, table));
            jdbcTemplate.update(String.format("ALTER INDEX IF EXISTS %s_pkey RENAME TO temp_%s_pkey;", table, table));

            jdbcTemplate.update(String.format("ALTER TABLE IF EXISTS replica_%s RENAME TO %s;", table, table));
            jdbcTemplate.update(String.format("ALTER INDEX IF EXISTS replica_%s_pkey RENAME TO %s_pkey;",table,table));
            log.info("Rename {} table is Successful", table);
        });

        log.info("End Table renaming");
    }

    private void dropTables() {
        log.info("Dropping the existing temp tables.");
        AM_TABLES.forEach(table -> jdbcTemplate.update(String.format("DROP TABLE IF EXISTS temp_%s CASCADE;",table)));
        log.info("Drop temp table is Successful");
    }
}
