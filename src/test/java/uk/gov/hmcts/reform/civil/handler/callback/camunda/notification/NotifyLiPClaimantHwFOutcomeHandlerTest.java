package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE;

@SpringBootTest(classes = {
    NotifyLiPClaimantHwFOutcomeHandler.class,
    JacksonAutoConfiguration.class
})
public class NotifyLiPClaimantHwFOutcomeHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService toggleService;
    @Autowired
    private NotifyLiPClaimantHwFOutcomeHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private static final String EMAIL_TEMPLATE_NO_REMISSION = "test-hwf-noremission-id";
        private static final String EMAIL_TEMPLATE_UPDATE_REF_NUMBER = "test-hwf-updaterefnumber-id";
        private static final String EMAIL = "test@email.com";
        private static final String REFERENCE_NUMBER = "hwf-outcome-notification-000DC001";
        private static final String CLAIMANT = "Mr. John Rambo";
        public static final String HWF_REFERENCE = "000HWF001";
        public static final String CLAIM_REFERENCE = "000DC001";
        public static final String CLAIM_FEE_AMOUNT = "1000.00";
        public static final String HEARING_FEE_AMOUNT = "2000.00";

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyApplicantForHwfNoRemission()).thenReturn(
                EMAIL_TEMPLATE_NO_REMISSION);
            when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber()).thenReturn(
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER);
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_NoRemission_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(NO_REMISSION_HWF)
                .noRemissionDetails("no remission")
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(EMAIL)
                                .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber(HWF_REFERENCE).build()).build())
                .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(100000)).build())
                .claimIssuedHwfDetails(hwfeeDetails)
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_NO_REMISSION,
                getNotificationDataMapNoRemissionClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_NoRemission_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(NO_REMISSION_HWF)
                .noRemissionDetails("no remission")
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.INCORRECT_EVIDENCE).build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(EMAIL)
                                .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .hearingHelpFeesReferenceNumber(HWF_REFERENCE)
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(200000)).build())
                .hearingHwfDetails(hwfeeDetails)
                .hwfFeeType(FeeType.HEARING)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_NO_REMISSION,
                getNotificationDataMapNoRemissionHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(EMAIL)
                                .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber(HWF_REFERENCE).build()).build())
                .claimIssuedHwfDetails(hwfeeDetails)
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER,
                getNotificationDataMapUpdateRefNumberClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(EMAIL)
                                .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .hearingHelpFeesReferenceNumber(HWF_REFERENCE)
                .hearingHwfDetails(hwfeeDetails)
                .hwfFeeType(FeeType.HEARING)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER,
                getNotificationDataMapUpdateRefNumberHearing(),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMapNoRemissionClaimIssued() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                REASONS, NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET.getLabel(),
                TYPE_OF_FEE, FeeType.CLAIMISSUED.getLabel(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                AMOUNT, CLAIM_FEE_AMOUNT
            );
        }

        private Map<String, String> getNotificationDataMapNoRemissionHearing() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                REASONS, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabel(),
                TYPE_OF_FEE, FeeType.HEARING.getLabel(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                AMOUNT, HEARING_FEE_AMOUNT
            );
        }

        private Map<String, String> getNotificationDataMapUpdateRefNumberClaimIssued() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                TYPE_OF_FEE, FeeType.CLAIMISSUED.getLabel(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMapUpdateRefNumberHearing() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                TYPE_OF_FEE, FeeType.HEARING.getLabel(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
            );
        }
    }
}
