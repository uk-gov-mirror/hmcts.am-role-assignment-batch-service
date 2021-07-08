package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActorCacheEntity implements Serializable {


    private String actorIds;
    private long etag;
    private String roleAssignmentResponse;

}

