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
        final ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        log.info("Putting application to sleep for 5 mins");
        Thread.sleep(1000 * 60 * 5);
        log.info("The sleep is complete.");
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                                           exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
