package uk.gov.hmcts.reform.roleassignmentbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor")
public class RoleAssignmentBatchApplication {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentBatchApplication.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(6000);
        int exitCode = SpringApplication.exit(context);
        log.info(String.format("RoleAssignmentBatchApplication Application exiting with exit code %s", exitCode));
        System.exit(exitCode);
    }
}
