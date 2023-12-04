package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationClaimantOfHearingHandler.TASK_ID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationClaimantOfHearingHandler.TASK_ID_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@SpringBootTest(classes = {
    NotificationClaimantOfHearingHandler.class,
    JacksonAutoConfiguration.class,
})
public class NotificationClaimantOfHearingHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private HearingFeesService hearingFeesService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @MockBean
    HearingNoticeCamundaService hearingNoticeCamundaService;
    @Autowired
    private NotificationClaimantOfHearingHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn("test-template-fee-claimant-id");
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn("test-template-no-fee-claimant-id");
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-claimant-lip-id");
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC())
                .thenReturn("test-template-fee-claimant-id-hmc");
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC())
                .thenReturn("test-template-no-fee-claimant-id-hmc");
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
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData),
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
                                .build());

            LocalDate now = LocalDate.of(2022, 9, 29);
            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDate::now).thenReturn(now);
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
                // When
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
                                .build());

            LocalDate now = LocalDate.of(2022, 9, 29);
            try (MockedStatic<LocalDate> mock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDate::now).thenReturn(now);
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING_HMC").build()).build();
                // When
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
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
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
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData),
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
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-fee-claimant-id",
                getNotificationFeeDataMap(caseData),
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
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData),
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
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData),
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
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantemail@hmcts.net",
                "test-template-no-fee-claimant-id",
                getNotificationNoFeeDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorLip_whenInvokedAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1030")
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicant1@example.com",
                "test-template-claimant-lip-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-000HN001"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationFeeDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "12345", "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "23-11-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationFeeDataMapHMC(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(), "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "06-10-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "12345", "hearingDate", "07-10-2022", "hearingTime", "08:30am"
        );
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDataMapHMC(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(), "hearingFee", "£0.00",
            "hearingDate", "07-10-2022", "hearingTime", "08:30am", "hearingDueDate", "06-10-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationFeeDatePMDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "", "hearingFee", "£300.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "06-10-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDatePMDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "claimantReferenceNumber", "", "hearingDate", "07-10-2022", "hearingTime", "03:30pm"
        );
    }

    @NotNull
    private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "hearingDate", "17-05-2023", "hearingTime", "10:30am"
        );
    }

    @NotNull
    private Map<String, String> getNotificationNoFeeDatePMDataMapHMC(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(), "hearingFee", "£0.00",
            "hearingDate", "07-10-2022", "hearingTime", "03:30pm", "hearingDueDate", "06-10-2022"
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_CLAIMANT_HEARING").build()).build())).isEqualTo(TASK_ID_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvokedHMC() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("NOTIFY_CLAIMANT_HEARING").build()).build();
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
