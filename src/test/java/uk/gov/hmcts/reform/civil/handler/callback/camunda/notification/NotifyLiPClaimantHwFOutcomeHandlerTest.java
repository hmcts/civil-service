package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DOCUMENTS_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PART_AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REMAINING_AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE_WELSH;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotifyLiPClaimantHwFOutcomeHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private NotifyLiPClaimantHwFOutcomeHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private static final String EMAIL_TEMPLATE_INVALID_HWF_REFERENCE = "test-hwf-invalidrefnumber-id";
        private static final String EMAIL_TEMPLATE_NO_REMISSION = "test-hwf-noremission-id";
        private static final String EMAIL_TEMPLATE_MORE_INFO_HWF_BILINGUAL = "test-hwf-more-info-bilingual-id";
        private static final String EMAIL_TEMPLATE_MORE_INFO_HWF = "test-hwf-more-info-id";
        private static final String EMAIL_TEMPLATE_UPDATE_REF_NUMBER = "test-hwf-updaterefnumber-id";
        private static final String EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION = "test-hwf-partialRemission-id";
        private static final String EMAIL_NO_REMISSION_TEMPLATE_HWF_BILINGUAL = "test-hwf-noremission-bilingual-id";
        private static final String EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION_BILINGUAL = "test-hwf-partialRemission-bilingual-id";
        private static final String EMAIL_TEMPLATE_UPDATE_REF_NUMBER_BILINGUAL = "test-hwf-updaterefnumber-bilingual-id";
        private static final String EMAIL_TEMPLATE_INVALID_HWF_REFERENCE_BILINGUAL = "test-hwf-invalidrefnumber-bilingual-id";
        private static final String EMAIL_TEMPLATE_FEE_PAYMENT_OUTCOME = "test-hwf-feePayment-outcome-id";
        private static final String EMAIL_TEMPLATE_BILINGUAL_FEE_PAYMENT_OUTCOME = "test-hwf-feePayment-outcome-bilingual-id";
        private static final String EMAIL = "test@email.com";
        private static final String EMAIL_SOLICITOR = "solicitor_test@email.com";
        private static final String REFERENCE_NUMBER = "hwf-outcome-notification-000DC001";
        private static final String CLAIMANT = "Mr. John Rambo";
        private static final String HWF_REFERENCE = "000HWF001";
        private static final String CLAIM_REFERENCE = "000DC001";
        private static final String CLAIM_FEE_AMOUNT = "1000.00";
        private static final String HEARING_FEE_AMOUNT = "2000.00";
        private static final String REMISSION_AMOUNT = "100000.00";
        private static final String OUTSTANDING_AMOUNT_IN_POUNDS = "500.00";

        private static final LocalDate NOW = LocalDate.now();

        private static final CaseData CLAIM_ISSUE_CASE_DATA = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
            .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                            .partyEmail(EMAIL)
                            .build())
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber(
                HWF_REFERENCE).build()).build())
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(100000)).build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        private static final CaseData HEARING_CASE_DATA = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
            .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                            .partyEmail(EMAIL)
                            .build())
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .hearingHelpFeesReferenceNumber(HWF_REFERENCE)
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(200000)).build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyApplicantForHwfNoRemission()).thenReturn(
                EMAIL_TEMPLATE_NO_REMISSION);
            when(notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh()).thenReturn(
                EMAIL_NO_REMISSION_TEMPLATE_HWF_BILINGUAL);
            when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded()).thenReturn(
                EMAIL_TEMPLATE_MORE_INFO_HWF);
            when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh()).thenReturn(
                EMAIL_TEMPLATE_MORE_INFO_HWF_BILINGUAL);
            when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber()).thenReturn(
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER);
            when(notificationsProperties.getNotifyApplicantForHwfPartialRemission()).thenReturn(
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION);
            when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber()).thenReturn(
                EMAIL_TEMPLATE_INVALID_HWF_REFERENCE);
            when(notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual()).thenReturn(
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION_BILINGUAL);
            when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual()).thenReturn(
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER_BILINGUAL);
            when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual()).thenReturn(
                EMAIL_TEMPLATE_INVALID_HWF_REFERENCE_BILINGUAL);
            when(notificationsProperties.getNotifyApplicantForHwfFeePaymentOutcome()).thenReturn(
                EMAIL_TEMPLATE_FEE_PAYMENT_OUTCOME);
            when(notificationsProperties.getNotifyApplicantForHwfFeePaymentOutcomeInBilingual()).thenReturn(
                EMAIL_TEMPLATE_BILINGUAL_FEE_PAYMENT_OUTCOME);
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_NoRemission_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(NO_REMISSION_HWF)
                .noRemissionDetails("no remission")
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder().claimIssuedHwfDetails(hwfeeDetails).build();

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
        void shouldNotifyApplicantSolicitor_HwfOutcome_NoRemission_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(NO_REMISSION_HWF)
                .noRemissionDetails("no remission")
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .applicant1Represented(YesOrNo.YES)
                .claimIssuedHwfDetails(hwfeeDetails)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(EMAIL_SOLICITOR).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL_SOLICITOR,
                EMAIL_TEMPLATE_NO_REMISSION,
                getNotificationDataMapNoRemissionClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_NoRemission_ClaimIssuedBilingual() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(NO_REMISSION_HWF)
                .noRemissionDetails("no remission")
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .claimantBilingualLanguagePreference("BOTH")
                .claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_NO_REMISSION_TEMPLATE_HWF_BILINGUAL,
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

            CaseData caseData = HEARING_CASE_DATA.toBuilder().hearingHwfDetails(hwfeeDetails).build();
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
        void shouldNotifyApplicant_HwfOutcome_MoreInformation_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF).build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .helpWithFeesMoreInformationClaimIssue(HelpWithFeesMoreInformation.builder()
                                                           .hwFMoreInfoDocumentDate(NOW)
                                                           .hwFMoreInfoRequiredDocuments(
                                                               getMoreInformationDocumentList()).build())
                .claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_MORE_INFO_HWF,
                getNotificationDataMapMoreInfoClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_MoreInformation_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF).build();
            CaseData caseData = HEARING_CASE_DATA.toBuilder()
                .helpWithFeesMoreInformationHearing(HelpWithFeesMoreInformation.builder()
                                                        .hwFMoreInfoDocumentDate(NOW)
                                                        .hwFMoreInfoRequiredDocuments(
                                                            getMoreInformationDocumentList()).build())
                .hearingHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_MORE_INFO_HWF,
                getNotificationDataMapMoreInfoHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(INVALID_HWF_REFERENCE)
                .build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder().claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_INVALID_HWF_REFERENCE,
                getNotificationCommonDataMapForClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(INVALID_HWF_REFERENCE)
                .build();

            CaseData caseData = HEARING_CASE_DATA.toBuilder().hearingHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_INVALID_HWF_REFERENCE,
                getNotificationCommonDataMapForHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_PartialRemission_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                .remissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .outstandingFeeInPounds(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                .build();

            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder().claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION,
                getNotificationDataMapPartialRemissionClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_PartialRemission_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                .remissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .outstandingFeeInPounds(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                .build();

            CaseData caseData = HEARING_CASE_DATA.toBuilder().hearingHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION,
                getNotificationDataMapPartialRemissionHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_HwfOutcome_PartialRemission_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                .remissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .outstandingFeeInPounds(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                .build();

            CaseData caseData = HEARING_CASE_DATA.toBuilder()
                .applicant1Represented(YesOrNo.YES)
                .hearingHwfDetails(hwfeeDetails)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(EMAIL_SOLICITOR).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL_SOLICITOR,
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION,
                getNotificationDataMapPartialRemissionHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_ClaimIssued() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER)
                .build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder().claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER,
                getNotificationCommonDataMapForClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_Hearing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER)
                .build();
            CaseData caseData = HEARING_CASE_DATA.toBuilder().hearingHwfDetails(hwfeeDetails).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER,
                getNotificationCommonDataMapForHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_MoreInformationNeeded_Bilingual() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF).build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .claimantBilingualLanguagePreference("BOTH")
                .helpWithFeesMoreInformationClaimIssue(HelpWithFeesMoreInformation.builder()
                                                           .hwFMoreInfoDocumentDate(NOW)
                                                           .hwFMoreInfoRequiredDocuments(
                                                               getMoreInformationDocumentList()).build())
                .claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_MORE_INFO_HWF_BILINGUAL,
                getNotificationDataMapMoreInfoClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_Bilingual() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER)
                .build();
            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .claimIssuedHwfDetails(hwfeeDetails)
                .claimantBilingualLanguagePreference("BOTH")
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_UPDATE_REF_NUMBER_BILINGUAL,
                getNotificationCommonDataMapForClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_partRemission_Bilingual() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                .remissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .outstandingFeeInPounds(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                .build();

            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .claimantBilingualLanguagePreference("BOTH")
                .claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION_BILINGUAL,
                getNotificationDataMapPartialRemissionClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotNotify_HwfOutcome_PartialRemission_Hearing_WhenEmailMissing() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED)
                .remissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .outstandingFeeInPounds(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                .build();

            CaseData caseData = HEARING_CASE_DATA.toBuilder()
                .applicant1Represented(YesOrNo.YES)
                .hearingHwfDetails(hwfeeDetails)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, never()).sendMail(
                null,
                EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION,
                getNotificationDataMapPartialRemissionHearing(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_Bilingual() {
            // Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(INVALID_HWF_REFERENCE)
                .build();

            CaseData caseData = CLAIM_ISSUE_CASE_DATA.toBuilder()
                .claimantBilingualLanguagePreference("BOTH")
                .claimIssuedHwfDetails(hwfeeDetails).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                EMAIL,
                EMAIL_TEMPLATE_INVALID_HWF_REFERENCE_BILINGUAL,
                getNotificationCommonDataMapForClaimIssued(),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMapNoRemissionClaimIssued() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                REASONS, NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET.getLabel(),
                REASONS_WELSH, NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET.getLabelWelsh(),
                TYPE_OF_FEE, FeeType.CLAIMISSUED.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.CLAIMISSUED.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                AMOUNT, CLAIM_FEE_AMOUNT
            );
        }

        private Map<String, String> getNotificationDataMapNoRemissionHearing() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                REASONS, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabel(),
                REASONS_WELSH, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabelWelsh(),
                TYPE_OF_FEE, FeeType.HEARING.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.HEARING.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                AMOUNT, HEARING_FEE_AMOUNT
            );
        }

        private Map<String, String> getNotificationDataMapMoreInfoClaimIssued() {
            return Map.of(
                HWF_MORE_INFO_DATE, formatLocalDate(NOW, DATE),
                CLAIMANT_NAME, CLAIMANT,
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                TYPE_OF_FEE, FeeType.CLAIMISSUED.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.CLAIMISSUED.getLabelInWelsh(),
                HWF_MORE_INFO_DOCUMENTS, getMoreInformationDocumentListString(),
                HWF_MORE_INFO_DOCUMENTS_WELSH, getMoreInformationDocumentListStringWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMapMoreInfoHearing() {
            return Map.of(
                HWF_MORE_INFO_DATE, formatLocalDate(NOW, DATE),
                CLAIMANT_NAME, CLAIMANT,
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                TYPE_OF_FEE, FeeType.HEARING.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.HEARING.getLabelInWelsh(),
                HWF_MORE_INFO_DOCUMENTS, getMoreInformationDocumentListString(),
                HWF_MORE_INFO_DOCUMENTS_WELSH, getMoreInformationDocumentListStringWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
            );
        }

        private Map<String, String> getNotificationCommonDataMapForClaimIssued() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                TYPE_OF_FEE, FeeType.CLAIMISSUED.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.CLAIMISSUED.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
            );
        }

        private Map<String, String> getNotificationCommonDataMapForHearing() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                TYPE_OF_FEE, FeeType.HEARING.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.HEARING.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMapPartialRemissionClaimIssued() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                TYPE_OF_FEE, FeeType.CLAIMISSUED.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.CLAIMISSUED.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                PART_AMOUNT, "1000.00",
                REMAINING_AMOUNT, OUTSTANDING_AMOUNT_IN_POUNDS
            );
        }

        private Map<String, String> getNotificationDataMapPartialRemissionHearing() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CLAIM_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                TYPE_OF_FEE, FeeType.HEARING.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.HEARING.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                PART_AMOUNT, "1000.00",
                REMAINING_AMOUNT, OUTSTANDING_AMOUNT_IN_POUNDS
            );
        }

        private List<HwFMoreInfoRequiredDocuments> getMoreInformationDocumentList() {
            return Collections.singletonList(HwFMoreInfoRequiredDocuments.CHILD_MAINTENANCE);
        }

        private String getMoreInformationDocumentListStringWelsh() {
            List<HwFMoreInfoRequiredDocuments> list = getMoreInformationDocumentList();
            StringBuilder documentList = new StringBuilder();
            for (HwFMoreInfoRequiredDocuments doc : list) {
                documentList.append(doc.getNameBilingual());
                if (!doc.getDescriptionBilingual().isEmpty()) {
                    documentList.append(" - ");
                    documentList.append(doc.getDescriptionBilingual());
                }
                documentList.append("\n");
                documentList.append("\n");
            }
            return documentList.toString();
        }

        private String getMoreInformationDocumentListString() {
            List<HwFMoreInfoRequiredDocuments> list = getMoreInformationDocumentList();
            StringBuilder documentList = new StringBuilder();
            for (HwFMoreInfoRequiredDocuments doc : list) {
                documentList.append(doc.getName());
                if (!doc.getDescription().isEmpty()) {
                    documentList.append(" - ");
                    documentList.append(doc.getDescription());
                }
                documentList.append("\n");
                documentList.append("\n");
            }
            return documentList.toString();
        }
    }
}
