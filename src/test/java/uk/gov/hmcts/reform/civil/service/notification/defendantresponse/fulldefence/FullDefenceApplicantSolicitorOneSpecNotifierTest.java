package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;

class FullDefenceApplicantSolicitorOneSpecNotifierTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private FullDefenceApplicantSolicitorOneSpecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(new Organisation().setName("Signer Name")));
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
    void shouldNotifyApplicantSolicitorSpec_whenInvoked() {

        LocalDate whenWillPay = LocalDate.now().plusMonths(1);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                .setWhenWillThisAmountBePaid(whenWillPay)
            )
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();

        when(notificationsProperties.getClaimantSolicitorDefendantResponseForSpec())
            .thenReturn("spec-claimant-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("applicantsolicitor@example.com"),
            ArgumentMatchers.eq("spec-claimant-template-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @Test
    void shouldNotifyApplicantSolicitorSpecImmediately_whenInvoked() {

        LocalDate whenWillPay = LocalDate.now().plusMonths(1);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
            )
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();

        when(notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec()).thenReturn("templateImm-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("applicantsolicitor@example.com"),
            ArgumentMatchers.eq("templateImm-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @Test
    void shouldNotifyApplicantSolicitorSpecImmediatelyScenerio2_whenInvoked() {

        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
            )
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();

        when(notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec()).thenReturn("templateImm-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("applicantsolicitor@example.com"),
            ArgumentMatchers.eq("templateImm-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @Test
    void shouldNotifyApplicantLipSpecFullDefence_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1Represented(YesOrNo.NO)
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();

        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn(
            "templateImm-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("rambo@email.com"),
            ArgumentMatchers.eq("templateImm-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIMANT_NAME).equals(expected.get(CLAIMANT_NAME));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @Test
    void shouldNotifyApplicantLipSpecFullDefenceForBilingual_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();

        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn(
            "templateImm-bilingual-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("rambo@email.com"),
            ArgumentMatchers.eq("templateImm-bilingual-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIMANT_NAME).equals(expected.get(CLAIMANT_NAME));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @Test
    void shouldNotifyApplicantSolicitorSpecImmediatelyScenerio3_whenInvoked() {

        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
            )
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();

        when(notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec()).thenReturn("templateImm-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("applicantsolicitor@example.com"),
            ArgumentMatchers.eq("templateImm-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @Test
    void shouldNotifyApplicantSolicitorSpecImmediately_whenInvoked_JudgmentOnlineFlagEnabled() {

        LocalDate whenWillPay = LocalDate.now().plusMonths(1);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
            )
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpecJBA()).thenReturn("templateImm-id-jo");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("applicantsolicitor@example.com"),
            ArgumentMatchers.eq("templateImm-id-jo"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @NotNull
    public Map<String, String> getNotificationDataMapSpec() {
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
        expectedProperties.put("defendantName", "Mr. Sole Trader");
        expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
        expectedProperties.put("claimantName", "Mr. John Rambo");
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
