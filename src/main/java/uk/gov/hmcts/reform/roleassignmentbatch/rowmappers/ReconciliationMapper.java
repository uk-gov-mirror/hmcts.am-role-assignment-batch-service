package uk.gov.hmcts.reform.roleassignmentbatch.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ReconciliationData;

@Component
public class ReconciliationMapper implements RowMapper<ReconciliationData> {

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
