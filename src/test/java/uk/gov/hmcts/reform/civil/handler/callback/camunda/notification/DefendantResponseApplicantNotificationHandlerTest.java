package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DefendantResponseApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class DefendantResponseApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private DefendantResponseApplicantNotificationHandler handler;

    @BeforeEach
    void setup() {
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn("template-id");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithCcEvent() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-applicant-notification-000DC001"
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                "defendantName", "Mr. Sole Trader"
            );
        }
    }

    @Nested
    class AboutToSubmitV1Callback {

        @Nested
        class OneVsOneScenario {

            @Test
            void shouldNotifyApplicantSolicitorIn1v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In1v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }
        }

        @Nested
        class OneVsTwoScenario {

            @Test
            void shouldNotifyApplicantSolicitorIn1v2Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In1v2Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2In1v2Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }
        }

        @Nested
        class TwoVsOneScenario {
            @Test
            void shouldNotifyApplicantSolicitorIn2v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In2v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .version(V_1)
                    .request(CallbackRequest.builder()
                                 .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                                 .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
                || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
                );
            } else {
                //if there are 2 respondents on the case, concatenate the names together for the template subject line
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME,
                    getPartyNameBasedOnType(caseData.getRespondent1())
                        .concat(" and ")
                        .concat(getPartyNameBasedOnType(caseData.getRespondent2()))
                );
            }
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC);
    }
}
