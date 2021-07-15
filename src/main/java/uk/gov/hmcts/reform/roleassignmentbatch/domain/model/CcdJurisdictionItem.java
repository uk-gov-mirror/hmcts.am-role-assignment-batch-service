package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CcdJurisdictionItem {
    private String jurisdiction;
    private String count;
}