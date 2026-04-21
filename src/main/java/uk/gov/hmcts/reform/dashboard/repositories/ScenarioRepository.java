package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.entities.ScenarioEntity;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ScenarioRepository extends CrudRepository<ScenarioEntity, Long> {

    Optional<ScenarioEntity> findByName(String name);
}
