package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceNotificationType;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceSolicitorNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceSolicitorNotifierFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC_RESP1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC_RESP2;

@ExtendWith(MockitoExtension.class)
class DefendantResponseApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private FullDefenceSolicitorNotifier mockNotifier1;
    @Mock
    private FullDefenceSolicitorNotifier mockNotifier2;
    @Mock
    private FullDefenceSolicitorNotifier mockNotifier3;
    @Mock
    private FullDefenceSolicitorNotifier mockNotifier4;

    @Mock
    private FullDefenceSolicitorNotifierFactory fullDefenceSolicitorNotifierFactory;

    @InjectMocks
    private DefendantResponseApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {


        @Test
        void shouldNotifyApplicantSolicitorIn1v1Scenario_whenV1CallbackInvoked() {
            when(fullDefenceSolicitorNotifierFactory.getNotifier(eq(FullDefenceNotificationType.APPLICANT_SOLICITOR_ONE), any())).thenReturn(mockNotifier1);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                        .build())
                .build();

            handler.handle(params);

            verify(mockNotifier1).notifySolicitorForDefendantResponse(any());
            verifyNoInteractions(mockNotifier2, mockNotifier3, mockNotifier4);
        }

        @Test
        void shouldNotifyRespondentSolicitor1In1v1Scenario_whenV1CallbackInvoked() {
            when(fullDefenceSolicitorNotifierFactory.getNotifier(eq(FullDefenceNotificationType.APPLICANT_SOLICITOR_ONE_CC), any())).thenReturn(mockNotifier2);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                        .build())
                .build();

            handler.handle(params);

            verify(mockNotifier2).notifySolicitorForDefendantResponse(any());
            verifyNoInteractions(mockNotifier1, mockNotifier3, mockNotifier4);
        }

        @Test
        void shouldNotifyRespondentSolicitorCC_whenV1CallbackInvoked() {
            when(fullDefenceSolicitorNotifierFactory.getNotifier(eq(FullDefenceNotificationType.RESPONDENT_SOLICITOR_ONE_CC), any())).thenReturn(mockNotifier3);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                             .build())
                .build();

            handler.handle(params);

            verify(mockNotifier3).notifySolicitorForDefendantResponse(any());
            verifyNoInteractions(mockNotifier1, mockNotifier2, mockNotifier4);
        }

        @Test
        void shouldNotifyRespondentSolicitor1In1v1ScenarioSecondSol_whenV1CallbackInvoked() {
            when(fullDefenceSolicitorNotifierFactory.getNotifier(eq(FullDefenceNotificationType.RESPONDENT_SOLICITOR_TWO_CC), any())).thenReturn(mockNotifier4);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC")
                             .build())
                .build();

            handler.handle(params);

            verify(mockNotifier4).notifySolicitorForDefendantResponse(any());
            verifyNoInteractions(mockNotifier1, mockNotifier2, mockNotifier3);
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC_RESP2);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC_RESP1);
    }
}
