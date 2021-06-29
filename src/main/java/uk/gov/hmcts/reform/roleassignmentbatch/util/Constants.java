package uk.gov.hmcts.reform.roleassignmentbatch.util;

public class Constants {

    public static final String REQUEST_QUERY = "INSERT INTO replica_role_assignment_request(id, correlation_id,client_id,"
                                        + "authenticated_user_id,assigner_id,request_type,"
                                        + "status,"
                                        + "process,reference,"
                                        + "replace_existing,role_assignment_id,log,created)"
                                        + " VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,"
                                        + ":requestType,:status,:process,:reference,"
                                        + ":replaceExisting,"
                                        + ":roleAssignmentId,:log,:created)";

    public static final String AUDIT_QUERY = "insert into audit_faults(failed_at, reason, ccd_users, request, history,"
                                             + " live) values(:failedAt, :reason, :ccdUsers, :request, :history, :live)";

    public static final String HISTORY_QUERY = "insert into replica_role_assignment_history(id, status, actor_id_type, "
                                               + "role_type, role_name, status_sequence, classification, grant_type, "
                                               + "read_only, created, "
                                               + "actor_id, attributes, request_id) "
                                               + "values(:id, :status, :actorIdType, :roleType, :roleName, "
                                               + ":sequence, :classification, :grantType,"
                                               + ":readOnly, :created, "
                                               + ":actorId, :attributes::jsonb, :requestId)";

    public static final String ACTOR_CACHE_QUERY = "insert into replica_actor_cache_control(actor_id,etag,json_response) "
                                                   + "values(:actorIds,:etag, :roleAssignmentResponse) on conflict(actor_id) do nothing;";

    public static final String ROLE_ASSIGNMENT_LIVE_TABLE = "insert into replica_role_assignment(id, actor_id_type, actor_id, role_type, role_name, "
        + "classification, grant_type, role_category, read_only, created, "
        + "attributes) "
        + "values(:id, :actorIdType, :actorId, :roleType, :roleName, "
        + ":classification, :grantType, :roleCategory, :readOnly, :created, "
        + ":attributes::jsonb)";
}
