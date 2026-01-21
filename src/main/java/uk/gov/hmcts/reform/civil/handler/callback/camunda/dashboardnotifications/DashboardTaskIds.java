package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public final class DashboardTaskIds {

    private DashboardTaskIds() {
        // utility class
    }

    public static final String CASE_PROCEEDS_IN_CASEMAN = "GenerateDashboardNotificationsCaseProceedsInCaseman";
    public static final String DISMISS_CASE = "GenerateDashboardNotificationsDismissCase";
    public static final String EVIDENCE_UPLOADED = "GenerateDashboardNotificationsEvidenceUploaded";
    public static final String TRAIL_READY_CHECK = "GenerateDashboardNotificationsTrailReadyCheck";
    public static final String COURT_OFFICER_ORDER = "GenerateDashboardNotificationsCourtOfficerOrder";
    public static final String CREATE_SDO = "GenerateDashboardNotificationCreateSDO";
}
