package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class HearingFeeUnpaidApplicantNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private HearingFeeUnpaidApplicantNotificationHandler handler;

    public static final String TEMPLATE_ID = "template-id";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getApplicantHearingFeeUnpaid()).thenReturn(TEMPLATE_ID);

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
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

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
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            CASEMAN_REF, "000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            CLAIMANT_V_DEFENDANT, "Rambo v Trader",
            PARTY_NAME, caseData.getApplicant1().getPartyName()
        );
    }

}
