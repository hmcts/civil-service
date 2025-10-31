package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;

import java.util.List;

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
        var actual = List.of(
            formatter.claimDismissedNoActionSinceIssue(),
            formatter.claimDismissedNoClaimDetailsWithinWindow(),
            formatter.claimDismissedAfterNoDefendantResponse(),
            formatter.claimDismissedNoUserActionForSixMonths(),
            formatter.manualDeterminationRequired(),
            formatter.judgmentByAdmissionOffline(),
            formatter.onlyOneRespondentNotified(),
            formatter.claimantProceeds(),
            formatter.lipVsLrFullOrPartAdmissionReceived(),
            formatter.defendantFullyAdmits(),
            formatter.defendantPartialAdmission(),
            formatter.defendantRejectsAndCounterClaims(),
            formatter.divergentRespond(),
            formatter.unrepresentedDefendants(),
            formatter.unregisteredDefendantSolicitorFirms(),
            formatter.unrepresentedAndUnregisteredDefendantSolicitorFirm(),
            formatter.claimantsProceed(),
            formatter.claimantsIntendNotToProceed(),
            formatter.onlyOneDefendantNotified(),
            formatter.caseTakenOfflineByStaff(),
            formatter.notSuitableForSdo(),
            formatter.judgementByAdmissionRequested(),
            formatter.claimDismissedAfterNoApplicantResponse(),
            formatter.noDefendantSolicitorAppointed(),
            formatter.multitrackUnspecOffline()
        );

        var expected = List.of(
            "RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.",
            "RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.",
            "RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.",
            "RPA Reason: Claim dismissed. No user action has been taken for 6 months.",
            "RPA Reason: Manual Determination Required.",
            "RPA Reason: Judgment by Admission requested and claim moved offline.",
            "RPA Reason: Only one of the respondent is notified.",
            "RPA Reason: Claimant proceeds.",
            "RPA Reason: LiP vs LR - full/part admission received.",
            "RPA Reason: Defendant fully admits.",
            "RPA Reason: Defendant partial admission.",
            "RPA Reason: Defendant rejects and counter claims.",
            "RPA Reason: Divergent respond.",
            "RPA Reason: Unrepresented defendant(s).",
            "RPA Reason: Unregistered defendant solicitor firm(s).",
            "RPA Reason: Unrepresented defendant and unregistered defendant solicitor firm",
            "RPA Reason: Claimant(s) proceeds.",
            "RPA Reason: Claimant(s) intends not to proceed.",
            "RPA Reason: Only one of the defendants is notified.",
            "RPA Reason: Case taken offline by staff.",
            "RPA Reason: Not suitable for SDO.",
            "RPA Reason: Judgement by Admission requested and claim moved offline.",
            "RPA Reason: Claim dismissed after no response from applicant past response deadline.",
            "RPA Reason: No Defendant Solicitor appointed.",
            "RPA Reason:Multitrack Unspec going offline."
        );

        assertThat(actual)
            .containsExactlyElementsOf(expected);
    }
}
