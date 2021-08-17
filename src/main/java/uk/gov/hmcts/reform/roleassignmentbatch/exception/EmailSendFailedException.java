package uk.gov.hmcts.reform.roleassignmentbatch.exception;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailSendFailedException(Throwable cause) {
        super(cause);
    }
}