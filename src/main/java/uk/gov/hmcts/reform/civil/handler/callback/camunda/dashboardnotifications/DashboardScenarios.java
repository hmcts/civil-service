package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.Getter;

@Getter
public enum DashboardScenarios {

    SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED("Scenario.AAA6.ClaimIssue.ClaimSubmit.Required"),
    SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED("Scenario.AAA6.ClaimIssue.ClaimFee.Required"),
    SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT("Scenario.AAA6.ClaimIssue.Response.Await"),
    SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED("Scenario.AAA6.ClaimIssue.Response.Required"),
    SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING("Scenario.AAA6.ClaimantIntent.GoToHearing.Claimant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT("Scenario.AAA6.ClaimantIntent.ClaimSettled.Claimant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT("Scenario.AAA6.ClaimantIntent.PartAdmit.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT("Scenario.AAA6.ClaimantIntent.ClaimSettled.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_COURT_AGREE_DEFENDANT_DEFENDANT(
        "Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_REQUESTED("Scenario.AAA6.ClaimIssue.HWF.Requested"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF("Scenario.AAA6.ClaimIssue.HWF.InvalidRef"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED("Scenario.AAA6.ClaimIssue.HWF.InfoRequired"),
    SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT("Scenario.AAA6.ClaimantIntent.CCJ.Requested.Defendant"),
    SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT("Scenario.AAA6.ClaimantIntent.PartAdmit.Claimant"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED("Scenario.AAA6.ClaimIssue.HWF.Updated"),
    SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_NO_RESPONSE_CLAIMANT("Scenario.AAA6.ClaimantIntent.SettlementNoResponse.Claimant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT("Scenario.AAA6.ClaimantIntent.FullAdmit.Claimant"),
    SCENARIO_AAA6_CLAIMANT_MEDIATION("Scenario.AAA6.ClaimantIntent.Mediation.Claimant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_REJECTS_COURT_AGREES_WITH_CLAIMANT_DEFENDANT(
        "Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT_CLAIMANT_ACCEPTS_DEFENDANT("Scenario.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION("Scenario.AAA6.ClaimIssue.HWF.PartRemission"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION("Scenario.AAA6.ClaimIssue.HWF.Rejected"),
    SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT("Scenario.AAA6.ClaimantIntent.CCJ.Requested.Claimant"),
    SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_ACCEPTED_DEFENDANT("Scenario.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Defendant"),
    SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_CLAIMANT("Scenario.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant"),
    SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_DEFENDANT("Scenario.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEFENDANT_PART_ADMIT("Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_PART_ADMIT_FULL_DEFENCE_STATES_PAID_CLAIMANT_CONFIRMS_DEFENDANT(
            "Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.ClaimantConfirms.Defendant"
    ),
    SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENCE_CLAIMANT_DISPUTES_DEFENDANT("Scenario.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.Defendant"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT("Scenario.AAA6.ClaimIssue.HWF.PhonePayment"),
    SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT("Scenario.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant"),
    SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT("Scenario.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant"),
    SCENARIO_AAA6_CLAIMANT_REJECTED_NOT_PAID_DEFENDANT("Scenario.AAA6.ClaimantIntent.GoToHearing.DefPartAdmit.FullDefence.StatesPaid.PartOrFull.ClaimantDisputes.Defendant"),
    SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION("Scenario.AAA6.ClaimIssue.HWF.FullRemission"),
    SCENARIO_AAA6_DEFENDANT_ALREADY_PAID("Scenario.AAA6.DefResponse.FullDefenceOrPartAdmin.AlreadyPaid.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING_DEF_FULL_DEFENSE_CLAIMANT_DISPUTES_NO_MEDIATION_DEFENDANT(
        "Scenario.AAA6.ClaimantIntent.GoToHearing.DefFullDefence.ClaimantDisputes.NoMediation.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_DEFENDANT("Scenario.AAA6.ClaimantIntent.Mediation.Defendant"),
    SCENARIO_AAA6_CLAIMANT_COURT_AGREE_WITH_CLAIMANT_CCJ_DEFENDANT("Scenario.AAA6.ClaimantIntent.RequestCCJ.ClaimantRejectsDefPlan.CourtAgreesWithClaimant.Defendant"),
    SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_EVENT_CLAIMANT("Scenario.AAA6.ClaimantIntent.ClaimSettledEvent.Claimant"),
    SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT("Scenario.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }
}
