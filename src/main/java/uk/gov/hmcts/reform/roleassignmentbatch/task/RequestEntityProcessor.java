package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import uk.gov.hmcts.reform.domain.model.CcdCaseUsers;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;

public class RequestEntityProcessor implements ItemProcessor<CcdCaseUsers, RequestEntity> {


    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param ccdCaseUsers to be processed
     * @return potentially modified or new item for continued processing, {@code null} if processing of the
     * provided item should not continue.
     * @throws Exception thrown if exception occurs during processing.
     */
    @Override
    public RequestEntity process(CcdCaseUsers ccdCaseUsers) throws Exception {
        return RequestEntity.builder()
                            //.id("adfadkufhadfhb".toString())
                            .correlationId(UUID.randomUUID().toString())
                            .clientId("ccd_migration")
                            .authenticatedUserId("A fixed Authenticated User Id")
                            .assignerId(ccdCaseUsers.getUserId())
                            .requestType("CREATE")
                            .status("APPROVED")
                            .process("CCD")
                            .replaceExisting(false)
                            .roleAssignmentId(UUID.randomUUID())
                            .reference(ccdCaseUsers.getCaseDataId().concat(ccdCaseUsers.getUserId()))
                            .log(null)
                            .created(LocalDateTime.now())
                            .build();
    }
}