package uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums;

public enum FlagsEnum {

    GET_LD_FLAG("get-ld-flag");

    private final String label;

    FlagsEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
