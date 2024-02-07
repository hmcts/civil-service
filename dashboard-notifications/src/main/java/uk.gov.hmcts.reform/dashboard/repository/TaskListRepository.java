package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.data.TaskListEntity;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public interface TaskListRepository extends CrudRepository<TaskListEntity, UUID> {

    Optional<List<TaskListEntity>> findByReferenceAndTaskItemTemplateRole(String reference, String role);
}
