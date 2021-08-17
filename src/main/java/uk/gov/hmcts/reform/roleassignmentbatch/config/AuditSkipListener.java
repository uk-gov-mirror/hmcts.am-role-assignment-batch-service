package uk.gov.hmcts.reform.roleassignmentbatch.config;

import java.util.Collections;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.AuditOperationType;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils;

public class AuditSkipListener implements SkipListener<CcdCaseUser, EntityWrapper> {

    private static final Logger LOG = LoggerFactory.getLogger(AuditSkipListener.class);

    @Autowired
    private JdbcBatchItemWriter<AuditFaults> auditWriter;

    @Override
    public void onSkipInRead(Throwable t) {
        LOG.warn("onSkipInRead {}", t.getMessage());
    }

    @SneakyThrows
    @Override
    public void onSkipInProcess(CcdCaseUser item, Throwable t) {
        LOG.warn("onSkipInProcess Item " + item + " was skipped due to: " + t.getMessage());
        var auditFaults = AuditFaults.builder()
                .reason(t.getMessage())
                .failedAt(AuditOperationType.PROCESS.getLabel())
                .ccdUsers(JacksonUtils.convertValueJsonNode(item).toString()).build();
        auditWriter.write(Collections.singletonList(auditFaults));
    }

    @SneakyThrows
    @Override
    public void onSkipInWrite(EntityWrapper item, Throwable t) {
        LOG.warn("onSkipInWrite Item " + item + " was skipped due to: " + t.getMessage());
        var auditFaults = AuditFaults.builder()
                .reason(t.getMessage())
                .failedAt(AuditOperationType.WRITE.getLabel())
                .ccdUsers(JacksonUtils.convertValueJsonNode(item.getCcdCaseUser()).toString())
                .actorCache(JacksonUtils.convertValueJsonNode(item.getActorCacheEntity()).toString())
                .request(JacksonUtils.convertValueJsonNode(item.getRequestEntity()).toString())
                .history(JacksonUtils.convertValueJsonNode(item.getRoleAssignmentHistoryEntity()).toString())
                .live(JacksonUtils.convertValueJsonNode(item.getRoleAssignmentEntity()).toString()).build();
        auditWriter.write(Collections.singletonList(auditFaults));
    }

}
