package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.UUID;

public class RoleAssignmentHistory {
    private UUID id;
    private UUID requestID, actorID;

    public RoleAssignmentHistory(UUID id, UUID requestID, UUID actorID) {
        this.id = id;
        this.requestID = requestID;
        this.actorID = actorID;
    }

    public void  setID(UUID id){
        this.id=id;
    }
    public UUID  getID(){
        return id;
    }

    public void  setRequestID(UUID requestID){
        this.requestID=requestID;
    }
    public UUID  getRequestID(){
        return requestID;
    }

    public void  setActorID(UUID actorID){
        this.actorID=actorID;
    }

    public UUID  getActorID(){
        return actorID;
    }

    @Override
    public String toString() {
        return String.format("RoleAssignmentHistory[id=%s, requestID='%s', actorID='%s']", id.toString(), requestID.toString(), actorID.toString());
        //return String.format("Hello");
    }

}