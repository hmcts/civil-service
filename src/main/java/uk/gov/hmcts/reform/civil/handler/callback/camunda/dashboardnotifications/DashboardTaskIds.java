package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public final class DashboardTaskIds {

    private DashboardTaskIds() {
        // utility class
    }

    public static final String CASE_PROCEEDS_IN_CASEMAN = "GenerateDashboardNotificationsCaseProceedsInCaseman";
    public static final String DISMISS_CASE = "GenerateDashboardNotificationsDismissCase";
    public static final String APPLY_NOC_DECISION_DEFENDANT_LIP = "GenerateDashboardNotificationsApplyNocDecisionDefendantLip";
    public static final String GENERATE_DJ_FORM_SPEC = "GenerateDashboardNotificationsDjFormSpec";
    public static final String EVIDENCE_UPLOADED = "GenerateDashboardNotificationsEvidenceUploaded";
    public static final String UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION =
        "GenerateDashboardNotificationsUploadTranslatedDocumentClaimantIntention";
    public static final String REQUEST_JUDGEMENT_ADMISSION_SPEC =
        "GenerateDashboardNotificationsRequestJudgementAdmissionSpec";
    public static final String CLAIMANT_RESPONSE_CUI = "GenerateDashboardNotificationsClaimantResponseCui";
    public static final String CLAIMANT_RESPONSE_SPEC = "GenerateDashboardNotificationsClaimantResponseSpec";
    public static final String UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_LR_INTENTION =
        "GenerateDashboardNotificationsUploadTranslatedDocumentClaimantLrIntention";
    public static final String UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN =
        "GenerateDashboardNotificationsUploadTranslatedDocumentClaimantRejectsRepaymentPlan";
    public static final String TRAIL_READY_CHECK = "GenerateDashboardNotificationsTrailReadyCheck";
    public static final String COURT_OFFICER_ORDER = "GenerateDashboardNotificationsCourtOfficerOrder";
}
