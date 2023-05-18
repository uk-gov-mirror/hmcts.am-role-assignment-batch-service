package uk.gov.hmcts.reform.roleassignmentbatch;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@SuppressWarnings("HideUtilityClassConstructor")
@Slf4j
public class RoleAssignmentBatchApplication {

    @Autowired
    private static TelemetryClient client;

    @Autowired
    public RoleAssignmentBatchApplication(TelemetryClient client) {
        RoleAssignmentBatchApplication.client = client;
    }

    public static void main(String[] args) throws Exception {
        final ApplicationContext context = SpringApplication.run(RoleAssignmentBatchApplication.class, args);

        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentBatchApplication Application exiting with exit code %s",
                exitCode);
        log.info(exitCodeLog);
        client.flush();
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 5L);
        System.exit(exitCode);
    }
}