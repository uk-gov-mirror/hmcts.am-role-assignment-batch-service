package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CcdRoleName implements Serializable {
    private String count;
    private List<CcdRoleNameItem> roleNames;
}