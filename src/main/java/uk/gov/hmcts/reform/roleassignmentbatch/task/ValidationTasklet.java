package uk.gov.hmcts.reform.roleassignmentbatch.task;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.AFTER_VALIDATION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sendgrid.Response;
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
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.AuditOperationType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ReconQuery;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.CcdViewRowMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;
import uk.gov.hmcts.reform.roleassignmentbatch.service.ReconciliationDataService;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

@Component
@Slf4j
public class ValidationTasklet implements Tasklet {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcBatchItemWriter<AuditFaults> auditFaultsJdbcBatchItemWriter;

    @Autowired
    ReconciliationDataService reconciliationDataService;

    @Autowired
    EmailService emailService;

    @Autowired
    CcdViewRowMapper ccdViewRowMapper;

    @Value("${ccd.roleNames}")
    List<String> configRoleMappings;

    @Value("${ccd.roleCategories}")
    List<String> configRoleCategories;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Validating CcdCaseUsers");
        performNullChecksOnCcdFields(contribution);
        validateCaseId(contribution);
        validateRoleMappings(contribution);
        validateRoleType(contribution);
        log.info("Validating CcdCaseUsers is complete.");
        sendEmailForAnyValidationError(contribution);
        return RepeatStatus.FINISHED;
    }

    private void sendEmailForAnyValidationError(StepContribution contribution) {
        List<Map<String, Object>> validationErrors = jdbcTemplate.queryForList("select reason from audit_faults");
        if (!CollectionUtils.isEmpty(validationErrors)) {
            String errors = validationErrors.stream()
                            .map(Map::values)
                            .collect(Collectors.toList())
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));

            String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
            ReconciliationData reconciliationData =
                ReconciliationData.builder()
                                  .runId(jobId)
                                  .status(ReconQuery.FAILED.getKey())
                                  .notes(errors)
                                  .build();
            reconciliationDataService.saveReconciliationData(reconciliationData);
            Response response = emailService.sendEmail(jobId, AFTER_VALIDATION);
            if (response != null) {
                log.info("Error during Validation - Reconciliation Status mail has been sent to target recipients");
            }
        }
    }

    private void performNullChecksOnCcdFields(StepContribution contribution) throws Exception {
        List<CcdCaseUser> ccdCaseUsers = jdbcTemplate.query(Constants.CCD_RECORDS_HAVING_NULL_FIELDS, ccdViewRowMapper);
        if (!ccdCaseUsers.isEmpty()) {
            List<String> invalidIds = ccdCaseUsers.stream().map(CcdCaseUser::getId).collect(Collectors.toList());
            log.error("CCD View has null fields. The ID's are as follows: {}", invalidIds);
            AuditFaults auditFaults =
                AuditFaults.builder()
                           .reason(String.format("CCD View has null fields.The ID's are as follows: %s", invalidIds))
                           .failedAt(AuditOperationType.VALIDATION.getLabel())
                           .ccdUsers(invalidIds.toString()).build();
            auditFaultsJdbcBatchItemWriter.write(Collections.singletonList(auditFaults));
            contribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    protected void validateCaseId(StepContribution contribution) throws Exception {

        List<String> invalidCcdViewCaseIds
            = jdbcTemplate.query(Constants.QUERY_INVALID_CASE_IDS, (rs, rowNum) -> rs.getString(1));
        if (!invalidCcdViewCaseIds.isEmpty()) {
            log.error(Constants.INVALID_CASE_IDS);
            persistFaults(invalidCcdViewCaseIds, Constants.INVALID_CASE_IDS);
            contribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    protected void validateRoleMappings(StepContribution contribution) throws Exception {
        List<String> ccdViewRoles = jdbcTemplate.query(
            Constants.DISTINCT_CASE_ROLES_FROM_CCD, (rs, rowNum) -> rs.getString(1));

        if (!isASubsetOf(configRoleMappings, ccdViewRoles)) {
            List<String> invalidRoles = findDifferences(configRoleMappings, ccdViewRoles);
            log.error(String.format(Constants.INVALID_ROLES, invalidRoles));
            persistFaults(invalidRoles, Constants.INVALID_ROLES);
            contribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    protected void validateRoleType(StepContribution contribution) throws Exception {
        List<String> ccdRoleCategories = jdbcTemplate.query(
            Constants.DISTINCT_ROLE_CATEGORY_FROM_CCD, (rs, rowNum) -> rs.getString(1));

        if (!isASubsetOf(configRoleCategories, ccdRoleCategories)) {
            List<String> invalidRoles = findDifferences(configRoleCategories, ccdRoleCategories);
            log.error(Constants.INVALID_ROLE_CATEGORIES +  invalidRoles);
            persistFaults(invalidRoles, Constants.INVALID_ROLE_CATEGORIES);
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
