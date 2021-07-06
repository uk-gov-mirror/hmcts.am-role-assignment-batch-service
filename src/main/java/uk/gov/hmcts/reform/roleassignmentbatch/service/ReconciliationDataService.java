package uk.gov.hmcts.reform.roleassignmentbatch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.domain.model.AmJurisdiction;
import uk.gov.hmcts.reform.domain.model.AmJurisdictionItem;
import uk.gov.hmcts.reform.domain.model.AmRoleName;
import uk.gov.hmcts.reform.domain.model.AmRoleNameItem;
import uk.gov.hmcts.reform.domain.model.CcdJurisdiction;
import uk.gov.hmcts.reform.domain.model.CcdJurisdictionItem;
import uk.gov.hmcts.reform.domain.model.CcdRoleName;
import uk.gov.hmcts.reform.domain.model.CcdRoleNameItem;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ReconQuery;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils.convertValueJsonNode;

@Service
@Slf4j
public class ReconciliationDataService {

    private final JdbcTemplate jdbcTemplate;

    public ReconciliationDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int populateTotalRecord(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Map<String, Object>> groupByFieldNameAndCount(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * This generic method is used group by key with count and return with Json string.
     *
     * @param source
     * @param type
     * @return
     */
    public String populateAsJsonData(List<Map<String, Object>> source, String type) {
        List<CcdJurisdictionItem> ccdJurisdictionItemList = new ArrayList<>();
        List<CcdRoleNameItem> ccdRoleNameItemList = new ArrayList<>();
        List<AmJurisdictionItem> amJurisdictionItemList = new ArrayList<>();
        List<AmRoleNameItem> amRoleNameItemList = new ArrayList<>();
        long sum = 0;
        String result = null;
        for (Map<String, Object> objectMap : source) {
            long count = (long) objectMap.get("count");
            sum = sum + count;
            switch (type) {
                case "jurisdiction":
                    CcdJurisdictionItem ccdJurisdictionItem = CcdJurisdictionItem.builder()
                            .count(Long.valueOf(objectMap.get(ReconQuery.COUNT.getKey()).toString()))
                            .jurisdiction(objectMap.get(ReconQuery.CCD_JURISDICTION_KEY.getKey()).toString())
                            .build();
                    ccdJurisdictionItemList.add(ccdJurisdictionItem);
                    CcdJurisdiction ccdJurisdiction = CcdJurisdiction.builder()
                            .totalCcdJurisdictionsCount(sum)
                            .jurisdictions(ccdJurisdictionItemList).build();
                    result = convertValueJsonNode(ccdJurisdiction).toString();
                    break;
                case "case_role":
                    CcdRoleNameItem ccdRoleNameItem = CcdRoleNameItem.builder()
                            .count(Long.valueOf(objectMap.get(ReconQuery.COUNT.getKey()).toString()))
                            .caseRole(objectMap.get(ReconQuery.CCD_CASE_ROLE_KEY.getKey()).toString())
                            .build();
                    ccdRoleNameItemList.add(ccdRoleNameItem);
                    CcdRoleName ccdRoleName = CcdRoleName.builder()
                            .totalCcdRoleNamesCount(sum)
                            .ccdRoleNames(ccdRoleNameItemList).build();
                    result = convertValueJsonNode(ccdRoleName).toString();
                    break;
                case "caseTypeId":
                    AmJurisdictionItem amJurisdictionItem = AmJurisdictionItem.builder()
                            .count(Long.valueOf(objectMap.get(ReconQuery.COUNT.getKey()).toString()))
                            .caseTypeId(objectMap.get(ReconQuery.AM_JURISDICTION_KEY.getKey()).toString())

                            .build();
                    amJurisdictionItemList.add(amJurisdictionItem);
                    AmJurisdiction amJurisdiction = AmJurisdiction.builder()
                            .totalAmJurisdictionsCount(sum)
                            .jurisdictions(amJurisdictionItemList).build();
                    result = convertValueJsonNode(amJurisdiction).toString();
                    break;
                case "role_name":
                    AmRoleNameItem amRoleNameItem = AmRoleNameItem.builder()
                            .count(Long.valueOf(objectMap.get(ReconQuery.COUNT.getKey())
                                    .toString()))
                            .roleName(objectMap.get(ReconQuery.AM_CASE_ROLE_KEY.getKey())
                                    .toString())
                            .build();
                    amRoleNameItemList.add(amRoleNameItem);
                    AmRoleName amRoleName = AmRoleName.builder()
                            .totalAmRoleNamesCount(sum)
                            .amRoleNames(amRoleNameItemList).build();
                    result = convertValueJsonNode(amRoleName).toString();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
        }
        return result;
    }

    /**
     * This is used to persist data into Reconciliation_table.
     *
     * @param reconciliationData
     * @return
     */
    public int saveReconciliationData(ReconciliationData reconciliationData) {
        int success = jdbcTemplate.update(ReconQuery.INSERT_RECONCILIATION_QUERY.getKey(),
                reconciliationData.getRunId(),
                reconciliationData.getCcdJurisdictionData(),
                reconciliationData.getCcdRoleNameData(),
                reconciliationData.getAmJurisdictionData(),
                reconciliationData.getAmRoleNameData(),
                reconciliationData.getTotalCountFromCcd(),
                reconciliationData.getTotalCountFromAm(),
                reconciliationData.getStatus(),
                reconciliationData.getNotes());
        log.info("Reconciliation data is saved successfully. Please refer final result in Reconciliation_Data table.");
        return success;
    }


}
