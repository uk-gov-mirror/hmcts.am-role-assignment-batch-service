package uk.gov.hmcts.reform.roleassignmentbatch.entities;


import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RoleAssignmentIdentity implements Serializable {

    private UUID id;

    private String status;

    private UUID requestEntity;
}

