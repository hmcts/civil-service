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
    SCENARIO_AAA7_NOTICE_CLAIM_ISSUE_HWF_PART_REMISSION("Scenario.AAA7.Notice.ClaimIssue.HWF.PartRemission"),
    SCENARIO_AAA7_NOTICE_CLAIM_ISSUE_HWF_NO_REMISSION("Scenario.AAA7.Notice.ClaimIssue.HWF.Rejected");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }
}
