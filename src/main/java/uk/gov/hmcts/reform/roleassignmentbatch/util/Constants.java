package uk.gov.hmcts.reform.roleassignmentbatch.util;

public class Constants {
    public final String REQUEST_QUERY = "INSERT INTO role_assignment_request (id, correlation_id,client_id,authenticated_user_id,assigner_id,request_type,status," +
                                 "process,reference," +
                                 "replace_existing,role_assignment_id,log,created)" +
                                 " VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,:requestType,:status,:process,:reference," +
                                 ":replaceExisting," +
                                 ":roleAssignmentId,:log,:created)";
}
