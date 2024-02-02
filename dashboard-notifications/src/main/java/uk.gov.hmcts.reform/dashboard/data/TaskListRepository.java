package uk.gov.hmcts.reform.dashboard.data;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;
@Transactional
@Repository
@EnableAutoConfiguration
public interface TaskListRepository extends CrudRepository<TaskListEntity, Long> {
    List<TaskListEntity> findTaskListByCaseReference(String caseReference);
}
