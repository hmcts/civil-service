package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public final class DashboardTaskIds {

    private DashboardTaskIds() {
        // utility class
    }

    public static final String CASE_PROCEEDS_IN_CASEMAN = "GenerateDashboardNotificationsCaseProceedsInCaseman";
    public static final String DISMISS_CASE = "GenerateDashboardNotificationsDismissCase";
    public static final String CREATE_CLAIM_AFTER_PAYMENT = "GenerateDashboardNotificationsCreateClaimAfterPayment";
    public static final String AMEND_RESTITCH_BUNDLE = "GenerateDashboardNotificationsAmendRestitchBundle";
    public static final String TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY =
        "GenerateDashboardNotificationsTrialArrangementsNotifyParty";
    public static final String BUNDLE_CREATION = "GenerateDashboardNotificationsBundleCreation";
    public static final String CLAIM_SETTLED = "GenerateDashboardNotificationsClaimSettled";
    public static final String APPLY_NOC_DECISION_DEFENDANT_LIP = "GenerateDashboardNotificationsApplyNocDecisionDefendantLip";
    public static final String GENERATE_DJ_FORM_SPEC = "GenerateDashboardNotificationsDjFormSpec";
    public static final String EVIDENCE_UPLOADED = "GenerateDashboardNotificationsEvidenceUploaded";
    public static final String UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION =
        "GenerateDashboardNotificationsUploadTranslatedDocumentClaimantIntention";
    public static final String REQUEST_JUDGEMENT_ADMISSION_SPEC =
        "GenerateDashboardNotificationsRequestJudgementAdmissionSpec";
    public static final String JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC =
        "GenerateDashboardNotificationsJudgementByAdmissionNonDivergentSpec";
    public static final String CLAIMANT_RESPONSE_CUI = "GenerateDashboardNotificationsClaimantResponseCui";
    public static final String CLAIMANT_RESPONSE_SPEC = "GenerateDashboardNotificationsClaimantResponseSpec";
    public static final String TAKE_CASE_OFFLINE = "GenerateDashboardNotificationsTakeCaseOffline";
    public static final String UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_LR_INTENTION =
        "GenerateDashboardNotificationsUploadTranslatedDocumentClaimantLrIntention";
    public static final String UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN =
        "GenerateDashboardNotificationsUploadTranslatedDocumentClaimantRejectsRepaymentPlan";
    public static final String MEDIATION_UNSUCCESSFUL = "GenerateDashboardNotificationsMediationUnsuccessful";
    public static final String MEDIATION_SUCCESSFUL = "GenerateDashboardNotificationsMediationSuccessful";
    public static final String DECISION_RECONSIDERATION = "GenerateDashboardNotificationsDecisionReconsideration";
    public static final String COURT_OFFICER_ORDER = "GenerateDashboardNotificationsCourtOfficerOrder";
    public static final String STAY_CASE = "GenerateDashboardNotificationsStayCase";
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
    public static final String HELP_WITH_HEARING_FEES = "GenerateDashboardNotificationsLipHelpWithHearingFees";
    public static final String CITIZEN_HEARING_FEE_PAYMENT = "GenerateDashboardNotificationsCitizenHearingFeePayment";
    public static final String DEFENDANT_SIGN_SETTLEMENT_AGREEMENT = "GenerateDashboardNotificationsSignSettlementAgreement";
    public static final String DISCONTINUE_CLAIM_CLAIMANT = "GenerateDashboardNotificationsDiscontinueClaimClaimant";
    public static final String NOTIFY_LIP_CLAIMANT_HWF_OUTCOME = "GenerateDashboardNotificationsNotifyLipClaimantHwfOutcome";
    public static final String DEFENDANT_RESPONSE_DEADLINE_CHECK = "GenerateDashboardNotificationsDefendantResponseDeadlineCheck";
    public static final String HEARING_SCHEDULED_HMC = "GenerateDashboardNotificationsHearingScheduledHmc";
    public static final String HEARING_SCHEDULED = "GenerateDashboardNotificationsHearingScheduled";
    public static final String SETTLE_CLAIM_MARKED_PAID_IN_FULL = "GenerateDashboardNotificationsSettleClaimPaidInFull";
    public static final String SET_ASIDE_JUDGMENT = "GenerateDashboardNotificationsSetAsideJudgement";
    public static final String INFORM_AGREED_EXTENSION_DATE_SPEC =
        "GenerateDashboardNotificationsInformAgreedExtensionDateSpec";
    public static final String TRIAL_READY_CHECK = "GenerateDashboardNotificationsTrialReadyCheck";
    public static final String TRIAL_READY_CHECK_RESPONDENT1 = "GenerateDashboardNotificationsTrialReadyCheckRespondent1";
    public static final String CREATE_SDO = "GenerateDashboardNotificationCreateSDO";
    public static final String FINAL_ORDER = "GenerateDashboardNotificationFinalOrder";
    public static final String STAY_LIFTED = "GenerateDashboardNotificationStayLifted";

}
