package uk.gov.hmcts.reform.roleassignmentbatch.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.RoleConfigRole;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;

@Slf4j
@Named
@Singleton
public class JacksonUtils {

    public static final JsonFactory jsonFactory = JsonFactory.builder()
            // Change per-factory setting to prevent use of `String.intern()` on symbols
            .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
            .build();
    public static final ObjectMapper MAPPER = JsonMapper.builder(jsonFactory)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .build();
    @Getter
    private static final Map<String, List<RoleConfigRole>> configuredRoles = new HashMap<>();

    private JacksonUtils() {
    }

    public static Map<String, JsonNode> convertValue(Object from) {
        return MAPPER.convertValue(from, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static JsonNode convertValueJsonNode(Object from) {
        return MAPPER.convertValue(from, JsonNode.class);
    }

    public static TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }
}