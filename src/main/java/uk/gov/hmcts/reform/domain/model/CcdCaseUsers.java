package uk.gov.hmcts.reform.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CcdCaseUsers {
    private String caseDataId;
    private String userId;
    private String caseRole;
    private String jurisdiction;
    private String caseType;
    private String roleCategory;
}
