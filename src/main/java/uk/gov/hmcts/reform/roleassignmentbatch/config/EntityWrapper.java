package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;

@Getter
@Setter
@Builder
public class EntityWrapper {
    public Newtable newtable;
    public RequestEntity requestEntity;
}
