package uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums;

public enum FlagsEnum {
    CCD_AM_MIGRATION_MAIN("ccd-am-migration"),
    CCD_AM_MIGRATION_RENAME("ccd-am-migration-rename-tables");

    private final String label;

    FlagsEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
