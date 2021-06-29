package uk.gov.hmcts.reform.domain.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CcdCaseUser implements Serializable {
    private String caseDataId;
    private String userId;
    private String caseRole;
    private String jurisdiction;
    private String caseType;
    private String roleCategory;
    private String beginDate;
}
