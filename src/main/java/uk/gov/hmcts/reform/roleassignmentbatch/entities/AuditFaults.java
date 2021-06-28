package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuditFaults implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "failed_at")
    private String failedAt;

    private String reason;

    @Column(name = "ccd_users")
    private String ccdUsers;

    private String request;

    private String history;

    private String live;
}
