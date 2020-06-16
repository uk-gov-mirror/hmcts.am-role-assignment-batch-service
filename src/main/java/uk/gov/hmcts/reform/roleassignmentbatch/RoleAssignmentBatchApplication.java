package uk.gov.hmcts.reform.roleassignmentbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Slf4j
@SuppressWarnings("HideUtilityClassConstructor")
public class RoleAssignmentBatchApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(5000);
        int exitCode = SpringApplication.exit(context);
        log.info("RoleAssignmentBatchApplication Application exiting with exit code " + exitCode);
        System.exit(exitCode);
    }
}
