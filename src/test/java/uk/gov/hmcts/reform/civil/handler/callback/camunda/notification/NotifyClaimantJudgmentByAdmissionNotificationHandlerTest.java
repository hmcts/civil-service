package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
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

import java.util.HashMap;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
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

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationsProperties notificationsProperties;

    @MockitoBean
    private OrganisationService organisationService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private NotificationsSignatureConfiguration configuration;

    @Autowired
    private NotifyClaimantJudgmentByAdmissionNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyClaimantLRJudgmentByAdmissionTemplate()).thenReturn(
                TEMPLATE_ID);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

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

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
        String defendantName = "";
        var scenario = getMultiPartyScenario(caseData);
        if (scenario.equals(ONE_V_ONE) || scenario.equals(TWO_V_ONE)) {
            defendantName = getPartyNameBasedOnType(caseData.getRespondent1());
        } else {
            defendantName = getPartyNameBasedOnType(caseData.getRespondent1())
                + " and " + getPartyNameBasedOnType(caseData.getRespondent2());
        }
        expectedProperties.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData),
            DEFENDANT_NAME, defendantName,
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        return expectedProperties;
    }

    @NotNull
    public Map<String, String> addCommonProperties() {
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        return expectedProperties;
    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
