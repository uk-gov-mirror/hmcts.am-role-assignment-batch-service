package uk.gov.hmcts.reform.roleassignmentbatch.processors;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.JacksonUtils.convertValueJsonNode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.item.ItemProcessor;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Status;
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
        attributes.put("jurisdiction", convertValueJsonNode(ccdCaseUser.getJurisdiction()));
        String reference = ccdCaseUser.getCaseDataId().concat("-").concat(ccdCaseUser.getUserId());

        RequestEntity requestEntity = RequestEntity.builder()
                                                   .id(requestUuid)
                                                   .correlationId(UUID.randomUUID().toString())
                                                   .clientId("ccd_gw")
                                                   .authenticatedUserId("ccd_migration")
                                                   .assignerId("ccd_migration")
                                                   .requestType("CREATE")
                                                   .status("APPROVED")
                                                   .process("CCD")
                                                   .replaceExisting(false)
                                                   //.roleAssignmentId(UUID.randomUUID())
                                                   .reference(reference)
                                                   .log("This is a migrated record from CCD.")
                                                   .created(LocalDateTime.now())
                                                   .build();
        UUID assignmentId = UUID.randomUUID();
        HistoryEntity roleAssignmentHistoryEntity =
            HistoryEntity.builder()
                         .id(assignmentId)
                         .requestId(requestUuid)
                         .status(Status.LIVE.name())
                         .actorId(ccdCaseUser.getUserId())
                         .actorIdType("IDAM")
                         .process("CCD")
                         .reference(reference)
                         .roleType(RoleType.CASE.name())
                         .roleName(ccdCaseUser.getCaseRole())
                         .sequence(1)
                         .classification(Classification.RESTRICTED.name())
                         .grantType(GrantType.SPECIFIC.name())
                         .roleCategory(ccdCaseUser.getRoleCategory())
                         .readOnly(false)
                         .beginTime(ccdCaseUser.getBeginDate())
                         .created(LocalDateTime.now())
                         .log("This is a migrated record from CCD.")
                         .attributes(convertValueJsonNode(attributes).toString())
                         .build();
        RoleAssignmentEntity roleAssignmentEntity =
            RoleAssignmentEntity.builder()
                                .id(assignmentId)
                                .actorIdType("IDAM")
                                .actorId(ccdCaseUser.getUserId())
                                .roleType(RoleType.CASE.name())
                                .roleName(ccdCaseUser.getCaseRole())
                                .classification(Classification.RESTRICTED.name())
                                .grantType(GrantType.SPECIFIC.name())
                                .roleCategory(ccdCaseUser.getRoleCategory())
                                .readOnly(false)
                                .beginTime(ccdCaseUser.getBeginDate())
                                .created(LocalDateTime.now())
                                .attributes(convertValueJsonNode(attributes).toString())
                                .build();

        /*ActorCacheEntity actorCacheEntity =
            ActorCacheEntity.builder()
                            .actorIds(ccdCaseUser.getUserId()) //using random as dummy data violates unique key rule
                            .etag(0L)
                            .roleAssignmentResponse("{}")
                            .build()*/
        return EntityWrapper.builder()
                .ccdCaseUser(ccdCaseUser)
                //.actorCacheEntity(actorCacheEntity)
                .requestEntity(requestEntity)
                .roleAssignmentHistoryEntity(roleAssignmentHistoryEntity)
                .roleAssignmentEntity(roleAssignmentEntity)
                .build();

             

    }
}