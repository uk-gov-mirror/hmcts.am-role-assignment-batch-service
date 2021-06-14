package uk.gov.hmcts.reform.roleassignmentbatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;

@Service
public class PersistenceService {

    @Autowired
    private ActorCacheRepository actorCacheRepository;

    public void insertIntoActorCacheRepo (ActorCacheEntity entity) {
        actorCacheRepository.save(entity);
    }
}
