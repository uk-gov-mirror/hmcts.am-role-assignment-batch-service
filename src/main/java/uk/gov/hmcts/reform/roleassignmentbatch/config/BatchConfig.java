package uk.gov.hmcts.reform.roleassignmentbatch.config;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ANY;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.BEGIN_DATE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_ROLE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.DISABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ENABLED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.FAILED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.JURISDICTION;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ROLE_CATEGORY;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.STOPPED;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.USER_ID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.launchdarkly.sdk.server.LDClient;
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
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.ApplicationParams;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.processors.EntityWrapperProcessor;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.RenameTablesPostMigration;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ReplicateTablesTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ValidationTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.WriteToActorCacheTableTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;
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
    @Value("${csv-file-name}")
    String fileName;
    @Value("${csv-file-path}")
    String filePath;
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
    DataSource dataSource;

    @Autowired
    PagingQueryProvider queryProvider;

    @Autowired
    ApplicationParams applicationParams;

    @Autowired
    NotificationListener stepListener;

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
    public FlatFileItemReader<CcdCaseUser> ccdCaseUsersReader() {
        return new FlatFileItemReaderBuilder<CcdCaseUser>()
            .name("historyEntityReader")
            .linesToSkip(1)
            .saveState(false)
            .resource(new PathResource(filePath + fileName))
            .delimited()
            .names(CASE_DATA_ID, USER_ID, CASE_ROLE, JURISDICTION, CASE_TYPE, ROLE_CATEGORY, BEGIN_DATE)
            .lineMapper(lineMapper())
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                {
                    setTargetType(CcdCaseUser.class);
                }
            })

            .build();
    }

    @Bean
    public LineMapper<CcdCaseUser> lineMapper() {
        final DefaultLineMapper<CcdCaseUser> defaultLineMapper = new DefaultLineMapper<>();
        final DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(CASE_DATA_ID, USER_ID, CASE_ROLE, JURISDICTION, CASE_TYPE, ROLE_CATEGORY, BEGIN_DATE);
        final CcdFieldSetMapper ccdFieldSetMapper = new CcdFieldSetMapper();
        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(ccdFieldSetMapper);
        return defaultLineMapper;
    }


    @Component
    public static class CcdFieldSetMapper implements FieldSetMapper<CcdCaseUser> {
        @Override
        public CcdCaseUser mapFieldSet(FieldSet fieldSet) {
            final CcdCaseUser caseUsers = new CcdCaseUser();
            caseUsers.setCaseDataId(fieldSet.readString(CASE_DATA_ID));
            caseUsers.setUserId(fieldSet.readString(USER_ID));
            caseUsers.setCaseRole(fieldSet.readString(CASE_ROLE));
            caseUsers.setJurisdiction(fieldSet.readString(JURISDICTION));
            caseUsers.setCaseType(fieldSet.readString(CASE_TYPE));
            caseUsers.setRoleCategory(fieldSet.readString(ROLE_CATEGORY));
            caseUsers.setBeginDate(fieldSet.readString(BEGIN_DATE));
            return caseUsers;
        }
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
    public JdbcBatchItemWriter<RequestEntity> insertInRequestTable() {
        return new JdbcBatchItemWriterBuilder<RequestEntity>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(Constants.REQUEST_QUERY)
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<AuditFaults> insertInAuditFaults() {
        return new JdbcBatchItemWriterBuilder<AuditFaults>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(Constants.AUDIT_QUERY)
            .dataSource(dataSource)
            .build();
    }

    @Bean
    SkipListener<CcdCaseUser, EntityWrapper> auditSkipListener() {
        return new AuditSkipListener();
    }

    //TODO: Remove later as we are inserting into actor cache in a separate step.
    @Bean
    public JdbcBatchItemWriter<ActorCacheEntity> insertIntoActorCacheTable() {
        return
            new JdbcBatchItemWriterBuilder<ActorCacheEntity>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(Constants.ACTOR_CACHE_QUERY)
                .dataSource(dataSource)
                .assertUpdates(false)
                .build();
    }


    @Bean
    public JdbcBatchItemWriter<HistoryEntity> insertIntoHistoryTable() {
        return
            new JdbcBatchItemWriterBuilder<HistoryEntity>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(Constants.HISTORY_QUERY)
                .dataSource(dataSource)
                .build();
    }

    //TODO: Remove once actual CCD View is available.
    @Bean
    public JdbcBatchItemWriter<CcdCaseUser> insertIntoCcdView() {
        return
            new JdbcBatchItemWriterBuilder<CcdCaseUser>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into ccd_view(case_data_id,user_id,case_role,jurisdiction,case_type,role_category,"
                     + "begin_date) values (:caseDataId,:userId,:caseRole,:jurisdiction,:caseType,:roleCategory,"
                     + ":beginDate)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<RoleAssignmentEntity> insertIntoRoleAssignmentTable() {
        return
            new JdbcBatchItemWriterBuilder<RoleAssignmentEntity>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(Constants.ROLE_ASSIGNMENT_LIVE_TABLE)
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<CcdCaseUser> databaseItemReader() {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("status", "NEW");

        return new JdbcPagingItemReaderBuilder<CcdCaseUser>()
            .name("ccdCaseUserReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            //.parameterValues(parameterValues)
            .rowMapper(new CcdViewRowMapper())
            .saveState(false)
            .pageSize(chunkSize)
            .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean queryProvider() {
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

        provider.setSelectClause("select id,case_data_id,user_id,case_role,jurisdiction,case_type,role_category,"
                                 + "begin_date");
        provider.setFromClause("from ccd_view");
        //provider.setWhereClause("where status=:status");
        provider.setSortKey("id");
        provider.setDataSource(dataSource);

        return provider;
    }

    public class CcdViewRowMapper implements RowMapper<CcdCaseUser> {

        @Override
        public CcdCaseUser mapRow(ResultSet rs, int rowNum) throws SQLException {

            CcdCaseUser ccdCaseUser = new CcdCaseUser();
            ccdCaseUser.setCaseDataId(rs.getString(CASE_DATA_ID));
            ccdCaseUser.setUserId(rs.getString(USER_ID));
            ccdCaseUser.setCaseRole(rs.getString(CASE_ROLE));
            ccdCaseUser.setCaseType(rs.getString(CASE_TYPE));
            ccdCaseUser.setBeginDate(rs.getString(BEGIN_DATE));
            ccdCaseUser.setRoleCategory(rs.getString(ROLE_CATEGORY));
            ccdCaseUser.setJurisdiction(rs.getString(JURISDICTION));
            return ccdCaseUser;
        }
    }

    @Bean
    EntityWrapperWriter entityWrapperWriter() {
        return new EntityWrapperWriter();
    }

    /*@Bean
    CcdToRasSetupTasklet ccdToRasSetupTasklet() {
        return new CcdToRasSetupTasklet(fileName, filePath, containerName, accountName, accountKey);
    }*/

    @Bean
    ValidationTasklet validationTasklet() {
        return new ValidationTasklet();
    }

    @Bean
    ReplicateTablesTasklet replicateTablesTasklet() {
        return new ReplicateTablesTasklet();
    }

    @Bean
    WriteToActorCacheTableTasklet writeToActorCacheTableTasklet() {
        return new WriteToActorCacheTableTasklet();
    }

    @Bean
    RenameTablesPostMigration renameTablesPostMigration() {
        return new RenameTablesPostMigration();
    }

    @Bean
    public Step renameTablesPostMigrationStep() {
        return steps.get("renameTablesPostMigrationStep")
                    .tasklet(renameTablesPostMigration())
                    .build();
    }

    /*@Bean
    public Step ccdToRasSetupStep() {
        return steps.get("ccdToRasSetupStep")
                    .tasklet(ccdToRasSetupTasklet())
                    .build();
    }*/

    @Bean
    public Step validationStep() {
        return steps.get("validationStep")
                    .tasklet(validationTasklet())
                    .build();
    }

    @Bean
    public Step replicateTables() {
        return steps.get("ReplicateTables")
                    .tasklet(replicateTablesTasklet())
                    .build();
    }

    @Bean
    public Step writeToActorCache() {
        return steps.get("writeToActorCache")
                    .tasklet(writeToActorCacheTableTasklet())
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
                    .reader(databaseItemReader())
                    .processor(entityWrapperProcessor())
                    .writer(entityWrapperWriter())
                    .listener(stepListener)
                    .taskExecutor(taskExecutor())
                    .throttleLimit(10)
                    .build();
    }

    @Bean
    public Step injectDataIntoView() {
        return steps.get("injectDataIntoView")
                    .<CcdCaseUser, CcdCaseUser>chunk(chunkSize)
                    .reader(ccdCaseUsersReader())
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
            .start(validationStep())
            .next(replicateTables())
            .next(injectDataIntoView())
            .next(ccdToRasStep())
            .next(writeToActorCache())
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
                .from(checkCcdProcessStatus()).on(ENABLED).to(processCcdDataToTempTablesFlow())
                                                                .on(FAILED).end(FAILED)
                .next(checkRenamingTablesStatus()).on(DISABLED).end(STOPPED)
                .from(checkRenamingTablesStatus()).on(ANY).to(renameTablesPostMigrationStep())
                .end()
                .build();

    }

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        return new LDClient(sdkKey);
    }
}
