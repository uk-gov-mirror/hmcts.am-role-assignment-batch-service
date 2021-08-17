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
public class HistoryEntity implements Serializable {


    private UUID id;

    private String status;

    private UUID requestId;

    private String actorIdType;

    private String process;

    private String actorId;

    private String reference;

    private String roleType;

    private String log;

    private String roleName;

    private int sequence;

    private String classification;

    private String grantType;

    private String notes;

    private String roleCategory;

    private boolean readOnly;

    private LocalDateTime created;

    private String beginTime;

    private String attributes;

    private LocalDateTime endTime;

    //@Column(name = "authorisations")
    // @Type(type = "uk.gov.hmcts.reform.roleassignmentbatch.config.GenericArrayUserType")
    //private String[] authorisations

}

