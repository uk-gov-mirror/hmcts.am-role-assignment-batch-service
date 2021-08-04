package uk.gov.hmcts.reform.roleassignmentbatch.util;

import java.util.List;

public class Constants {

    private Constants(){
    }

    public static final String DISABLED = "DISABLED";
    public static final String ENABLED = "ENABLED";
    public static final String STOPPED = "STOPPED";
    public static final String FAILED = "FAILED";
    public static final String ANY = "*";
    public static final String ID = "id";
    public static final String CASE_DATA_ID = "case_data_id";
    public static final String USER_ID = "user_id";
    public static final String CASE_ROLE = "case_role";
    public static final String JURISDICTION = "jurisdiction";
    public static final String CASE_TYPE = "case_type";
    public static final String ROLE_CATEGORY = "role_category";
    public static final String BEGIN_DATE = "begin_date";
    public static final List<String> AM_TABLES = List.of("role_assignment","role_assignment_history",
            "role_assignment_request","actor_cache_control");

    public static final String REQUEST_QUERY = "INSERT INTO replica_role_assignment_request(id, correlation_id,"
                                        + "client_id,authenticated_user_id,assigner_id,request_type,"
                                        + "status,"
                                        + "process,reference,"
                                        + "replace_existing,role_assignment_id,log,created)"
                                        + " VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,"
                                        + ":requestType,:status,:process,:reference,"
                                        + ":replaceExisting,"
                                        + ":roleAssignmentId,:log,:created)";

    public static final String AUDIT_QUERY = "insert into audit_faults(failed_at, reason, ccd_users, request, history, "
                                             + "actor_cache, live) values(:failedAt, :reason, :ccdUsers, :request, "
                                             + ":history, :actorCache, :live)";

    public static final String HISTORY_QUERY = "insert into replica_role_assignment_history(id, status, actor_id_type, "
                                               + "role_type, role_name, status_sequence, classification, grant_type, "
                                               + "read_only, created, actor_id, attributes, request_id,"
                                               + " role_category, begin_time, process, reference, log) "
                                               + "values(:id, :status, :actorIdType, :roleType, :roleName, "
                                               + ":sequence, :classification, :grantType,"
                                               + ":readOnly, :created, "
                                               + ":actorId, :attributes::jsonb, :requestId, :roleCategory,"
                                               + " :beginTime, :process, :reference, :log)";

    public static final String ACTOR_CACHE_QUERY = "insert into replica_actor_cache_control"
                                                   + "(actor_id,etag,json_response)"
                                                   + " values(:actorIds,:etag, :roleAssignmentResponse) on "
                                                   + " conflict(actor_id) do nothing;";

    public static final String ROLE_ASSIGNMENT_LIVE_TABLE = "insert into replica_role_assignment(id, actor_id_type,"
        + " actor_id, role_type, role_name, "
        + "classification, grant_type, role_category, read_only, created, "
        + "attributes, begin_time) "
        + "values(:id, :actorIdType, :actorId, :roleType, :roleName, "
        + ":classification, :grantType, :roleCategory, :readOnly, :created, "
        + ":attributes::jsonb, :beginTime)";

    public static final String GET_RECONCILIATION_DATA = "select * from reconciliation_data where run_id =?";
    public static final String GET_LATEST_RECONCILIATION_DATA =
        "select * from reconciliation_data rd where run_id = (select max(run_id) from reconciliation_data rd2);";

    //check role category as well
    public static final String CCD_RECORDS_HAVING_NULL_FIELDS =
        "select id,case_data_id,user_id,case_role,jurisdiction,case_type,role_category,begin_date from ccd_view where"
        + " length(case_data_id) = 0 or length(case_role) = 0 or length(jurisdiction) = 0 or length(case_type) = 0"
        + " or length(role_category) = 0 or begin_date is null or length(user_id) = 0 limit 100";

    public static final String INSERT_INTO_ACTOR_CACHE = "insert into replica_actor_cache_control"
                                                         + " (actor_id, etag,json_response)"
        + " select distinct(actor_id),0, '{}'::jsonb from replica_role_assignment;";

    public static final String QUERY_INVALID_CASE_IDS = "SELECT distinct (case_data_id) FROM ccd_view"
                                                        + " WHERE LENGTH(case_data_id) != 16 LIMIT 100";
    public static final String DISTINCT_CASE_ROLES_FROM_CCD = "select distinct (case_role) from ccd_view";
    public static final String DISTINCT_ROLE_CATEGORY_FROM_CCD = "select distinct (role_category) from ccd_view";
    public static final String COUNT_AM_ROLE_ASSIGNMENT_TABLE = "select count (*) from role_assignment";
    public static final String COUNT_AM_HISTORY_TABLE = "select count (*) from role_assignment_history";
    public static final String COUNT_AM_REQUEST_TABLE = "select count (*) from role_assignment_request";
    public static final String COUNT_ACTOR_CACHE_TABLE = "select count (*) from actor_cache_control";

    public static final String COPY_DATA_INTO_ACTOR_CACHE = "INSERT into replica_actor_cache_control(SELECT * FROM"
                                                            + " actor_cache_control) on conflict do nothing;";
    public static final String COPY_DATA_INTO_ROLE_ASSIGNMENT = "INSERT into replica_role_assignment(SELECT * FROM"
                                                                + " role_assignment) on conflict do nothing;";
    public static final String COPY_DATA_INTO_HISTORY = "INSERT into replica_role_assignment_request(SELECT * FROM"
                                                        + " role_assignment_request) on conflict do nothing;";
    public static final String COPY_DATA_INTO_REQUEST = "INSERT into replica_role_assignment_history(SELECT * FROM"
                                                        + " role_assignment_history) on conflict do nothing;";

    public static final String NO_RECONCILIATION_DATA_FOUND = "No reconciliation data found for Job Id: %s";
    public static final String INVALID_ROLES = "The following roles are invalid : ";
    public static final String INVALID_ROLE_CATEGORIES = "The following role categories are invalid :";
    public static final String INVALID_CASE_IDS = "The following caseIds is not of valid length: ";
    public static final String ERROR_BUILDIND_CCD_RECONCILIATION_DATA =
        "The total records do not match with sum(groupBy(jurisdiction/roleName))";
    public static final String EMPTY_STRING = "";
    public static final String BEFORE_CCD_MIGRATION = "Before CCD Migration - Reconciliation Status";
    public static final String AFTER_CCD_MIGRATION = "After CCD Migration - Reconciliation Status";
    public static final String AFTER_TABLE_RENAME = "After Table Rename - Reconciliation Status";
    public static final String AFTER_VALIDATION = "Validation - Reconciliation Status";

    public static final String RECONCILIATION = "Reconciliation";
    public static final String DELETE_EXPIRED_JOB = "DeleteExpiredJob";
    public static final String DELETE_EXPIRED_RECORD_JOB_STATUS = "Delete Expired Record Job Status";
}
