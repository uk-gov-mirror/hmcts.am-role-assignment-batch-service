package uk.gov.hmcts.reform.roleassignmentbatch.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class DeleteExpiredRecordsTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    StepContribution stepContribution;

    @Mock
    ChunkContext chunkContext;

    @Mock
    ResultSet rs;

    @Mock
    StepExecution stepExecution;

    @Mock
    JobExecution jobExecution;

    @Mock
    EmailService emailService;

    private DeleteExpiredRecords sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.sut = new DeleteExpiredRecords(emailService, jdbcTemplate, 5);
    }

    @Test
    void execute_mailEnabled() throws IOException {

        // GIVEN
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
               .thenReturn(400);

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenReturn(list);
        when(stepContribution.getStepExecution()).thenReturn(stepExecution);
        when(stepContribution.getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(stepContribution.getStepExecution().getJobExecution().getId()).thenReturn(Long.valueOf(1));

        when(emailService.isMailEnabled()).thenReturn(true);

        // WHEN
        RepeatStatus result = sut.execute(stepContribution, chunkContext);

        // THEN
        assertEquals(RepeatStatus.FINISHED, result);
        verify(emailService, times(1)).sendEmail(any());
    }

    @Test
    void execute_mailNotEnabled() throws IOException {

        // GIVEN
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
            .thenReturn(400);

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
            .thenReturn(list);
        when(stepContribution.getStepExecution()).thenReturn(stepExecution);
        when(stepContribution.getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(stepContribution.getStepExecution().getJobExecution().getId()).thenReturn(Long.valueOf(1));

        when(emailService.isMailEnabled()).thenReturn(false);

        // WHEN
        RepeatStatus result = sut.execute(stepContribution, chunkContext);

        // THEN
        assertEquals(RepeatStatus.FINISHED, result);
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void executeThrowsException() throws IOException {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
               .thenThrow(NullPointerException.class);

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenReturn(list);

        assertThrows(NullPointerException.class, () ->
            sut.execute(stepContribution, chunkContext));
    }

    @Test
    void deleteRoleAssignmentRecords() throws IOException {

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());

        when(jdbcTemplate.update(any(), any(), any())).thenReturn(1);

        assertEquals(1, sut.deleteRoleAssignmentRecords(list));
    }

    @Test
    void insertIntoRoleAssignmentHistoryTable() throws IOException {

        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());
        int[][] data = new int[1][1];
        data[0][0] = 1;
        when(jdbcTemplate.batchUpdate(anyString(), any(), anyInt(), any())).thenReturn(data);

        assertEquals(data, sut.insertIntoRoleAssignmentHistoryTable(list));
    }

    @Test
    void getLiveRecordsFromHistoryTable() throws IOException {
        List<RoleAssignmentHistory> list = new ArrayList<>();
        list.add(TestDataBuilder.buildRoleAssignmentHistory());
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenReturn(list);
        assertEquals(list, sut.getLiveRecordsFromHistoryTable());
    }

    @Test
    void getLiveRecordsFromHistoryTableWithValidValues() {
        LocalDateTime timeStamp = LocalDateTime.now();
        Timestamp beginDate = Timestamp.valueOf(timeStamp.plusDays(1));
        Timestamp endDate = Timestamp.valueOf(timeStamp.plusMonths(1));
        Timestamp created = Timestamp.valueOf(timeStamp);
        when(jdbcTemplate.query(
            ArgumentMatchers.anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any()))
               .thenAnswer(invocation -> {

                   final ResultSetExtractor<List<RoleAssignmentHistory>> resultSetExtractor =
                       invocation.getArgument(1);
                   when(rs.next()).thenReturn(true, false);

                   when(rs.getObject("id"))
                           .thenReturn(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"));
                   when(rs.getString("request_id")).thenReturn("123e4567-e89b-42d3-a456-556642445678");
                   when(rs.getString("actor_id_type")).thenReturn(ActorIdType.IDAM.name());
                   when(rs.getObject("actor_id")).thenReturn("3168da13-00b3-41e3-81fa-cbc71ac28a0f");
                   when(rs.getString("role_type")).thenReturn(RoleType.CASE.name());
                   when(rs.getString("role_name")).thenReturn("Judge");
                   when(rs.getString("classification")).thenReturn(Classification.PUBLIC.name());
                   when(rs.getString("grant_type")).thenReturn(GrantType.STANDARD.name());
                   when(rs.getString("role_category")).thenReturn(RoleCategory.JUDICIAL.name());
                   when(rs.getBoolean("read_only")).thenReturn(true);
                   when(rs.getTimestamp("begin_time")).thenReturn(beginDate);
                   when(rs.getTimestamp("end_time")).thenReturn(endDate);
                   when(rs.getString("status")).thenReturn(Status.LIVE.toString());
                   when(rs.getString("reference")).thenReturn("reference");
                   when(rs.getString("process")).thenReturn("process");
                   when(rs.getString("attributes")).thenReturn("attributes");
                   when(rs.getString("notes")).thenReturn("notes");
                   when(rs.getString("log")).thenReturn("logs");
                   when(rs.getInt("status_sequence")).thenReturn(1);
                   when(rs.getTimestamp("created")).thenReturn(created);
                   return resultSetExtractor.extractData(rs);
               });

        List<RoleAssignmentHistory> result = sut.getLiveRecordsFromHistoryTable();
        assertEquals("IDAM", result.get(0).getActorIDType());
        assertEquals("CASE", result.get(0).getRoleType());
        assertEquals("Judge", result.get(0).getRoleName());
        assertEquals("PUBLIC", result.get(0).getClassification());
        assertEquals("STANDARD", result.get(0).getGrantType());
        assertEquals("JUDICIAL", result.get(0).getRoleCategory());
        assertTrue(result.get(0).isReadOnly());
        assertEquals("LIVE", result.get(0).getStatus());
        assertEquals(beginDate, result.get(0).getBeginTime());
        assertEquals(endDate, result.get(0).getEndTime());
        assertEquals("reference", result.get(0).getReference());
        assertEquals("process", result.get(0).getProcess());
        assertEquals("attributes", result.get(0).getAttributes());
        assertEquals("notes", result.get(0).getNotes());
        assertEquals("logs", result.get(0).getLog());
        assertEquals(1, result.get(0).getStatusSequence());
        assertEquals(created, result.get(0).getCreated());
    }

    @Test
    void getCountFromHistoryTable() {
        when(jdbcTemplate.queryForObject("SELECT count(*) from role_assignment_history rah", Integer.class))
               .thenReturn(400);
        assertEquals(400, sut.getCountFromHistoryTable());
    }

}
