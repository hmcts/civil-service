package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class CaseHandledOfflineApplicantSolicitorSpecNotifierTest {

    public static final String TEMPLATE = "template";

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CaseHandledOfflineApplicantSolicitorSpecNotifier notifier;

    @BeforeEach
    void setup() {
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

    @Nested
    class NotifyLip {

        @Test
        void shouldNotifyApplicantLiP() {
            when(notificationsProperties.getClaimantLipClaimUpdatedTemplate())
                .thenReturn(TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder()
                .specClaim1v1LipvLr()
                .build();

            notifier.notifyApplicantSolicitorForCaseHandedOffline(caseData);

            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE,
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLiPInBilingual() {
            when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate())
                .thenReturn(TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder()
                .specClaim1v1LipvLrBilingual()
                .build();

            notifier.notifyApplicantSolicitorForCaseHandedOffline(caseData);

            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE,
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }
    }

    @Test
    void shouldNotifyDefendantSolicitor1_when1v1CounterClaimCase() {
        when(notificationsProperties.getClaimantSolicitorCounterClaimForSpec())
            .thenReturn(TEMPLATE);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();
        caseData = caseData
            .toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .build();

        notifier.notifyApplicantSolicitorForCaseHandedOffline(caseData);

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            TEMPLATE,
            getNotificationDataMapSpec(caseData),
            "defendant-response-case-handed-offline-applicant-notification-000DC001"
        );
    }

    @Nested
    class NotifySolicitor {

        @Test
        void shouldNotifyApplicantSolicitor_when1v1Case() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn(TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .build();

            notifier.notifyApplicantSolicitorForCaseHandedOffline(caseData);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE,
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_when2v1Case() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn(TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .build();

            notifier.notifyApplicantSolicitorForCaseHandedOffline(caseData);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE,
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_when2v1CaseSpec() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn(TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .multiPartyClaimOneClaimant1ClaimResponseType()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                .build();

            notifier.notifyApplicantSolicitorForCaseHandedOffline(caseData);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE,
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldReturnAddPropertiesSpec_whenInvoked() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .build();

            assertThat(notifier.addPropertiesSpec1v2DiffSol(caseData))
                .containsEntry("legalOrgName", "Signer Name")
                .containsEntry("claimReferenceNumber", "1594901956117591");

        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            if (caseData.isLipvLROneVOne()) {
                Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                    PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                    OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                    SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                    HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
                ));
                expectedProperties.put("welshOpeningHours", "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
                expectedProperties.put("welshPhoneContact", "Ffôn: 0300 303 5174");
                expectedProperties.put("welshHmctsSignature", "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
                expectedProperties.put("welshContact", "E-bost: ymholiadaucymraeg@justice.gov.uk");
                expectedProperties.put("specContact", "Email: contactocmc@justice.gov.uk");
                return expectedProperties;
            } else {
                Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                    PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                    CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                    CASEMAN_REF, "000DC001",
                    PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                    OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                    SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                    HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

                ));
                expectedProperties.put("welshOpeningHours", "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
                expectedProperties.put("welshPhoneContact", "Ffôn: 0300 303 5174");
                expectedProperties.put("welshHmctsSignature", "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
                expectedProperties.put("welshContact", "E-bost: ymholiadaucymraeg@justice.gov.uk");
                expectedProperties.put("specContact", "Email: contactocmc@justice.gov.uk");
                return expectedProperties;
            }
        } else if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue()
                    .concat(" against " + caseData.getApplicant1().getPartyName())
                    .concat(" and " + caseData.getRespondent1ClaimResponseTypeToApplicant2())
                    .concat(" against " + caseData.getApplicant2().getPartyName()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CASEMAN_REF, "000DC001",
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm"));

            expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
            expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            expectedProperties.put("welshOpeningHours", "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            expectedProperties.put("welshPhoneContact", "Ffôn: 0300 303 5174");
            expectedProperties.put("welshHmctsSignature", "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            expectedProperties.put("welshContact", "E-bost: ymholiadaucymraeg@justice.gov.uk");
            expectedProperties.put("specContact", "Email: contactocmc@justice.gov.uk");
            return expectedProperties;
        } else {
            //1v2 template is used and expects different data
            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                RESPONDENT_ONE_RESPONSE, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                RESPONDENT_TWO_RESPONSE, caseData.getRespondent2ClaimResponseType().getDisplayedValue(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CASEMAN_REF, "000DC001"
            ));
            expectedProperties.put(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
            expectedProperties.put(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            expectedProperties.put(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
            expectedProperties.put(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            expectedProperties.put("welshOpeningHours", "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            expectedProperties.put("welshPhoneContact", "Ffôn: 0300 303 5174");
            expectedProperties.put("welshHmctsSignature", "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            expectedProperties.put("welshContact", "E-bost: ymholiadaucymraeg@justice.gov.uk");
            expectedProperties.put("specContact", "Email: contactocmc@justice.gov.uk");
            return expectedProperties;
        }

    }

    private Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
            "claimantLR", "Signer Name",
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001",
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        ));
        expectedProperties.put("welshOpeningHours", "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        expectedProperties.put("welshPhoneContact", "Ffôn: 0300 303 5174");
        expectedProperties.put("welshHmctsSignature", "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        expectedProperties.put("welshContact", "E-bost: ymholiadaucymraeg@justice.gov.uk");
        expectedProperties.put("specContact", "Email: contactocmc@justice.gov.uk");
        return expectedProperties;
    }

}
