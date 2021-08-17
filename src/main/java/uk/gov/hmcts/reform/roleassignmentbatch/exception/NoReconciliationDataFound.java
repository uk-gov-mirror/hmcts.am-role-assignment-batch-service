package uk.gov.hmcts.reform.roleassignmentbatch.exception;

public class NoReconciliationDataFound extends RuntimeException {
    public NoReconciliationDataFound(String message) {
        super(message);
    }
}
