package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimContinuingOnlineApplicantForSpecNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
public class ClaimContinuingOnlineApplicantForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineApplicantForSpecNotificationHandler handler;

    public static final String ORG_NAME = "Signer Name";
    public static final String APPLICANT_SOLICITOR_EMAIL = "applicantsolicitor@example.com";
    public static final String REFERENCE = "claim-continuing-online-notification-000DC001";
    public static final String TEMPLATE = "template-id";
    public static final String TEMPLATE_1v2 = "template-id-1v2-two-legal-reps";
    public static final String RESPONSE_DEADLINE = "responseDeadline";
    public static final String PARTY_NAME = "partyName";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyClaimantSolicitor_in1v1_whenInvoked() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData, false);
            expectedProperties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_when1v2_SameLegalRep() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec()).thenReturn(TEMPLATE_1v2);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimSameDefendantSolicitor()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_1v2,
                getNotificationDataMap(caseData, false),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_when1v2_TwoLegalReps() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec()).thenReturn(TEMPLATE_1v2);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_1v2,
                getNotificationDataMap(caseData, true),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_in2v1() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoApplicants()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData, false);
            expectedProperties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData, boolean is1v2DS) {

            Map<String, String> properties = new HashMap<>(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE),
                PARTY_REFERENCES, is1v2DS ? "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234" :
                "Claimant reference: 12345 - Defendant reference: 6789"
            ));

            if (caseData.getRespondent2() != null) {
                properties.put(RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
                properties.put(RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
            } else if (caseData.getRespondent1() != null) {
                properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
                properties.put(RESPONSE_DEADLINE, formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));
            }

            return properties;
        }

        @Test
        void shouldGetApplicantSolicitor1ClaimStatementOfTruth_whenNoOrgFound() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.empty());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData, false);
            expectedProperties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenRespondent1NotRepresented() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData =
                CaseDataBuilder.builder()
                    .respondent1(Party.builder().partyName(PARTY_NAME).build())
                    .atStateClaimDetailsNotified().respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData, false);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenRespondent1IsRepresented() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData =
                CaseDataBuilder.builder()
                    .respondent1(Party.builder().partyName(PARTY_NAME).build())
                    .atStateClaimDetailsNotified().respondent1Represented(YesOrNo.YES).build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData, false);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldSkipEvent_whenApplicant1SolicitorNotRepresented() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateClaimDetailsNotified().applicant1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).isNull();
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    }
}
