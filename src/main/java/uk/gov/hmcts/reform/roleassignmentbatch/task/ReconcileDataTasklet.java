package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ReconQuery;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;
import uk.gov.hmcts.reform.roleassignmentbatch.exception.NoReconciliationDataFound;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.ReconciliationMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.service.ReconciliationDataService;
import uk.gov.hmcts.reform.roleassignmentbatch.util.BatchUtil;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

@Component
@Slf4j
public class ReconcileDataTasklet implements Tasklet {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReconciliationDataService reconDataService;

    /**
     * Reconciliation Logic with below steps.
     * 1. Ccd_view count is validate against am_role_assgnment table.
     * 2. If both count does not match then status ccolumn updated with Failed in Reconciliation_data table
     * 3. ccd_jurisdiction_data,ccd_role_name_data,replica_am_jurisdiction_data,replica_am_role_name_data
     * updated with grouing by role and total record
     * 4. Migration job status is updated with Failed status if there is any record in Audit_table.
     */

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Reconciling the CCD and AM Data.");

        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
        log.info("Starting the reconciliation for job id: {}", jobId);

        List<Map<String, Object>> groupByAmJurisdiction = reconDataService
            .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_JURISDICTION.getKey());
        List<Map<String, Object>> groupByAmRoleName = reconDataService
            .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_CASE_ROLE.getKey());

        String amJurisdictionData = reconDataService
            .populateAsJsonData(groupByAmJurisdiction, ReconQuery.CASE_TYPE.getKey());
        String amRoleNameData = reconDataService
            .populateAsJsonData(groupByAmRoleName, ReconQuery.ROLE_NAME.getKey());

        ReconciliationData reconcileData = jdbcTemplate.queryForObject(Constants.GET_RECONCILIATION_DATA,
                                                                       new ReconciliationMapper(), jobId);
        if (reconcileData == null) {
            setJobExitStatus(Boolean.FALSE, contribution);
            throw new NoReconciliationDataFound(String.format(Constants.NO_RECONCILIATION_DATA_FOUND, jobId));
        }

        reconcileData.setReplicaAmJurisdictionData(amJurisdictionData);
        reconcileData.setReplicaAmRoleNameData(amRoleNameData);
        // This data should be set after the table rename.
        // reconcileData.setAmRecordsAfterMigration(BatchUtil.getAmRecordsCount(jdbcTemplate));

        Integer auditRecords = jdbcTemplate.queryForObject(ReconQuery.AUDIT_FAULTS_TOTAL_COUNT.getKey(), Integer.class);
        int totalCountFromRoleAssignment = reconDataService.populateTotalRecord(ReconQuery.AM_TOTAL_COUNT.getKey());

        boolean jobPassed = true;
        String notes = "";

        if (auditRecords != null && auditRecords > 0) {
            jobPassed = false;
            notes = ReconQuery.CHECK_AUDIT_TABLE.getKey();
        }

        if (totalCountFromRoleAssignment != reconcileData.getTotalCountFromCcd()) {
            jobPassed = false;
            notes = notes.concat(ReconQuery.FAILED_STATUS.getKey());
        }

        if (!isJurisdictionDataEqual(BatchUtil.getObjectMapper(), amJurisdictionData, reconcileData)) {
            jobPassed = false;
            notes = notes.concat(ReconQuery.CHECK_JURISDICTION_DATA.getKey());
        }

        if (!isRoleDataEqual(BatchUtil.getObjectMapper(), amRoleNameData, reconcileData)) {
            jobPassed = false;
            notes = notes.concat(ReconQuery.CHECK_ROLENAME_DATA.getKey());
        }

        setJobExitStatus(jobPassed, contribution);

        reconcileData.setTotalCountFromAm(totalCountFromRoleAssignment);
        reconcileData.setStatus(jobPassed ? ReconQuery.PASSED.getKey() : ReconQuery.FAILED.getKey());
        reconcileData.setNotes(StringUtils.hasText(notes) ? notes : ReconQuery.SUCCESS_STATUS.getKey());
        reconDataService.saveReconciliationData(reconcileData);

        log.info("End the reconciliation for job id: {}", jobId);
        return RepeatStatus.FINISHED;
    }

    private boolean isRoleDataEqual(ObjectMapper mapper, String amRoleNameData,
                                    ReconciliationData reconcileData) throws JsonProcessingException {
        log.info("Am Role Name Data");
        log.info(String.valueOf(mapper.readTree(amRoleNameData)));
        log.info("CCD Role Name Data");
        log.info(String.valueOf(mapper.readTree(reconcileData.getCcdRoleNameData())));
        boolean isRoleDataEqual = (mapper.readTree(amRoleNameData))
            .equals(mapper.readTree(reconcileData.getCcdRoleNameData()));
        log.info("is Role data equals: " + isRoleDataEqual);
        return isRoleDataEqual;
    }

    private boolean isJurisdictionDataEqual(ObjectMapper mapper, String amJurisdictionData,
                                            ReconciliationData reconcileData) throws JsonProcessingException {
        log.info("AM Jurisdiction Data");
        log.info(String.valueOf(mapper.readTree(amJurisdictionData)));
        log.info("CCD Jurisdiction Data");
        log.info(String.valueOf(mapper.readTree(reconcileData.getCcdJurisdictionData())));
        boolean isJurisdictionDataEqual = (mapper.readTree(amJurisdictionData))
            .equals(mapper.readTree(reconcileData.getCcdJurisdictionData()));
        log.info("isEquals:" + isJurisdictionDataEqual);
        return isJurisdictionDataEqual;
    }

    private void setJobExitStatus(boolean status, StepContribution contribution) {
        if (!status) {
            log.error(ReconQuery.MIGRATION_JOB_FAILED.getKey());
            contribution.setExitStatus(ExitStatus.FAILED);
        } else {
            log.error(ReconQuery.SUCCESS_STATUS.getKey());
        }
    }
}
