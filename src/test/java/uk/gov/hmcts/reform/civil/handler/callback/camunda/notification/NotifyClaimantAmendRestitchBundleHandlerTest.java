package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
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
    NotifyClaimantAmendRestitchBundleHandler.class,
    JacksonAutoConfiguration.class
})
class NotifyClaimantAmendRestitchBundleHandlerTest {

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
    NotifyClaimantAmendRestitchBundleHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build()).build();
    }

    @ParameterizedTest
    @CsvSource({
        "NO, NO, template-id",
        "YES, NO, bilingual-template-id",
        "NO, YES, 'template-LR-id'",
        "YES, YES, 'template-LR-id'"
    })
    void shouldSendEmailBasedOnConditions(YesOrNo bilingual, YesOrNo represented, String expectedTemplateId) {
        Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
        when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
        when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
        when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
        when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
        when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
        when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
        when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
        when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        if (represented.equals(YesOrNo.YES)) {
            when(notificationsProperties.getNotifyLRBundleRestitched()).thenReturn(TEMPLATE_LR_ID);
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
        } else {
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            if (bilingual == YesOrNo.YES) {
                caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
                when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BILINGUAL_TEMPLATE_ID);
            } else {
                when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
            }
        }

        caseData = caseData.toBuilder().applicantSolicitor1UserDetails(
            IdamUserDetails.builder().email("claimantLR@hmcts.net").build())
            .applicant1Represented(represented).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        if (YesOrNo.NO.equals(represented)) {
            verify(notificationService).sendMail(
                "claimant@hmcts.net",
                expectedTemplateId,
                getNotificationDataMap(caseData, true),
                "amend-restitch-bundle-claimant-notification-1594901956117591"
            );
        } else {
            verify(notificationService).sendMail(
                "claimantLR@hmcts.net",
                expectedTemplateId,
                getNotificationDataMap(caseData, false),
                "amend-restitch-bundle-claimant-notification-1594901956117591"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData, boolean isLipCase) {
        Map<String, String> properties = new HashMap<>(addCommonProperties());
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(PARTY_NAME, "John Doe");
        properties.put(CLAIMANT_V_DEFENDANT, "John Doe V Jack Jackson");
        properties.put(BUNDLE_RESTITCH_DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK)));
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        if (isLipCase) {
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            properties.put(CNBC_CONTACT, configuration.getCnbcContact());
        } else {
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
            properties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        }
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
