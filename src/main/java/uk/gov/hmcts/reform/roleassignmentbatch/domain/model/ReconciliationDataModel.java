package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReconciliationDataModel implements Serializable {
    private String runId;
    private LocalDateTime createdDate;
    private CcdJurisdiction ccdJurisdictionData;
    private CcdRoleName ccdRoleNameData;
    private AmJurisdiction amJurisdictionData;
    private AmRoleName amRoleNameData;
    private int totalCountFromCcd;
    private int totalCountFromAm;
    private String status;
    private String notes;
}
