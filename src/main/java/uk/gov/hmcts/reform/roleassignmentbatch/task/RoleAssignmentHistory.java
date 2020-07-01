package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.sql.Timestamp;
import java.util.UUID;

public class RoleAssignmentHistory {
    private UUID id;
    private UUID requestId;
    private String actorIDType;
    private UUID actorId;
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
        return "RoleAssignmentHistory{" +
                "id=" + id +
                ", requestId=" + requestId +
                ", actorIDType='" + actorIDType + '\'' +
                ", actorId=" + actorId +
                ", roleType='" + roleType + '\'' +
                ", roleName='" + roleName + '\'' +
                ", classification='" + classification + '\'' +
                ", grantType='" + grantType + '\'' +
                ", roleCategory='" + roleCategory + '\'' +
                ", readOnly=" + readOnly +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", reference='" + reference + '\'' +
                ", process='" + process + '\'' +
                ", attributes='" + attributes + '\'' +
                ", notes='" + notes + '\'' +
                ", log='" + log + '\'' +
                ", statusSequence=" + statusSequence +
                ", created=" + created +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getActorIDType() {
        return actorIDType;
    }

    public void setActorIDType(String actorIDType) {
        this.actorIDType = actorIDType;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getRoleCategory() {
        return roleCategory;
    }

    public void setRoleCategory(String roleCategory) {
        this.roleCategory = roleCategory;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Timestamp getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getStatusSequence() {
        return statusSequence;
    }

    public void setStatusSequence(int statusSequence) {
        this.statusSequence = statusSequence;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

}