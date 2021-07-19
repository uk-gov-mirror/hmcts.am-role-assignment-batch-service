package uk.gov.hmcts.reform.roleassignmentbatch.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;

@Component
public class ReconciliationMapper implements RowMapper<ReconciliationData> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    ObjectMapper mapper;

    public ReconciliationMapper() {
        mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    @SneakyThrows
    @Override
    public ReconciliationData mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ReconciliationData.builder()
                                 .runId(rs.getString("run_id"))
                                 .createdDate(rs.getObject("created_date", LocalDateTime.class))
                                 .ccdJurisdictionData(rs.getString("ccd_jurisdiction_data"))
                                 .ccdRoleNameData(rs.getString("ccd_role_name_data"))
                                 .amJurisdictionData(rs.getString("am_jurisdiction_data"))
                                 .amRoleNameData(rs.getString("am_role_name_data"))
                                 .ccdJurisdictionData(rs.getString("ccd_jurisdiction_data"))
                                 .totalCountFromCcd(rs.getInt("total_count_from_ccd"))
                                 .totalCountFromAm(rs.getInt("total_count_from_am"))
                                 .status(rs.getString("status"))
                                 .notes(rs.getString("notes"))
                                 .build();


    }

}
