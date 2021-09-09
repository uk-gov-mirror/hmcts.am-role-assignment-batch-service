package uk.gov.hmcts.reform.roleassignmentbatch.service;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils.convertValueJsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.AmJurisdiction;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.AmJurisdictionItem;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.AmRoleName;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.AmRoleNameItem;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.CcdJurisdiction;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.CcdJurisdictionItem;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.CcdRoleName;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.CcdRoleNameItem;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ReconQuery;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;

@Service
@Slf4j
public class ReconciliationDataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int populateTotalRecord(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Map<String, Object>> groupByFieldNameAndCount(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * This generic method is used group by key with count and return with Json string.
     *
     * @param source source
     * @param type type
     * @return String
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
                            .count(objectMap.get(ReconQuery.COUNT.getKey()).toString())
                            .jurisdiction(objectMap.get(ReconQuery.CCD_JURISDICTION_KEY.getKey()).toString())
                            .build();
                    ccdJurisdictionItemList.add(ccdJurisdictionItem);
                    CcdJurisdiction ccdJurisdiction = CcdJurisdiction.builder()
                                                                     .count(String.valueOf(sum))
                                                                     .jurisdictions(ccdJurisdictionItemList).build();
                    result = convertValueJsonNode(ccdJurisdiction).toString();
                    break;
                case "case_role":
                    CcdRoleNameItem ccdRoleNameItem = CcdRoleNameItem.builder()
                            .count(objectMap.get(ReconQuery.COUNT.getKey()).toString())
                            .roleName(objectMap.get(ReconQuery.CCD_CASE_ROLE_KEY.getKey()).toString())
                            .build();
                    ccdRoleNameItemList.add(ccdRoleNameItem);
                    CcdRoleName ccdRoleName = CcdRoleName.builder()
                                                         .count(String.valueOf(sum))
                                                         .roleNames(ccdRoleNameItemList)
                                                         .build();
                    result = convertValueJsonNode(ccdRoleName).toString();
                    break;
                case "caseType":
                    AmJurisdictionItem amJurisdictionItem = AmJurisdictionItem.builder()
                            .count(objectMap.get(ReconQuery.COUNT.getKey()).toString())
                            .jurisdiction(objectMap.get(ReconQuery.CASE_TYPE.getKey()).toString())

                            .build();
                    amJurisdictionItemList.add(amJurisdictionItem);
                    AmJurisdiction amJurisdiction = AmJurisdiction.builder()
                                                                  .count(String.valueOf(sum))
                                                                  .jurisdictions(amJurisdictionItemList).build();
                    result = convertValueJsonNode(amJurisdiction).toString();
                    break;
                case "role_name":
                    AmRoleNameItem amRoleNameItem = AmRoleNameItem.builder()
                            .count(objectMap.get(ReconQuery.COUNT.getKey()).toString())
                            .roleName(objectMap.get(ReconQuery.ROLE_NAME.getKey())
                                    .toString())
                            .build();
                    amRoleNameItemList.add(amRoleNameItem);
                    AmRoleName amRoleName = AmRoleName.builder()
                                                      .count(String.valueOf(sum))
                                                      .roleNames(amRoleNameItemList).build();
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
     * @param reconciliationData reconciliationData
     * @return int
     */
    public int saveReconciliationData(ReconciliationData reconciliationData) {
        int success = jdbcTemplate.update(ReconQuery.INSERT_RECONCILIATION_QUERY.getKey(),
                reconciliationData.getRunId(),
                reconciliationData.getCcdJurisdictionData(),
                reconciliationData.getCcdRoleNameData(),
                reconciliationData.getReplicaAmJurisdictionData(),
                reconciliationData.getReplicaAmRoleNameData(),
                reconciliationData.getTotalCountFromCcd(),
                reconciliationData.getTotalCountFromAm(),
                reconciliationData.getStatus(),
                reconciliationData.getNotes(),
                reconciliationData.getAmRecordsBeforeMigration(),
                reconciliationData.getAmRecordsAfterMigration());
        log.info("Reconciliation data is saved successfully. Please refer final result in Reconciliation_Data table.");
        return success;
    }


}
