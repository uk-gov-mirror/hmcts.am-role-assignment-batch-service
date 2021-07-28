package uk.gov.hmcts.reform.roleassignmentbatch.writer;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.EntityWrapper;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RoleAssignmentEntity;

@Slf4j
public class EntityWrapperWriter implements ItemWriter<EntityWrapper> {

    @Autowired
    private JdbcBatchItemWriter<RequestEntity> requestEntityWriter;

    @Autowired
    private JdbcBatchItemWriter<HistoryEntity> roleAssignmentHistoryWriter;

    @Autowired
    private JdbcBatchItemWriter<RoleAssignmentEntity> roleAssignmentWriter;

    @Override
    public void write(List<? extends EntityWrapper> items) throws Exception {
        final long currentTime = System.currentTimeMillis();

        List<RequestEntity> requestEntities = items.stream()
                                                   .map(EntityWrapper::getRequestEntity)
                                                   .collect(Collectors.toList());
        requestEntityWriter.write(requestEntities);

        List<HistoryEntity> historyEntities = items.stream()
                                                   .map(EntityWrapper::getRoleAssignmentHistoryEntity)
                                                   .collect(Collectors.toList());
        roleAssignmentHistoryWriter.write(historyEntities);

        List<RoleAssignmentEntity> roleAssignmentEntities = items.stream()
                                                                 .map(EntityWrapper::getRoleAssignmentEntity)
                                                                 .collect(Collectors.toList());
        roleAssignmentWriter.write(roleAssignmentEntities);

        log.info("Time taken for :" + items.size() + " items is : "
                           + (System.currentTimeMillis() - currentTime));
    }
}
