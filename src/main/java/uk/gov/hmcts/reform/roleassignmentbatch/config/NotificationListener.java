package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ReconQuery;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;
import uk.gov.hmcts.reform.roleassignmentbatch.service.ReconciliationDataService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NotificationListener extends JobExecutionListenerSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReconciliationDataService reconDataService;

    /**
     * Reconciliation Logic with below steps.
     * 1. Ccd_view count is validate against am_role_assgnment table.
     * 2. If both count does not match then status column updated with Failed in Reconciliation_data table
     * 3. ccd_jurisdiction_data,ccd_role_name_data,am_jurisdiction_data,am_role_name_data
     * updated with grouing by role and total record
     * 4. Migration job status is updated with Failed status if there is any record in Audit_table.
     *
     * @param jobExecution
     */
    @Override
    @Transactional
    public void afterJob(final JobExecution jobExecution) {

        log.info("!!! Current JOB FINISHED with status {}! Time to verify the results", jobExecution.getStatus());

        JobParameters jobParameters = jobExecution.getJobParameters();
        String jobId = jobParameters.getParameters().get("run.id").toString();

        int totalCountFromCcdView = reconDataService.populateTotalRecord(ReconQuery.CCD_TOTAL_COUNT.getKey());
        int totalCountFromRoleAssignment = reconDataService.populateTotalRecord(ReconQuery.AM_TOTAL_COUNT.getKey());
        List<Map<String, Object>> groupByCcdJurisdiction = reconDataService
                .groupByFieldNameAndCount(ReconQuery.GROUP_BY_CCD_JURISDICTION.getKey());

        List<Map<String, Object>> groupByCcdRoleName = reconDataService
                .groupByFieldNameAndCount(ReconQuery.GROUP_BY_CCD_CASE_ROLE.getKey());
        List<Map<String, Object>> groupByAmJurisdiction = reconDataService
                .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_JURISDICTION.getKey());
        List<Map<String, Object>> groupByAmRoleName = reconDataService
                .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_CASE_ROLE.getKey());

        String ccdJurisdictionData = reconDataService
                .populateAsJsonData(groupByCcdJurisdiction, ReconQuery.CCD_JURISDICTION_KEY.getKey());
        String ccdRoleNameData = reconDataService
                .populateAsJsonData(groupByCcdRoleName, ReconQuery.CCD_CASE_ROLE_KEY.getKey());
        String amJurisdictionData = reconDataService
                .populateAsJsonData(groupByAmJurisdiction, ReconQuery.AM_JURISDICTION_KEY.getKey());
        String amRoleNameData = reconDataService
                .populateAsJsonData(groupByAmRoleName, ReconQuery.AM_CASE_ROLE_KEY.getKey());

        ReconciliationData reconciliationData = ReconciliationData.builder()
                .runId(jobId)
                .ccdJurisdictionData(ccdJurisdictionData)
                .ccdRoleNameData(ccdRoleNameData)
                .amJurisdictionData(amJurisdictionData)
                .amRoleNameData(amRoleNameData)
                .totalCountFromCcd(totalCountFromCcdView)
                .totalCountFromAm(totalCountFromRoleAssignment)
                .status(totalCountFromCcdView == totalCountFromRoleAssignment
                        ? ReconQuery.PASSED.getKey() : ReconQuery.FAILED.getKey())
                .notes(totalCountFromCcdView == totalCountFromRoleAssignment
                        ? ReconQuery.SUCCESS_STATUS.getKey() : ReconQuery.FAILED_STATUS.getKey())
                .build();
        reconDataService.saveReconciliationData(reconciliationData);

        Integer integer = jdbcTemplate.queryForObject(ReconQuery.AUDIT_FAULTS_TOTAL_COUNT.getKey(), Integer.class);
        //Making Job status as failed if there are any record in Audit_Fault table.
        if (integer > 0) {
            jobExecution.setStatus(BatchStatus.FAILED);
            log.info("CCD Migration job is failed. Please check Audit_Fault table");
        }
    }
}
