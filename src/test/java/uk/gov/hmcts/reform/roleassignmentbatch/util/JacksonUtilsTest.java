package uk.gov.hmcts.reform.roleassignmentbatch.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentHistory;
import uk.gov.hmcts.reform.roleassignmentbatch.helper.TestDataBuilder;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.roleassignmentbatch.helper.TestDataBuilder.TEST_ACTOR_ID;

@RunWith(MockitoJUnitRunner.class)
class JacksonUtilsTest {

    @Test
    void convertValue() throws IOException {

        // GIVEN
        JsonNode input = TestDataBuilder.buildAttributesFromFile();

        // WHEN
        Map<String, JsonNode> jsonNodeMap = JacksonUtils.convertValue(input);

        // THEN
        assertNotNull(jsonNodeMap);
        assertEquals("north-east",jsonNodeMap.get("region").asText());
        assertEquals("divorce",jsonNodeMap.get("jurisdiction").asText());

    }

    @Test
    void convertObjectIntoJsonNode() throws IOException {

        // GIVEN
        RoleAssignmentHistory input = TestDataBuilder.buildRoleAssignmentHistory();

        // WHEN
        JsonNode jsonNode = JacksonUtils.convertValueJsonNode(input);

        // THEN
        assertNotNull(jsonNode);
        assertNotNull(jsonNode.findValue("actorId"));
        assertEquals(TEST_ACTOR_ID, jsonNode.findValue("actorId").asText());

    }

    @Test
    void getHashMapTypeReference() {
        assertNotNull(JacksonUtils.getHashMapTypeReference());
    }

}
