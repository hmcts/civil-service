package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Repository
public interface TaskItemTemplateRepository extends CrudRepository<TaskItemTemplateEntity, Long> {

    List<TaskItemTemplateEntity> findByScenarioName(String name);

    List<TaskItemTemplateEntity> findByScenarioNameAndRole(String name, String role);

    List<TaskItemTemplateEntity> findByCategoryEnAndRole(String category, String role);
}
