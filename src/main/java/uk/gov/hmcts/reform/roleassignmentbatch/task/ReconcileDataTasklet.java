package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
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
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        log.info("Reconciling the CCD and AM Data.");

        String jobId = contribution.getStepExecution().getJobExecution().getId().toString();
        log.info("Starting the reconciliation for job id: {}", jobId);

        int totalCountFromRoleAssignment = reconDataService.populateTotalRecord(ReconQuery.AM_TOTAL_COUNT.getKey());

        List<Map<String, Object>> groupByAmJurisdiction = reconDataService
            .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_JURISDICTION.getKey());
        List<Map<String, Object>> groupByAmRoleName = reconDataService
            .groupByFieldNameAndCount(ReconQuery.GROUP_BY_AM_CASE_ROLE.getKey());

        String amJurisdictionData = reconDataService
            .populateAsJsonData(groupByAmJurisdiction, ReconQuery.AM_JURISDICTION_KEY.getKey());
        String amRoleNameData = reconDataService
            .populateAsJsonData(groupByAmRoleName, ReconQuery.AM_CASE_ROLE_KEY.getKey());

     ReconciliationData reconcileData = jdbcTemplate.queryForObject(Constants.GET_RECONCILIATION_DATA, new ReconciliationMapper() , jobId);
        System.out.println("am Jurisdiction Data" );
        System.out.println((mapper.readTree(amJurisdictionData)));
        System.out.println("CCD Jurisdiction Data" );
        System.out.println((mapper.readTree(reconcileData.getCcdJurisdictionData())));
       boolean isJurisdictionDataEqual = (mapper.readTree(amJurisdictionData)).equals(mapper.readTree(reconcileData.getCcdJurisdictionData()));
        System.out.println("isEquals:" + isJurisdictionDataEqual);

        System.out.println("Am Role Name Data");
        System.out.println((mapper.readTree(amRoleNameData)));
        System.out.println("CCD Role Name Data");
        System.out.println(mapper.readTree(reconcileData.getCcdRoleNameData()));
        boolean isRoleDataEqual = (mapper.readTree(amRoleNameData)).equals(mapper.readTree(reconcileData.getCcdRoleNameData()));
        System.out.println("is Role data equals: "+ isRoleDataEqual);

        boolean isTotalMatching = totalCountFromRoleAssignment == reconcileData.getTotalCountFromCcd();

        reconcileData.setAmJurisdictionData(amJurisdictionData);
        reconcileData.setAmRoleNameData(amRoleNameData);
        reconcileData.setStatus((isRoleDataEqual && isJurisdictionDataEqual && isTotalMatching)
                                        ? ReconQuery.SUCCESS_STATUS.getKey(): ReconQuery.FAILED_STATUS.getKey());
            /*ReconciliationData reconciliationData =
            ReconciliationData.builder()
                              .runId(jobId)
                              .amJurisdictionData(amJurisdictionData)
                              .amRoleNameData(amRoleNameData)
                              .totalCountFromAm(totalCountFromRoleAssignment)
                              .status(totalCountFromCcdView == totalCountFromRoleAssignment
                                      ? ReconQuery.PASSED.getKey() : ReconQuery.FAILED.getKey())
                              .notes(totalCountFromCcdView == totalCountFromRoleAssignment
                                     ? ReconQuery.SUCCESS_STATUS.getKey() : ReconQuery.FAILED_STATUS.getKey())
                              .build();*/
        reconDataService.saveReconciliationData(reconcileData);

        Integer integer = jdbcTemplate.queryForObject(ReconQuery.AUDIT_FAULTS_TOTAL_COUNT.getKey(), Integer.class);
        //Making Job status as failed if there are any record in Audit_Fault table.
        /*if (!(isRoleDataEqual && isJurisdictionDataEqual && isTotalMatching)) {
            stepExecution.getJobExecution().setStatus(BatchStatus.FAILED);
            log.warn("CCD Migration job is failed. Please check Audit_Fault/reconciliation_data table");*/


        return RepeatStatus.FINISHED;
    }
}
