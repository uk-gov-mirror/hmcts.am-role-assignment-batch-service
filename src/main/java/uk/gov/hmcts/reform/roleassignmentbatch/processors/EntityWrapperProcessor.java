package uk.gov.hmcts.reform.roleassignmentbatch.processors;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils.convertValueJsonNode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.hmcts.reform.domain.model.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentEntity;


public class EntityWrapperProcessor implements ItemProcessor<CcdCaseUser, EntityWrapper> {


    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param ccdCaseUser to be processed
     * @return potentially modified or new item for continued processing, {@code null} if processing of the
     *     provided item should not continue.
     * @throws Exception thrown if exception occurs during processing.
     */
    @Override
    public EntityWrapper process(CcdCaseUser ccdCaseUser) throws Exception {
        UUID requestUuid = UUID.randomUUID();
        Map<String, JsonNode> attributes = new HashMap<>();
        attributes.put("caseId", convertValueJsonNode(ccdCaseUser.getCaseDataId()));
        attributes.put("caseTypeId", convertValueJsonNode(ccdCaseUser.getCaseType()));
        RequestEntity requestEntity = RequestEntity.builder()
                                                   .id(requestUuid)
                                                   .correlationId(UUID.randomUUID().toString())
                                                   .clientId("ccd")
                                                   .authenticatedUserId("A fixed Authenticated User Id")
                                                   .assignerId(ccdCaseUser.getUserId())
                                                   .requestType("CREATE")
                                                   .status("APPROVED")
                                                   .process("CCD")
                                                   .replaceExisting(false)
                                                   .roleAssignmentId(UUID.randomUUID())
                                                   .reference(ccdCaseUser.getCaseDataId()
                                                                         .concat(ccdCaseUser.getUserId()))
                                                   .log(null)
                                                   .created(LocalDateTime.now())
                                                   .build();
        HistoryEntity roleAssignmentHistoryEntity =
            HistoryEntity.builder()
                         .id(UUID.randomUUID())
                         .status(Status.APPROVED.name())
                         .requestId(requestUuid)
                         .actorId(ccdCaseUser.getUserId())
                         .actorIdType("IDAM")
                         .roleType(RoleType.CASE.name())
                         .roleName(ccdCaseUser.getCaseRole())
                         .sequence(1)
                         .classification("CLASSIFIED")
                         .grantType(GrantType.STANDARD.name())
                         .roleCategory(RoleCategory.JUDICIAL.name())
                         .readOnly(false)
                         .created(LocalDateTime.now())
                         .attributes(convertValueJsonNode(attributes).toString())
                         .build();
        RoleAssignmentEntity roleAssignmentEntity =
            RoleAssignmentEntity.builder()
                                .id(requestUuid)
                                .actorIdType("IDAM")
                                .actorId(ccdCaseUser.getUserId())
                                .roleType(RoleType.CASE.name())
                                .roleName(ccdCaseUser.getCaseRole())
                                .classification("CLASSIFIED")
                                .grantType(GrantType.STANDARD.name())
                                .roleCategory(RoleCategory.JUDICIAL.name())
                                .readOnly(false)
                                .created(LocalDateTime.now())
                                .attributes(convertValueJsonNode(attributes).toString())
                                .build();
        ActorCacheEntity actorCacheEntity =
            ActorCacheEntity.builder()
                            .actorIds(ccdCaseUser.getUserId()) //using random as dummy data violates unique key rule
                            .etag(0L)
                            .roleAssignmentResponse(convertValueJsonNode(attributes).toString())
                            .build();
        return EntityWrapper.builder()

                .ccdCaseUser(ccdCaseUser)
                .actorCacheEntity(actorCacheEntity)
                .requestEntity(requestEntity)
                .roleAssignmentHistoryEntity(roleAssignmentHistoryEntity)
                .roleAssignmentEntity(roleAssignmentEntity)
                .build();

             

    }
}