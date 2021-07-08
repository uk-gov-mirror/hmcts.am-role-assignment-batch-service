package uk.gov.hmcts.reform.domain.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CcdJurisdiction {
    private long totalCcdJurisdictionsCount;
    private List<uk.gov.hmcts.reform.domain.model.CcdJurisdictionItem> jurisdictions;
}