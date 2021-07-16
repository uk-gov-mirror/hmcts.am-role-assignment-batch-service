package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.ReconciliationMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.service.ReconciliationDataService;
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
     * 3. ccd_jurisdiction_data,ccd_role_name_data,am_jurisdiction_data,am_role_name_data
     * updated with grouing by role and total record
     * 4. Migration job status is updated with Failed status if there is any record in Audit_table.
     */

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        log.info("Reconciling the CCD and AM Data.");

        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
        log.info("Starting the reconciliation for job id: {}", jobId);

        List<Map<String, Object>> groupByAmJurisdiction = reconDataService
            .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_JURISDICTION.getKey());
        List<Map<String, Object>> groupByAmRoleName = reconDataService
            .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_CASE_ROLE.getKey());

        String amJurisdictionData = reconDataService
            .populateAsJsonData(groupByAmJurisdiction, ReconQuery.AM_JURISDICTION_KEY.getKey());
        String amRoleNameData = reconDataService
            .populateAsJsonData(groupByAmRoleName, ReconQuery.AM_CASE_ROLE_KEY.getKey());

        ReconciliationData reconcileData = jdbcTemplate.queryForObject(Constants.GET_RECONCILIATION_DATA,
                                                                       new ReconciliationMapper(), jobId);
        log.info("am Jurisdiction Data");
        log.info(String.valueOf((mapper.readTree(amJurisdictionData))));
        log.info("CCD Jurisdiction Data");
        log.info(String.valueOf((mapper.readTree(reconcileData.getCcdJurisdictionData()))));
        boolean isJurisdictionDataEqual = (mapper.readTree(amJurisdictionData))
            .equals(mapper.readTree(reconcileData.getCcdJurisdictionData()));
        log.info("isEquals:" + isJurisdictionDataEqual);

        log.info("Am Role Name Data");
        log.info(String.valueOf((mapper.readTree(amRoleNameData))));
        log.info("CCD Role Name Data");
        log.info(String.valueOf(mapper.readTree(reconcileData.getCcdRoleNameData())));
        boolean isRoleDataEqual = (mapper.readTree(amRoleNameData))
            .equals(mapper.readTree(reconcileData.getCcdRoleNameData()));
        log.info("is Role data equals: " + isRoleDataEqual);

        reconcileData.setAmJurisdictionData(amJurisdictionData);
        reconcileData.setAmRoleNameData(amRoleNameData);

        Integer auditRecords = jdbcTemplate.queryForObject(ReconQuery.AUDIT_FAULTS_TOTAL_COUNT.getKey(), Integer.class);
        int totalCountFromRoleAssignment = reconDataService.populateTotalRecord(ReconQuery.AM_TOTAL_COUNT.getKey());
        boolean status = true;
        String notes = "";
        boolean isTotalMatching = totalCountFromRoleAssignment == reconcileData.getTotalCountFromCcd();
        if (auditRecords != null && auditRecords > 0) {
            status = false;
            notes = ReconQuery.CHECK_AUDIT_TABLE.getKey();
        }

        if (!isTotalMatching) {
            status = false;
            notes = notes.concat(ReconQuery.FAILED_STATUS.getKey());
        }

        if (!isJurisdictionDataEqual) {
            status = false;
            notes = notes.concat(ReconQuery.CHECK_JURISDICTION_DATA.getKey());
        }

        if (!isRoleDataEqual) {
            status = false;
            notes = notes.concat(ReconQuery.CHECK_ROLENAME_DATA.getKey());
        }
        if (!status) {
            setJobExitStatus(contribution);
        }

        reconcileData.setStatus(status ? ReconQuery.PASSED.getKey() : ReconQuery.FAILED.getKey());
        reconcileData.setNotes(StringUtils.hasText(notes) ? notes : ReconQuery.SUCCESS_STATUS.getKey());
        reconDataService.saveReconciliationData(reconcileData);
        return RepeatStatus.FINISHED;
    }

    private void setJobExitStatus(StepContribution contribution) {
        log.error(ReconQuery.MIGRATION_JOB_FAILED.getKey());
        contribution.setExitStatus(ExitStatus.FAILED);
    }
}
