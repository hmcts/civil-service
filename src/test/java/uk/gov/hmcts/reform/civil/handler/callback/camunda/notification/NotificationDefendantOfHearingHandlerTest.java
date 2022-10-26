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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT;

@SpringBootTest(classes = {
    NotificationDefendantOfHearingHandler.class,
    JacksonAutoConfiguration.class,
})
public class NotificationDefendantOfHearingHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;

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
        }

        @Nested
        class OneVsOneScenario {
            @Test
            void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                    .hearingDate(LocalDate.of(2022, 10, 07))
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                    .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                    .hearingReference("000HN001")
                    .hearingTimeHourMinute("1530")
                    .hearingDueDate(LocalDate.of(2022, 11, 23))
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT_HEARING").build()).build();
                handler.handle(params);
                verify(notificationService).sendMail(
                    "respondent1email@hmcts.net",
                    "test-template-no-fee-defendant-id",
                    getNotificationDataMap(caseData),
                    "notification-of-hearing-000HN001"
                );
            }
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 07))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReference("000HN001")
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .solicitorReferences(SolicitorReferences.builder().respondentSolicitor1Reference("6789").build())
                .respondentSolicitor2Reference("10111213")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT_HEARING").build()).build();
            handler.handle(params);
            verify(notificationService, times(2)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            //Email to respondent1
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1email@hmcts.net");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-no-fee-defendant-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-000HN001");
            //Email to respondent2
            assertThat(targetEmail.getAllValues().get(1)).isEqualTo("respondent2email@hmcts.net");
            assertThat(emailTemplate.getAllValues().get(1)).isEqualTo("test-template-no-fee-defendant-id");
            assertThat(notificationDataMap.getAllValues().get(1)).isEqualTo(getNotificationDataMapDef2(caseData));
            assertThat(reference.getAllValues().get(1)).isEqualTo("notification-of-hearing-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 07))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantemail@hmcts.net").build())
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReference("000HN001")
                .hearingTimeHourMinute("1530")
                .hearingDueDate(LocalDate.of(2022, 11, 23))
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT_HEARING").build()).build();
            handler.handle(params);
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "defendantReferenceNumber", "6789", "hearingFee", "£0.00", "hearingDate", "07-10-2022",
            "hearingTime", "15:30", "hearingDueDate", "23-11-2022"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMapDef2(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            "defendantReferenceNumber", "10111213", "hearingFee", "£0.00", "hearingDate", "07-10-2022",
            "hearingTime", "15:30", "hearingDueDate", "23-11-2022"
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_DEFENDANT_HEARING").build()).build())).isEqualTo(TASK_ID_DEFENDANT);
    }
}
