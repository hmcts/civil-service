package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;

import java.util.List;

@Transactional(readOnly = true)
@Repository
public interface TaskItemTemplateRepository extends CrudRepository<TaskItemTemplateEntity, Long> {

    List<TaskItemTemplateEntity> findByScenarioName(String name);

    List<TaskItemTemplateEntity> findByCategoryEnAndRole(String category, String role);
}
