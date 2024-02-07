package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.data.TaskItemTemplateEntity;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface TaskItemTemplateRepository extends CrudRepository<TaskItemTemplateEntity, Long> {

}
