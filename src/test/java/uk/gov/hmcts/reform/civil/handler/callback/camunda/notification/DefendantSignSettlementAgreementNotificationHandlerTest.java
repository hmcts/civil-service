package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;

@ExtendWith(MockitoExtension.class)
class DefendantSignSettlementAgreementNotificationHandlerTest {

    private DefendantSignSettlementAgreementNotificationHandler handler;

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private PinInPostConfiguration pipInPostConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;

    private static final String templateId = "templateId";

    @BeforeEach
    void setup() {
        handler = new DefendantSignSettlementAgreementNotificationHandler(
            notificationService,
            notificationsProperties,
            pipInPostConfiguration,
            configuration,
            featureToggleService
        );
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
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
    void notifyApplicantForSignedSettlement() {

        Mockito.when(notificationsProperties.getNotifyApplicantForSignedSettlementAgreement())
            .thenReturn(templateId);
        CaseData.CaseDataBuilder caseData = createCaseData();
        CaseData caseDataInstance =
            (CaseData) caseData.caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES)
                                                .build()).build();

        CallbackParams params = createCallbackParams(
            CaseEvent.NOTIFY_LIP_APPLICANT_FOR_SIGN_SETTLEMENT_AGREEMENT,
            caseDataInstance
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            "applicant1@gmail.com",
            templateId,
            createExpectedTemplateProperties(),
            "notify-signed-settlement-legacy ref"
        );
    }

    @Test
    void notifyDefendantForSignedSettlement() {

        Mockito.when(notificationsProperties.getNotifyRespondentForSignedSettlementAgreement())
            .thenReturn(templateId);
        CaseData.CaseDataBuilder caseData = createCaseData();
        CaseData caseDataInstance =
            (CaseData) caseData.caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES)
                                                .build()).build();

        CallbackParams params = createCallbackParams(
            CaseEvent.NOTIFY_LIP_RESPONDENT_FOR_SIGN_SETTLEMENT_AGREEMENT,
            caseDataInstance
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            "respondent@gmail.com",
            templateId,
            createExpectedTemplateProperties(),
            "notify-signed-settlement-legacy ref"
        );
    }

    @Test
    void notifyApplicantForRejectedSignedSettlement() {

        Mockito.when(notificationsProperties.getNotifyApplicantForNotAgreedSignSettlement())
            .thenReturn(templateId);
        CaseData.CaseDataBuilder caseData = createCaseData();
        CaseData caseDataInstance =
            (CaseData) caseData.caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO)
                                                .build()).build();

        CallbackParams params = createCallbackParams(
            CaseEvent.NOTIFY_LIP_APPLICANT_FOR_SIGN_SETTLEMENT_AGREEMENT,
            caseDataInstance
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            "applicant1@gmail.com",
            templateId,
            createExpectedTemplateProperties(),
            "notify-signed-settlement-legacy ref"
        );
    }

    @Test
    void notifyDefendantForRejectedSignedSettlement() {

        Mockito.when(notificationsProperties.getNotifyRespondentForNotAgreedSignSettlement())
            .thenReturn(templateId);
        CaseData.CaseDataBuilder caseData = createCaseData();
        CaseData caseDataInstance =
            (CaseData) caseData.caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO)
                                                .build()).build();

        CallbackParams params = createCallbackParams(
            CaseEvent.NOTIFY_LIP_RESPONDENT_FOR_SIGN_SETTLEMENT_AGREEMENT,
            caseDataInstance
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            "respondent@gmail.com",
            templateId,
            createExpectedTemplateProperties(),
            "notify-signed-settlement-legacy ref"
        );
    }

    @Test
    void notifyApplicantForSignedSettlement_InBilingual() {
        Mockito.when(notificationsProperties.getNotifyApplicantLipForSignedSettlementAgreementInBilingual())
                .thenReturn(templateId);
        CaseData.CaseDataBuilder caseData = createCaseData();
        CaseData caseDataInstance =
                (CaseData) caseData.caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES)
                        .build())
                        .claimantBilingualLanguagePreference(Language.WELSH.toString()).build();

        CallbackParams params = createCallbackParams(
                CaseEvent.NOTIFY_LIP_APPLICANT_FOR_SIGN_SETTLEMENT_AGREEMENT,
                caseDataInstance
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            "applicant1@gmail.com",
            templateId,
            createExpectedTemplateProperties(),
            "notify-signed-settlement-legacy ref"
        );
    }

    @Test
    void notifyApplicantForRejectedSignedSettlement_InBilingual() {
        Mockito.when(notificationsProperties.getNotifyApplicantLipForNotAgreedSignSettlementInBilingual())
                .thenReturn(templateId);
        CaseData.CaseDataBuilder caseData = createCaseData();
        CaseData caseDataInstance =
                (CaseData) caseData.caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO)
                        .build())
                        .claimantBilingualLanguagePreference(Language.WELSH.toString()).build();

        CallbackParams params = createCallbackParams(
                CaseEvent.NOTIFY_LIP_APPLICANT_FOR_SIGN_SETTLEMENT_AGREEMENT,
                caseDataInstance
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            "applicant1@gmail.com",
            templateId,
            createExpectedTemplateProperties(),
            "notify-signed-settlement-legacy ref"
        );
    }

    private CaseData.CaseDataBuilder createCaseData() {
        return CaseData.builder()
            .legacyCaseReference("legacy ref")
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("mr")
                            .individualFirstName("applicant1")
                            .individualLastName("lip")
                            .partyEmail("applicant1@gmail.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("mr")
                             .individualFirstName("respondent")
                             .individualLastName("lip")
                             .partyEmail("respondent@gmail.com")
                             .build());

    }

    private CallbackParams createCallbackParams(CaseEvent caseEvent, CaseData caseData) {
        return CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(caseEvent.name())
                         .build())
            .build();
    }

    private Map<String, String> createExpectedTemplateProperties() {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            "defendantName", "mr respondent lip",
            "claimReferenceNumber", "legacy ref",
            "claimantName", "mr applicant1 lip",
            "frontendBaseUrl", "dummy_cui_front_end_url"
        ));
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
