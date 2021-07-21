package uk.gov.hmcts.reform.roleassignmentbatch.service;

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
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;
import uk.gov.hmcts.reform.roleassignmentbatch.exception.EmailSendFailedException;
import uk.gov.hmcts.reform.roleassignmentbatch.exception.NoReconciliationDataFound;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.ReconciliationMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * This method is used to send CCD migration status via email.
     *
     * @param runId
     * @param emailSubject
     * @return
     */
    @SneakyThrows
    @Override
    public Response sendEmail(String runId, String emailSubject) {
        Response response = null;
        response = sendEmail(mailTo, emailSubject, runId);
        if (response!=null && !HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
            EmailSendFailedException emailSendFailedException = new EmailSendFailedException(new HttpException(String.format(
                    "SendGrid returned a non-success response (%d); body: %s",
                    response.getStatusCode(),
                    response.getBody()
            )));
            log.error("", emailSendFailedException);
        }
        return response;
    }

    /***
     * Sendgrid mail logic.
     *
     * @param emailTo
     * @param emailSubject
     * @param runId
     * @return
     */
    public Response sendEmail(List<String> emailTo, String emailSubject, String runId) throws IOException {
        Response response = null;
        if (mailEnabled) {
            String concatEmailSubject = environmentName.concat("::" + emailSubject);
            var personalization = new Personalization();
            emailTo.forEach(email -> personalization.addTo(new Email(email)));
            Content content = new Content("text/html", buildThymeleafTemplate(runId));
            Mail mail = new Mail();
            mail.setFrom(new Email(mailFrom));
            mail.setSubject(concatEmailSubject);
            mail.addContent(content);
            mail.addPersonalization(personalization);
            var request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            return sendGrid.api(request);

        } else {
            log.info("Email alert has been disabled during CCD Migration");
        }
        return response;
    }

    /**
     * This is used to build thymeleafe template
     *
     * @param runId
     * @return
     */
    private String buildThymeleafTemplate(String runId) {
        ReconciliationData reconData = jdbcTemplate.queryForObject(Constants.GET_RECONCILIATION_DATA,
                new ReconciliationMapper(), runId);
        if (reconData == null) {
            throw new NoReconciliationDataFound(String.format(Constants.NO_RECONCILIATION_DATA_FOUND, runId));
        }
        Context context = new Context();
        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("runId", reconData.getRunId());
        templateMap.put("createdDate", reconData.getCreatedDate());
        templateMap.put("ccdJurisdictionData", reconData.getCcdJurisdictionData());
        templateMap.put("ccdRoleNameData", reconData.getCcdRoleNameData());
        templateMap.put("amJurisdictionData", reconData.getAmJurisdictionData());
        templateMap.put("amRoleNameData", reconData.getAmRoleNameData());
        templateMap.put("totalCountFromCcd", reconData.getTotalCountFromCcd());
        templateMap.put("totalCountFromAm", reconData.getTotalCountFromAm());
        templateMap.put("status", reconData.getStatus());
        templateMap.put("notes", reconData.getNotes());
        context.setVariables(templateMap);
        return templateEngine.process("email.html", context);
    }
}