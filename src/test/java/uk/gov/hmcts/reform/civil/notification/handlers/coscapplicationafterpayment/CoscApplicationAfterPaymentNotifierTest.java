package uk.gov.hmcts.reform.civil.notification.handlers.coscapplicationafterpayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CoscPaidInFullApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationHandler;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SuppressWarnings("removal")
class CoscApplicationAfterPaymentNotifierTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldDispatchCivilNotificationWithUnchangedLegacyEmail(boolean queryManagement) {
        NotificationService legacyService = mock(NotificationService.class);
        NotificationService notificationService = mock(NotificationService.class);
        CaseTaskTrackingService tracking = mock(CaseTaskTrackingService.class);
        NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
            invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
                : RETURNS_DEFAULTS.answer(invocation));
        NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
            invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
                : RETURNS_DEFAULTS.answer(invocation));
        FeatureToggleService toggles = mock(FeatureToggleService.class);
        CaseData data = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build().toBuilder()
            .businessProcess(new BusinessProcess().setActivityId("CoscApplicationAfterPaymentNotifyParties")).build();
        when(toggles.isPublicQueryManagementEnabled(data)).thenReturn(queryManagement);
        var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, data).build();
        new CoscPaidInFullApplicantNotificationHandler(legacyService, templates, signatures, toggles).handle(params);
        var notifier = new CoscApplicationAfterPaymentNotifier(notificationService, tracking,
            new CoscApplicationAfterPaymentAllPartiesEmailGenerator(
                new CoscApplicationAfterPaymentAppSolOneEmailDTOGenerator(templates, signatures, toggles)));
        var factory = new NotifierFactory(notifier);
        var handler = new NotificationHandler(factory, mock(CoreCaseDataService.class),
            new ObjectMapper().registerModule(new JavaTimeModule()));

        assertThat(factory.getNotifier(data.getBusinessProcess().getActivityId())).isSameAs(notifier);
        handler.handle(params);

        var legacyEmails = mockingDetails(legacyService).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals("sendMail"))
            .map(invocation -> Arrays.asList(invocation.getArguments())).toList();
        var newEmails = mockingDetails(notificationService).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals("sendMail"))
            .map(invocation -> Arrays.asList(invocation.getArguments())).toList();
        assertThat(legacyEmails).hasSize(1);
        assertThat(newEmails).containsExactlyElementsOf(legacyEmails);
        assertThat(data.getNotificationSummary()).contains("cosc-application-email-notification-000DC001");
        verifyNoInteractions(tracking);
    }
}
