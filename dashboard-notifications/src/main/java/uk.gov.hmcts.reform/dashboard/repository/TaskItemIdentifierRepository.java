package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.dashboard.data.TaskItemIdentifierEntity;

public interface TaskItemIdentifierRepository extends CrudRepository<TaskItemIdentifierEntity, Long> {
}
