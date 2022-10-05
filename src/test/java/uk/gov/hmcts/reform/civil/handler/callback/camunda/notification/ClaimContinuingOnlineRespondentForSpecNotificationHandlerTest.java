package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimContinuingOnlineRespondentForSpecNotificationHandler.TASK_ID_Respondent1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    ClaimContinuingOnlineRespondentForSpecNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
public class ClaimContinuingOnlineRespondentForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private Time time;
    @MockBean
    private FeatureToggleService toggleService;

    @Autowired
    private ClaimContinuingOnlineRespondentForSpecNotificationHandler handler;

    @org.junit.Test
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec())
                .thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("test solicatior").build()));
        }

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
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
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Solicitor_whenInvoked() {
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
                getNotificationDataMap(caseData),
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

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
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
}
