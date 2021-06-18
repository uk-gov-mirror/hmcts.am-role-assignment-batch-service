package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EntityWrapper {
    public RequestEntity requestEntity;
    public HistoryEntity roleAssignmentHistoryEntity;
    public RoleAssignmentEntity roleAssignmentEntity;
    public ActorCacheEntity actorCacheEntity;
}
