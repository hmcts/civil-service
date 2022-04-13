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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimContinuingOnlineApplicantForSpecNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@RunWith(SpringRunner.class)
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
    @MockBean
    private FeatureToggleService toggleService;
    @Autowired
    private ClaimContinuingOnlineApplicantForSpecNotificationHandler handler;

    public static final String ORG_NAME = "Signer Name";
    public static final String APPLICANT_SOLICITOR_EMAIL = "applicantsolicitor@example.com";
    public static final String REFERENCE = "claim-continuing-online-notification-000DC001";
    public static final String TEMPLATE = "template-id";
    public static final String TEMPLATE_1v2_TWO_LRS = "template-id-1v2-two-legal-reps";

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
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec()).thenReturn(TEMPLATE);
            when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2TwoLRsForSpec())
                .thenReturn(TEMPLATE_1v2_TWO_LRS);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_CLAIMANT_LR_SPEC")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor_when1v2_TwoLegalReps() {
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
                TEMPLATE_1v2_TWO_LRS,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            if (getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                return Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                    CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                    RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                    RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                    ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                    RESPONSE_DEADLINE, formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT)
                );
            } else {
                return Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                    CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                    ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                    RESPONSE_DEADLINE, formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT)
                );
            }
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
}
