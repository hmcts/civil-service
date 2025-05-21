package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    NotifyClaimantJudgmentByAdmissionNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
class NotifyClaimantJudgmentByAdmissionNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    private static final String REFERENCE_NUMBER = "8372942374";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationsSignatureConfiguration configuration;

    @Autowired
    private NotifyClaimantJudgmentByAdmissionNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyClaimantLRJudgmentByAdmissionTemplate()).thenReturn(
                TEMPLATE_ID);
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        }

        @Test
        void shouldNotifyClaimantJudgmentByAdmission_whenInvoked() {

            CaseData caseData = CaseDataBuilder.builder()
                .legacyCaseReference(REFERENCE_NUMBER)
                .ccdCaseReference(12345L)
                .atStateClaimDraft()
                .applicant1Represented(YesOrNo.YES)
                .buildJudmentOnlineCaseDataWithPaymentImmediately();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_CLAIMANT_JUDGMENT_BY_ADMISSION.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "claimant-judgment-by-admission-8372942374"
            );
        }
    }

    public Map<String, String> getNotificationDataMap(CaseData caseData) {
        String defendantName = "";
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            defendantName = getPartyNameBasedOnType(caseData.getRespondent1());
        } else {
            defendantName = getPartyNameBasedOnType(caseData.getRespondent1())
                .concat(" and ")
                .concat(getPartyNameBasedOnType(caseData.getRespondent2()));
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData),
            DEFENDANT_NAME,  defendantName,
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

        );
    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
