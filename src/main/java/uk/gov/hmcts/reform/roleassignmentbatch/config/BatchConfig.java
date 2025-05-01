package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteJudicialExpiredRecords;

@Slf4j
@Configuration
public class BatchConfig {

    @Value("${delete-expired-records}")
    String deleteExpiredRecordsStepName;

    @Value("${delete-expired-judicial-records}")
    String deleteExpiredJudicialRecordsStepName;

    @Value("${batchjob-name}")
    String jobName;

    @Bean
    public Step stepOrchestration(JobRepository jobRepository, DeleteExpiredRecords deleteExpiredRecords,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder(deleteExpiredRecordsStepName, jobRepository)
                .tasklet(deleteExpiredRecords, transactionManager)
                .build();
    }

    @Bean
    public Job runRoutesJob(JobRepository jobRepository,
                            @Autowired DeleteExpiredRecords deleteExpiredRecords,
                            @Autowired DeleteJudicialExpiredRecords deleteJudicialExpiredRecords,
                            PlatformTransactionManager transactionManager) {
        return new JobBuilder(jobName, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepOrchestration(jobRepository, deleteExpiredRecords, transactionManager))
                .next(stepDeleteJudicialExpired(jobRepository, deleteJudicialExpiredRecords, transactionManager))
                .build();
    }

    @Bean
    public Step stepDeleteJudicialExpired(JobRepository jobRepository,
                                          DeleteJudicialExpiredRecords stepDeleteJudicialExpired,
                                          PlatformTransactionManager transactionManager) {
        return new StepBuilder(deleteExpiredJudicialRecordsStepName, jobRepository)
                .tasklet(stepDeleteJudicialExpired, transactionManager)
                .build();
    }

}
