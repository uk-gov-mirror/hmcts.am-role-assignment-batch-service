package uk.gov.hmcts.reform.roleassignmentbatch.config;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ANY;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.FAILED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.STOPPED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import uk.gov.hmcts.reform.roleassignmentbatch.ApplicationParams;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.processors.EntityWrapperProcessor;
import uk.gov.hmcts.reform.roleassignmentbatch.task.BuildCcdViewMetrics;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ReconcileDataTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.RenameTablesPostMigration;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ReplicateTablesTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ValidationTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.WriteToActorCacheTableTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.writer.CcdViewWriterTemp;
import uk.gov.hmcts.reform.roleassignmentbatch.writer.EntityWrapperWriter;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {


    @Value("${delete-expired-records}")
    String taskParent;
    @Value("${batchjob-name}")
    String jobName;

    @Value("${azure.container-name}")
    String containerName;
    @Value("${migration.chunkSuze}")
    private int chunkSize;

    @Value("${azure.account-name}")
    String accountName;
    @Value("${azure.account-key}")
    String accountKey;

    @Autowired
    JobBuilderFactory jobs;

    @Autowired
    StepBuilderFactory steps;

    @Autowired
    ApplicationParams applicationParams;

    @Autowired
    JdbcPagingItemReader<CcdCaseUser> databaseItemReader;

    @Autowired
    FlatFileItemReader<CcdCaseUser> ccdCaseUsersReader;

    @Autowired
    BuildCcdViewMetrics buildCcdViewMetrics;

    @Autowired
    ReplicateTablesTasklet replicateTablesTasklet;

    @Autowired
    RenameTablesPostMigration renameTablesPostMigration;

    @Autowired
    WriteToActorCacheTableTasklet writeToActorCacheTableTasklet;

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
                            @Autowired DeleteExpiredRecords deleteExpiredRecords) {

        return jobs.get(jobName)
                   .incrementer(new RunIdIncrementer())
                   .start(stepOrchestration(steps, deleteExpiredRecords))
                   .build();
    }

    @Bean
    public EntityWrapperProcessor entityWrapperProcessor() {
        return new EntityWrapperProcessor();
    }

    //TODO: Remove later
    @Bean
    public CcdViewWriterTemp ccdViewWriterTemp() {
        return new CcdViewWriterTemp();
    }

    @Bean
    SkipListener<CcdCaseUser, EntityWrapper> auditSkipListener() {
        return new AuditSkipListener();
    }

    @Bean
    EntityWrapperWriter entityWrapperWriter() {
        return new EntityWrapperWriter();
    }

    @Bean
    public Step renameTablesPostMigrationStep() {
        return steps.get("renameTablesPostMigrationStep")
                    .tasklet(renameTablesPostMigration)
                    .build();
    }

    @Bean
    public Step buildCCdViewMetricsStep() {
        return steps.get("buildCCdViewMetricsStep")
                    .tasklet(buildCcdViewMetrics)
                    .build();
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
    public Step writeToActorCache() {
        return steps.get("writeToActorCache")
                    .tasklet(writeToActorCacheTableTasklet)
                    .build();
    }

    @Bean
    public Step reconcileData() {
        return steps.get("reconcileDataTasklet")
                    .tasklet(reconcileDataTasklet)
                    .build();
    }

    @Bean
    public Step ccdToRasStep() {
        return steps.get("ccdToRasStep")
                    .<CcdCaseUser, EntityWrapper>chunk(chunkSize)
                    .faultTolerant()
                    .retryLimit(3)
                    .retry(DeadlockLoserDataAccessException.class)
                    .skip(Exception.class).skip(Throwable.class)
                    .skipLimit(1000)
                    .listener(auditSkipListener())
                    .reader(databaseItemReader)
                    .processor(entityWrapperProcessor())
                    .writer(entityWrapperWriter())
                    .taskExecutor(taskExecutor())
                    .throttleLimit(10)
                    .build();
    }

    @Bean
    public Step injectDataIntoView() {
        return steps.get("injectDataIntoView")
                    .<CcdCaseUser, CcdCaseUser>chunk(chunkSize)
                    .reader(ccdCaseUsersReader)
                    .writer(ccdViewWriterTemp())
                    .taskExecutor(taskExecutor())
                    .throttleLimit(10)
                    .build();
    }


    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("ccd_migration");
    }

    @Bean
    public JobExecutionDecider checkLdStatus() {
        return new JobRunnableDecider();
    }

    @Bean
    public JobExecutionDecider checkCcdProcessStatus() {
        return (job, step) -> new FlowExecutionStatus(applicationParams.getProcessCcdDataEnabled());
    }

    @Bean
    public JobExecutionDecider checkRenamingTablesStatus() {
        return (job, step) -> new FlowExecutionStatus(applicationParams.getRenamingPostMigrationTablesEnabled());
    }

    @Bean
    public Step firstStep() {
        return steps.get("LdValidation")
                    .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
                    .build();
    }

    @Bean
    public Flow processCcdDataToTempTablesFlow() {
        return new FlowBuilder<Flow>("processCcdDataToTempTables")
            .start(replicateTables())
            .next(validationStep())
            .next(injectDataIntoView())
            .next(buildCCdViewMetricsStep())
            .next(ccdToRasStep())
            .next(writeToActorCache())
            .next(reconcileData())
            .build();
    }

    /**
     * Job will start and check for LD flag, In case LD flag is disabled it will end the JOB
     * otherwise will check or CCD migration flag, In case flag is enabled will process the CCD data to temp tables
     * otherwise will check migration rename to live tables, In case flag is enabled will rename the temp tables to
     * live tables otherwise it will end the job.
     *
     * @return job
     */
    @Bean
    public Job ccdToRasBatchJob() {
        return jobs.get("ccdToRasBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(firstStep()) //Dummy step as Decider will work after Step
                .next(checkLdStatus()).on(DISABLED).end(STOPPED)
                .from(checkLdStatus()).on(ANY).to(checkCcdProcessStatus())
                .from(checkCcdProcessStatus()).on(DISABLED).to(checkRenamingTablesStatus())
                .from(checkCcdProcessStatus()).on(ANY).to(processCcdDataToTempTablesFlow())
                                                        .on(FAILED).end(FAILED)
                                                        .on(ANY).to(checkRenamingTablesStatus())
                .from(checkRenamingTablesStatus()).on(DISABLED).end(STOPPED)
                .from(checkRenamingTablesStatus()).on(ANY).to(renameTablesPostMigrationStep())
                                                        .on(FAILED).end(FAILED)
                                                        .on(ANY).end()
                .end()
                .build();

    }
}
