package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.AuditOperationType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.CcdViewRowMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;
import uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils;

@Component
@Slf4j
public class ValidationTasklet implements Tasklet {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcBatchItemWriter<AuditFaults> auditFaultsJdbcBatchItemWriter;

    @Autowired
    CcdViewRowMapper ccdViewRowMapper;

    @Value("${csv-file-name}")
    String fileName;
    @Value("${csv-file-path}")
    String filePath;
    @Value("${ccd.roleNames}")
    List<String> roleMappings;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Validating CcdCaseUsers");
        performNullChecksOnCcdFields(contribution);
        validateCaseId(contribution);
        validateRoleMappings(roleMappings, contribution);
        log.info("Validating CcdCaseUsers is complete.");
        return RepeatStatus.FINISHED;
    }

    private void performNullChecksOnCcdFields(StepContribution contribution) throws Exception {
        List<CcdCaseUser> ccdCaseUsers = jdbcTemplate.query(Constants.CCD_RECORDS_HAVING_NULL_FIELDS, ccdViewRowMapper);
        if (!ccdCaseUsers.isEmpty()) {
            log.error("Validation CcdCaseUsers was skipped due to NULLS: " + ccdCaseUsers);
            var auditFaults = AuditFaults.builder()
                                         .reason("Validation CcdCaseUsers was skipped due to NULLs")
                                         .failedAt(AuditOperationType.VALIDATION.getLabel())
                                         .ccdUsers(JacksonUtils.convertValueJsonNode(ccdCaseUsers).toString()).build();
            auditFaultsJdbcBatchItemWriter.write(Collections.singletonList(auditFaults));
            contribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    protected void validateCaseId(StepContribution contribution) throws Exception {

        List<String> invalidCcdViewCaseIds
            = jdbcTemplate.query(Constants.QUERY_INVALID_CASE_IDS, (rs, rowNum) -> rs.getString(1));
        if (!invalidCcdViewCaseIds.isEmpty()) {
            log.error(String.format(Constants.INVALID_CASE_IDS, invalidCcdViewCaseIds));
            persistFaults(invalidCcdViewCaseIds, Constants.INVALID_CASE_IDS);
            contribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    protected void validateRoleMappings(List<String> roleMappings, StepContribution contribution) throws Exception {
        List<String> ccdViewRoles = jdbcTemplate.query(Constants.DISTINCT_CASE_ROLES_FROM_CCD, (rs, rowNum) -> rs.getString(1));

        if (!isASubsetOf(roleMappings, ccdViewRoles)) {
            List<String> invalidRoles = findDifferences(roleMappings, ccdViewRoles);
            log.error(String.format(Constants.INVALID_ROLES, invalidRoles));
            persistFaults(invalidRoles, Constants.INVALID_ROLES);
            contribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    private boolean isASubsetOf(List<?> list, List<?> sublist) {
        return list.containsAll(sublist);
    }

    private List<String> findDifferences(List<String> list, List<String> sublist) {
        return sublist.stream()
                      .filter(element -> !list.contains(element))
                      .collect(Collectors.toList());
    }

    private void persistFaults(List<String> invalidDataList, String reason) throws Exception {
        AuditFaults fault = new AuditFaults();
        fault.setFailedAt(AuditOperationType.VALIDATION.getLabel());
        fault.setReason(reason + invalidDataList);

        auditFaultsJdbcBatchItemWriter.write(Collections.singletonList(fault));
    }

}
