package uk.gov.hmcts.reform.dashboard.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;
@Transactional
@Repository
public interface TaskListTemplateRepository extends CrudRepository<TaskListTemplateEntity, Long> {
    List<TaskListTemplateEntity> findByReference(String reference);
}
