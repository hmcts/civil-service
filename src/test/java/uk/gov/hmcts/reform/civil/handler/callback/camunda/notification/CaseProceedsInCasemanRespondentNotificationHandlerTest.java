package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;

@ExtendWith(MockitoExtension.class)
class CaseProceedsInCasemanRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @InjectMocks
    private CaseProceedsInCasemanRespondentNotificationHandler handler;

    private final CaseEvent eventId1 = CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN;
    private final CaseEvent eventId2 = CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(eventId1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        String taskId1 = "CaseProceedsInCasemanNotifyRespondentSolicitor1";
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            eventId1.toString()).build()).build())).isEqualTo(taskId1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId2_whenInvoked() {
        String taskId2 = "CaseProceedsInCasemanNotifyRespondentSolicitor2";
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            eventId2.toString()).build()).build())).isEqualTo(taskId2);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenFlowStateHasTransitionedToClaimNotified() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            params.getRequest().setEventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN.name());

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
        void shouldNotifyRespondentSolicitor2_whenFlowStateHasTransitionedToClaimNotified() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            try (MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class)) {
                multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.getMultiPartyScenario(any()))
                    .thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

                params.getRequest().setEventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN.name());

                when(stateFlowEngine.hasTransitionedTo(params.getRequest().getCaseDetails(), CLAIM_NOTIFIED))
                    .thenReturn(true);

                handler.handle(params);

                verify(notificationService).sendMail(
                    eq("respondentsolicitor2@example.com"),
                    eq("template-id"),
                    any(),
                    eq("case-proceeds-in-caseman-respondent-notification-000DC001")
                );
            }
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenFlowStateHasNotTransitionedToClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(stateFlowEngine.hasTransitionedTo(params.getRequest().getCaseDetails(), CLAIM_NOTIFIED))
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

        @Test
        void shouldNotNotifyRespondentSolicitor2_ifNoRespSolicitor2IsOnCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            params.getRequest().setEventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN.name());

            try (MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class)) {
                multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.getMultiPartyScenario(any()))
                    .thenReturn(ONE_V_ONE);

                var response = handler.handle(params);

                verifyNoInteractions(notificationService);
                assertThat(response).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
            }
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789"
            );
        }
    }
}
