package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationClaimantOfHearingHandler.TASK_ID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationClaimantOfHearingHandler.TASK_ID_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

@ExtendWith(MockitoExtension.class)
class NotificationClaimantOfHearingHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private HearingFeesService hearingFeesService;
    @Mock
    NotificationsProperties notificationsProperties;
    @Mock
    HearingNoticeCamundaService hearingNoticeCamundaService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private NotificationClaimantOfHearingHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn("test-template-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData, false),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1HMC() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build();

            when(hearingFeesService.getFeeForHearingFastTrackClaims(any()))
                .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build());
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                                .hearingId("HER1234")
                                .hearingStartDateTime(LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    LocalTime.of(15, 30)))
                                .hearingType("AAA7-TRI")
                                .build());

            LocalDate now = LocalDate.of(2022, 9, 29);
            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDate::now).thenReturn(now);
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
                // When
                when(notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC())
                    .thenReturn("test-template-fee-claimant-id-hmc");
                handler.handle(params);
                // Then
                verify(notificationService).sendMail(
                    "applicantemail@hmcts.net",
                    "test-template-fee-claimant-id-hmc",
                    getNotificationFeeDataMapHMC(caseData),
                    "notification-of-hearing-HER1234"
                );
            }
        }

        @Test
        void shouldNotifyApplicantSolicitorWithoutFee_whenInvoked1v1DisposalHearingHMC() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build();

            when(hearingFeesService.getFeeForHearingFastTrackClaims(any()))
                .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(0)).build());
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                                .hearingId("HER1234")
                                .hearingStartDateTime(LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    LocalTime.of(15, 30)))
                                .hearingType("AAA7-DIS")
                                .build());

            LocalDate now = LocalDate.of(2022, 9, 29);
            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDate::now).thenReturn(now);
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
                // When
                when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC())
                    .thenReturn("test-template-no-fee-claimant-id-hmc");
                handler.handle(params);
                // Then
                verify(notificationService).sendMail(
                    "applicantemail@hmcts.net",
                    "test-template-no-fee-claimant-id-hmc",
                    getNotificationNoFeeDatePMDataMapHMC(caseData),
                    "notification-of-hearing-HER1234"
                );
            }
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1WithNoFee() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .hearingDueDate(null)
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(null)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDatePMDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked1v1WithNoFeeHMC() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build();

            when(hearingFeesService.getFeeForHearingFastTrackClaims(any()))
                .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(0)).build());
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                                .hearingId("HER1234")
                                .hearingStartDateTime(LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    LocalTime.of(15, 30)))
                                .hearingType("AAA7-TRI")
                                .build());

            LocalDate now = LocalDate.of(2022, 9, 29);
            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDate::now).thenReturn(now);
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
                // When
                when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC())
                    .thenReturn("test-template-no-fee-claimant-id-hmc");
                handler.handle(params);
                // Then
                verify(notificationService).sendMail(
                    "applicantemail@hmcts.net",
                    "test-template-no-fee-claimant-id-hmc",
                    getNotificationNoFeeDatePMDataMapHMC(caseData),
                    "notification-of-hearing-HER1234"
                );
            }
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithSpecClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build();

            when(hearingFeesService.getFeeForHearingFastTrackClaims(any()))
                .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(0)).build());
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                                .hearingId("HER1234")
                                .hearingStartDateTime(LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    LocalTime.of(15, 30)))
                                .hearingType("AAA7-TRI")
                                .build());

            LocalDate now = LocalDate.of(2022, 9, 29);
            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDate::now).thenReturn(now);
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
                // When
                when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC())
                    .thenReturn("test-template-no-fee-claimant-id-hmc");
                handler.handle(params);
                // Then
                verify(notificationService).sendMail(
                    "applicantemail@hmcts.net",
                    "test-template-no-fee-claimant-id-hmc",
                    getNotificationNoFeeDatePMDataMapHMC(caseData),
                    "notification-of-hearing-HER1234"
                );
            }
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v1WithNoSolicitorReferences() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(2022, 10, 6))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(SolicitorReferences.builder().build())
                .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn("test-template-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDatePMDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn("test-template-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData, true),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithFeeAnd2v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn("test-template-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData, false),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData, false),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd1v1HearingOther() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .hearingNoticeList(HearingNoticeList.OTHER)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeOtherHearingTypeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd1v1HearingOtherAfterRetrigger() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .hearingNoticeList(HearingNoticeList.OTHER)
                .listingOrRelisting(ListingOrRelisting.RELISTING)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData, false),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData, true),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedNoFeeAnd2v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("0830")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .build())
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData, false),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvokedAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1030")
                .applicant1Represented(YesOrNo.NO)
                .claimantUserDetails(IdamUserDetails.builder().email("applicant1@example.com").build())
                .hearingReferenceNumber("000HN001")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-claimant-lip-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "rambo@email.com",
                "test-template-claimant-lip-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvokedAnd1v1HMC() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .applicant1Represented(YesOrNo.NO)
                .claimantUserDetails(IdamUserDetails.builder().email("applicant1@example.com").build())
                .hearingReferenceNumber("000HN001")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
            // When
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                                .hearingId("HER1234")
                                .hearingStartDateTime(LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    LocalTime.of(10, 30)))
                                .hearingType("AAA7-DIS")
                                .build());
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-claimant-lip-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "rambo@email.com",
                "test-template-claimant-lip-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-HER1234"
            );
        }

        @Test
        void shouldNotifyApplicantLipinWelsh_whenInvokedAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1030")
                .applicant1Represented(YesOrNo.NO)
                .claimantUserDetails(IdamUserDetails.builder().email("applicant1@example.com").build())
                .hearingReferenceNumber("000HN001")
                .claimantBilingualLanguagePreference(Language.BOTH.getDisplayedValue())
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh())
                .thenReturn("test-template-claimant-lip-welsh-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "rambo@email.com",
                "test-template-claimant-lip-welsh-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-000HN001"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationFeeDataMap(CaseData caseData, boolean is1v2) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            "claimantReferenceNumber", "12345", "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "23-11-2022",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, is1v2 ? "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided"
                : "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationFeeDataMapHMC(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(), "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "06-10-2022",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDataMap(CaseData caseData, boolean is1v2) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            "claimantReferenceNumber", "12345", "hearingDate", "07-10-2022", "hearingTime", "08:30am",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, is1v2 ? "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided"
                : "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeOtherHearingTypeDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(), "hearingFee", "£0.00",
            "claimantReferenceNumber", "12345", "hearingDate", "07-10-2022",
            "hearingTime", "08:30am", "hearingDueDate", "",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDataMapHMC(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(), "hearingFee", "£0.00",
            "hearingDate", "07-10-2022", "hearingTime", "08:30am", "hearingDueDate", "06-10-2022",
            CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationFeeDatePMDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            "claimantReferenceNumber", "", "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "06-10-2022",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDatePMDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            "claimantReferenceNumber", "", "hearingDate", "07-10-2022", "hearingTime", "03:30pm",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDateHearingOtherDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            "claimantReferenceNumber", "", "hearingDate", "07-10-2022", "hearingTime", "03:30pm",
            CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            "hearingDate", "17-05-2023", "hearingTime", "10:30am",
            CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDatePMDataMapHMC(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(), "hearingFee", "£0.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "06-10-2022",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
        expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        return expectedProperties;
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_CLAIMANT_HEARING").build()).build())).isEqualTo(TASK_ID_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvokedHMC() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_CLAIMANT_HEARING_HMC").build()).build())).isEqualTo(TASK_ID_CLAIMANT_HMC);
    }

    @Test
    void shouldReturnEventNotFoundMessage_whenInvokedWithInvalidEvent() {

        // Given: an invalid event id
        CallbackParams callbackParams = CallbackParamsBuilder.builder().request(CallbackRequest.builder()
                                                                                    .eventId("TRIGGER_LOCATION_UPDATE").build()).build();
        // When: I call the camundaActivityId
        // Then: an exception is thrown
        CallbackException ex = assertThrows(CallbackException.class, () -> handler.camundaActivityId(callbackParams),
                                            "A CallbackException was expected to be thrown but wasn't.");
        assertThat(ex.getMessage()).contains("Callback handler received illegal event");
    }
}
