package uk.gov.hmcts.reform.domain.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CcdRoleName {
    private long totalCcdRoleNamesCount;
    private List<uk.gov.hmcts.reform.domain.model.CcdRoleNameItem> ccdRoleNames;
}