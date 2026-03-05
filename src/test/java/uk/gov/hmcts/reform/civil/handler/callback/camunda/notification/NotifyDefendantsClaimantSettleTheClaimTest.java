package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

@ExtendWith(MockitoExtension.class)
public class NotifyDefendantsClaimantSettleTheClaimTest extends BaseCallbackHandlerTest {

    @Mock
    private OrganisationService organisationService;
    @InjectMocks
    private NotifyDefendantsClaimantSettleTheClaim notificationHandler;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @Mock
    NotificationsProperties notificationsProperties;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;

    @Nested
    class AboutToSubmitCallback {

        private static final String DEFENDANT_LIP_EMAIL_ADDRESS = "defendantmail@hmcts.net";
        private static final String DEFENDANT_LR_EMAIL_ADDRESS = "defendantlr@hmcts.net";
        private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
        private static final String REFERENCE_NUMBER = "8372942374";
        private static final String EMAIL_TEMPLATE = "test-notification-id";
        private static final String EMAIL_TEMPLATE_LR = "test-notification-lr-id";
        private static final String CLAIMANT_ORG_NAME = "Org Name";

        @BeforeEach
        void setUp() {
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
        void shouldSendNotificationToDefendantLip_whenLiPvLiPandDefendantHasEmail() {
            //When
            given(notificationsProperties.getNotifyDefendantLIPClaimantSettleTheClaimTemplate()).willReturn(EMAIL_TEMPLATE);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_LIP_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM.name()).build()).build();
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(),
                                                           reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_LIP_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(RESPONDENT_NAME, "ABC ABC");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIM_REFERENCE_NUMBER, "8372942374");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIMANT_NAME, "Org Name");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(PHONE_CONTACT, configuration.getPhoneContact());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(OPENING_HOURS, configuration.getOpeningHours());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(LIP_CONTACT, configuration.getLipContactEmail());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CNBC_CONTACT, configuration.getCnbcContact());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldSendNotificationToDefendantLR_whenLiPvLRandDefendantHasEmail(boolean referenceWasProvided) {
            //When
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation().setName("Legal Rep Name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            given(notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimTemplate()).willReturn(EMAIL_TEMPLATE_LR);

            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .respondent1Represented(YesOrNo.YES)
                .respondent1OrganisationPolicy(organisationPolicy("ORG_ID", "[RESPONDENTSOLICITORONE]"))
                .respondentSolicitor1EmailAddress(DEFENDANT_LR_EMAIL_ADDRESS)
                .solicitorReferences(new SolicitorReferences()
                    .setRespondentSolicitor1Reference(referenceWasProvided ? "Def Ref Num" : null))
                .legacyCaseReference(REFERENCE_NUMBER)
                .ccdCaseReference(1234567891234567L)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM.name()).build()).build();
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(),
                                                           reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_LR_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE_LR);
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIM_REFERENCE_NUMBER, "8372942374");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIMANT_NAME, "Org Name");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567891234567");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(DEFENDANT_REFERENCE_NUMBER,
                                                                                referenceWasProvided ? "Def Ref Num" : "Not provided");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(LEGAL_REP_NAME, "Legal Rep Name");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(PHONE_CONTACT, configuration.getPhoneContact());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(OPENING_HOURS, configuration.getOpeningHours());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(LIP_CONTACT, configuration.getLipContactEmail());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CNBC_CONTACT, configuration.getCnbcContact());
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        }

        @Test
        void shouldOnlySendNotificationToDefendantLR_whenLiPvLRandDefendantHasEmail() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_LIP_EMAIL_ADDRESS).build())
                .respondent1Represented(YesOrNo.YES)
                .respondent1OrganisationPolicy(organisationPolicy("ORG_ID", "[RESPONDENTSOLICITORONE]"))
                .respondentSolicitor1EmailAddress(DEFENDANT_LR_EMAIL_ADDRESS)
                .solicitorReferences(new SolicitorReferences().setRespondentSolicitor1Reference(null))
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .ccdCaseReference(1234567891234567L)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM.name()).build()).build();
            //When
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation().setName("Legal Rep Name")));
            given(notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimTemplate()).willReturn(EMAIL_TEMPLATE_LR);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(),
                                                           reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_LR_EMAIL_ADDRESS);
        }
    }

    private OrganisationPolicy organisationPolicy(String organisationId, String role) {
        OrganisationPolicy policy = new OrganisationPolicy();
        if (organisationId != null) {
            policy.setOrganisation(new Organisation().setOrganisationID(organisationId));
        }
        return policy.setOrgPolicyCaseAssignedRole(role);
    }
}
