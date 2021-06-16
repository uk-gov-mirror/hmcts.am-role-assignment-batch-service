package uk.gov.hmcts.reform.roleassignmentbatch.task;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import uk.gov.hmcts.reform.domain.model.CcdCaseUsers;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;


public class NewTableProcessor implements ItemProcessor<CcdCaseUsers, Newtable> {


    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param ccdCaseUsers to be processed
     * @return potentially modified or new item for continued processing, {@code null} if processing of the
     * provided item should not continue.
     * @throws Exception thrown if exception occurs during processing.
     */
    @Override
    public Newtable process(CcdCaseUsers ccdCaseUsers) throws Exception {
        return Newtable.builder()
                       .myid(UUID.randomUUID().toString()).column2(UUID.randomUUID().toString()).build();
    }
}