package uk.gov.hmcts.reform.roleassignmentbatch.writer;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.domain.model.CcdCaseUser;

public class CcdViewWriterTemp implements ItemWriter<CcdCaseUser> {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    JdbcBatchItemWriter<CcdCaseUser> ccdCaseUserJdbcBatchItemWriter;
    @Override
    public void write(List<? extends CcdCaseUser> items) throws Exception {
        //ccdCaseUserJdbcBatchItemWriter.write(items);
        for(int i = 0; i<=33; i++) {
            for(CcdCaseUser user: items) {
                ccdCaseUserJdbcBatchItemWriter.write(Collections.singletonList(user));
            }

        }
    }
}
