package uk.gov.hmcts.reform.roleassignmentbatch.util;

public class Constants {

    public static final String REQUEST_QUERY = "INSERT INTO role_assignment_request (id, correlation_id,client_id,"
                                               + "authenticated_user_id,assigner_id,request_type,status,process,reference,replace_existing,"
                                               + "role_assignment_id,log,created) VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,"
                                               + ":requestType,:status,:process,:reference,:replaceExisting,:roleAssignmentId,:log,:created)";

    public static final String AUDIT_QUERY = "insert into audit_faults(failed_at, reason, ccd_users, request, history,"
                                             + " live) values(:failedAt, :reason, :ccdUsers, :request, :history, :live)";

    public static final String HISTORY_QUERY = "INSERT INTO role_assignment_history (id, request_id, actor_id_type, "
                                               + " actor_id, role_type, role_name, classification, grant_type, role_category, read_only, "
                                               + "begin_time, end_time, status, reference, process, status_sequence, attributes, "
                                               + "created) VALUES(:id, :requestEntity.id, :actorIdType, :actorId, :roleType,"
                                               + " :roleName, :classification, :grantType, :roleCategory, false, :beginTime, :endTime,"
                                               + " :status, :reference, :process, 1, '{}', now() )";
}
