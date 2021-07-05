package uk.gov.hmcts.reform.roleassignmentbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableBatchProcessing
@SuppressWarnings("HideUtilityClassConstructor")
@Slf4j
public class RoleAssignmentBatchApplication {

    public static void main(String[] args) throws Exception {

        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 8);
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                                           exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
