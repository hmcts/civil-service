package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.model.NotificationTemplate;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTemplate, UUID> {
}
