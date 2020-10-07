/*
package uk.gov.hmcts.reform.roleassignmentbatch.config;

import javax.sql.DataSource;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LiquibaseMigration implements Tasklet {

    @Autowired
    DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseMigration.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
            new JdbcConnection(dataSource.getConnection()));
        Liquibase liquibase = null;
        try {
            liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                                      new ClassLoaderResourceAccessor(), database
            );
            liquibase.update(new Contexts());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            if (liquibase != null) {
                if (liquibase.listLocks() != null && liquibase.listLocks().length == 1) {
                    DatabaseChangeLogLock lock = liquibase.listLocks()[0];
                    if (lock != null && StringUtils.isNotEmpty(lock.getLockedBy())) {
                        LOGGER.error("Force releasing the database lock after 10 seconds.");
                        Thread.sleep(10000);
                        liquibase.forceReleaseLocks();
                        LOGGER.error("Lock release is completed.");
                    }
                }
                liquibase.close();
            }
        }
        return RepeatStatus.FINISHED;
    }
}
*/
