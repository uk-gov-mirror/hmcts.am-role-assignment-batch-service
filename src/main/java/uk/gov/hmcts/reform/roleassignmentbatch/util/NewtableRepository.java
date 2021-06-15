package uk.gov.hmcts.reform.roleassignmentbatch.util;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignmentbatch.entities.Newtable;

@Repository
@Transactional
public interface NewtableRepository extends CrudRepository<Newtable, String> {
}
