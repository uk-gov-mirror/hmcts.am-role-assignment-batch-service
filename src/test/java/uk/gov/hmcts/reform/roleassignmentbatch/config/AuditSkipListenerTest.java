package uk.gov.hmcts.reform.roleassignmentbatch.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import uk.gov.hmcts.reform.domain.model.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.helper.TestDataBuilder;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class AuditSkipListenerTest {

    @Mock
    private JdbcBatchItemWriter<AuditFaults> auditWriter;

    @InjectMocks
    AuditSkipListener auditSkipListener = new AuditSkipListener();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyOnSkipInProcess() throws Exception {
        CcdCaseUsers ccdCaseUsers = TestDataBuilder.buildCcdCaseUsers();
        String msg = "Exception";
        Throwable t = new Throwable(msg);

        auditSkipListener.onSkipInProcess(ccdCaseUsers, t);
        Mockito.verify(auditWriter, Mockito.times(1)).write(Mockito.any());
    }

    @Test
    public void verifyOnSkipInRead() {
        Assertions.assertThrows(NullPointerException.class, () -> auditSkipListener.onSkipInRead(null));
    }

    @Test
    public void verifyOnSkipInWrite() throws Exception {
        UUID requestUuid = UUID.randomUUID();
        CcdCaseUsers ccdCaseUsers = TestDataBuilder.buildCcdCaseUsers();
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryEntity(requestUuid, ccdCaseUsers.getUserId(),
                                                                         ccdCaseUsers.getCaseDataId());
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(requestUuid, ccdCaseUsers.getUserId(),
                                                                         ccdCaseUsers.getCaseDataId());
        EntityWrapper entityWrapper = EntityWrapper.builder().ccdCaseUsers(ccdCaseUsers).historyEntity(historyEntity)
                                                   .requestEntity(requestEntity).build();
        String msg = "Exception";
        Throwable t = new Throwable(msg);

        auditSkipListener.onSkipInWrite(entityWrapper, t);
        Mockito.verify(auditWriter, Mockito.times(1)).write(Mockito.any());
    }
}
