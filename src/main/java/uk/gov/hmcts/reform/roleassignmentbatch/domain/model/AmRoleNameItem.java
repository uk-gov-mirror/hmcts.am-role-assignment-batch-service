package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AmRoleNameItem {
    private String roleName;
    private String count;
}