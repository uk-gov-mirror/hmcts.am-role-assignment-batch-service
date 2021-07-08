package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;

@Getter
@Setter
@Builder
public class EntityWrapper {
    public CcdCaseUser ccdCaseUser;
    public RequestEntity requestEntity;
    public HistoryEntity roleAssignmentHistoryEntity;
    public RoleAssignmentEntity roleAssignmentEntity;
    public ActorCacheEntity actorCacheEntity;
}
