package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public interface TaskListRepository extends CrudRepository<TaskListEntity, UUID> {

    List<TaskListEntity> findByReferenceAndTaskItemTemplateRole(String reference, String role);

    Optional<TaskListEntity> findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
        String reference, String role, String templateName);

    List<TaskListEntity> findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
        String reference, String role, Collection<Integer> currentStatus);

    List<TaskListEntity> findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
        String reference, String role, Collection<Integer> currentStatus, Collection<Long> templates);
}
