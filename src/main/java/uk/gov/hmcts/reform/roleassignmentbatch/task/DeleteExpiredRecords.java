package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentHistory;
import uk.gov.hmcts.reform.roleassignmentbatch.util.BatchUtil;

@Component
public class DeleteExpiredRecords implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpiredRecords.class);
    private final JdbcTemplate jdbcTemplate;
    private final int batchSize;

    public DeleteExpiredRecords(@Autowired JdbcTemplate jdbcTemplate, @Value("${batch-size}")int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.batchSize = batchSize;
    }

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Delete Expired records task starts::");
        int currentRecordsInHistoryTable = getCountFromHistoryTable();
        try {
            List<RoleAssignmentHistory> rah = this.getLiveRecordsFromHistoryTable();
            String historyLog = String.format("Retrieve History records whose End Time is less than current time."
                                              + " Number of records: %s", rah.size());
            log.info(historyLog);
            for (RoleAssignmentHistory ra : rah) {
                ra.setStatus("EXPIRED");
                int statusSequence = ra.getStatusSequence();
                ra.setStatusSequence(statusSequence + 1);
                ra.setLog("Record Expired");
                ra.setCreated(new Timestamp(System.currentTimeMillis()));
            }
            log.info("Deleting Live records.");
            int rowsDeleted = this.deleteRoleAssignmentRecords(rah);
            String rowsDeletedLog = String.format("Number of live records deleted : %s", rowsDeleted);
            log.info(rowsDeletedLog);

            this.insertIntoRoleAssignmentHistoryTable(rah);

            String numRecordsUpdatedLog = String.format("Updated number of records in History Table : %s",
                                                        getCountFromHistoryTable() - currentRecordsInHistoryTable);
            log.info(numRecordsUpdatedLog);
        } catch (DataAccessException e) {
            log.info(String.format(" DataAccessException %s", e.getMessage()));
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        log.info("Delete expired records is successful");
        return RepeatStatus.FINISHED;
    }

    public int deleteRoleAssignmentRecords(List<RoleAssignmentHistory> rah) {
        String deleteSql = "DELETE FROM role_assignment WHERE id=?";
        int rows = 0;
        for (RoleAssignmentHistory ra : rah) {
            Object[] params = {ra.getId()};
            // define SQL types of the arguments
            int[] types = {Types.VARCHAR};
            rows += jdbcTemplate.update(deleteSql, params, types);
        }
        return rows;
    }

    public int[][] insertIntoRoleAssignmentHistoryTable(List<RoleAssignmentHistory> rah) {
        return jdbcTemplate.batchUpdate(
            "INSERT INTO role_assignment_history "
            + "VALUES(?,?::uuid,?,?,?,?,?,?,?,?,?,?,?,?,?,?::jsonb,?::jsonb,?,?,?)",
            rah, batchSize, BatchUtil.prepareSetterForRoleAssignmentHistory());
    }

    public List<RoleAssignmentHistory> getLiveRecordsFromHistoryTable() {
        String getSQL = "SELECT * from role_assignment_history rah  "
                        + "WHERE id in (SELECT id FROM role_assignment WHERE end_time <= now()) and status='LIVE'";
        List<RoleAssignmentHistory> list = new ArrayList<>();
        return
            jdbcTemplate.query(getSQL, rs -> {
                while (rs.next()) {
                    RoleAssignmentHistory roleAssignmentHistory = new RoleAssignmentHistory();
                    roleAssignmentHistory.setId(rs.getString("id"));
                    roleAssignmentHistory.setRequestId(rs.getObject("request_id", java.util.UUID.class));
                    roleAssignmentHistory.setActorIDType(rs.getString("actor_id_type"));
                    roleAssignmentHistory.setActorId(rs.getString("actor_id"));
                    roleAssignmentHistory.setRoleType(rs.getString("role_type"));
                    roleAssignmentHistory.setRoleName(rs.getString("role_name"));
                    roleAssignmentHistory.setClassification(rs.getString("classification"));
                    roleAssignmentHistory.setGrantType(rs.getString("grant_type"));
                    roleAssignmentHistory.setRoleCategory(rs.getString("role_category"));
                    roleAssignmentHistory.setReadOnly(rs.getBoolean("read_only"));
                    roleAssignmentHistory.setBeginTime(rs.getTimestamp("begin_time"));
                    roleAssignmentHistory.setEndTime(rs.getTimestamp("end_time"));
                    roleAssignmentHistory.setStatus(rs.getString("status"));
                    roleAssignmentHistory.setReference(rs.getString("reference"));
                    roleAssignmentHistory.setProcess(rs.getString("process"));
                    roleAssignmentHistory.setAttributes(rs.getString("attributes"));
                    roleAssignmentHistory.setNotes(rs.getString("notes"));
                    roleAssignmentHistory.setLog(rs.getString("log"));
                    roleAssignmentHistory.setStatusSequence(rs.getInt("status_sequence"));
                    roleAssignmentHistory.setCreated(rs.getTimestamp("created"));
                    list.add(roleAssignmentHistory);
                }
                return list;
            });
    }

    public Integer getCountFromHistoryTable() {
        String getSQL = "SELECT count(*) from role_assignment_history rah";
        return jdbcTemplate.queryForObject(getSQL, Integer.class);
    }

}
