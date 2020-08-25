package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteExpiredRecords implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpiredRecords.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${batch-size}")
    int batchSize;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Delete Expired records task starts::");
        int currentRecordsInHistoryTable = getCountFromHistoryTable();
        try {
            List<RoleAssignmentHistory> rah = this.getLiveRecordsFromHistoryTable();
            log.info(String.format("Retrieve History records whose End Time is less than current time."
                                   + " Number of records: %s", rah.size()));
            for (RoleAssignmentHistory ra : rah) {
                ra.setStatus("EXPIRED");
                int statusSequence = ra.getStatusSequence();
                ra.setStatusSequence(statusSequence + 1);
                ra.setLog("Record Expired");
                ra.setCreated(new Timestamp(System.currentTimeMillis()));
            }
            log.info("Deleting Live records.");
            int rowsDeleted = this.deleteRoleAssignmentRecords(rah);
            log.info(String.format("Number of live records deleted : %s", rowsDeleted));

            this.insertIntoRoleAssignmentHistoryTable(rah);

            log.info(String.format("Updated number of records in History Table : %s",
                                   getCountFromHistoryTable() - currentRecordsInHistoryTable));
        } catch (DataAccessException e) {
            log.info(String.format(" DataAccessException %s", e.getMessage()));
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        log.info("Delete expired records is successful");
        return RepeatStatus.FINISHED;
    }

    public int deleteRoleAssignmentRecords(List<RoleAssignmentHistory> rah) throws DataAccessException {
        String deleteSql = "DELETE FROM role_assignment WHERE id = ?";
        int rows = 0;
        for (RoleAssignmentHistory ra : rah) {
            Object[] params = {ra.getId()};
            // define SQL types of the arguments
            int[] types = {Types.VARCHAR};
            rows += jdbcTemplate.update(deleteSql, params, types);
        }
        return rows;
    }

    public int[] insertIntoRoleAssignmentHistoryTable(List<RoleAssignmentHistory> rah) {
        return jdbcTemplate.batchUpdate("INSERT INTO role_assignment_history "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, rah.get(i).getId());
                        ps.setObject(2, rah.get(i).getRequestId());
                        ps.setString(3, rah.get(i).getActorIDType());
                        ps.setObject(4, rah.get(i).getActorId());
                        ps.setString(5, rah.get(i).getRoleType());
                        ps.setString(6, rah.get(i).getRoleName());
                        ps.setString(7, rah.get(i).getClassification());
                        ps.setString(8, rah.get(i).getGrantType());
                        ps.setString(9, rah.get(i).getRoleCategory());
                        ps.setBoolean(10, rah.get(i).isReadOnly());
                        ps.setTimestamp(11, rah.get(i).getBeginTime());
                        ps.setTimestamp(12, rah.get(i).getEndTime());
                        ps.setString(13, rah.get(i).getStatus());
                        ps.setString(14, rah.get(i).getReference());
                        ps.setString(15, rah.get(i).getProcess());
                        ps.setString(16, rah.get(i).getAttributes());
                        ps.setString(17, rah.get(i).getNotes());
                        ps.setString(18, rah.get(i).getLog());
                        ps.setInt(19, rah.get(i).getStatusSequence());
                        ps.setTimestamp(20, rah.get(i).getCreated());
                    }

                    @Override
                    public int getBatchSize() {
                        return rah.size() > batchSize ? batchSize : rah.size();
                    }
                });
    }


    public List<RoleAssignmentHistory> getLiveRecordsFromHistoryTable() {
        String getSQL = "SELECT * from role_assignment_history rah  "
                        + "WHERE id in (SELECT id FROM role_assignment WHERE end_time <= now()) and status='LIVE'";
        return
            jdbcTemplate.query(getSQL, rs -> {

                List<RoleAssignmentHistory> list = new ArrayList<>();
                while (rs.next()) {
                    RoleAssignmentHistory roleAssignmentHistory = new RoleAssignmentHistory();
                    roleAssignmentHistory.setId(rs.getObject("id", java.util.UUID.class));
                    roleAssignmentHistory.setRequestId(rs.getObject("request_id", java.util.UUID.class));
                    roleAssignmentHistory.setActorIDType(rs.getString("actor_id_type"));
                    roleAssignmentHistory.setActorId(rs.getObject("actor_id", java.util.UUID.class));
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
