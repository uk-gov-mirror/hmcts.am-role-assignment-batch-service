package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CcdJurisdictionItem {
    private String jurisdiction;
    private long count;
}