package uk.gov.hmcts.reform.civil.model.citizenui;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum TranslatedDocumentType {
    @CCD(label = "Defendant Response")
    DEFENDANT_RESPONSE,
    @CCD(label = "Claim Issue")
    CLAIM_ISSUE,
    @CCD(label = "Claimant Intention")
    CLAIMANT_INTENTION,
    ORDER_NOTICE,
    @CCD(label = "Standard Direction Order")
    STANDARD_DIRECTION_ORDER,
    @CCD(label = "Interlocutory Judgment")
    INTERLOCUTORY_JUDGMENT,
    @CCD(label = "Manual Determination Document")
    MANUAL_DETERMINATION,
    @CCD(label = "Decision on reconsideration request")
    DECISION_MADE_ON_APPLICATIONS,
    @CCD(label = "Notice Of Discontinuance Defendant Document")
    NOTICE_OF_DISCONTINUANCE_DEFENDANT,
    @CCD(label = "Judge Final Order")
    FINAL_ORDER,
    @CCD(label = "Settlement Agreement")
    SETTLEMENT_AGREEMENT,
    @CCD(label = "Court Officer Order")
    COURT_OFFICER_ORDER,
    @CCD(label = "Hearing Notice")
    HEARING_NOTICE,
    APPLICATION_SUMMARY_DOCUMENT,
    APPLICATION_SUMMARY_DOCUMENT_RESPONDED,
    REQUEST_FOR_MORE_INFORMATION_ORDER,
    HEARING_ORDER,
    GENERAL_ORDER,
    DISMISSAL_ORDER,
    JUDGES_DIRECTIONS_ORDER,
    WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL,
    WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT,
    UPLOADED_DOCUMENTS_APPLICANT,
    UPLOADED_DOCUMENTS_RESPONDENT,
    WRITTEN_REPRESENTATIONS_APPLICANT,
    WRITTEN_REPRESENTATIONS_RESPONDENT,
    REQUEST_MORE_INFORMATION_APPLICANT,
    REQUEST_MORE_INFORMATION_RESPONDENT,
    JUDGES_DIRECTIONS_APPLICANT,
    JUDGES_DIRECTIONS_RESPONDENT,
    APPROVE_OR_EDIT_ORDER;
}
