package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.HistoryEntity;

public class HistoryEntityProcessor implements ItemProcessor<HistoryEntity, HistoryEntity> {


    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param historyEntity to be processed
     * @return potentially modified or new item for continued processing, {@code null} if processing of the
     * provided item should not continue.
     * @throws Exception thrown if exception occurs during processing.
     */
    @Override
    public HistoryEntity process(HistoryEntity historyEntity) throws Exception {
        final UUID id = historyEntity.getId();
        final String classification = historyEntity.getLog();
        System.out.println(id);
        System.out.println(classification);

        return historyEntity;
    }
}