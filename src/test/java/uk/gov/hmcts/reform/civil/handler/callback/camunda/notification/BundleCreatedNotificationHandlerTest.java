package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
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
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BundleCreatedNotificationHandler.TASK_ID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BundleCreatedNotificationHandler.TASK_ID_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BundleCreatedNotificationHandler.TASK_ID_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
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

@ExtendWith(MockitoExtension.class)
class BundleCreatedNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private BundleCreatedNotificationHandler handler;

    public static final String TEMPLATE_ID = "template-id";
    public static final String TEMPLATE_ID_BILINGUAL = "template-id-bilingual";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
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
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getBundleCreationTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            //Given: Case data at hearing scheduled state and callback param with Notify applicant event
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to applicant
            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "bundle-created-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorOne_whenInvoked() {
            when(notificationsProperties.getBundleCreationTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            //Given: Case data at hearing scheduled state and callback param with Notify respondent1 event
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent1
            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorTwo_whenInvokedWithDiffSol() {
            when(notificationsProperties.getBundleCreationTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            //Given: Case data at hearing scheduled state and callback param with Notify respondent2 event and
            // different solicitor for respondent2
            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingDateScheduled()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent2
            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenIsNotRepresented() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            //Given: Case data at hearing scheduled state and callback param with Notify respondent1 Lip
            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingDateScheduled().build().toBuilder()
                .respondent1Represented(YesOrNo.NO).respondent1(
                    Party.builder().partyName("John Doe").partyEmail("doe@doe.com").individualFirstName("John")
                        .individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent1 lipy
            verify(notificationService).sendMail(
                "doe@doe.com",
                "template-id",
                getNotificationLipDataMap(caseData),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenIsNotRepresentedBilingual() {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(TEMPLATE_ID_BILINGUAL);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            //Given: Case data at hearing scheduled state and callback param with Notify respondent1 Lip
            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingDateScheduled().build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(Language.BOTH.toString())
                                                             .build())
                                 .build())
                .respondent1Represented(YesOrNo.NO).respondent1(
                    Party.builder().partyName("John Doe").partyEmail("doe@doe.com").individualFirstName("John")
                        .individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent1 lipy
            verify(notificationService).sendMail(
                "doe@doe.com",
                TEMPLATE_ID_BILINGUAL,
                getNotificationLipDataMap(caseData),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void addPropertiesLipForApplicant() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
                .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                                .type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                                 .type(Party.Type.INDIVIDUAL).build()).build();

            Map<String, String> properties = handler.addPropertiesLip(caseData, TASK_ID_APPLICANT);

            assertThat(properties).containsEntry("claimReferenceNumber", "1594901956117591");
            assertThat(properties).containsEntry("claimantvdefendant", "John Doe V Jack Jackson");
            assertThat(properties).containsEntry("name", "John Doe");
        }

        @Test
        void addPropertiesLipForRespondent() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
                .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                                .type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                                 .type(Party.Type.INDIVIDUAL).build()).build();

            Map<String, String> properties = handler.addPropertiesLip(caseData, TASK_ID_DEFENDANT1);

            assertThat(properties).containsEntry("claimReferenceNumber", "1594901956117591");
            assertThat(properties).containsEntry("claimantvdefendant", "John Doe V Jack Jackson");
            assertThat(properties).containsEntry("name", "Jack Jackson");
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> notificationData = new HashMap<>(addCommonProperties(false));
            notificationData.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
            notificationData.put(CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData));
            notificationData.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            notificationData.put(CLAIM_LEGAL_ORG_NAME_SPEC, "org name");
            notificationData.put(CASEMAN_REF, "000DC001");
            return notificationData;
        }

        @NotNull
        private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
            Map<String, String> notificationData = new HashMap<>(addCommonProperties(true));
            notificationData.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
            notificationData.put(CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData));
            notificationData.put(PARTY_NAME, "John Doe");
            return notificationData;
        }

        @NotNull
        public Map<String, String> addCommonProperties(boolean isLipCase) {
            Map<String, String> expectedProperties = new HashMap<>();
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            if (isLipCase) {
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
                expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
            } else {
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
                expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
            }
            return expectedProperties;
        }
    }

    @Test
    void shouldNotNotifyRespondentSolicitorTwo_whenInvokedWithSameSol() {
        //Given: Case data at hearing scheduled state and callback param with Notify respondent2 event and
        // same solicitor for respondent2
        CaseData caseData = CaseDataBuilder.builder()
            .atStateHearingDateScheduled()
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com").build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED.name()).build()
        ).build();

        //When: handler is called
        handler.handle(params);

        //Then: Email should not be sent to respondent2
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()).build()))
            .isEqualTo(TASK_ID_APPLICANT);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()).build()))
            .isEqualTo(TASK_ID_DEFENDANT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED.name()).build()).build()))
            .isEqualTo(TASK_ID_DEFENDANT2);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED);
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED);
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED);
    }
}
