package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReconciliationData {

    @Id
    @Column(name = "run_id", nullable = false)
    private String runId;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "ccd_jurisdiction_data", nullable = false, columnDefinition = "jsonb")
    private String ccdJurisdictionData;

    @Column(name = "ccd_role_name_data", nullable = false, columnDefinition = "jsonb")
    private String ccdRoleNameData;

    @Column(name = "replica_am_jurisdiction_data", nullable = false, columnDefinition = "jsonb")
    private String replicaAmJurisdictionData;

    @Column(name = "am_role_name_data", nullable = false, columnDefinition = "jsonb")
    private String replicaAmRoleNameData;

    @Column(name = "total_count_from_ccd", nullable = false, columnDefinition = "jsonb")
    private int totalCountFromCcd;

    @Column(name = "total_count_from_replica_am", nullable = false, columnDefinition = "jsonb")
    private int totalCountFromAm;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "notes", nullable = false)
    private String notes;
}
