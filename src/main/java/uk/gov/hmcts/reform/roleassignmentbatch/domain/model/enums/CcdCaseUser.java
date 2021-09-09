package uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CcdCaseUser implements Serializable {
    private String rowNo;
    private String reference;
    private String userId;
    private String caseRole;
    private String jurisdiction;
    private String caseType;
    private String roleCategory;
    private String startDate;
}
