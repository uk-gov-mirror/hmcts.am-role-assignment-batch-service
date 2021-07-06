package uk.gov.hmcts.reform.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class AmJurisdiction {
    private long totalAmJurisdictionsCount;
    private List<AmJurisdictionItem> jurisdictions;
}