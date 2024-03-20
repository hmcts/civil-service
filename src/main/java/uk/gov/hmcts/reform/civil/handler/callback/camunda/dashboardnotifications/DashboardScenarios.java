package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.Getter;

@Getter
public enum DashboardScenarios {

    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED("Scenario.AAA7.ClaimIssue.ClaimSubmit.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_FEE_REQUIRED("Scenario.AAA7.ClaimIssue.ClaimFee.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT("Scenario.AAA7.ClaimIssue.Response.Await"),
    SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_REQUIRED("Scenario.AAA7.ClaimIssue.Response.Required"),
    SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING("Scenario.AAA7.ClaimantIntent.GoToHearing.Claimant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT("Scenario.AAA7.ClaimantIntent.ClaimSettled.Claimant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT("Scenario.AAA7.ClaimantIntent.PartAdmit.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT("Scenario.AAA7.ClaimantIntent.ClaimSettled.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT(
        "Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_REQUESTED("Scenario.AAA7.ClaimIssue.HWF.Requested"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF("Scenario.AAA7.ClaimIssue.HWF.InvalidRef"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_INFO_REQUIRED("Scenario.AAA7.ClaimIssue.HWF.InfoRequired"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT("Scenario.AAA7.ClaimantIntent.CCJ.Requested.Defendant"),
    SCENARIO_AAA7_CLAIM_PART_ADMIT_CLAIMANT("Scenario.AAA7.ClaimantIntent.PartAdmit.Claimant"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_UPDATED("Scenario.AAA7.ClaimIssue.HWF.Updated"),
    SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_NO_RESPONSE_CLAIMANT("Scenario.AAA7.ClaimantIntent.SettlementNoResponse.Claimant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT("Scenario.AAA7.ClaimantIntent.FullAdmit.Claimant"),
    SCENARIO_AAA7_CLAIMANT_MEDIATION("Scenario.AAA7.ClaimantIntent.Mediation.Claimant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT(
        "Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT("Scenario.AAA7.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_PART_REMISSION("Scenario.AAA7.ClaimIssue.HWF.PartRemission"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_NO_REMISSION("Scenario.AAA7.ClaimIssue.HWF.Rejected"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT("Scenario.AAA7.ClaimantIntent.CCJ.Requested.Claimant"),
    SCENARIO_AAA7_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_CLAIMANT("Scenario.AAA7.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant"),
    SCENARIO_AAA7_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_DEFENDANT("Scenario.AAA7.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT("Scenario.AAA7.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT(
            "Scenario.AAA7.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant"
    ),
    SCENARIO_AAA7_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT("Scenario.AAA7.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT("Scenario.AAA7.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant"),
    SCENARIO_AAA7_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT("Scenario.AAA7.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_FULL_REMISSION("Scenario.AAA7.ClaimIssue.HWF.FullRemission"),
    SCENARIO_AAA7_DEFENDANT_RESPONSE_MORE_TIME_REQUESTED_CLAIMANT("Scenario.AAA7.DefResponse.MoreTimeRequested.Claimant");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }
}
