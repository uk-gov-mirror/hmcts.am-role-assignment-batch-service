package uk.gov.hmcts.reform.roleassignmentbatch.processors;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import uk.gov.hmcts.reform.domain.model.CcdCaseUsers;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;


public class EntityWrapperProcessor implements ItemProcessor<CcdCaseUsers, EntityWrapper> {


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
    public EntityWrapper process(CcdCaseUsers ccdCaseUsers) throws Exception {
        UUID requestUuid = UUID.randomUUID();
        Newtable newtable = Newtable.builder().myid(requestUuid.toString()).column2(requestUuid.toString()).build();
        RequestEntity requestEntity = RequestEntity.builder()
                                                   .id(requestUuid)
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
        return EntityWrapper.builder()
                            .newtable(newtable)
                            .requestEntity(requestEntity)
                            .build();
    }
}