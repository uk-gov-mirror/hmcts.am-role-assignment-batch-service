package uk.gov.hmcts.reform.roleassignmentbatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;
import uk.gov.hmcts.reform.roleassignmentbatch.exception.EmailSendFailedException;
import uk.gov.hmcts.reform.roleassignmentbatch.exception.NoReconciliationDataFound;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.ReconciliationMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DELETE_EXPIRED_JOB;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.RECONCILIATION;

/**
 * This class sends emails to intended recipients for ccd migration process
 * with detailed reason of reconciliation data.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.mail.from}")
    private String mailFrom;

    @Value("${spring.mail.to}")
    private List<String> mailTo;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${ENV_NAME:''}")
    private String environmentName;

    @Autowired
    private SendGrid sendGrid;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SpringTemplateEngine templateEngine;



    /**
     * Generic Method is used to send mail notification to the caller.
     *
     * @param emailData EmailData as parameter
     * @return Sedngrid response
     */
    @Override
    @SneakyThrows
    public Response sendEmail(EmailData emailData) {
        Response response = null;
        if (mailEnabled) {
            String concatEmailSubject = environmentName.concat("::" + emailData.getEmailSubject());
            var personalization = new Personalization();
            mailTo.forEach(email -> personalization.addTo(new Email(email)));
            Context context = new Context();
            Content content = null;
            emailData.setEmailTo(mailTo);
            emailData.setEmailSubject(concatEmailSubject);
            emailData.setRunId(emailData.getRunId());
            if (RECONCILIATION.equals(emailData.getModule())) {
                emailData.setTemplateMap(reconThymeleafTemplate(emailData.getRunId()));
                context.setVariables(emailData.getTemplateMap());
                String process = templateEngine.process("recon-email.html", context);
                content = new Content("text/html", process);
            }
            if (DELETE_EXPIRED_JOB.equals(emailData.getModule())) {
                emailData.setTemplateMap(emailData.getTemplateMap());
                context.setVariables(emailData.getTemplateMap());
                String process = templateEngine.process("delete-count.html", context);
                content = new Content("text/html", process);
            }
            Mail mail = new Mail();
            mail.setFrom(new Email(mailFrom));
            mail.setSubject(concatEmailSubject);
            mail.addContent(content);
            mail.addPersonalization(personalization);
            var request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = sendGrid.api(request);
            if (response != null && !HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                EmailSendFailedException emailSendFailedException = new EmailSendFailedException(
                        new HttpException(String.format(
                        "SendGrid returned a non-success response (%d); body: %s",
                        response.getStatusCode(),
                        response.getBody()
                )));
                log.error("{}", emailSendFailedException);
            }
        }
        return response;
    }


    /**
     * Thymeleaf builder template for reconciliation mail notification.
     *
     * @param runId JobId is the parameter
     * @return
     */
    private Map<String, Object> reconThymeleafTemplate(String runId) {
        ReconciliationData reconData = jdbcTemplate.queryForObject(Constants.GET_LATEST_RECONCILIATION_DATA,
                new ReconciliationMapper());
        if (reconData == null) {
            throw new NoReconciliationDataFound(String.format(Constants.NO_RECONCILIATION_DATA_FOUND, runId));
        }

        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("runId", reconData.getRunId());
        templateMap.put("createdDate", reconData.getCreatedDate());
        templateMap.put("ccdJurisdictionData", reconData.getCcdJurisdictionData());
        templateMap.put("ccdRoleNameData", reconData.getCcdRoleNameData());
        templateMap.put("amJurisdictionData", reconData.getReplicaAmJurisdictionData());
        templateMap.put("amRoleNameData", reconData.getReplicaAmRoleNameData());
        templateMap.put("totalCountFromCcd", reconData.getTotalCountFromCcd());
        templateMap.put("totalCountFromAm", reconData.getTotalCountFromAm());
        templateMap.put("status", reconData.getStatus());
        templateMap.put("notes", reconData.getNotes());
        templateMap.put("amRecordsBeforeMigration", reconData.getAmRecordsBeforeMigration());
        templateMap.put("amRecordsAfterMigration", reconData.getAmRecordsAfterMigration());
        return templateMap;
    }
}