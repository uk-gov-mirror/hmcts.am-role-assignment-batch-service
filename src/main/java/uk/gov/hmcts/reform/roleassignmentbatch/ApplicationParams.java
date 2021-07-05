package uk.gov.hmcts.reform.roleassignmentbatch;

import org.springframework.beans.factory.annotation.Value;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class ApplicationParams {

    @Value("${audit.exception.enabled:true}")
    private boolean auditEnabled;

    @Value("${migration.masterFlag:false}")
    private boolean processCcdDataAllowed;

    @Value("${migration.renameTables:false}")
    private boolean renamingPostMigrationTablesEnabled;

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public String getProcessCcdDataEnabled() {
        return processCcdDataAllowed ? ENABLED : DISABLED;
    }

    public String getRenamingPostMigrationTablesEnabled() {
        return renamingPostMigrationTablesEnabled ? ENABLED : DISABLED;
    }
}
