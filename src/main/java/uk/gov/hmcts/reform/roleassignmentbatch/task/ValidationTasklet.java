package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.AuditOperationType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils;

@Component
@Slf4j
public class ValidationTasklet implements Tasklet {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcBatchItemWriter<AuditFaults> auditWriter;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Validating CcdCaseUsers ");
        var rm = (RowMapper<CcdCaseUser>) (ResultSet rs, int rowNum) -> {
            var ccdCaseUser = new CcdCaseUser();
            ccdCaseUser.setCaseDataId(rs.getString("case_data_id"));
            ccdCaseUser.setUserId(rs.getString("user_id"));
            ccdCaseUser.setCaseRole(rs.getString("case_role"));
            ccdCaseUser.setCaseType(rs.getString("case_type"));
            ccdCaseUser.setBeginDate(rs.getString("begin_date"));
            ccdCaseUser.setRoleCategory(rs.getString("role_category"));
            ccdCaseUser.setJurisdiction(rs.getString("jurisdiction"));
            return ccdCaseUser;
        };
        List<CcdCaseUser> ccdCaseUsers = jdbcTemplate.query("select id,case_data_id,user_id,case_role,jurisdiction,"
                + "case_type,role_category,begin_date from ccd_view where case_data_id is null or user_id is null "
                + "or case_role is null or jurisdiction is null or case_type is null or role_category is null or "
                + "begin_date is null", rm);
        if (!ccdCaseUsers.isEmpty()) {
            log.warn("Validation CcdCaseUsers was skipped due to NULLS: " + ccdCaseUsers);
            var auditFaults = AuditFaults.builder()
                    .reason("Validation CcdCaseUsers was skipped due to NULLs")
                    .failedAt(AuditOperationType.VALIDATION.getLabel())
                    .ccdUsers(JacksonUtils.convertValueJsonNode(ccdCaseUsers).toString()).build();
            auditWriter.write(Collections.singletonList(auditFaults));
            contribution.setExitStatus(ExitStatus.FAILED);
        }
        return RepeatStatus.FINISHED;
    }

}
