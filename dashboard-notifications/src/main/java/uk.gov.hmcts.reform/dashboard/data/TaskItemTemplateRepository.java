package uk.gov.hmcts.reform.dashboard.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;

@Transactional
@Repository
public interface TaskItemTemplateRepository extends CrudRepository<TaskItemTemplateEntity, Long> {

}
