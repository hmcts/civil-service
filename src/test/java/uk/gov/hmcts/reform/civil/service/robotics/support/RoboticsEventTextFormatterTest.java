package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoboticsEventTextFormatterTest {

    private final RoboticsEventTextFormatter formatter = new RoboticsEventTextFormatter();

    @Test
    void withRpaPrefixAddsPrefix() {
        assertThat(formatter.withRpaPrefix("Claim moved offline."))
            .isEqualTo("RPA Reason: Claim moved offline.");
    }

    @Test
    void withRpaPrefixReturnsNullWhenMessageMissing() {
        assertThat(formatter.withRpaPrefix(null)).isNull();
    }

    @Test
    void formatUsesUkLocale() {
        String formatted = formatter.format("Amount: %.2f", 1234.5);

        assertThat(formatted).isEqualTo("Amount: 1234.50");
    }

    @Test
    void formatRpaCombinesPrefixAndFormatting() {
        String formatted = formatter.formatRpa("Claim dismissed after %s days.", 30);

        assertThat(formatted).isEqualTo("RPA Reason: Claim dismissed after 30 days.");
    }

    @Test
    void claimDismissedHelpersReturnExpectedStrings() {
        assertThat(formatter.claimDismissedNoActionSinceIssue())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.");
        assertThat(formatter.claimDismissedNoClaimDetailsWithinWindow())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
        assertThat(formatter.claimDismissedAfterNoDefendantResponse())
            .isEqualTo("RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.");
        assertThat(formatter.claimDismissedNoUserActionForSixMonths())
            .isEqualTo("RPA Reason: Claim dismissed. No user action has been taken for 6 months.");
        assertThat(formatter.manualDeterminationRequired())
            .isEqualTo("RPA Reason: Manual Determination Required.");
        assertThat(formatter.judgmentByAdmissionOffline())
            .isEqualTo("RPA Reason: Judgment by Admission requested and claim moved offline.");
        assertThat(formatter.judgmentRecorded())
            .isEqualTo("RPA Reason: Judgment recorded.");
        assertThat(formatter.onlyOneRespondentNotified())
            .isEqualTo("RPA Reason: Only one of the respondent is notified.");
        assertThat(formatter.claimantProceeds())
            .isEqualTo("RPA Reason: Claimant proceeds.");
        assertThat(formatter.lipVsLrFullOrPartAdmissionReceived())
            .isEqualTo("RPA Reason: LiP vs LR - full/part admission received.");
        assertThat(formatter.defendantFullyAdmits())
            .isEqualTo("RPA Reason: Defendant fully admits.");
        assertThat(formatter.defendantPartialAdmission())
            .isEqualTo("RPA Reason: Defendant partial admission.");
        assertThat(formatter.defendantRejectsAndCounterClaims())
            .isEqualTo("RPA Reason: Defendant rejects and counter claims.");
        assertThat(formatter.divergentRespond())
            .isEqualTo("RPA Reason: Divergent respond.");
        assertThat(formatter.unrepresentedDefendants())
            .isEqualTo("RPA Reason: Unrepresented defendant(s).");
        assertThat(formatter.unregisteredDefendantSolicitorFirms())
            .isEqualTo("RPA Reason: Unregistered defendant solicitor firm(s).");
        assertThat(formatter.unrepresentedAndUnregisteredDefendantSolicitorFirm())
            .isEqualTo("RPA Reason: Unrepresented defendant and unregistered defendant solicitor firm");
        assertThat(formatter.claimantsProceed())
            .isEqualTo("RPA Reason: Claimant(s) proceeds.");
        assertThat(formatter.claimantsIntendNotToProceed())
            .isEqualTo("RPA Reason: Claimant(s) intends not to proceed.");
        assertThat(formatter.onlyOneDefendantNotified())
            .isEqualTo("RPA Reason: Only one of the defendants is notified.");
        assertThat(formatter.caseTakenOfflineByStaff())
            .isEqualTo("RPA Reason: Case taken offline by staff.");
        assertThat(formatter.notSuitableForSdo())
            .isEqualTo("RPA Reason: Not suitable for SDO.");
        assertThat(formatter.judgementByAdmissionRequested())
            .isEqualTo("RPA Reason: Judgement by Admission requested and claim moved offline.");
        assertThat(formatter.claimDismissedAfterNoApplicantResponse())
            .isEqualTo("RPA Reason: Claim dismissed after no response from applicant past response deadline.");
        assertThat(formatter.noDefendantSolicitorAppointed())
            .isEqualTo("RPA Reason: No Defendant Solicitor appointed.");
        assertThat(formatter.multitrackUnspecOffline())
            .isEqualTo("RPA Reason:Multitrack Unspec going offline.");
    }
}
