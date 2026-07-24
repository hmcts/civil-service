package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationDispatcher;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardNotificationTransactionalService {

    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;
    private final DashboardNotificationDispatcher dashboardNotificationDispatcher;

    public void dispatch(String activityId, DashboardTaskContext context) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            log.info("Starting transactional dashboard notification dispatch");

            entityManager.joinTransaction();

            dashboardNotificationDispatcher.dispatch(activityId, context);
        });
    }
}
