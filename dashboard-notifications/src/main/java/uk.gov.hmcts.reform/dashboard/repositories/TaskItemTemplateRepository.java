package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface TaskItemTemplateRepository extends CrudRepository<TaskItemTemplateEntity, Long> {

}
