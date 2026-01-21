package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public final class DashboardTaskIds {

    private DashboardTaskIds() {
        // utility class
    }

    public static final String CASE_PROCEEDS_IN_CASEMAN = "GenerateDashboardNotificationsCaseProceedsInCaseman";
    public static final String DISMISS_CASE = "GenerateDashboardNotificationsDismissCase";
    public static final String AMEND_RESTITCH_BUNDLE = "GenerateDashboardNotificationsAmendRestitchBundle";
    public static final String TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY =
        "GenerateDashboardNotificationsTrialArrangementsNotifyParty";
    public static final String BUNDLE_CREATION = "GenerateDashboardNotificationsBundleCreation";
    public static final String CLAIM_SETTLED = "GenerateDashboardNotificationsClaimSettled";
    public static final String EVIDENCE_UPLOADED = "GenerateDashboardNotificationsEvidenceUploaded";
    public static final String TRAIL_READY_CHECK = "GenerateDashboardNotificationsTrailReadyCheck";
    public static final String COURT_OFFICER_ORDER = "GenerateDashboardNotificationsCourtOfficerOrder";
}
