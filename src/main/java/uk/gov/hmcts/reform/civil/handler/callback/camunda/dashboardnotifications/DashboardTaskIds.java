package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public final class DashboardTaskIds {

    private DashboardTaskIds() {
        // utility class
    }

    public static final String CASE_PROCEEDS_IN_CASEMAN = "GenerateDashboardNotificationsCaseProceedsInCaseman";
    public static final String DISMISS_CASE = "GenerateDashboardNotificationsDismissCase";
    public static final String APPLY_NOC_DECISION_DEFENDANT_LIP = "GenerateDashboardNotificationsApplyNocDecisionDefendantLip";
    public static final String GENERATE_DJ_FORM_SPEC = "GenerateDashboardNotificationsDjFormSpec";
}
