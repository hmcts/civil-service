package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

@lombok.Getter
public enum DashboardScenarios {

    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED("Scenario.AAA7.ClaimIssue.ClaimSubmit.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_FEE_REQUIRED("Scenario.AAA7.ClaimIssue.ClaimFee.Required"),
    SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT("Scenario.AAA7.ClaimIssue.Response.Await"),
    SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_REQUIRED("Scenario.AAA7.ClaimIssue.Response.Required"),
    SCENARIO_AAA7_CLAIMANT_INTENT_PART_ADMIT_DEFENDANT("Scenario.AAA7.ClaimantIntent.PartAdmit.Defendant"),
    SCENARIO_AAA7_CLAIMANT_INTENT_CLAIM_SETTLED_DEFENDANT("Scenario.AAA7.ClaimantIntent.ClaimSettled.Defendant"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_REQUESTED("Scenario.AAA7.ClaimIssue.HWF.Requested"),
    SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF("Scenario.AAA7.ClaimIssue.HWF.InvalidRef");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }
}
