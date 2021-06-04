package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;

@SpringBootTest(classes = {
    CaseProceedsInCasemanRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class CaseProceedsInCasemanRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private StateFlowEngine stateFlowEngine;
    @Autowired
    private CaseProceedsInCasemanRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenFlowStateHasTransitionedToClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(stateFlowEngine.hasTransitionedTo(params.getRequest().getCaseDetails(), CLAIM_NOTIFIED))
                .thenReturn(true);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "case-proceeds-in-caseman-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenFlowStateHasNotTransitionedToClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(stateFlowEngine.hasSpecTransitionedTo(params.getRequest().getCaseDetails(), CLAIM_NOTIFIED))
                .thenReturn(false);

            handler.handle(params);

            verify(notificationService, never()).sendMail(anyString(), anyString(), anyMap(), anyString());
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenSpecFlowStateHasNotTransitionedToClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(stateFlowEngine.hasTransitionedTo(params.getRequest().getCaseDetails(), CLAIM_NOTIFIED))
                .thenReturn(false);

            handler.handle(params);

            verify(notificationService, never()).sendMail(anyString(), anyString(), anyMap(), anyString());
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                "frontendBaseUrl", "https://www.MyHMCTS.gov.uk"
            );
        }
    }
}
