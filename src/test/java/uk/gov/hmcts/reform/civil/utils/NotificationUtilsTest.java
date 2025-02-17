package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantEmail;

class NotificationUtilsTest {

    private OrganisationService organisationService = mock(OrganisationService.class);

    @Test
    void shouldReturnReferences_when1v1NoReferencesProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .solicitorReferences(null).build();

        String actual = NotificationUtils.buildPartiesReferencesEmailSubject(caseData);

        assertThat(actual).isEqualTo("Claimant reference: Not provided - Defendant reference: Not provided");
    }

    @Test
    void shouldReturnReferences_when1v1BothReferencesProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build();

        String actual = NotificationUtils.buildPartiesReferencesEmailSubject(caseData);

        assertThat(actual).isEqualTo("Claimant reference: 12345 - Defendant reference: 6789");
    }

    @Test
    void shouldReturnReferences_when1v2SSBothReferencesProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimSameDefendantSolicitor()
            .atStateApplicantRespondToDefenceAndProceed()
            .build();

        String actual = NotificationUtils.buildPartiesReferencesEmailSubject(caseData);

        assertThat(actual).isEqualTo("Claimant reference: 12345 - Defendant reference: 6789");
    }

    @Test
    void shouldReturnReferences_when1v2DSBothReferencesProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceed()
            .build();

        String actual = NotificationUtils.buildPartiesReferencesEmailSubject(caseData);

        assertThat(actual).isEqualTo("Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234");
    }

    @Test
    void shouldReturnReferences_when1v2DSNoReferencesProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondentSolicitor2Reference(null)
            .solicitorReferences(null).build();

        String actual = NotificationUtils.buildPartiesReferencesEmailSubject(caseData);

        assertThat(actual).isEqualTo("Claimant reference: Not provided - Defendant 1 reference: Not provided - Defendant 2 reference: Not provided");
    }

    @Test
    void shouldReturnTrue_whenRespondent1RepresentedApplicantProceedsCarm1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenRespondent1RepresentedApplicantProceedsCarm2v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateRespondentFullDefence()
            .applicant1ProceedWithClaimSpec2v1(YES)
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenRespondent1RepresentedApplicantProceedsCarm1v1LipApplicant() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1Represented(NO)
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1SettleClaim(NO)
                             .build())
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRespondent1RepresentedApplicantNotProceedsCarm1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndNotProceed()
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent1RepresentedApplicantNotProceedsCarm2v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateRespondentFullDefence()
            .applicant1ProceedWithClaimSpec2v1(NO)
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRespondent1RepresentedApplicantNotProceedCarm1v1LipApplicant() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1Represented(NO)
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1SettleClaim(YES)
                             .build())
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent1NotRepresentedApplicantProceedsCarm1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
            .specRespondent1Represented(NO)
            .respondent1Represented(null)
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent1RepresentedApplicantProceedsCarmNotEnabled1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm(caseData, false);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent2RepresentedApplicantProceedsCarm1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitorsSpec()
            .atStateApplicantRespondToDefenceAndProceed()
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRespondent2RepresentedApplicantNotProceedsCarm1v2() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndNotProceed()
            .multiPartyClaimTwoDefendantSolicitorsSpec()
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent1RepresentedApplicantNotProceedsCarm1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitorsSpec()
            .atStateRespondentFullDefence()
            .applicant1ProceedWithClaim(NO)
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent2NotRepresentedApplicantProceedsCarm1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
            .respondent1Represented(null)
            .multiPartyClaimTwoDefendantSolicitorsSpec()
            .build().toBuilder()
            .specRespondent2Represented(NO)
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent2RepresentedApplicantProceedsCarmNotEnabled1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
            .build();

        boolean actual = NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, false);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnOrgName_whenOrgIsPresent() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                              .organisationID("ORG123")
                              .build())
            .build();

        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                                                                         .name("org name")
                                                                                         .build()));

        String actual = NotificationUtils.getRespondentLegalOrganizationName(
            organisationPolicy,
            organisationService
        );

        assertThat(actual).isEqualTo("org name");
    }

    @Test
    void shouldReturnNull_whenOrgIsNotPresent() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                              .organisationID("ORG123")
                              .build())
            .build();

        when(organisationService.findOrganisationById(any())).thenReturn(Optional.ofNullable(null));

        String actual = NotificationUtils.getRespondentLegalOrganizationName(
            organisationPolicy,
            organisationService
        );

        assertThat(actual).isNull();
    }

    @Test
    void shouldReturnDefendantName_For1v2WithBothDefendants() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

        String actual = NotificationUtils.getDefendantNameBasedOnCaseType(caseData);

        assertThat(actual).isEqualTo("Mr. Sole Trader and Mr. John Rambo");
    }

    @Test
    void shouldReturnDefendantName_For1v1WithDefendant1() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();

        String actual = NotificationUtils.getDefendantNameBasedOnCaseType(caseData);

        assertThat(actual).isEqualTo("Mr. Sole Trader");
    }

    @Test
    void shouldReturnApplicantOrgName_whenOrgIsPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                                                                         .name("org name")
                                                                                         .build()));

        String actual = NotificationUtils.getApplicantLegalOrganizationName(
            caseData,
            organisationService
        );

        assertThat(actual).isEqualTo("org name");
    }

    @Test
    void shouldReturnApplicantOrgNameAsStmtOfTruth_whenOrgIsNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

        when(organisationService.findOrganisationById(any())).thenReturn(Optional.ofNullable(null));

        String actual = NotificationUtils.getApplicantLegalOrganizationName(
            caseData,
            organisationService
        );

        assertThat(actual).isEqualTo("Signer Name");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnTheCorrectApplicantEmail_ForApplicantLR(boolean isApplicantLip) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted().applicant1(Party.builder().partyEmail(null).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicantsolicitor@example.com").build())
            .build();

        if (isApplicantLip) {
            assertThat(getApplicantEmail(caseData, isApplicantLip)).isNull();
        } else {
            assertThat(getApplicantEmail(caseData, isApplicantLip)).isEqualTo("applicantsolicitor@example.com");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnTheCorrectApplicantEmail_ForApplicantLiP(boolean isApplicantLip) {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyEmail("lipapplicant@example.com").build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(null).build())
            .build();

        if (!isApplicantLip) {
            assertThat(getApplicantEmail(caseData, isApplicantLip)).isNull();
        } else {
            assertThat(getApplicantEmail(caseData, isApplicantLip)).isEqualTo("lipapplicant@example.com");
        }
    }
}
