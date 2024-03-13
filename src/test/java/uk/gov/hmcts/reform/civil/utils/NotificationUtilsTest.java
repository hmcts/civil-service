package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
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

class NotificationUtilsTest {

    private OrganisationService organisationService = mock(OrganisationService.class);

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
}
