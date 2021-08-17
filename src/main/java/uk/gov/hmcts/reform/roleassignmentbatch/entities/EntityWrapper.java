package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;

@Getter
@Setter
@Builder
public class EntityWrapper {
    private CcdCaseUser ccdCaseUser;
    private RequestEntity requestEntity;
    private HistoryEntity roleAssignmentHistoryEntity;
    private RoleAssignmentEntity roleAssignmentEntity;
    private ActorCacheEntity actorCacheEntity;
}
