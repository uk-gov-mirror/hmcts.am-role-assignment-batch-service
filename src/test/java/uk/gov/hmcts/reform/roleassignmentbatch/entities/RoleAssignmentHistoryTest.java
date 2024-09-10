package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentbatch.helper.TestDataBuilder;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleAssignmentHistoryTest {

    RoleAssignmentHistory roleAssignmentHistoryNoArgs = new RoleAssignmentHistory();

    RoleAssignmentHistory roleAssignmentHistory = TestDataBuilder.buildRoleAssignmentHistory();

    RoleAssignmentHistoryTest() throws IOException {
    }

    @Test
    void testToString() {
        assertTrue(roleAssignmentHistory.toString().contains("id="));
    }

    @Test
    void getters() {
        assertEquals(("21334a2b-79ce-44eb-9168-2d49a744be9c"), roleAssignmentHistory.getActorId());
        assertEquals(ActorIdType.IDAM.name(), roleAssignmentHistory.getActorIDType());
        assertTrue(roleAssignmentHistory.getAttributes().contains("jurisdiction"));
        assertNotNull(roleAssignmentHistory.getBeginTime().toLocalDateTime());
        assertEquals(Classification.PUBLIC.name(), roleAssignmentHistory.getClassification());
        assertNotNull(roleAssignmentHistory.getCreated().toLocalDateTime());
        assertNotNull(roleAssignmentHistory.getEndTime().toLocalDateTime());
        assertEquals(GrantType.STANDARD.name(), roleAssignmentHistory.getGrantType());
        assertEquals(("9785c98c-78f2-418b-ab74-a892c3ccca9f"), roleAssignmentHistory.getId());
        assertEquals("log", roleAssignmentHistory.getLog());
        assertTrue(roleAssignmentHistory.getNotes().contains("comment"));
        assertEquals("process", roleAssignmentHistory.getProcess());
        assertEquals("reference", roleAssignmentHistory.getReference());
        assertEquals(UUID.fromString("123e4567-e89b-42d3-a456-556642445678"), roleAssignmentHistory.getRequestId());
        assertEquals(RoleCategory.JUDICIAL.name(), roleAssignmentHistory.getRoleCategory());
        assertEquals("judge", roleAssignmentHistory.getRoleName());
        assertEquals(RoleType.CASE.name(), roleAssignmentHistory.getRoleType());
        assertEquals(Status.LIVE.toString(), roleAssignmentHistory.getStatus());
        assertEquals(10, roleAssignmentHistory.getStatusSequence());
    }

    @Test
    void setters() {
        assertNotNull(roleAssignmentHistoryNoArgs);

        roleAssignmentHistoryNoArgs.setActorId(("21334a2b-79ce-44eb-9168-2d49a744be9b"));
        assertEquals("21334a2b-79ce-44eb-9168-2d49a744be9b",
                roleAssignmentHistoryNoArgs.getActorId());

        roleAssignmentHistoryNoArgs.setId(("21334a2b-79ce-44eb-9168-2d49a744be9b"));
        assertEquals("21334a2b-79ce-44eb-9168-2d49a744be9b",
                roleAssignmentHistoryNoArgs.getActorId());

        roleAssignmentHistoryNoArgs.setRequestId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9b"));
        assertEquals("21334a2b-79ce-44eb-9168-2d49a744be9b",
                roleAssignmentHistoryNoArgs.getActorId());

        roleAssignmentHistoryNoArgs.setActorIDType(ActorIdType.IDAM.toString());
        assertEquals(ActorIdType.IDAM.toString(),
                roleAssignmentHistoryNoArgs.getActorIDType());

        roleAssignmentHistoryNoArgs.setRoleType(RoleType.CASE.toString());
        assertEquals(RoleType.CASE.toString(),
                roleAssignmentHistoryNoArgs.getRoleType());

        roleAssignmentHistoryNoArgs.setRoleName("role");
        assertEquals("role",
                roleAssignmentHistoryNoArgs.getRoleName());

        roleAssignmentHistoryNoArgs.setClassification(Classification.PUBLIC.name());
        assertEquals(Classification.PUBLIC.toString(),
                roleAssignmentHistoryNoArgs.getClassification());

        roleAssignmentHistoryNoArgs.setGrantType(GrantType.STANDARD.toString());
        assertEquals(GrantType.STANDARD.toString(),
                roleAssignmentHistoryNoArgs.getGrantType());

        roleAssignmentHistoryNoArgs.setRoleCategory(RoleCategory.JUDICIAL.toString());
        assertEquals(RoleCategory.JUDICIAL.toString(),
                roleAssignmentHistoryNoArgs.getRoleCategory());

        roleAssignmentHistoryNoArgs.setReadOnly(true);
        assertTrue(roleAssignmentHistoryNoArgs.isReadOnly());

        roleAssignmentHistoryNoArgs.setReadOnly(false);
        assertFalse(roleAssignmentHistoryNoArgs.isReadOnly());

        roleAssignmentHistoryNoArgs.setBeginTime(Timestamp.valueOf(LocalDateTime.now()));
        assertNotNull(roleAssignmentHistoryNoArgs.getBeginTime().toLocalDateTime());

        roleAssignmentHistoryNoArgs.setEndTime(Timestamp.valueOf(LocalDateTime.now()));
        assertNotNull(roleAssignmentHistoryNoArgs.getEndTime().toLocalDateTime());

        roleAssignmentHistoryNoArgs.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        assertNotNull(roleAssignmentHistoryNoArgs.getCreated().toLocalDateTime());

        roleAssignmentHistoryNoArgs.setProcess("pro");
        assertEquals("pro", roleAssignmentHistoryNoArgs.getProcess());

        roleAssignmentHistoryNoArgs.setReference("ref");
        assertEquals("ref", roleAssignmentHistoryNoArgs.getReference());

        roleAssignmentHistoryNoArgs.setAttributes("att");
        assertEquals("att", roleAssignmentHistoryNoArgs.getAttributes());

        roleAssignmentHistoryNoArgs.setNotes("note");
        assertEquals("note", roleAssignmentHistoryNoArgs.getNotes());

        roleAssignmentHistoryNoArgs.setLog("log");
        assertEquals("log", roleAssignmentHistoryNoArgs.getLog());

        roleAssignmentHistoryNoArgs.setStatus(Status.APPROVED.toString());
        assertEquals(Status.APPROVED.toString(), roleAssignmentHistoryNoArgs.getStatus());

        roleAssignmentHistoryNoArgs.setStatusSequence(10);
        assertEquals(10, roleAssignmentHistoryNoArgs.getStatusSequence());
    }
}