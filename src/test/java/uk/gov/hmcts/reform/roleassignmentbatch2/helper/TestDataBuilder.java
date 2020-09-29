package uk.gov.hmcts.reform.roleassignmentbatch2.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignmentbatch2.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignmentbatch2.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignmentbatch2.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignmentbatch2.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignmentbatch2.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentbatch2.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentbatch2.task.RoleAssignmentHistory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static RoleAssignmentHistory buildRoleAssignmentHistory() throws IOException {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignmentHistory
                .builder()
                .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
                .actorId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .actorIDType(ActorIdType.IDAM.name())
                .attributes(buildAttributesFromFile().toString())
                .created(Timestamp.valueOf(timeStamp))
                .beginTime(Timestamp.valueOf(timeStamp.plusDays(1)))
                .endTime(Timestamp.valueOf(timeStamp.plusMonths(1)))
                .reference("reference")
                .process(("process"))
                .statusSequence(10)
                .status(Status.LIVE.toString())
                .classification(Classification.PUBLIC.name())
                .grantType(GrantType.STANDARD.name())
                .log("log")
                .notes(buildNotesFromFile().toString())
                .readOnly(false)
                .requestId(UUID.fromString("123e4567-e89b-42d3-a456-556642445678"))
                .roleType(RoleType.CASE.name())
                .roleName("judge")
                .roleCategory(RoleCategory.JUDICIAL.name())
                .build();
    }

    private static JsonNode buildAttributesFromFile() throws IOException {
        InputStream inputStream =
                TestDataBuilder.class.getClassLoader().getResourceAsStream("attributes.json");
        assert inputStream != null;
        return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
        });
    }

    private static JsonNode buildNotesFromFile() throws IOException {
        InputStream inputStream =
                TestDataBuilder.class.getClassLoader().getResourceAsStream("notes.json");
        assert inputStream != null;
        return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
        });
    }

}