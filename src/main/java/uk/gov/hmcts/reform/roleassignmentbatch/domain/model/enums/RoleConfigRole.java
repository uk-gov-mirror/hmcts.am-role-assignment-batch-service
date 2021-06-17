package uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums;

import lombok.Getter;
import lombok.Value;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleCategory;

import java.util.Set;

@Value
@Getter
public class RoleConfigRole {

    private final String name;
    private final String label;
    private final String description;
    private final RoleCategory category;
    private final Set<RoleConfigPattern> patterns;
}
