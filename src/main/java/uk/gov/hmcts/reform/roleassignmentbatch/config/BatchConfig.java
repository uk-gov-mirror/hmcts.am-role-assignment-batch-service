package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteJudicialExpiredRecords;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

    @Value("${delete-expired-records}")
    String deleteExpiredRecordsStepName;

    @Value("${delete-expired-judicial-records}")
    String deleteExpiredJudicialRecordsStepName;

    @Value("${batchjob-name}")
    String jobName;

    @Autowired
    JobBuilderFactory jobs;

    @Autowired
    StepBuilderFactory steps;

    @Bean
    public Step stepOrchestration(@Autowired StepBuilderFactory steps,
                                  @Autowired DeleteExpiredRecords deleteExpiredRecords) {
        return steps.get(deleteExpiredRecordsStepName)
                    .tasklet(deleteExpiredRecords)
                    .build();
    }

    @Bean
    public Job runRoutesJob(@Autowired JobBuilderFactory jobs,
                            @Autowired StepBuilderFactory steps,
                            @Autowired DeleteExpiredRecords deleteExpiredRecords,
                            @Autowired DeleteJudicialExpiredRecords deleteJudicialExpiredRecords) {

        return jobs.get(jobName)
                .incrementer(new RunIdIncrementer())
                .start(stepOrchestration(steps, deleteExpiredRecords))
                .next(stepDeleteJudicialExpired(steps, deleteJudicialExpiredRecords))
                .build();
    }

    @Bean
    public Step stepDeleteJudicialExpired(@Autowired StepBuilderFactory steps,
                                          @Autowired DeleteJudicialExpiredRecords stepDeleteJudicialExpired) {
        return steps.get(deleteExpiredJudicialRecordsStepName)
                .tasklet(stepDeleteJudicialExpired)
                .build();
    }

}
