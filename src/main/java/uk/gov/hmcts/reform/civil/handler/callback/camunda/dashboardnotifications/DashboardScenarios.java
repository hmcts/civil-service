package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.Getter;

@Getter
public enum DashboardScenarios {

    SCENARIO_AAA7_CLAIM_ISSUE_CLAIM_FEE_REQUIRED ("Scenario.AAA7.ClaimIssue.ClaimFee.Required");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }
}
