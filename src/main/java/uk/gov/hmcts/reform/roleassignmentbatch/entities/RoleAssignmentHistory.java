package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoleAssignmentHistory {

    private String id;
    private UUID requestId;
    private String actorIDType;
    private String actorId;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private boolean readOnly;
    private Timestamp beginTime;
    private Timestamp endTime;
    private String status;
    private String reference;
    private String process;
    private String attributes;
    private String notes;
    private String log;
    private int statusSequence;
    private Timestamp created;

    @Override
    public String toString() {
        return "RoleAssignmentHistory{"
                + "id=" + id
                + ", requestId=" + requestId
                + ", actorIDType='" + actorIDType + '\''
                + ", actorId=" + actorId
                + ", roleType='" + roleType + '\''
                + ", roleName='" + roleName + '\''
                + ", classification='" + classification + '\''
                + ", grantType='" + grantType + '\''
                + ", roleCategory='" + roleCategory + '\''
                + ", readOnly=" + readOnly
                + ", beginTime=" + beginTime
                + ", endTime=" + endTime
                + ", status='" + status + '\''
                + ", reference='" + reference + '\''
                + ", process='" + process + '\''
                + ", attributes='" + attributes + '\''
                + ", notes='" + notes + '\''
                + ", log='" + log + '\''
                + ", statusSequence=" + statusSequence
                + ", created=" + created
                + '}';
    }

}
