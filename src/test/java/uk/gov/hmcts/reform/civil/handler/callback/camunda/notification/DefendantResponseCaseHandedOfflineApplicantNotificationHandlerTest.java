package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
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
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant.CaseHandledOffLineApplicantSolicitorNotifierFactory;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant.CaseHandledOfflineApplicantSolicitorSpecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant.CaseHandledOfflineApplicantSolicitorUnspecNotifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseCaseHandedOfflineApplicantNotificationHandler.TASK_ID_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DefendantResponseCaseHandedOfflineApplicantNotificationHandler.class,
    CaseHandledOffLineApplicantSolicitorNotifierFactory.class,
    CaseHandledOfflineApplicantSolicitorUnspecNotifier.class,
    CaseHandledOfflineApplicantSolicitorSpecNotifier.class,
    JacksonAutoConfiguration.class
})
class DefendantResponseCaseHandedOfflineApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NotificationsSignatureConfiguration configuration;

    @Autowired
    private DefendantResponseCaseHandedOfflineApplicantNotificationHandler handler;

    @Autowired
    private CaseHandledOfflineApplicantSolicitorSpecNotifier caseHandledOfflineApplicantSolicitorSpecNotifier;


    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
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
        void shouldNotifyApplicantSolicitor_when1v1Case() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLiP() {
            when(notificationsProperties.getClaimantLipClaimUpdatedTemplate())
                .thenReturn("template-id-lip");
            CaseData caseData = CaseDataBuilder.builder()
                .specClaim1v1LipvLr()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "template-id-lip",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLiPInBilingual() {
            when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate())
                .thenReturn("template-id-lip-bilingual");

            CaseData caseData = CaseDataBuilder.builder()
                .specClaim1v1LipvLrBilingual()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "template-id-lip-bilingual",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_when1v2Case() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                .thenReturn("template-id-multiparty");

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id-multiparty",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_when2v1Case() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_when2v1CaseSpec() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .multiPartyClaimOneClaimant1ClaimResponseType()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Nested
        class SpecCounterClaimScenario {

            @Test
            void shouldNotifyDefendantSolicitor1_when1v1CounterClaimCase() {
                when(notificationsProperties.getClaimantSolicitorCounterClaimForSpec())
                    .thenReturn("template-id");

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .build();
                caseData = caseData
                    .toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId(
                            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE").build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMapSpec(caseData),
                    "defendant-response-case-handed-offline-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldReturnAddPropertiesSpec_wheninvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build();

                assertThat(caseHandledOfflineApplicantSolicitorSpecNotifier.addPropertiesSpec1v2DiffSol(caseData))
                    .containsEntry("legalOrgName", "Signer Name")
                    .containsEntry("claimReferenceNumber", "1594901956117591");

            }
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder()
                .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE").build()).build()))
                .isEqualTo(TASK_ID_APPLICANT1);
        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            if (caseData.isLipvLROneVOne()) {
                Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
                expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
                expectedProperties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
                return expectedProperties;
            } else {
                Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
                expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
                expectedProperties.put(REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue());
                expectedProperties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
                expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
                expectedProperties.put(CASEMAN_REF, "000DC001");
                return expectedProperties;
            }
        } else if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            expectedProperties.put(REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue()
                .concat(" against " + caseData.getApplicant1().getPartyName())
                .concat(" and " + caseData.getRespondent1ClaimResponseTypeToApplicant2())
                .concat(" against " + caseData.getApplicant2().getPartyName()));
            expectedProperties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
            expectedProperties.put(CASEMAN_REF, "000DC001");
            return expectedProperties;
        } else {
            //1v2 template is used and expects different data
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            expectedProperties.put(RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            expectedProperties.put(RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
            expectedProperties.put(RESPONDENT_ONE_RESPONSE, caseData.getRespondent1ClaimResponseType().getDisplayedValue());
            expectedProperties.put(RESPONDENT_TWO_RESPONSE, caseData.getRespondent2ClaimResponseType().getDisplayedValue());
            expectedProperties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
            expectedProperties.put(CASEMAN_REF, "000DC001");
            return expectedProperties;
        }
    }

    private Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
        expectedProperties.put("claimantLR", "Signer Name");
        expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        expectedProperties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
        expectedProperties.put(CASEMAN_REF, "000DC001");
        return expectedProperties;
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
