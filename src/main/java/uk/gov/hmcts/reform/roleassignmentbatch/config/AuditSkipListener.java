package uk.gov.hmcts.reform.roleassignmentbatch.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.domain.model.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.AuditOperationType;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;

import javax.annotation.PostConstruct;
import java.util.Collections;

public class AuditSkipListener implements SkipListener<CcdCaseUser, EntityWrapper> {

    private static final Logger LOG = LoggerFactory.getLogger(AuditSkipListener.class);

    @Autowired
    private JdbcBatchItemWriter<AuditFaults> auditWriter;

    ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        LOG.warn("onSkipInRead " + t.getMessage());
    }

    @SneakyThrows
    @Override
    public void onSkipInProcess(CcdCaseUser item, Throwable t) {
        LOG.warn("onSkipInProcess Item " + item + " was skipped due to: " + t.getMessage());
        AuditFaults auditFaults = new AuditFaults();
        auditFaults.setReason(t.getMessage());
        auditFaults.setFailedAt(AuditOperationType.PROCESS.getLabel());
        auditFaults.setCcdUsers(getJson(item));
        auditWriter.write(Collections.singletonList(auditFaults));
    }

    @SneakyThrows
    @Override
    public void onSkipInWrite(EntityWrapper item, Throwable t) {
        LOG.warn("onSkipInWrite Item " + item + " was skipped due to: " + t.getMessage());
        AuditFaults auditFaults = new AuditFaults();
        auditFaults.setReason(t.getMessage());
        auditFaults.setFailedAt(AuditOperationType.WRITE.getLabel());
        auditFaults.setCcdUsers(getJson(item.getCcdCaseUser()));
        auditFaults.setRequest(getJson(item.getRequestEntity()));
        auditFaults.setHistory(getJson(item.getHistoryEntity()));
        auditWriter.write(Collections.singletonList(auditFaults));
    }

    private String getJson(Object item) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
        } catch (JsonProcessingException e) {
            LOG.warn("failed conversion: Pfra object to Json", e);
        }
        return null;
    }

}
