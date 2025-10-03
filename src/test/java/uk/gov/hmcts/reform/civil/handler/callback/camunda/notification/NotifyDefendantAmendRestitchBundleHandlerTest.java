package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BUNDLE_RESTITCH_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@SpringBootTest(classes = {
    NotifyDefendantAmendRestitchBundleHandler.class,
    JacksonAutoConfiguration.class
})
class NotifyDefendantAmendRestitchBundleHandlerTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String TEMPLATE_LR_ID = "template-LR-id";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    public static final String DATE_FORMAT = "dd-MM-yyyy";

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private NotificationsProperties notificationsProperties;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private NotificationsSignatureConfiguration configuration;
    @Autowired
    NotifyDefendantAmendRestitchBundleHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
            .build();
        Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
        when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
        when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
        when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
        when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
        when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
        when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
        when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
        when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
        when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
    }

    @ParameterizedTest
    @CsvSource({
        "NO, NO, NO, template-id",
        "NO, YES, NO, bilingual-template-id",
        "YES, NO, NO, 'template-LR-id'",
        "YES, NO, YES, 'template-LR-id'"
    })
    void shouldSendEmailBasedOnConditions(YesOrNo represented, YesOrNo bilingual, YesOrNo isRespondent2, String expectedTemplateId) {
        if (represented.equals(YesOrNo.NO)) {
            if (bilingual == YesOrNo.YES) {
                caseData = caseData.toBuilder().caseDataLiP(CaseDataLiP.builder()
                                                                .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                                            .respondent1ResponseLanguage(Language.BOTH.toString())
                                                                                            .build())
                                                                .build()).build();
                when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BILINGUAL_TEMPLATE_ID);
            } else {
                when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
            }
        } else {
            when(notificationsProperties.getNotifyLRBundleRestitched()).thenReturn(TEMPLATE_LR_ID);
        }

        caseData = caseData.toBuilder().respondent1Represented(represented).build();
        if (YesOrNo.YES.equals(isRespondent2)) {
            caseData = caseData.toBuilder()
                .respondent2(Party.builder().individualFirstName("John").individualLastName("Johnson")
                                 .type(Party.Type.INDIVIDUAL).build())
                .respondentSolicitor2EmailAddress("solicitor2@address.com")
                .addRespondent2(isRespondent2)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .build();
        }
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(YesOrNo.YES.equals(isRespondent2)
                         ? CaseEvent.NOTIFY_DEFENDANT_TWO_AMEND_RESTITCH_BUNDLE.name()
                            : CaseEvent.NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE.name()).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(callbackRequest).build();
        handler.handle(params);

        if (represented == YesOrNo.NO) {
            verify(notificationService).sendMail(
                "respondentLip@example.com",
                expectedTemplateId,
                getNotificationDataMap(caseData, isRespondent2),
                "amend-restitch-bundle-defendant-notification-1594901956117591"
            );
        } else {
            verify(notificationService).sendMail(
                YesOrNo.YES.equals(isRespondent2) ? "solicitor2@address.com" : "respondentsolicitor@example.com",
                expectedTemplateId,
                getNotificationDataMap(caseData, isRespondent2),
                "amend-restitch-bundle-defendant-notification-1594901956117591"
            );
        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData, YesOrNo isRespondent2) {
        Map<String, String> properties = new HashMap<>(addCommonProperties());

        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(PARTY_NAME, YesOrNo.YES.equals(isRespondent2) ? "John Johnson" : "Jack Jackson");
        properties.put(CLAIMANT_V_DEFENDANT, YesOrNo.YES.equals(isRespondent2)
            ? "John Doe V Jack Jackson, John Johnson"
            : "John Doe V Jack Jackson");
        properties.put(BUNDLE_RESTITCH_DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK)));
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
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
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
        return expectedProperties;
    }
}

