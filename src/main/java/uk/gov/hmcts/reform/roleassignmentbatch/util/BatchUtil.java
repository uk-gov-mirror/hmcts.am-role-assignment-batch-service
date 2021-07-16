package uk.gov.hmcts.reform.roleassignmentbatch.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentHistory;

public class BatchUtil {
    private BatchUtil() {
    }

    public static ParameterizedPreparedStatementSetter<RoleAssignmentHistory> prepareSetterForRoleAssignmentHistory() {
        return new ParameterizedPreparedStatementSetter<RoleAssignmentHistory>() {
            @Override
            public void setValues(PreparedStatement ps, RoleAssignmentHistory roleAssignmentHistory)
                throws SQLException {
                ps.setObject(1, roleAssignmentHistory.getId());
                ps.setObject(2, roleAssignmentHistory.getRequestId());
                ps.setString(3, roleAssignmentHistory.getActorIDType());
                ps.setObject(4, roleAssignmentHistory.getActorId());
                ps.setString(5, roleAssignmentHistory.getRoleType());
                ps.setString(6, roleAssignmentHistory.getRoleName());
                ps.setString(7, roleAssignmentHistory.getClassification());
                ps.setString(8, roleAssignmentHistory.getGrantType());
                ps.setString(9, roleAssignmentHistory.getRoleCategory());
                ps.setBoolean(10, roleAssignmentHistory.isReadOnly());
                ps.setTimestamp(11, roleAssignmentHistory.getBeginTime());
                ps.setTimestamp(12, roleAssignmentHistory.getEndTime());
                ps.setString(13, roleAssignmentHistory.getStatus());
                ps.setString(14, roleAssignmentHistory.getReference());
                ps.setString(15, roleAssignmentHistory.getProcess());
                ps.setString(16, roleAssignmentHistory.getAttributes());
                ps.setString(17, roleAssignmentHistory.getNotes());
                ps.setString(18, roleAssignmentHistory.getLog());
                ps.setInt(19, roleAssignmentHistory.getStatusSequence());
                ps.setTimestamp(20, roleAssignmentHistory.getCreated());
            }
        };
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
