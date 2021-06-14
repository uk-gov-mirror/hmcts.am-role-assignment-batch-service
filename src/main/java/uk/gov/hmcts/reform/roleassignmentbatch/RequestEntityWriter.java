package uk.gov.hmcts.reform.roleassignmentbatch;

import java.util.List;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;

@Configuration
public class RequestEntityWriter implements ItemWriter<RequestEntity> {

    //private final DataSource dataSource;
//
    @Autowired
    EntityManager entityManager;

//    @Autowired
//    EntityTransaction txn;
    @Autowired
    private RequestRepository requestRepository;

    private StepExecution stepExecution;
    private final String query = "INSERT INTO role_assignment_request (id, correlation_id,client_id,authenticated_user_id,assigner_id,request_type,status," +
                                 "process,reference," +
                                 "replace_existing,role_assignment_id,log,created)" +
                                 " VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,:requestType,:status,:process,:reference," +
                                 ":replaceExisting," +
                                 ":roleAssignmentId,:log,:created)";

   /* public RequestEntityWriter(@Autowired DataSource dataSource) {
        this.dataSource = dataSource;
    }
*/
    /**
     * Process the supplied data element. Will not be called with any null items
     * in normal operation.
     *
     * @param items items to be written
     * @throws Exception if there are errors. The framework will catch the
     *                   exception and convert or rethrow it as appropriate.
     */
    @Transactional
    public void write(List<? extends RequestEntity> items) throws Exception {

        try {
            System.out.println("items.size is :" + items.size());
            Iterable<? extends RequestEntity> itemList = requestRepository.saveAll(items);
//            txn.begin();
            entityManager.persist(items.get(0));
//            txn.commit();
            System.out.println(requestRepository.findAll());
            System.out.println("Persistance Complete: " + itemList);
        } catch(Exception e) {
            e.printStackTrace();
        }


    }

}
