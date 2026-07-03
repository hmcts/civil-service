package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardNotificationTaskCaseReference;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationRegistry;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetriggerDashboardNotificationTaskTest {

    private static final String DASHBOARD_TASK_ID = "GenerateDashboardNotificationsHearingScheduledHmc";
    private static final String PROCESS_INSTANCE_ID = "process-123";
    private static final String CASE_REFERENCE = "1234567890123456";
    private static final String AUTH_TOKEN = "Bearer token";

    private DashboardNotificationRegistry registry;
    private UserService userService;
    private PlatformTransactionManager transactionManager;
    private EntityManager entityManager;
    private RetriggerDashboardNotificationTask task;

    @BeforeEach
    void setUp() {
        registry = mock(DashboardNotificationRegistry.class);
        userService = mock(UserService.class);
        transactionManager = mock(PlatformTransactionManager.class);
        entityManager = mock(EntityManager.class);
        TransactionStatus transactionStatus = new SimpleTransactionStatus();
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        task = new RetriggerDashboardNotificationTask(
            registry,
            userService,
            new SystemUpdateUserConfiguration("system-user", "password"),
            transactionManager,
            entityManager
        );
    }

    @Test
    void migrateCaseDataShouldRunRegisteredDashboardWorkflow() {
        AtomicReference<DashboardTaskContext> contextReference = new AtomicReference<>();
        DashboardWorkflowTask workflow = new DashboardWorkflowTask() {
            @Override
            public void execute(DashboardTaskContext context) {
                contextReference.set(context);
            }
        };

        when(registry.workflowsFor(DASHBOARD_TASK_ID, DashboardCaseType.CIVIL)).thenReturn(List.of(workflow));
        when(userService.getAccessToken("system-user", "password")).thenReturn(AUTH_TOKEN);

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_REFERENCE))
            .businessProcess(new BusinessProcess().updateProcessInstanceId("existing-process"))
            .build();
        DashboardNotificationTaskCaseReference caseReference = caseReference();

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertSame(caseData, result);
        verify(registry).workflowsFor(DASHBOARD_TASK_ID, DashboardCaseType.CIVIL);
        verify(userService).getAccessToken("system-user", "password");

        CallbackParams callbackParams = contextReference.get().callbackParams();
        assertEquals(AUTH_TOKEN, callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN));
        assertEquals(Long.valueOf(CASE_REFERENCE), contextReference.get().caseData().getCcdCaseReference());
        assertEquals(DASHBOARD_TASK_ID, contextReference.get().caseData().getBusinessProcess().getActivityId());
        assertEquals(PROCESS_INSTANCE_ID, contextReference.get().caseData().getBusinessProcess().getProcessInstanceId());
        assertEquals("existing-process", caseData.getBusinessProcess().getProcessInstanceId());
        verify(entityManager).joinTransaction();
        verify(entityManager).flush();
        verify(transactionManager).commit(any());
    }

    @Test
    void migrateCaseDataShouldThrowWhenDashboardTaskIdIsMissing() {
        DashboardNotificationTaskCaseReference caseReference = new DashboardNotificationTaskCaseReference();
        caseReference.setCaseReference(CASE_REFERENCE);
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, caseReference)
        );

        assertEquals("Case reference and dashboardTaskId must not be blank", exception.getMessage());
    }

    @Test
    void migrateCaseDataShouldThrowWhenNoWorkflowIsRegistered() {
        CaseData caseData = CaseData.builder().build();
        DashboardNotificationTaskCaseReference caseReference = caseReference();
        when(registry.workflowsFor(DASHBOARD_TASK_ID, DashboardCaseType.CIVIL)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, caseReference)
        );

        assertEquals("No dashboard notification handlers registered for: " + DASHBOARD_TASK_ID, exception.getMessage());
    }

    private DashboardNotificationTaskCaseReference caseReference() {
        DashboardNotificationTaskCaseReference caseReference = new DashboardNotificationTaskCaseReference();
        caseReference.setCaseReference(CASE_REFERENCE);
        caseReference.setDashboardTaskId(DASHBOARD_TASK_ID);
        caseReference.setDashboardProcessInstanceId(PROCESS_INSTANCE_ID);
        return caseReference;
    }
}
