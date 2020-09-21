package uk.gov.hmcts.reform.roleassignmentbatch1.task;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

public class DeleteExpiredRecordsTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    DeleteExpiredRecords sut = new DeleteExpiredRecords(jdbcTemplate, 5);

    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

}
