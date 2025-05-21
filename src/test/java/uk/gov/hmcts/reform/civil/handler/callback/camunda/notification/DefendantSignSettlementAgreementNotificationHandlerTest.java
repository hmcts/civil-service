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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;

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
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
        when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(configuration.getLipContactEmail()).thenReturn("Email: contactocmc@justice.gov.uk");
        when(configuration.getLipContactEmailWelsh()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
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
