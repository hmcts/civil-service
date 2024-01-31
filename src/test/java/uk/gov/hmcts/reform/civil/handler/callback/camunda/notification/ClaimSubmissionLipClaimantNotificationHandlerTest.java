package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimSubmissionLipClaimantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerTest.AboutToSubmitCallback.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
class ClaimSubmissionLipClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private ClaimSubmissionLipClaimantNotificationHandler handler;

    @Test
    void shouldNotifyLipRespondent_whenInvoked() {
        when(notificationsProperties.getNotifyClaimantLipForClaimSubmissionTemplate()).thenReturn(
            "template-id");

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .individualTitle("Mr.")
                            .individualFirstName("Claimant")
                            .individualLastName("Guy")
                            .type(Party.Type.INDIVIDUAL)
                            .partyEmail("individual.claimant@email.com")
                            .build())
            .respondent1(Party.builder()
                            .individualTitle("Mr.")
                            .individualFirstName("Defendant")
                            .individualLastName("Guy")
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .legacyCaseReference(LEGACY_CASE_REFERENCE)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId("NOTIFY_LIP_CLAIMANT_CLAIM_SUBMISSION")
                .build()).build();

        handler.handle(params);

        verify(notificationService).sendMail(
            "individual.claimant@email.com",
            "template-id",
            getNotificationDataMap(caseData),
            "claim-submission-lip-claimant-notification-000DC001"
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_LIP_CLAIMANT_CLAIM_SUBMISSION").build()).build())).isEqualTo(TASK_ID);
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, "Mr. Defendant Guy",
            CLAIMANT_NAME, "Mr. Claimant Guy"
        );
    }

}
