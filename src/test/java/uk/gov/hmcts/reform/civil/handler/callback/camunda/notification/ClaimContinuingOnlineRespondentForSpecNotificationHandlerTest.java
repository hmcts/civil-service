package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimContinuingOnlineRespondentForSpecNotificationHandler.TASK_ID_Respondent1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineRespondentForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private Time time;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ClaimContinuingOnlineRespondentForSpecNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("test solicatior").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails.builder()
                                                             .email("testorg@email.com")
                                                             .organisationName("test solicatior").build())
                .claimDetailsNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now())
                .addRespondent2(YesOrNo.NO)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Solicitor_whenInvoked() {
            when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("test solicatior").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails.builder()
                                                             .email("testorg@email.com")
                                                             .organisationName("test solicatior").build())
                .claimDetailsNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondent2SolicitorIfNoSecondDefendant_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails.builder()
                                                             .email("testorg@email.com")
                                                             .organisationName("test solicatior").build())
                .claimDetailsNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now())
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                    .build()).build();

            handler.handle(params);
        }

        @Test
        void shouldNotNotifyRespondent2SolicitorIf2ndDefendantSameLegalRep_whenInvoked() {
            when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("test solicatior").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails.builder()
                                                             .email("testorg@email.com")
                                                             .organisationName("test solicatior").build())
                .claimDetailsNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                    .build()).build();

            handler.handle(params);
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, "test solicatior",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(LocalDate.now(), DATE)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC").build())
                                                 .build())).isEqualTo(TASK_ID_Respondent1);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    }
}
