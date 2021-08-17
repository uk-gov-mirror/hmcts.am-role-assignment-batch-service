package uk.gov.hmcts.reform.roleassignmentbatch.config;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.START_DATE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.REFERENCE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_ROLE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.JURISDICTION;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ROLE_CATEGORY;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.USER_ID;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.launchdarkly.sdk.server.LDClient;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.AuditFaults;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.rowmappers.CcdViewRowMapper;
import uk.gov.hmcts.reform.roleassignmentbatch.util.Constants;

@Component
public class ConfigurationBeans {

    @Autowired
    DataSource dataSource;

    @Autowired
    PagingQueryProvider queryProvider;

    @Value("${migration.chunkSize}")
    private int chunkSize;

    @Value("${csv-file-name}")
    String fileName;

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
                .sql("insert into ccd_user_view(reference,user_id,case_role,jurisdiction,case_type_id,role_category,"
                     + "start_date) values (:reference,:userId,:caseRole,:jurisdiction,:caseTypeId,:roleCategory,"
                     + ":startDate)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        return new LDClient(sdkKey);
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
    public JdbcPagingItemReader<CcdCaseUser> databaseItemReader(@Autowired CcdViewRowMapper ccdViewRowMapper) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("status", "NEW");

        return new JdbcPagingItemReaderBuilder<CcdCaseUser>()
            .name("ccdCaseUserReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            //.parameterValues(parameterValues)
            .rowMapper(ccdViewRowMapper)
            .saveState(false)
            .pageSize(chunkSize)
            .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean queryProvider() {
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

        provider.setSelectClause("select row_no,reference,user_id,case_role,jurisdiction,case_type_id,role_category,"
                                 + "start_date");
        provider.setFromClause("from ccd_user_view");
        //provider.setWhereClause("where status=:status");
        provider.setSortKey("row_no");
        provider.setDataSource(dataSource);

        return provider;
    }

    @Bean
    public FlatFileItemReader<CcdCaseUser> ccdCaseUsersReader() {
        return new FlatFileItemReaderBuilder<CcdCaseUser>()
            .name("historyEntityReader")
            .linesToSkip(1)
            .saveState(false)
            .resource(new ClassPathResource(fileName))
            .delimited()
            .names(REFERENCE, USER_ID, CASE_ROLE, JURISDICTION, CASE_TYPE_ID, ROLE_CATEGORY, START_DATE)
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
        lineTokenizer.setNames(REFERENCE, USER_ID, CASE_ROLE, JURISDICTION, CASE_TYPE_ID, ROLE_CATEGORY, START_DATE);
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
            caseUsers.setReference(fieldSet.readString(REFERENCE));
            caseUsers.setUserId(fieldSet.readString(USER_ID));
            caseUsers.setCaseRole(fieldSet.readString(CASE_ROLE));
            caseUsers.setJurisdiction(fieldSet.readString(JURISDICTION));
            caseUsers.setCaseTypeId(fieldSet.readString(CASE_TYPE_ID));
            caseUsers.setRoleCategory(fieldSet.readString(ROLE_CATEGORY));
            caseUsers.setStartDate(fieldSet.readString(START_DATE));
            return caseUsers;
        }
    }

}
