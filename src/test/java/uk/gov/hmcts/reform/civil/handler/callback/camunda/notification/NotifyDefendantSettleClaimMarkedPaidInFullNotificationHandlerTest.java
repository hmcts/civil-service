package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@SpringBootTest(classes = {
    NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
@Ignore
public class NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandlerTest extends BaseCallbackHandlerTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String TASK_ID_RESPONDENT1 = "NotifyDefendantSettleClaimMarkedPaidInFull1";
    private static final String TASK_ID_RESPONDENT2 = "NotifyDefendantSettleClaimMarkedPaidInFull2";

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private OrganisationService organisationService;

    @MockitoBean
    private NotificationsProperties notificationsProperties;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private NotificationsSignatureConfiguration configuration;

    @Autowired
    private NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifySettleClaimMarkedPaidInFullDefendantTemplate()).thenReturn(TEMPLATE_ID);
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
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
        }

        @Test
        void shouldNotifyDefendantSolicitor1_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "defendant-settle-claim-marked-paid-in-full-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor2_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "defendant-settle-claim-marked-paid-in-full-000DC001"
            );

        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(LEGAL_ORG_NAME, "Test Org Name");
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        properties.putAll(addCommonProperties());

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
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        return expectedProperties;
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT2);

    }
}
