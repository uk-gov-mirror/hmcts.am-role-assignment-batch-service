package uk.gov.hmcts.reform.roleassignmentbatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
        String dbPass = System.getenv("ROLE_ASSIGNMENT_DB_PASSWORD");
        if (null == dbPass || StringUtils.isEmpty(dbPass)) {
            log.info("The database password is null or empty");
        } else {
            log.info("the length of dbPass is : " + dbPass.length());
        }

        String s2sValue = System.getenv("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
        if (null == s2sValue || StringUtils.isEmpty(s2sValue)) {
            log.info("The s2sValue is null or empty");
        } else {
            log.info("the length of s2sValue is : " + s2sValue.length());
        }
        log.info("Delete expired records is successful");
        log.info("Sys outing the details");
        log.info("userName: " + System.getenv("ROLE_ASSIGNMENT_DB_USERNAME"));
        log.info("ROLE_ASSIGNMENT_DB_HOST: " + System.getenv("ROLE_ASSIGNMENT_DB_HOST"));
        log.info("ROLE_ASSIGNMENT_DB_NAME: " + System.getenv("ROLE_ASSIGNMENT_DB_NAME"));
        log.info("Sys outing the details : end");
        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        dbPass = System.getenv("ROLE_ASSIGNMENT_DB_PASSWORD");
        if (null == dbPass || StringUtils.isEmpty(dbPass)) {
            log.info("The database password is null or empty");
        } else {
            log.info("the length of dbPass is : " + dbPass.length());
        }

        s2sValue = System.getenv("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
        if (null == s2sValue || StringUtils.isEmpty(s2sValue)) {
            log.info("The s2sValue is null or empty");
        } else {
            log.info("the length of s2sValue is : " + s2sValue.length());
        }
        log.info("Delete expired records is successful");
        log.info("Sys outing the details");
        log.info("userName: " + System.getenv("ROLE_ASSIGNMENT_DB_USERNAME"));
        log.info("ROLE_ASSIGNMENT_DB_HOST: " + System.getenv("ROLE_ASSIGNMENT_DB_HOST"));
        log.info("ROLE_ASSIGNMENT_DB_NAME: " + System.getenv("ROLE_ASSIGNMENT_DB_NAME"));
        log.info("Sys outing the details : end");
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 60 * 9);
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                                           exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
