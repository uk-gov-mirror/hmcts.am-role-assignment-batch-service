package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CcdJurisdiction {
    private String count;
    private List<CcdJurisdictionItem> jurisdictions;
}