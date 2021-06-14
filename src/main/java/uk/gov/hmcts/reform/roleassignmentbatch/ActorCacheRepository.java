package uk.gov.hmcts.reform.roleassignmentbatch;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.ActorCacheEntity;

@Repository
public interface ActorCacheRepository extends CrudRepository<ActorCacheEntity, String> {
/*
    @Modifying
    @Query("Insert into actor_cache_entity values(4444, 5)")
    ActorCacheEntity insertIntoActorCache(*//*@Param("actorId") String id, @Param("etag") String name*//*);*/

    ActorCacheEntity findByActorIds(String actorId);
}

