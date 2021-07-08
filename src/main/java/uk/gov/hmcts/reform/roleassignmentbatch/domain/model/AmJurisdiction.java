package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AmJurisdiction {
    private long totalAmJurisdictionsCount;
    private List<AmJurisdictionItem> jurisdictions;
}