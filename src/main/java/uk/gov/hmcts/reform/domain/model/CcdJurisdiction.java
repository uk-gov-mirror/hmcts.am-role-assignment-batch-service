package uk.gov.hmcts.reform.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class CcdJurisdiction {
    private long totalCcdJurisdictionsCount;
    private List<CcdJurisdictionItem> jurisdictions;
}