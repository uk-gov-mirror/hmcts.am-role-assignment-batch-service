package uk.gov.hmcts.reform.roleassignmentbatch.writer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.CcdCaseUser;

public class CcdViewWriterTemp implements ItemWriter<CcdCaseUser> {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    JdbcBatchItemWriter<CcdCaseUser> ccdCaseUserJdbcBatchItemWriter;

    @Override
    public void write(List<? extends CcdCaseUser> items) throws Exception {
        List<String> uuidList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            uuidList.add("userId" + i);
        }
        Random random = SecureRandom.getInstanceStrong();
        //ccdCaseUserJdbcBatchItemWriter.write(items)

        for (CcdCaseUser user : items) {
            user.setUserId(uuidList.get(random.nextInt(uuidList.size())));
        }
        for (int i = 0; i <= 0; i++) {
            //ccdCaseUserJdbcBatchItemWriter.write(items);

        }
    }
}
