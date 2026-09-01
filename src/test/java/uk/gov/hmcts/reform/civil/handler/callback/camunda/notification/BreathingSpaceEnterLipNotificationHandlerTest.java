package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;

@ExtendWith(SpringExtension.class)
class BreathingSpaceEnterLipNotificationHandlerTest {

    private static final String CLAIMANT_EMAIL = "claimant@example.com";
    private static final String DEFENDANT_EMAIL = "defendant@example.com";
    private static final String ENGLISH_CLAIMANT_TEMPLATE = "english-claimant-template";
    private static final String WELSH_CLAIMANT_TEMPLATE = "welsh-claimant-template";
    private static final String ENGLISH_DEFENDANT_TEMPLATE = "english-defendant-template";
    private static final String WELSH_DEFENDANT_TEMPLATE = "welsh-defendant-template";

    @InjectMocks
    private BreathingSpaceEnterLipNotificationHandler handler;

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;

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
        when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
        when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
    }

    @Test
    void shouldNotifyLipClaimant_withEnglishTemplate() {
        when(notificationsProperties.getNotifyApplicant1EnteredBreathingSpaceLip())
            .thenReturn(ENGLISH_CLAIMANT_TEMPLATE);

        CaseData caseData = lipCaseData(CLAIMANT_EMAIL, DEFENDANT_EMAIL, null, null);

        handler.handle(callbackParams(caseData, CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER));

        verify(notificationService).sendMail(
            eq(CLAIMANT_EMAIL),
            eq(ENGLISH_CLAIMANT_TEMPLATE),
            argThat(map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(CASE_ID.toString())
                && map.get(NotificationData.CLAIMANT_NAME).equals("Ann Claimant")
                && map.get(NotificationData.RESPONDENT_NAME).equals("Bob Defendant")
                && map.get(NotificationData.PARTY_NAME).equals("Ann Claimant")
                && map.get(NotificationData.HMCTS_SIGNATURE).equals(configuration.getHmctsSignature())
                && map.get(NotificationData.LIP_CONTACT).equals(configuration.getLipContactEmail())),
            argThat(ref -> ref.contains("legacy ref"))
        );
    }

    @Test
    void shouldNotifyLipClaimant_withWelshTemplate_whenClaimantBilingual() {
        when(notificationsProperties.getNotifyApplicant1EnteredBreathingSpaceLipWelsh())
            .thenReturn(WELSH_CLAIMANT_TEMPLATE);

        CaseData caseData = lipCaseData(CLAIMANT_EMAIL, DEFENDANT_EMAIL, Language.BOTH.toString(), null);

        handler.handle(callbackParams(caseData, CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER));

        verify(notificationService).sendMail(
            eq(CLAIMANT_EMAIL),
            eq(WELSH_CLAIMANT_TEMPLATE),
            anyMap(),
            anyString()
        );
    }

    @Test
    void shouldNotifyLipDefendant_withEnglishTemplate() {
        when(notificationsProperties.getNotifyEnteredBreathingSpaceForDefendantLip())
            .thenReturn(ENGLISH_DEFENDANT_TEMPLATE);

        CaseData caseData = lipCaseData(CLAIMANT_EMAIL, DEFENDANT_EMAIL, null, Language.ENGLISH.toString());

        handler.handle(callbackParams(caseData, CaseEvent.NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER));

        verify(notificationService).sendMail(
            eq(DEFENDANT_EMAIL),
            eq(ENGLISH_DEFENDANT_TEMPLATE),
            argThat(map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(CASE_ID.toString())
                && map.get(NotificationData.CLAIMANT_NAME).equals("Ann Claimant")
                && map.get(NotificationData.RESPONDENT_NAME).equals("Bob Defendant")
                && map.get(NotificationData.PARTY_NAME).equals("Bob Defendant")),
            argThat(ref -> ref.contains("legacy ref"))
        );
    }

    @Test
    void shouldNotifyLipDefendant_withWelshTemplate_whenDefendantBilingual() {
        when(notificationsProperties.getNotifyEnteredBreathingSpaceForDefendantLipWelsh())
            .thenReturn(WELSH_DEFENDANT_TEMPLATE);

        CaseData caseData = lipCaseData(CLAIMANT_EMAIL, DEFENDANT_EMAIL, null, Language.BOTH.toString());

        handler.handle(callbackParams(caseData, CaseEvent.NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER));

        verify(notificationService).sendMail(
            eq(DEFENDANT_EMAIL),
            eq(WELSH_DEFENDANT_TEMPLATE),
            anyMap(),
            anyString()
        );
    }

    @Test
    void shouldSkipSend_whenLipClaimantEmailBlank() {
        CaseData caseData = lipCaseData(null, DEFENDANT_EMAIL, null, null);

        handler.handle(callbackParams(caseData, CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER));

        verify(notificationService, never()).sendMail(anyString(), anyString(), anyMap(), anyString());
        Mockito.verifyNoInteractions(notificationsProperties);
    }

    @Test
    void shouldSkipSend_whenLipDefendantEmailBlank() {
        CaseData caseData = lipCaseData(CLAIMANT_EMAIL, null, null, null);

        handler.handle(callbackParams(caseData, CaseEvent.NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER));

        verify(notificationService, never()).sendMail(anyString(), anyString(), anyMap(), anyString());
        Mockito.verifyNoInteractions(notificationsProperties);
    }

    private CaseData lipCaseData(String claimantEmail,
                                 String defendantEmail,
                                 String claimantLanguagePreference,
                                 String defendantResponseLanguage) {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .applicant1(new Party()
                            .setType(Party.Type.INDIVIDUAL)
                            .setIndividualFirstName("Ann")
                            .setIndividualLastName("Claimant")
                            .setPartyEmail(claimantEmail))
            .respondent1(new Party()
                             .setType(Party.Type.INDIVIDUAL)
                             .setIndividualFirstName("Bob")
                             .setIndividualLastName("Defendant")
                             .setPartyEmail(defendantEmail));

        if (claimantLanguagePreference != null) {
            builder.claimantBilingualLanguagePreference(claimantLanguagePreference);
        }

        if (defendantResponseLanguage != null) {
            builder.caseDataLiP(new CaseDataLiP()
                                    .setRespondent1LiPResponse(new RespondentLiPResponse()
                                                                   .setRespondent1ResponseLanguage(defendantResponseLanguage)));
        }

        return builder.build();
    }

    private CallbackParams callbackParams(CaseData caseData, CaseEvent event) {
        return new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder().eventId(event.name()).build());
    }
}
