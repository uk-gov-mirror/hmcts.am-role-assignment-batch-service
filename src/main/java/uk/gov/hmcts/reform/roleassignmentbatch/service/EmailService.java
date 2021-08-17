package uk.gov.hmcts.reform.roleassignmentbatch.service;

import com.sendgrid.Response;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.EmailData;

public interface EmailService {
    public Response sendEmail(EmailData emailData);

}
