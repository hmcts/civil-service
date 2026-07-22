package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationDispatcher;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationTransactionalServiceTest {

    private static final String ACTIVITY_ID = "GenerateDashboardNotificationsDefendantResponse";

    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private EntityManager entityManager;
    @Mock
    private DashboardNotificationDispatcher dashboardNotificationDispatcher;
    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private DashboardNotificationTransactionalService service;

    @Test
    void shouldDispatchDashboardNotificationInsideJoinedTransaction() {
        when(transactionManager.getTransaction(any())).thenReturn(null);

        service.dispatch(ACTIVITY_ID, context);

        InOrder inOrder = inOrder(transactionManager, entityManager, dashboardNotificationDispatcher);
        inOrder.verify(transactionManager).getTransaction(any());
        inOrder.verify(entityManager).joinTransaction();
        inOrder.verify(dashboardNotificationDispatcher).dispatch(ACTIVITY_ID, context);
        inOrder.verify(transactionManager).commit(null);
    }
}
