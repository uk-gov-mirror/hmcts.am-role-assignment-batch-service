package uk.gov.hmcts.reform.roleassignmentbatch.writer;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;

public class EntityWrapperWriter implements ItemWriter<EntityWrapper> {

    @Autowired
    private JdbcBatchItemWriter<Newtable> newtableWriter;
    @Autowired
    private JdbcBatchItemWriter<RequestEntity> requestEntityWriter;

    @Override
    public void write(List<? extends EntityWrapper> items) throws Exception {
        for (EntityWrapper item: items) {
            newtableWriter.write(Collections.singletonList(item.getNewtable()));
            requestEntityWriter.write(Collections.singletonList(item.getRequestEntity()));
        }
    }
}
