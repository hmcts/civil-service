package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationDispatcher;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

class DashboardNotificationTransactionalServiceIntegrationTest extends DashboardBaseIntegrationTest {

    private static final String AUTH_TOKEN = "Bearer system-user-token";
    private static final String CASE_ID = "1770206234704889";
    private static final String CLAIMANT = "CLAIMANT";
    private static final String HEARING_FEE_PAYMENT_ACTIVITY_ID = "GenerateDashboardNotificationsCitizenHearingFeePayment";

    @Autowired
    private DashboardNotificationTransactionalService dashboardNotificationTransactionalService;

    @Autowired
    private DashboardNotificationService dashboardNotificationService;

    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    @Autowired
    private NotificationActionRepository notificationActionRepository;

    @MockBean
    private DashboardNotificationDispatcher dashboardNotificationDispatcher;

    @Test
    void shouldDispatchCreateOnlyDashboardScenarioInsideTransaction() {
        String activityId = "GenerateDashboardNotificationsCreateClaimAfterPayment";
        String notificationName = "Notice.AAA6.ClaimIssued.Claimant";

        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            dashboardNotificationService.saveOrUpdate(notification(CASE_ID, notificationName, CLAIMANT));
            return null;
        }).when(dashboardNotificationDispatcher).dispatch(eq(activityId), any(DashboardTaskContext.class));

        assertThatCode(() -> dispatch(activityId)).doesNotThrowAnyException();

        assertThat(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(
            CASE_ID,
            CLAIMANT,
            notificationName
        )).hasSize(1);
    }

    @Test
    void shouldDispatchHearingFeePaymentScenarioAndBulkDeleteExistingNotificationsInsideTransaction() {
        DashboardNotificationsEntity existingNotification = dashboardNotificationsRepository.save(
            notification(CASE_ID, "Notice.AAA6.CP.HearingFee.Unpaid.Claimant", CLAIMANT)
        );
        notificationActionRepository.save(notificationAction(existingNotification.getId()));

        String replacementNotification = "Scenario.AAA6.CP.HearingFee.Paid.Claimant";
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            dashboardNotificationService.deleteByReferenceAndCitizenRole(CASE_ID, CLAIMANT);
            dashboardNotificationService.saveOrUpdate(notification(CASE_ID, replacementNotification, CLAIMANT));
            return null;
        }).when(dashboardNotificationDispatcher)
            .dispatch(eq(HEARING_FEE_PAYMENT_ACTIVITY_ID), any(DashboardTaskContext.class));

        assertThatCode(() -> dispatch(HEARING_FEE_PAYMENT_ACTIVITY_ID)).doesNotThrowAnyException();

        assertThat(dashboardNotificationsRepository.findById(existingNotification.getId())).isEmpty();
        assertThat(notificationActionRepository.findByDashboardNotificationIdIn(List.of(existingNotification.getId())))
            .isEmpty();
        assertThat(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(
            CASE_ID,
            CLAIMANT,
            replacementNotification
        )).hasSize(1);
    }

    @Test
    void shouldDispatchNamedNotificationRemovalInsideTransaction() {
        String notificationToRemove = "Notice.AAA6.Payment.Required.Claimant";
        String notificationToKeep = "Notice.AAA6.Other.Claimant";
        dashboardNotificationsRepository.save(notification(CASE_ID, notificationToRemove, CLAIMANT));
        dashboardNotificationsRepository.save(notification(CASE_ID, notificationToKeep, CLAIMANT));

        String activityId = "RemoveDashboardNotificationPayment";
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
                notificationToRemove,
                CASE_ID,
                CLAIMANT
            );
            return null;
        }).when(dashboardNotificationDispatcher).dispatch(eq(activityId), any(DashboardTaskContext.class));

        assertThatCode(() -> dispatch(activityId)).doesNotThrowAnyException();

        assertThat(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(
            CASE_ID,
            CLAIMANT,
            notificationToRemove
        )).isEmpty();
        assertThat(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(
            CASE_ID,
            CLAIMANT,
            notificationToKeep
        )).hasSize(1);
    }

    private void dispatch(String activityId) {
        dashboardNotificationTransactionalService.dispatch(
            activityId,
            DashboardTaskContext.civil(CaseData.builder().ccdCaseReference(Long.valueOf(CASE_ID)).build(), AUTH_TOKEN)
        );
    }

    private DashboardNotificationsEntity notification(String reference, String name, String citizenRole) {
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        notification.setReference(reference);
        notification.setName(name);
        notification.setCitizenRole(citizenRole);
        notification.setTitleEn("Title");
        notification.setDescriptionEn("Description");
        notification.setTitleCy("Title cy");
        notification.setDescriptionCy("Description cy");
        notification.setParams(new HashMap<>());
        notification.setCreatedBy("integration-test");
        notification.setCreatedAt(OffsetDateTime.now());
        return notification;
    }

    private NotificationActionEntity notificationAction(UUID dashboardNotificationId) {
        NotificationActionEntity notificationAction = new NotificationActionEntity();
        notificationAction.setReference(CASE_ID);
        notificationAction.setActionPerformed("Click");
        notificationAction.setCreatedBy("integration-test");
        notificationAction.setCreatedAt(OffsetDateTime.now());
        notificationAction.setDashboardNotificationId(dashboardNotificationId);
        return notificationAction;
    }
}
