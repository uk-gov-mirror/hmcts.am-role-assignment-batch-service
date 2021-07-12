package uk.gov.hmcts.reform.roleassignmentbatch.writer;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentEntity;

public class EntityWrapperWriter implements ItemWriter<EntityWrapper> {

    @Autowired
    private JdbcBatchItemWriter<RequestEntity> requestEntityWriter;

    @Autowired
    private JdbcBatchItemWriter<HistoryEntity> roleAssignmentHistoryWriter;

    @Autowired
    private JdbcBatchItemWriter<RoleAssignmentEntity> roleAssignmentWriter;

    @Autowired
    private JdbcBatchItemWriter<ActorCacheEntity> actorCacheWriter;

    @Override
    public void write(List<? extends EntityWrapper> items) throws Exception {
        for (EntityWrapper item : items) {
            requestEntityWriter.write(Collections.singletonList(item.getRequestEntity()));
            roleAssignmentHistoryWriter.write(Collections.singletonList(item.getRoleAssignmentHistoryEntity()));
            roleAssignmentWriter.write(Collections.singletonList(item.getRoleAssignmentEntity()));
            // Actor cache entity will be built in a separate step.
        }
    }
}
