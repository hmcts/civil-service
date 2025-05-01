package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimSetAsideJudgmentClaimantNotificationHandlerTest.TEMPLATE_ID_LIP;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class ClaimDJNonDivergentDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimDJNonDivergentDefendantNotificationHandler handler;

    private static final String TEMPLATE_ID = "template-id";
    private static final String TASK_ID_RESPONDENT1 = "NotifyDJNonDivergentDefendant1";
    private static final String TASK_ID_RESPONDENT2 = "NotifyDJNonDivergentDefendant2";
    private static final String TASK_ID_RESPONDENT1_LIP = "NotifyDJNonDivergentDefendant1LiP";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyDefendantSolicitor1_whenInvoked() {
            when(notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "dj-non-divergent-defendant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor2_whenInvoked() {
            when(notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "dj-non-divergent-defendant-notification-000DC001"
            );

        }

        @Test
        void shouldNotifyDefendantLipSolicitor_whenInvoked() {
            when(notificationsProperties.getNotifyUpdateTemplate()).thenReturn(TEMPLATE_ID_LIP);

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate().toBuilder()
                .applicant1(Party.builder()
                                .individualFirstName("Applicant1")
                                .individualLastName("ApplicantLastName").partyName("Applicant1")
                                .type(Party.Type.INDIVIDUAL).partyEmail("applicantLip@example.com").build())
                .respondent1(Party.builder().partyName("Respondent1")
                                 .individualFirstName("Respondent1").individualLastName("RespondentLastName")
                                 .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
                .applicant1Represented(YesOrNo.NO)
                .legacyCaseReference("000DC001")
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
                .build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentLip@example.com",
                TEMPLATE_ID_LIP,
                getNotificationDataMapLip(caseData),
                "dj-non-divergent-defendant-notification-lip-000DC001"
            );
        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, "Test Org Name",
            DEFENDANT_NAME_INTERIM, "Mr. Sole Trader and Mr. John Rambo",
            CLAIMANT_NAME, "Mr. John Rambo",
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private Map<String, String> getNotificationDataMapLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            PARTY_NAME, caseData.getRespondent1().getPartyName()
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT2);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1_LIP);
    }
}
