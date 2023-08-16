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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    HearingFeeUnpaidApplicantNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
class HearingFeeUnpaidApplicantNotificationHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private HearingFeeUnpaidApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getApplicantHearingFeeUnpaid()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "hearing-fee-unpaid-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build()
                .toBuilder().applicant1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE_ID,
                getNotificationLipDataMap(caseData),
                "hearing-fee-unpaid-claimantLip-notification-000DC001"
            );
        }
    }

    @Test
    void shouldNotNotifyApplicantLip_whenHadNoEmail() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build()
            .toBuilder().applicant1Represented(YesOrNo.NO).applicant1(
                Party.builder().type(Party.Type.SOLE_TRADER)
                    .partyName("Sole Trader").soleTraderLastName("Trader").build()).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        // When
        handler.handle(params);
        // Then
        verifyNoInteractions(notificationService);
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE)
        );
    }

    @NotNull
    private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            CLAIMANT_V_DEFENDANT, "Rambo v Trader",
            PARTY_NAME, caseData.getApplicant1().getPartyName()
        );
    }

}
