package uk.gov.hmcts.reform.roleassignmentbatch.util;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;

@Repository
public interface NewtableRepository extends CrudRepository<Newtable, Long> {
}
