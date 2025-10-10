package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DISCONTINUANCE_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@SpringBootTest(classes = {
    NotifyDefendantClaimDiscontinuedNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
class NotifyDefendantClaimDiscontinuedNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    private static final String REFERENCE_NUMBER = "8372942374";

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationsProperties notificationsProperties;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private NotificationsSignatureConfiguration configuration;

    @MockitoBean
    private OrganisationService organisationService;

    @Autowired
    private NotifyDefendantClaimDiscontinuedNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyClaimDiscontinuedLRTemplate()).thenReturn(
                TEMPLATE_ID);
            when(notificationsProperties.getNotifyClaimDiscontinuedLipTemplate()).thenReturn(
                TEMPLATE_ID);
            when(notificationsProperties.getNotifyClaimDiscontinuedWelshLipTemplate()).thenReturn(
                TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Test
        void shouldNotifyDefendantLRClaimDiscontinued_whenInvoked() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            CaseData caseData = CaseDataBuilder.builder()
                .legacyCaseReference(REFERENCE_NUMBER)
                .ccdCaseReference(12345L)
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .atStateClaimDraft().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DISCONTINUANCE_DEFENDANT1.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                addProperties(caseData),
                "defendant-claim-discontinued-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenInvoked() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .applicant1(Party.builder()
                                .individualFirstName("Applicant1").individualLastName("ApplicantLastName").partyName("Applicant1")
                                .type(Party.Type.INDIVIDUAL).partyEmail("applicantLip@example.com").build())
                .respondent1(Party.builder().partyName("Respondent1").individualFirstName("Respondent1").individualLastName("RespondentLastName")
                                 .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
                .respondent1Represented(null)
                .legacyCaseReference("000DC001")
                .specRespondent1Represented(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_DISCONTINUANCE_DEFENDANT1.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentLip@example.com",
                TEMPLATE_ID,
                getNotificationLipDataMap(caseData),
                "defendant-claim-discontinued-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentWelshLip_whenInvoked() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .applicant1(Party.builder()
                                .individualFirstName("Applicant1").individualLastName("ApplicantLastName")
                                .partyName("Applicant1")
                                .type(Party.Type.INDIVIDUAL).partyEmail("applicantLip@example.com").build())
                .respondent1(Party.builder().partyName("Respondent1").individualFirstName("Respondent1")
                                 .individualLastName("RespondentLastName")
                                 .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
                .respondent1Represented(NO)
                .legacyCaseReference("000DC001")
                .specRespondent1Represented(NO)
                .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                              .respondent1ResponseLanguage("BOTH")
                                                                              .build()).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_DISCONTINUANCE_DEFENDANT1.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentLip@example.com",
                TEMPLATE_ID,
                getNotificationLipDataMap(caseData),
                "defendant-claim-discontinued-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondentLipWithOutEmail_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .applicant1(Party.builder()
                                .individualFirstName("Applicant1").individualLastName("ApplicantLastName").partyName("Applicant1")
                                .type(Party.Type.INDIVIDUAL).partyEmail("applicantLip@example.com").build())
                .respondent1(Party.builder().partyName("Respondent1").individualFirstName("Respondent1").individualLastName("RespondentLastName")
                                 .type(Party.Type.INDIVIDUAL).build())
                .respondent1Represented(NO)
                .legacyCaseReference("000DC001")
                .specRespondent1Represented(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_DISCONTINUANCE_DEFENDANT1.name()).build()
            ).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }
    }

    private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName()
        ));
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
        return expectedProperties;
    }

    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, "Test Org Name",
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
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

}
