package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Entity(name = "role_assignment_history")
//@IdClass(RoleAssignmentIdentity.class)
public class HistoryEntity implements Serializable {

    @Id
    private UUID id;
    @Id
    private String status;

    @Column(name = "actor_id_type", nullable = false)
    private String actorIdType;

    @Column(name = "process")
    private String process;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "reference")
    private String reference;

    @Column(name = "role_type", nullable = false)
    private String roleType;

    @Column(name = "log")
    private String log;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "status_sequence", nullable = false)
    private int sequence;

    @Column(name = "classification", nullable = false)
    private String classification;

    @Column(name = "grant_type", nullable = false)
    private String grantType;

//    @Column(name = "notes", nullable = true, columnDefinition = "jsonb")
//    @Convert(converter = JsonBConverter.class)
//    private JsonNode notes;

    @Column(name = "role_category")
    private String roleCategory;

    @Column(name = "read_only", nullable = false)
    private boolean readOnly;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    private String attributes;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Id
    @Column(name = "request_id")
    private UUID requestId;

//    @Column(name = "authorisations")
//    @Type(type = "uk.gov.hmcts.reform.roleassignmentbatch.config.GenericArrayUserType")
//    private String[] authorisations;

}

