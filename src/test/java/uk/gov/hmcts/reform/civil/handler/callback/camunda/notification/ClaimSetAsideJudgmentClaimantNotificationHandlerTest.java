package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON_FROM_CASEWORKER;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@SpringBootTest(classes = {
    ClaimSetAsideJudgmentClaimantNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
public class ClaimSetAsideJudgmentClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String TEMPLATE_ID_LIP = "template-id-lip";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private ClaimSetAsideJudgmentClaimantNotificationHandler handler;

    @MockBean
    private OrganisationService organisationService;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifySetAsideJudgmentTemplate()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getNotifyUpdateTemplate()).thenReturn(TEMPLATE_ID_LIP);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyApplicantOnlyOneSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicant2RespondToDefenceAndProceed_2v1()
                .build();
            caseData.setJoSetAsideJudgmentErrorText("test error");

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.SET_ASIDE_JUDGMENT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "set-aside-judgment-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLipSolicitor_whenInvoked() {
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
                .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
                .build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.SET_ASIDE_JUDGMENT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "applicantLip@example.com",
                TEMPLATE_ID_LIP,
                getNotificationDataMapLip(caseData),
                "set-aside-judgment-applicant-notification-lip-000DC001"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, "Test Org Name",
            REASON_FROM_CASEWORKER, "test error",
            DEFENDANT_NAME_INTERIM, "Mr. Sole Trader"
        );
    }

    private Map<String, String> getNotificationDataMapLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            PARTY_NAME, caseData.getApplicant1().getPartyName()
        );
    }
}
