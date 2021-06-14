package uk.gov.hmcts.reform.roleassignmentbatch;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.RequestEntity;

@Repository
public interface RequestRepository extends CrudRepository<RequestEntity, UUID> {
}

