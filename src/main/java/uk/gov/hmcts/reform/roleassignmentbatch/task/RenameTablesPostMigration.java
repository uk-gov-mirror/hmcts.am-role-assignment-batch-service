package uk.gov.hmcts.reform.roleassignmentbatch.task;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.AFTER_TABLE_RENAME;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.AM_TABLES;

import com.sendgrid.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.ReconciliationMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;
import uk.gov.hmcts.reform.roleassignmentbatch.service.ReconciliationDataService;
import uk.gov.hmcts.reform.roleassignmentbatch.util.BatchUtil;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

@Component
@Slf4j
public class RenameTablesPostMigration implements Tasklet {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EmailService emailService;

    @Autowired
    ReconciliationDataService reconciliationDataService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        if (jdbcTemplate.queryForObject("SELECT to_regclass('replica_role_assignment');", String.class) == null) {
            log.warn("Replica tables are not available to rename, Please run Migration job first.");
            contribution.setExitStatus(new ExitStatus("FAILED", "Replica Tables not exist"));
        } else {
            dropTables();
            renameTables();
            createIndexes();
            sendEmailAfterRenamingTables(contribution);
        }
        return RepeatStatus.FINISHED;
    }

    private void sendEmailAfterRenamingTables(StepContribution contribution) {
        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();

        ReconciliationData reconcileData = jdbcTemplate.queryForObject(Constants.GET_LATEST_RECONCILIATION_DATA,
                                                                       new ReconciliationMapper());
        reconcileData.setAmRecordsAfterMigration(BatchUtil.getAmRecordsCount(jdbcTemplate));
        reconciliationDataService.saveReconciliationData(reconcileData);
        Response response = emailService.sendEmail(jobId, AFTER_TABLE_RENAME);
        if (response != null) {
            log.info("After Table Rename - Reconciliation Status mail has been sent to target recipients");
        }
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

        AM_TABLES.forEach(table -> {
            log.info("Renaming the {} table...", table);
            jdbcTemplate.update(String.format("ALTER TABLE IF EXISTS %s RENAME TO temp_%s;", table, table));
            jdbcTemplate.update(String.format("ALTER INDEX IF EXISTS %s_pkey RENAME TO temp_%s_pkey;", table, table));

            jdbcTemplate.update(String.format("ALTER TABLE IF EXISTS replica_%s RENAME TO %s;", table, table));
            jdbcTemplate.update(String.format(
                "ALTER INDEX IF EXISTS replica_%s_pkey RENAME TO %s_pkey;", table, table));
            log.info("Rename {} table is Successful", table);
        });

        log.info("End Table renaming");
    }

    private void dropTables() {
        log.info("Dropping the existing temp tables.");
        AM_TABLES.forEach(table -> jdbcTemplate.update(String.format("DROP TABLE IF EXISTS temp_%s CASCADE;", table)));
        log.info("Drop temp table is Successful");
    }
}
