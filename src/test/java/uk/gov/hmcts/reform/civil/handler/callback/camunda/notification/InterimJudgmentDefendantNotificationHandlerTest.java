package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_DEF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;

@ExtendWith(MockitoExtension.class)
public class InterimJudgmentDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InterimJudgmentDefendantNotificationHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenInvoked() {
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));


            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id-app",
                getNotificationDataMap(),
                "interim-judgment-approval-notification-def-000DC001"
            );
        }

        @Test
        void shouldReturnPartyNameIfRespondentIsLip() {

            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mr.")
                                 .individualFirstName("Don")
                                 .individualLastName("Smith")
                                 .build())
                .respondent1OrganisationPolicy(null)
                .legacyCaseReference("12DC910")
                .respondent2OrganisationPolicy(null).build().toBuilder()
                .ccdCaseReference(1594901956117591L).build();

            Map<String, String> propertyMap = handler.addProperties(caseData);
            assertEquals("Mr. Don Smith", propertyMap.get(LEGAL_ORG_DEF));
        }

        @Test
        void shouldReturnPartyNameIfOrgnisationPolicyIsSetButOrgIdMissing() {

            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mr.")
                                 .individualFirstName("Don")
                                 .individualLastName("Smith")
                                 .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .orgPolicyCaseAssignedRole("[RESPSOLICITORONE]")
                                                   .organisation(uk.gov.hmcts.reform.ccd.model
                                                                     .Organisation.builder().build()).build())
                .legacyCaseReference("12DC910")
                .respondent2OrganisationPolicy(null).build().toBuilder()
                .ccdCaseReference(1594901956117591L).build();

            Map<String, String> propertyMap = handler.addProperties(caseData);
            assertEquals("Mr. Don Smith", propertyMap.get(LEGAL_ORG_DEF));
        }

        @Test
        void shouldNotifyClaimantSolicitor2Defendants_whenInvoked() {
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));


            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                anyString(),
                eq("template-id-app"), anyMap(),
                eq("interim-judgment-approval-notification-def-000DC001"));
        }

        @Test
        public void shouldNotifyRespondent2SolWhenRespondent1Lip() {
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));


            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                anyString(),
                eq("template-id-app"), anyMap(),
                eq("interim-judgment-approval-notification-def-000DC001"));
        }

        private Map<String, String> getNotificationDataMap() {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put("Defendant LegalOrg Name", "Test Org Name");
            properties.put("Claim number", "1594901956117591");
            properties.put("Defendant Name", "Mr. Sole Trader");
            properties.put("partyReferences", "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CASEMAN_REF, "000DC001");
            return properties;
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
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            return expectedProperties;
        }
    }

    @Test
    void shouldNotNotify_whenLipDefendant() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                             .individualTitle("Mr.")
                             .individualFirstName("Don")
                             .individualLastName("Smith")
                             .build())
            .respondent1OrganisationPolicy(null)
            .legacyCaseReference("12DC910")
            .respondent2OrganisationPolicy(null).build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .ccdCaseReference(1594901956117591L).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verifyNoInteractions(notificationService);
        assertThat(response.getState()).isEqualTo("JUDICIAL_REFERRAL");
    }

    @Test
    public void shouldNotNotifyRespondent2WhenLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                             .individualTitle("Mr.")
                             .individualFirstName("Don")
                             .individualLastName("Smith")
                             .build())
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                             .individualTitle("Mrs.")
                             .individualFirstName("Donna")
                             .individualLastName("Smith")
                             .build())
            .respondent1OrganisationPolicy(null)
            .legacyCaseReference("12DC910")
            .respondent2OrganisationPolicy(null)
            .respondent1Represented(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .respondent2Represented(YesOrNo.NO)
            .ccdCaseReference(1594901956117591L).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verifyNoInteractions(notificationService);
        assertThat(response.getState()).isEqualTo("JUDICIAL_REFERRAL");
    }
}
