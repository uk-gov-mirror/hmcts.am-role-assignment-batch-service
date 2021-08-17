package uk.gov.hmcts.reform.roleassignmentbatch.entities;

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
public class RequestEntity {

    private UUID id;

    private String correlationId;

    private String clientId;

    private String authenticatedUserId;

    private String assignerId;

    private String requestType;

    private String status;

    private String process;

    private String reference;

    private Boolean replaceExisting;

    private UUID roleAssignmentId;

    private String log;

    private LocalDateTime created;

}

