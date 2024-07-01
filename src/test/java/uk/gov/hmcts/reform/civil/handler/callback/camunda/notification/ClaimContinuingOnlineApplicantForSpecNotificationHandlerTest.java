package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ClaimContinuingOnlineApplicantForSpecNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
public class ClaimContinuingOnlineApplicantForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
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

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec())
                .thenReturn(TEMPLATE_1v2);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));
        }

        @Test
        void shouldNotifyClaimantSolicitor_in1v1_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            // When
            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData);
            expectedProperties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));

            // Then
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_when1v2_SameLegalRep() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_1v2,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_when1v2_TwoLegalReps() {
            // Given
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

            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_1v2,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_in2v1() {
            // Given
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

            // When
            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData);
            expectedProperties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));

            // Then
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {

            Map<String, String> properties = new HashMap<>(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.empty());
            // When
            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData);
            expectedProperties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));

            // Then
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenRespondent1NotRepresented() {
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

            // When
            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData);

            // Then
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenRespondent1IsRepresented() {
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

            // When
            handler.handle(params);

            Map<String, String> expectedProperties = getNotificationDataMap(caseData);

            // Then
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

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
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
