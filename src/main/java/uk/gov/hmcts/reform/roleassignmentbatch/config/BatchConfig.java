package uk.gov.hmcts.reform.roleassignmentbatch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.domain.model.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.processors.EntityWrapperProcessor;
import uk.gov.hmcts.reform.roleassignmentbatch.task.CcdToRasSetupTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.DeleteExpiredRecords;
import uk.gov.hmcts.reform.roleassignmentbatch.task.RenameTablesPostMigration;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ReplicateTablesTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.task.ValidationTasklet;
import uk.gov.hmcts.reform.roleassignmentbatch.writer.EntityWrapperWriter;

import java.util.List;

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

    public final String REQUEST_QUERY = "INSERT INTO replica_role_assignment_request(id, correlation_id,client_id,"
            + "authenticated_user_id,assigner_id,request_type,"
            + "status,"
            + "process,reference,"
            + "replace_existing,role_assignment_id,log,created)"
            + " VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,"
            + ":requestType,:status,:process,:reference,"
            + ":replaceExisting,"
            + ":roleAssignmentId,:log,:created)";

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
            .resource(new PathResource("src/main/resources/book2.csv"))
            .delimited()
            .names("case_data_id", "user_id", "case_role", "jurisdiction", "case_type", "role_category")
            .lineMapper(lineMapper())
            .fieldSetMapper(new BeanWrapperFieldSetMapper<CcdCaseUser>() {{
                setTargetType(CcdCaseUser.class);
            }})
            .build();
    }

    @Bean
    public LineMapper<CcdCaseUser> lineMapper() {
        final DefaultLineMapper<CcdCaseUser> defaultLineMapper = new DefaultLineMapper<>();
        final DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("case_data_id", "user_id", "case_role", "jurisdiction", "case_type", "role_category");
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
            caseUsers.setCaseDataId(fieldSet.readString("case_data_id"));
            caseUsers.setUserId(fieldSet.readString("user_id"));
            caseUsers.setCaseRole(fieldSet.readString("case_role"));
            caseUsers.setJurisdiction(fieldSet.readString("jurisdiction"));
            caseUsers.setCaseType(fieldSet.readString("case_type"));
            caseUsers.setRoleCategory(fieldSet.readString("role_category"));
            return caseUsers;
        }
    }

    @Bean
    public EntityWrapperProcessor entityWrapperProcessor() {
        return new EntityWrapperProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<RequestEntity> insertInRequestTable() {
        return new JdbcBatchItemWriterBuilder<RequestEntity>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(REQUEST_QUERY)
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<ActorCacheEntity> insertIntoActorCacheTable() {
        return
            new JdbcBatchItemWriterBuilder<ActorCacheEntity>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into replica_actor_cache_control(actor_id,etag,json_response) "
                        + "values(:actorIds,:etag, :roleAssignmentResponse) on conflict(actor_id) do nothing;")
                .dataSource(dataSource)
                .assertUpdates(false)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<HistoryEntity> insertIntoHistoryTable() {
        return
                new JdbcBatchItemWriterBuilder<HistoryEntity>()
                        .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                        .sql("insert into replica_role_assignment_history(id, status, actor_id_type, "
                                + "role_type, role_name, status_sequence, classification, grant_type, "
                                + "read_only, created, "
                                + "actor_id, attributes, request_id) "
                                + "values(:id, :status, :actorIdType, :roleType, :roleName, "
                                + ":sequence, :classification, :grantType,"
                                + ":readOnly, :created, "
                                + ":actorId, :attributes::jsonb, :requestId)")
                        .dataSource(dataSource)
                        .build();
    }

    @Bean
    public JdbcBatchItemWriter<RoleAssignmentEntity> insertIntoRoleAssignmentTable() {
        return
                new JdbcBatchItemWriterBuilder<RoleAssignmentEntity>()
                        .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                        .sql("insert into replica_role_assignment(id, actor_id_type, actor_id, role_type, role_name, "
                                     + "classification, grant_type, role_category, read_only, created, "
                                     + "attributes) "
                                     + "values(:id, :actorIdType, :actorId, :roleType, :roleName, "
                                     + ":classification, :grantType, :roleCategory, :readOnly, :created, "
                                     + ":attributes::jsonb)")
                        .dataSource(dataSource)
                        .build();
    }

    @Bean
    EntityWrapperWriter entityWrapperWriter() {
        return new EntityWrapperWriter();
    }

    @Bean
    CcdToRasSetupTasklet ccdToRasSetupTasklet() {
        return new CcdToRasSetupTasklet(fileName, filePath, containerName, accountName, accountKey);
    }

    @Bean
    ValidationTasklet validationTasklet() {
        return new ValidationTasklet(fileName, filePath, ccdCaseUsersReader());
    }

    @Bean
    ReplicateTablesTasklet replicateTablesTasklet() {
        return new ReplicateTablesTasklet();
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

    @Bean
    public Step ccdToRasSetupStep() {
        return steps.get("ccdToRasSetupStep")
                .tasklet(ccdToRasSetupTasklet())
                .build();
    }

    @Bean
    public Step validationStep(){
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
    public Step ccdToRasStep() {
        return steps.get("ccdToRasStep")
                    .<CcdCaseUser, EntityWrapper>chunk(1000)
                    .reader(ccdCaseUsersReader())
                    .processor(entityWrapperProcessor())
                    .writer(entityWrapperWriter())
                    .taskExecutor(taskExecutor())
                    .throttleLimit(10)
                    .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    public Job ccdToRasBatchJob(@Autowired NotificationListener listener) {
        return jobs.get("ccdToRasBatchJob")
                   .incrementer(new RunIdIncrementer())
                   .listener(listener)
                   .start(ccdToRasSetupStep())
                   .next(validationStep())
                   .next(replicateTables())
                   .next(ccdToRasStep())
                   .next(renameTablesPostMigrationStep())
                   .build();

    }

}
