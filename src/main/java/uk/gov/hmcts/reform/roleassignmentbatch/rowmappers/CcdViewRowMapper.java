package uk.gov.hmcts.reform.roleassignmentbatch.rowmappers;

import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.BEGIN_DATE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_ROLE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.JURISDICTION;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.ROLE_CATEGORY;
import static uk.gov.hmcts.reform.roleassignmentbatch.util.Constants.USER_ID;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;

@Component
public class CcdViewRowMapper implements RowMapper<CcdCaseUser> {

    @Override
    public CcdCaseUser mapRow(ResultSet rs, int rowNum) throws SQLException {

        CcdCaseUser ccdCaseUser = new CcdCaseUser();
        ccdCaseUser.setCaseDataId(rs.getString(CASE_DATA_ID).trim());
        ccdCaseUser.setUserId(rs.getString(USER_ID).trim());
        ccdCaseUser.setCaseRole(rs.getString(CASE_ROLE).trim());
        ccdCaseUser.setCaseType(rs.getString(CASE_TYPE).trim());
        ccdCaseUser.setBeginDate(rs.getString(BEGIN_DATE).trim());
        ccdCaseUser.setRoleCategory(rs.getString(ROLE_CATEGORY).trim());
        ccdCaseUser.setJurisdiction(rs.getString(JURISDICTION).trim());
        return ccdCaseUser;
    }
}