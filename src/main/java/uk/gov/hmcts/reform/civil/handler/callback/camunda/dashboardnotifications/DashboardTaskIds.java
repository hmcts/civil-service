package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public final class DashboardTaskIds {

    private DashboardTaskIds() {
        // utility class
    }

    public static final String CASE_PROCEEDS_IN_CASEMAN = "GenerateDashboardNotificationsCaseProceedsInCaseman";
    public static final String DISMISS_CASE = "GenerateDashboardNotificationsDismissCase";
    public static final String AMEND_RESTITCH_BUNDLE = "GenerateDashboardNotificationsAmendRestitchBundle";
    public static final String TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY =
        "GenerateDashboardNotificationsRespondent1TrialReadyNotifyOthers";
    public static final String BUNDLE_CREATION = "GenerateDashboardNotificationsBundleCreation";
    public static final String CLAIM_SETTLED = "GenerateDashboardNotificationsClaimSettled";
    public static final String EVIDENCE_UPLOADED = "GenerateDashboardNotificationsEvidenceUploaded";
    public static final String MEDIATION_UNSUCCESSFUL = "GenerateDashboardNotificationsMediationUnsuccessful";
    public static final String MEDIATION_SUCCESSFUL = "GenerateDashboardNotificationsMediationSuccessful";
    public static final String DECISION_RECONSIDERATION = "GenerateDashboardNotificationsDecisionReconsideration";
    public static final String COURT_OFFICER_ORDER = "GenerateDashboardNotificationsCourtOfficerOrder";
    public static final String TRIAL_READY_NOTIFICATION = "GenerateDashboardNotificationsTrialArrangements";
    public static final String TRIAL_ARRANGEMENTS_NOTIFY_PARTY = "GenerateDashboardNotificationsTrialArrangementsNotifyParty";
    public static final String CREATE_LIP_CLAIM = "GenerateDashboardNotificationsCreateLipClaim";
    public static final String MOVE_TO_DECISION_OUTCOME = "GenerateDashboardNotificationsDecisionOutcome";
    public static final String DEFENDANT_RESPONSE = "GenerateDashboardNotificationsDefendantResponse";
    public static final String CLAIM_ISSUE = "GenerateDashboardNotificationsClaimIssue";
    public static final String DJ_NON_DIVERGENT = "GenerateDashboardNotificationsDJNonDivergent";
    public static final String RAISE_QUERY = "GenerateDashboardNotificationsRaiseQuery";
    public static final String RESPOND_TO_QUERY = "GenerateDashboardNotificationsRespondToQuery";
    public static final String HEARING_FEE_UNPAID = "GenerateDashboardNotificationsHearingFeeUnpaid";
    public static final String CITIZEN_HEARING_FEE_PAYMENT = "GenerateDashboardNotificationsCitizenHearingFeePayment";
    public static final String DEFENDANT_RESPONSE = "GenerateDashboardNotificationsDefendantResponse";
    public static final String HEARING_SCHEDULED_HMC = "GenerateDashboardNotificationsHearingScheduledHmc";
    public static final String HEARING_SCHEDULED = "GenerateDashboardNotificationsHearingScheduled";
    public static final String TRIAL_READY_CHECK = "GenerateDashboardNotificationsTrialReadyCheck";
    public static final String TRIAL_READY_CHECK_RESPONDENT1 = "GenerateDashboardNotificationsTrialReadyCheckRespondent1";

}
