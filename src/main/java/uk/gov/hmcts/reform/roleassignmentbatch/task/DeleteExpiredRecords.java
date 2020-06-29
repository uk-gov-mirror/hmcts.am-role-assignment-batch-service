package uk.gov.hmcts.reform.roleassignmentbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeleteExpiredRecords implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("::LeafRouteTask starts::");

        //steps: 1. Create a datasource in application.yaml file.
        // Please refer to Role-assignment-service project's application.yaml for that
        // https://github.com/hmcts/am-role-assignment-service
        //(UPDATE: Datasource has been configured. Pleae verify)
        //2. While creating the datasource, we will need to setup the environment variables as
        // in https://github.com/hmcts/am-role-assignment-service
        //Same variables can be reused.

        //3. Once datasource is ready, write a query using Spring JDBC to fetch the number of
        // records from Role_Assignment table
        //Reference: https://spring.io/guides/gs/relational-data-access/

        //test the changes in local docker

        log.info("::LeafRouteTask completes with {}::", "success");
        return RepeatStatus.FINISHED;
    }
}
