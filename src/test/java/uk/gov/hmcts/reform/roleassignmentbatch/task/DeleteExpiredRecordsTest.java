package uk.gov.hmcts.reform.roleassignmentbatch.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentHistory;
import uk.gov.hmcts.reform.roleassignmentbatch.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignmentbatch.service.EmailService;

@RunWith(MockitoJUnitRunner.class)
class DeleteExpiredRecordsTest {

    @Mock
    private JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

    @Mock
    StepContribution stepContribution = Mockito.mock(StepContribution.class);

    @Mock
    ChunkContext chunkContext = Mockito.mock(ChunkContext.class);

    @Mock
    ResultSet rs = Mockito.mock(ResultSet.class);

    private DeleteExpiredRecords sut = new DeleteExpiredRecords(jdbcTemplate, 5);

    @Mock
    StepExecution stepExecution = Mockito.mock(StepExecution.class);

    @Mock
    JobExecution jobExecution = Mockito.mock(JobExecution.class);

    @Mock
    EmailService emailService = Mockito.mock(EmailService.class);

    @BeforeAll
    public static void setUp() {
        //MockitoAnnotations.initMocks(this);
    }

    @Test
    void execute() throws IOException {
        Mockito.when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
               .thenReturn(400);

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        Mockito.when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenReturn(list);
        Mockito.when(stepContribution.getStepExecution()).thenReturn(stepExecution);
        Mockito.when(stepContribution.getStepExecution().getJobExecution()).thenReturn(jobExecution);
        Mockito.when(stepContribution.getStepExecution().getJobExecution().getId()).thenReturn(Long.valueOf(1));

        Assertions.assertEquals(RepeatStatus.FINISHED, sut.execute(stepContribution, chunkContext));
    }

    @Test
    void executeThrowsException() throws IOException {
        Mockito.when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
               .thenThrow(NullPointerException.class);

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        Mockito.when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenReturn(list);

        Assertions.assertThrows(NullPointerException.class, () ->
            sut.execute(stepContribution, chunkContext));
    }

    @Test
    void deleteRoleAssignmentRecords() throws IOException {

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        Mockito.when(jdbcTemplate.update(any(), any(), any())).thenReturn(1);

        Assertions.assertEquals(1, sut.deleteRoleAssignmentRecords(list));
    }

    @Test
    void insertIntoRoleAssignmentHistoryTable() throws IOException {

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());
        int[][] data = new int[1][1];
        data[0][0] = 1;
        Mockito.when(jdbcTemplate.batchUpdate(anyString(), any(), anyInt(), any())).thenReturn(data);

        Assertions.assertEquals(data, sut.insertIntoRoleAssignmentHistoryTable(list));
    }

    @Test
    void getLiveRecordsFromHistoryTable() throws IOException {
        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());
        Mockito.when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenReturn(list);
        Assertions.assertEquals(list, sut.getLiveRecordsFromHistoryTable());
    }

    @Test
    void getLiveRecordsFromHistoryTableWithValidValues() {
        LocalDateTime timeStamp = LocalDateTime.now();
        Timestamp beginDate = Timestamp.valueOf(timeStamp.plusDays(1));
        Timestamp endDate = Timestamp.valueOf(timeStamp.plusMonths(1));
        Timestamp created = Timestamp.valueOf(timeStamp);
        Mockito.when(jdbcTemplate.query(
            ArgumentMatchers.anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenAnswer((invocation) -> {

                   final ResultSetExtractor<List<RoleAssignmentHistory>> resultSetExtractor =
                       invocation.getArgument(1);
                   Mockito.when(rs.next()).thenReturn(true, false);

                   Mockito.when(rs.getObject(ArgumentMatchers.eq("id")))
                          .thenReturn(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"));
                   Mockito.when(rs.getString(ArgumentMatchers.eq("request_id")))
                          .thenReturn("123e4567-e89b-42d3-a456-556642445678");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("actor_id_type"))).thenReturn(ActorIdType.IDAM.name());
                   Mockito.when(rs.getObject(ArgumentMatchers.eq("actor_id")))
                          .thenReturn("3168da13-00b3-41e3-81fa-cbc71ac28a0f");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("role_type")))
                          .thenReturn(RoleType.CASE.name());
                   Mockito.when(rs.getString(ArgumentMatchers.eq("role_name"))).thenReturn("Judge");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("classification")))
                          .thenReturn(Classification.PUBLIC.name());
                   Mockito.when(rs.getString(ArgumentMatchers.eq("grant_type"))).thenReturn(GrantType.STANDARD.name());
                   Mockito.when(rs.getString(ArgumentMatchers.eq("role_category")))
                          .thenReturn(RoleCategory.JUDICIAL.name());
                   Mockito.when(rs.getBoolean(ArgumentMatchers.eq("read_only"))).thenReturn(true);
                   Mockito.when(rs.getTimestamp(ArgumentMatchers.eq("begin_time"))).thenReturn(beginDate);
                   Mockito.when(rs.getTimestamp(ArgumentMatchers.eq("end_time"))).thenReturn(endDate);
                   Mockito.when(rs.getString(ArgumentMatchers.eq("status"))).thenReturn(Status.LIVE.toString());
                   Mockito.when(rs.getString(ArgumentMatchers.eq("reference"))).thenReturn("reference");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("process"))).thenReturn("process");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("attributes"))).thenReturn("attributes");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("notes"))).thenReturn("notes");
                   Mockito.when(rs.getString(ArgumentMatchers.eq("log"))).thenReturn("logs");
                   Mockito.when(rs.getInt(ArgumentMatchers.eq("status_sequence"))).thenReturn(1);
                   Mockito.when(rs.getTimestamp(ArgumentMatchers.eq("created"))).thenReturn(created);
                   return resultSetExtractor.extractData(rs);
               });

        List<RoleAssignmentHistory> result = sut.getLiveRecordsFromHistoryTable();
        Assertions.assertEquals("IDAM", result.get(0).getActorIDType());
        Assertions.assertEquals("CASE", result.get(0).getRoleType());
        Assertions.assertEquals("Judge", result.get(0).getRoleName());
        Assertions.assertEquals("PUBLIC", result.get(0).getClassification());
        Assertions.assertEquals("STANDARD", result.get(0).getGrantType());
        Assertions.assertEquals("JUDICIAL", result.get(0).getRoleCategory());
        Assertions.assertEquals(true, result.get(0).isReadOnly());
        Assertions.assertEquals("LIVE", result.get(0).getStatus());
        Assertions.assertEquals(beginDate, result.get(0).getBeginTime());
        Assertions.assertEquals(endDate, result.get(0).getEndTime());
        Assertions.assertEquals("reference", result.get(0).getReference());
        Assertions.assertEquals("process", result.get(0).getProcess());
        Assertions.assertEquals("attributes", result.get(0).getAttributes());
        Assertions.assertEquals("notes", result.get(0).getNotes());
        Assertions.assertEquals("logs", result.get(0).getLog());
        Assertions.assertEquals(1, result.get(0).getStatusSequence());
        Assertions.assertEquals(created, result.get(0).getCreated());
    }

    @Test
    void getCountFromHistoryTable() {
        Mockito.when(jdbcTemplate.queryForObject("SELECT count(*) from role_assignment_history rah", Integer.class))
               .thenReturn(400);
        Assertions.assertEquals(400, sut.getCountFromHistoryTable());
    }
}
