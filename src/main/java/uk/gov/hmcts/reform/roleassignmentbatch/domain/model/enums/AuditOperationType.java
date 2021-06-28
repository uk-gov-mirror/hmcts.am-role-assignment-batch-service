package uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums;

public enum AuditOperationType {
    READ("Reading assignments"),
    PROCESS("Processing assignments"),
    WRITE("Writing assignments");

    private final String label;

    AuditOperationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
