package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantLipRequestJudgementByAdmissionApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    ClaimantLipRequestJudgementByAdmissionApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})

class ClaimantLipRequestJudgementByAdmissionApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private PinInPostConfiguration pinInPostConfiguration;
    @Autowired
    private ClaimantLipRequestJudgementByAdmissionApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyApplicantLipRequestJudgementByAdmissionNotificationTemplate()).thenReturn("template-id");
            when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
        }

        @Test
        void shouldNotifyLipApplicant_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "template-id",
                getNotificationDataMap(caseData),
                "request-judgement-by-admission-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT").build()).build())).isEqualTo(TASK_ID);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                RESPONDENT_NAME, "Mr. Sole Trader",
                CLAIMANT_NAME, "Mr. John Rambo",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                FRONTEND_URL, "dummy_cui_front_end_url"
            );
        }
    }
}
