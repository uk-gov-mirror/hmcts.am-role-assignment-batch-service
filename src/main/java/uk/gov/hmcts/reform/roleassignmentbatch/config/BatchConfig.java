package uk.gov.hmcts.reform.roleassignmentbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignmentbatch.processors.EntityWrapperProcessor;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteJudicialExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ReconcileDataTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ReplicateTablesTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ValidationTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.writer.EntityWrapperWriter;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

    @Value("${delete-expired-records}")
    String taskParent;

    @Value("${delete-expired-judicial-records}")
    String taskParentJudicial;

    @Value("${batchjob-name}")
    String jobName;

    @Value("${migration.chunkSize}")
    private int chunkSize;

    @Value("${migration.masterFlag}")
    boolean masterFlag;
    @Value("${migration.renameTables}")
    boolean renameTables;

    @Autowired
    JobBuilderFactory jobs;

    @Autowired
    StepBuilderFactory steps;

    @Autowired
    ReplicateTablesTasklet replicateTablesTasklet;

    @Autowired
    ReconcileDataTasklet reconcileDataTasklet;

    @Autowired
    ValidationTasklet validationTasklet;

    @Bean
    public Step stepOrchestration(@Autowired StepBuilderFactory steps,
                                  @Autowired DeleteExpiredRecords deleteExpiredRecords) {
        return steps.get(taskParent)
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
        return steps.get(taskParentJudicial)
                .tasklet(stepDeleteJudicialExpired)
                .build();
    }

    @Bean
    public EntityWrapperProcessor entityWrapperProcessor() {
        return new EntityWrapperProcessor();
    }

    @Bean
    EntityWrapperWriter entityWrapperWriter() {
        return new EntityWrapperWriter();
    }

    @Bean
    public Step validationStep() {
        return steps.get("validationStep")
                    .tasklet(validationTasklet)
                    .build();
    }

    @Bean
    public Step replicateTables() {
        return steps.get("ReplicateTables")
                    .tasklet(replicateTablesTasklet)
                    .build();
    }

    @Bean
    public Step reconcileData() {
        return steps.get("reconcileDataTasklet")
                    .tasklet(reconcileDataTasklet)
                    .build();
    }

    @Bean
    public JobExecutionDecider checkLdStatus() {
        return new JobRunnableDecider(masterFlag, renameTables);
    }

    @Bean
    public Step firstStep() {
        return steps.get("LdValidation")
                    .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
                    .build();
    }

}
