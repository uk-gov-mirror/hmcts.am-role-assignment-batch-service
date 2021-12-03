package uk.gov.hmcts.reform.roleassignmentbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DELETE_EXPIRED_JUDICIAL_JOB;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DELETE_EXPIRED_JUDICIAL_JOB_STATUS;

@Component
@Slf4j
public class DeleteJudicialExpiredRecords implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    public DeleteJudicialExpiredRecords(@Autowired @Qualifier("secondaryDataSource") DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Delete Judicial Expired records task starts::");
        Instant startTime = Instant.now();
        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
        try {
            Integer countEligibleJudicialRecords = getCountEligibleJudicialRecords();
            String judicialLog = String.format("Retrieve Judicial records whose End Time is less than current time."
                    + " Number of records: %s", countEligibleJudicialRecords);
            log.info(judicialLog);
            log.info("Deleting Live Judicial records.");
            int countDeleted = deleteJudicialBookingRecords();
            String rowsDeletedLog = String.format("Number of live records deleted : %s", countDeleted);
            log.info(rowsDeletedLog);

            int judicialRecordsPostDelete = getTotalJudicialRecords();
            String numRecordsUpdatedLog = String.format(" Number of records in Judicial Table post delete: %s",
                    judicialRecordsPostDelete);
            log.info(numRecordsUpdatedLog);

            Instant endTime = Instant.now();
            Map<String, Object> templateMap = new HashMap<>();
            templateMap.put("jobId", jobId);
            templateMap.put("startTime", startTime);
            templateMap.put("endTime", endTime);
            templateMap.put("elapsedTime", Duration.between(startTime, endTime).toMillis());
            templateMap.put("liveCount", countDeleted);
            templateMap.put("updatedRecordCount", judicialRecordsPostDelete);
            EmailData emailData = EmailData
                    .builder()
                    .runId(jobId)
                    .emailSubject(DELETE_EXPIRED_JUDICIAL_JOB_STATUS)
                    .module(DELETE_EXPIRED_JUDICIAL_JOB)
                    .templateMap(templateMap)
                    .build();
            emailService.sendEmail(emailData);
        } catch (DataAccessException e) {
            log.info(String.format(" DataAccessException %s", e.getMessage()));
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return RepeatStatus.FINISHED;
    }

    public int deleteJudicialBookingRecords(int days) {
        Object[] params = {days};
        // define SQL types of the arguments
        int[] types = {Types.INTEGER};
        String deleteSql = "DELETE from booking b where b.end_time < CURRENT_DATE - ?";
        return jdbcTemplate.update(deleteSql, params, types);
    }


    public Integer getCountEligibleJudicialRecords(int days) {
        Object[] params = {days};
        // define SQL types of the arguments
        int[] types = {Types.INTEGER};
        String getSQL = "SELECT count(*) from booking b where b.end_time < CURRENT_DATE - ?";
        return jdbcTemplate.queryForObject(getSQL, Integer.class);
    }

    public Integer getTotalJudicialRecords() {
        String getSQL = "SELECT count(*) from booking b ";
        return jdbcTemplate.queryForObject(getSQL, Integer.class);
    }

}
