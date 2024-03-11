package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

@lombok.Getter
public enum DashboardScenarios {

    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED("Scenario.AAA7.ClaimIssue.ClaimSubmit.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_FEE_REQUIRED("Scenario.AAA7.ClaimIssue.ClaimFee.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT("Scenario.AAA7.ClaimIssue.Response.Await"),
    SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_REQUIRED("Scenario.AAA7.ClaimIssue.Response.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_REQUESTED("Scenario.AAA7.ClaimIssue.HWF.Requested"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF("Scenario.AAA7.ClaimIssue.HWF.InvalidRef"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_INFO_REQUIRED("Scenario.AAA7.ClaimIssue.HWF.InfoRequired"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_UPDATED("Scenario.AAA7.ClaimIssue.HWF.Updated"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT("Scenario.AAA7.ClaimantIntent.CCJ.Requested.Claimant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT("Scenario.AAA7.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }
}
