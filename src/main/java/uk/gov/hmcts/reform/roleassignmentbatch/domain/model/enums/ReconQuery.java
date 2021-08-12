package uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums;

public enum ReconQuery {

    //Audit Fault
    AUDIT_FAULTS_TOTAL_COUNT("select count(1) from audit_faults"),

    //CCD
    CCD_TOTAL_COUNT("select count(1) from ccd_user_view"),
    GROUP_BY_CCD_JURISDICTION("select jurisdiction,count(1) from ccd_user_view group by jurisdiction order by jurisdiction"),
    GROUP_BY_CCD_CASE_ROLE("select case_role,count(1) from ccd_user_view group by case_role order by case_role"),
    //AM
    AM_TOTAL_COUNT("select count(1) from replica_role_assignment"),
    GROUP_BY_AM_JURISDICTION("select attributes->>'jurisdiction' as caseTypeId,count(1)"
                             + " from replica_role_assignment ra "
                             + "group by attributes->>'jurisdiction' order by attributes->>'jurisdiction'"),
    GROUP_BY_AM_CASE_ROLE("select role_name,count(1) from replica_role_assignment"
                          + " group by role_name order by role_name"),

    INSERT_RECONCILIATION_QUERY("insert into reconciliation_data (run_id, ccd_jurisdiction_data,"
                                + "ccd_role_name_data,replica_am_jurisdiction_data"
                                + ",replica_am_role_name_data,total_count_from_ccd,"
                                + " total_count_from_replica_am,status,notes,"
                                + "am_records_before_migration,am_records_after_migration)"
                                + " values (?, ?,?,?,?,?,?,?,?,?,?) on conflict (run_id) do update "
                                + "set ccd_jurisdiction_data = EXCLUDED.ccd_jurisdiction_data, "
                                + "ccd_role_name_data = EXCLUDED.ccd_role_name_data,"
                                + "replica_am_jurisdiction_data = EXCLUDED.replica_am_jurisdiction_data,"
                                + "replica_am_role_name_data = EXCLUDED.replica_am_role_name_data,"
                                + "total_count_from_ccd = EXCLUDED.total_count_from_ccd,"
                                + "total_count_from_replica_am = EXCLUDED.total_count_from_replica_am,"
                                + "status = EXCLUDED.status,"
                                + "am_records_after_migration = EXCLUDED.am_records_after_migration,"
                                + "am_records_before_migration = EXCLUDED.am_records_before_migration,"
                                + "notes = EXCLUDED.notes;"),

    CCD_JURISDICTION_KEY("jurisdiction"),
    CCD_CASE_ROLE_KEY("case_role"),
    CASE_TYPE("caseTypeId"),
    ROLE_NAME("role_name"),
    BATCH_IN_PROGRESS("BATCH_IN_PROGRESS"),
    PASSED("PASSED"),
    FAILED("FAILED"),
    IN_PROGRESS("The batch operation is in progress."),
    SUCCESS_STATUS("Total Record are matching from both ccd_user_view and am_role_assignment table."),
    FAILED_STATUS("Total Record are NOT matching from both ccd_user_view and am_role_assignment table."),
    CHECK_AUDIT_TABLE("There is some failure. Please check the Audit table"),
    CCD_VIEW_MODIFIED("The CCD view has been modified during the migration."
                      + " Initial records were %s, and current records are %s"),
    CHECK_JURISDICTION_DATA("There is some failure. Please check the Jurisdiction Data."),
    MIGRATION_JOB_FAILED("CCD Migration job has failed. Please check Audit_Fault/reconciliation_data table"),
    CHECK_ROLENAME_DATA("There is some failure. Please check the Role Name Data."),
    COUNT("count");

    private final String key;

    ReconQuery(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
