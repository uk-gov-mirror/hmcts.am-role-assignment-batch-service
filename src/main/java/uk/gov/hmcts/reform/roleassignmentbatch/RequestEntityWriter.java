package uk.gov.hmcts.reform.roleassignmentbatch;

import java.util.List;
import java.util.UUID;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.util.NewtableRepository;

@Component
public class RequestEntityWriter implements ItemWriter<RequestEntity> {

    @Autowired
    private final RequestRepository requestRepository;


    final ActorCacheRepository actorCacheRepository;

    @Autowired
    final PersistenceService persistenceService;


    final NewtableRepository newtableRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private StepExecution stepExecution;
    private final String query = "INSERT INTO role_assignment_request (id, correlation_id,client_id,authenticated_user_id,assigner_id,request_type,status," +
                                 "process,reference," +
                                 "replace_existing,role_assignment_id,log,created)" +
                                 " VALUES (:id, :correlationId,:clientId,:authenticatedUserId,:assignerId,:requestType,:status,:process,:reference," +
                                 ":replaceExisting," +
                                 ":roleAssignmentId,:log,:created)";

    @Autowired
    public RequestEntityWriter(RequestRepository requestRepository, ActorCacheRepository actorCacheRepository,
                               PersistenceService persistenceService, NewtableRepository newtableRepository) {
        this.requestRepository = requestRepository;
        this.actorCacheRepository = actorCacheRepository;
        this.persistenceService = persistenceService;
        this.newtableRepository = newtableRepository;
    }


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


        ActorCacheEntity entity = ActorCacheEntity.builder()
                                                  .actorIds(UUID.randomUUID().toString())
                                                  .etag(1)
                                                  .build();
        System.out.println("Actor Cache Entity is: " + entity);
        actorCacheRepository.save(entity);
        //actorCacheRepository.insertIntoActorCache();
        System.out.println("Save Actor Cache Completed");

        System.out.println("Records from actor cache: " + actorCacheRepository.findAll());

        persistenceService.insertIntoActorCacheRepo(ActorCacheEntity.builder()
                                                                    .actorIds(UUID.randomUUID().toString())
                                                                    .etag(1)
                                                                    .build());
        newtableRepository.save(Newtable.builder().column2("myvalue").build());
        System.out.println(newtableRepository.findAll());

       /* SessionFactory sessionFactory = new Configuration().buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(Newtable.builder().id(UUID.randomUUID().toString()).build());
        session.flush();;
        session.getTransaction().commit();
        session.close();*/

        //jdbcTemplate.execute("insert into nitish_table(myid) values('3')");



    }

}
