package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT1_HMC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT2_HMC;

@SpringBootTest(classes = {
    NotificationDefendantOfHearingHandler.class,
    JacksonAutoConfiguration.class,
})
public class NotificationDefendantOfHearingHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @MockBean
    HearingNoticeCamundaService hearingNoticeCamundaService;

    @Captor
    private ArgumentCaptor<String> targetEmail;

    @Captor
    private ArgumentCaptor<String> emailTemplate;

    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;

    @Captor
    private ArgumentCaptor<String> reference;

    @Autowired
    private NotificationDefendantOfHearingHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-defendant-lip-id");

            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                                .hearingStartDateTime(LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    LocalTime.of(15, 30)
                                ))
                                .hearingId("123456")
                                .build());

            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC())
                .thenReturn("test-template-no-fee-defendant-id-hmc");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1AndNoSolicitorReferences() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMapNoReference(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1AndNoSolicitorReferencesForDef1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(SolicitorReferences.builder().build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMapNoReference(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvokedNoFeeAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .solicitorReferences(SolicitorReferences.builder().respondentSolicitor1Reference("6789").build())
                .respondentSolicitor2Reference("10111213")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent2email@hmcts.net");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-no-fee-defendant-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMapDef2(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvokedNoFeeAnd1v2WithSameSolicitor() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress(null)
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .solicitorReferences(SolicitorReferences.builder().respondentSolicitor1Reference("6789").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1email@hmcts.net");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-no-fee-defendant-id");
            assertThat(notificationDataMap.getAllValues().get(0))
                .isEqualTo(getNotificationDataMapDef2WithNoReference(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd2v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorLip_whenInvokedAnd1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1(Party.builder().partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1@example.com",
                "test-template-defendant-lip-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorLip1_whenInvokedAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .applicant1(Party.builder().partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder().partyName("Peter").partyEmail("respondent2@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-defendant-lip-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationLipDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-lip-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitorLip2_whenInvokedAnd1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .applicant1(Party.builder().partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder().partyName("Peter").partyEmail("respondent2@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent2@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-defendant-lip-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationLipDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-lip-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitorLip1_whenInvokedAnd1v2AndResp2EmailNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .applicant1(Party.builder().partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder().partyName("Peter").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-defendant-lip-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationLipDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-lip-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1HMC() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING_HMC").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id-hmc",
                getNotificationDataMapHMC(caseData),
                "notification-of-hearing-123456"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v2DS_HMC() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .businessProcess(BusinessProcess.builder().processInstanceId("").build())
                .build().toBuilder()
                .respondentSolicitor2Reference("6789").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING_HMC").build()).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent2email@hmcts.net",
                "test-template-no-fee-defendant-id-hmc",
                getNotificationDataMapHMC(caseData),
                "notification-of-hearing-123456"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapHMC(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm",
                "defendantReferenceNumber", "6789"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "defendantReferenceNumber", "6789", "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapNoReference(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "defendantReferenceNumber", "", "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapDef2(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "defendantReferenceNumber", "10111213", "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapDef2WithNoReference(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "defendantReferenceNumber", "", "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm"
            );
        }

        @NotNull
        private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "hearingDate", "17-05-2023", "hearingTime", "11:00am"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                             .builder().eventId(
                    "NOTIFY_DEFENDANT1_HEARING").build()).build())).isEqualTo(TASK_ID_DEFENDANT1);
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvokedWithDefendant2() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                             .builder().eventId(
                    "NOTIFY_DEFENDANT2_HEARING").build()).build())).isEqualTo(TASK_ID_DEFENDANT2);
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvokedWithDefendant1Hmc() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                             .builder().eventId(
                    "NOTIFY_DEFENDANT1_HEARING_HMC").build()).build())).isEqualTo(TASK_ID_DEFENDANT1_HMC);
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvokedWithDefendant2Hmc() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                             .builder().eventId(
                    "NOTIFY_DEFENDANT2_HEARING_HMC").build()).build())).isEqualTo(TASK_ID_DEFENDANT2_HMC);
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
}
