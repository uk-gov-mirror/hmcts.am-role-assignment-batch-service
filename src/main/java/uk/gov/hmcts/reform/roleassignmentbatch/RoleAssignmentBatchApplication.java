package uk.gov.hmcts.reform.roleassignmentbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@SuppressWarnings("HideUtilityClassConstructor")
public class RoleAssignmentBatchApplication {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentBatchApplication.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        //Currently, it is set to 1 hours. We need to update it later once the changes are finalized.
        Thread.sleep(1000 * 60 * 60);
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                                           exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
