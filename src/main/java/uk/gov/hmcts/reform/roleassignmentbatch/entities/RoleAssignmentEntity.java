package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

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
public class RoleAssignmentEntity implements Serializable {

    private UUID id;

    private String actorIdType;

    private String actorId;

    private String roleType;

    private String roleName;

    private String classification;

    private String grantType;

    private String roleCategory;

    private boolean readOnly;

    private String beginTime;

    private String endTime;

    private LocalDateTime created;

    private String attributes;

    //@Column(name = "authorisations")
    //@Type(type = "uk.gov.hmcts.reform.roleassignmentbatch.config.GenericArrayUserType")
    //private String[] authorisations

}
