package uk.gov.hmcts.reform.roleassignmentbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@SuppressWarnings("HideUtilityClassConstructor")
@Slf4j
public class RoleAssignmentBatchApplication {

    public static void main(String[] args) throws Exception {
        log.info("Sys outing the details");
        log.info("userName: " + System.getenv("ROLE_ASSIGNMENT_DB_USERNAME"));
        log.info("ROLE_ASSIGNMENT_DB_PASSWORD: " + System.getenv("ROLE_ASSIGNMENT_DB_PASSWORD"));
        log.info("ROLE_ASSIGNMENT_DB_HOST: " + System.getenv("ROLE_ASSIGNMENT_DB_HOST"));
        log.info("AM_ROLE_ASSIGNMENT_SERVICE_SECRET: "
                 + System.getenv("AM_ROLE_ASSIGNMENT_SERVICE_SECRET"));
        log.info("ROLE_ASSIGNMENT_DB_NAME: " + System.getenv("ROLE_ASSIGNMENT_DB_NAME"));
        log.info("AM_ROLE_ASSIGNMENT_SERVICE_SECRET: " + System.getenv("AM_ROLE_ASSIGNMENT_SERVICE_SECRET"));
        log.info("Sys outing the details : end");
        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 6);
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                                           exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
