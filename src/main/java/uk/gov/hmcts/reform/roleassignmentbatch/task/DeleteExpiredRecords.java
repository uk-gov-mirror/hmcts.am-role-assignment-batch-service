package uk.gov.hmcts.reform.roleassignmentbatch.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.sql.Types;

@Component
public class DeleteExpiredRecords implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpiredRecords.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("DELETE EXPIRED RECORDS starts::");
        log.info("TRANSACTION CREATED");
        try {
            List<RoleAssignmentHistory> rah = this.getLiveRecords();
            log.info("RETRIEVED LIVE RECORDS WHOSE END TIME > CURRENT TIME. NO OF RECORDS RETRIEVED :" + rah.size());
            for (RoleAssignmentHistory ra : rah) {
                //Change the Required Variables
                log.info(ra.toString());
                ra.setStatus("DELETED");
                int statusSequence = ra.getStatusSequence();
                ra.setStatusSequence(statusSequence + 1);
                ra.setLog("Record Deleted");
                ra.setCreated(new Timestamp(System.currentTimeMillis()));
            }
            this.deleteRoleAssignmentRecords(rah);
            log.info("DELETED ROLE ASSIGNMENT RECORDS IN LIVE TABLE");
            int[] batchUpdateStatusArray = this.batchUpdateRoleAssignmentHistory(rah);
            log.info("UPDATED ROLE ASSIGNMENT HISTORY TABLE");
        } catch (DataAccessException e) {
            log.info(" DataAccessException " + e.getMessage());
        } catch (Exception e) {
            log.info(" Exception :" + e.getMessage());
        }

        log.info("DELETE EXPIRED RECORDS", "success");
        return RepeatStatus.FINISHED;
    }

    public int[] deleteRoleAssignmentRecords(List<RoleAssignmentHistory> rah) throws DataAccessException {
        String deleteSql = "DELETE FROM role_assignment WHERE id = ?";
        int[] rows = new int[rah.size()];
        for (RoleAssignmentHistory ra : rah) {
            Object[] params = {ra.getId()};
            // define SQL types of the arguments
            int[] types = {Types.VARCHAR};
            rows[0] = jdbcTemplate.update(deleteSql, params, types);
        }
        return rows;
    }

    public int[] batchUpdateRoleAssignmentHistory(List<RoleAssignmentHistory> rah) throws DataAccessException {
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
                        return rah.size();
                    }
                });
    }


    public List<RoleAssignmentHistory> getLiveRecords() throws DataAccessException {
        String getSQL = "select * from role_assignment_history rah  "
                + "WHERE id in (SELECT id FROM role_assignment WHERE end_time <= now()) and status='LIVE'";
        List<RoleAssignmentHistory> rah =
                jdbcTemplate.query(getSQL, new ResultSetExtractor<List<RoleAssignmentHistory>>() {

                    public List<RoleAssignmentHistory> extractData(
                            ResultSet rs) throws SQLException, DataAccessException {

                        List<RoleAssignmentHistory> list = new ArrayList<RoleAssignmentHistory>();
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
                    }
                });
        return rah;
    }
}
