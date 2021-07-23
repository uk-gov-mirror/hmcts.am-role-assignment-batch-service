package uk.gov.hmcts.reform.roleassignmentbatch.service;

import com.sendgrid.Response;

public interface EmailService {
    public Response sendEmail(String jobId, String emailSubject);
}
