package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
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
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
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
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
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
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
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
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
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

    private Map<String, String> getNotificationDataMapSpec() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            "claimantName", "Mr. John Rambo",
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

        );
    }
}
