package uk.gov.hmcts.reform.civil.documentmanagement.model;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum DocumentType {
    @CCD(label = "Sealed Claim form")
    SEALED_CLAIM,
    @CCD(label = "Acknowledgement of claim form")
    ACKNOWLEDGEMENT_OF_CLAIM,
    @CCD(label = "Acknowledgement of service form")
    ACKNOWLEDGEMENT_OF_SERVICE,
    @CCD(label = "Directions questionnaire form")
    DIRECTIONS_QUESTIONNAIRE,
    @CCD(label = "Defendant defence")
    DEFENDANT_DEFENCE,
    @CCD(label = "Defendant Draft Directions")
    DEFENDANT_DRAFT_DIRECTIONS,
    @CCD(label = "Default Judgment")
    DEFAULT_JUDGMENT,
    @CCD(label = "Claimant response")
    CLAIMANT_DEFENCE,
    @CCD(label = "Claimant Draft Directions")
    CLAIMANT_DRAFT_DIRECTIONS,
    @CCD(label = "Default Judgment Order")
    DEFAULT_JUDGMENT_SDO_ORDER,
    LITIGANT_IN_PERSON_CLAIM_FORM,
    @CCD(label = "Standard Directions Order")
    SDO_ORDER,
    @CCD(label = "Hearing Notice")
    HEARING_FORM,
    @CCD(label = "Translated Hearing Notice")
    HEARING_FORM_WELSH,
    PIP_LETTER,
    @CCD(label = "Order")
    JUDGE_FINAL_ORDER,
    @CCD(label = "Court Officer Order")
    COURT_OFFICER_ORDER,
    @CCD(label = "Defence Translated Document")
    DEFENCE_TRANSLATED_DOCUMENT,
    @CCD(label = "Draft claim form")
    DRAFT_CLAIM_FORM,
    @CCD(label = "Request for reconsideration")
    REQUEST_FOR_RECONSIDERATION,
    //General Application Document Type
    @CCD(label = "General order")
    GENERAL_ORDER,
    @CCD(label = "Directions order")
    DIRECTION_ORDER,
    @CCD(label = "Dismissal order")
    DISMISSAL_ORDER,
    @CCD(label = "Request for information")
    REQUEST_FOR_INFORMATION,
    @CCD(label = "Hearing order")
    HEARING_ORDER,
    @CCD(label = "Written representation sequential")
    WRITTEN_REPRESENTATION_SEQUENTIAL,
    @CCD(label = "Written representation concurrent")
    WRITTEN_REPRESENTATION_CONCURRENT,
    @CCD(label = "Hearing Notice")
    HEARING_NOTICE,
    @CCD(label = "Consent order")
    CONSENT_ORDER,
    @CCD(label = "Trial Ready")
    TRIAL_READY_DOCUMENT,
    @CCD(label = "Draft Application")
    GENERAL_APPLICATION_DRAFT,
    MEDIATION_AGREEMENT,
    @CCD(label = "Defendant settlement agreement form")
    SETTLEMENT_AGREEMENT,
    @CCD(label = "Claimant Lip Manual Determination")
    LIP_MANUAL_DETERMINATION,
    @CCD(label = "Interlocutory Judgment")
    INTERLOCUTORY_JUDGEMENT,
    @CCD(label = "CCJ Request Admission")
    CCJ_REQUEST_ADMISSION,
    @CCD(label = "CCJ Request Determination")
    CCJ_REQUEST_DETERMINATION,
    @CCD(label = "claimant claim form")
    CLAIMANT_CLAIM_FORM,
    @CCD(label = "Claim Issue Translated Document")
    CLAIM_ISSUE_TRANSLATED_DOCUMENT,
    @CCD(label = "Claimant Intention Translated Document")
    CLAIMANT_INTENTION_TRANSLATED_DOCUMENT,
    SET_ASIDE_JUDGMENT_LETTER,
    @CCD(label = "Default Judgment claimant")
    DEFAULT_JUDGMENT_CLAIMANT1,
    @CCD(label = "Default Judgment defendant")
    DEFAULT_JUDGMENT_DEFENDANT1,
    @CCD(label = "Default Judgment claimant")
    DEFAULT_JUDGMENT_CLAIMANT2,
    @CCD(label = "Default Judgment defendant")
    DEFAULT_JUDGMENT_DEFENDANT2,
    RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER,
    JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER,
    @CCD(label = "Decision on reconsideration request")
    DECISION_MADE_ON_APPLICATIONS,
    DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER,
    @CCD(label = "Judgment by admission claimant")
    JUDGMENT_BY_ADMISSION_CLAIMANT,
    @CCD(label = "Judgment by admission defendant")
    JUDGMENT_BY_ADMISSION_DEFENDANT,
    @CCD(label = "Judgment by determination claimant")
    JUDGMENT_BY_DETERMINATION_CLAIMANT,
    @CCD(label = "Judgment by determination defendant")
    JUDGMENT_BY_DETERMINATION_DEFENDANT,
    @CCD(label = "Translated order")
    ORDER_NOTICE_TRANSLATED_DOCUMENT,
    @CCD(label = "Send Application to other party")
    SEND_APP_TO_OTHER_PARTY,
    @CCD(label = "Notice Of Discontinue")
    NOTICE_OF_DISCONTINUANCE,
    SETTLE_CLAIM_PAID_IN_FULL_LETTER,
    @CCD(label = "Defendant Certificate of Debt Payment")
    CERTIFICATE_OF_DEBT_PAYMENT,
    COVER_LETTER,
    SDO_COVER_LETTER,
    @CCD(label = "Query Document")
    QUERY_DOCUMENT,
    @CCD(label = "Translated Standard Direction Order")
    SDO_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Interlocutory Judgment")
    INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Manual Determination Document")
    MANUAL_DETERMINATION_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Decision on reconsideration request")
    DECISION_MADE_ON_APPLICATIONS_TRANSLATED,
    @CCD(label = "Notice of discontinuance defendant")
    NOTICE_OF_DISCONTINUANCE_DEFENDANT,
    @CCD(label = "Notice of discontinuance defendant translated")
    NOTICE_OF_DISCONTINUANCE_DEFENDANT_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Judge final order")
    FINAL_ORDER_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Settlement Agreement")
    SETTLEMENT_AGREEMENT_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Court Officer Order")
    COURT_OFFICER_ORDER_TRANSLATED_DOCUMENT,
    @CCD(label = "Translated Hearing Notice")
    TRANSLATED_HEARING_NOTICE,
    @CCD(label = "Written Reps Applicant Translated")
    WRITTEN_REPRESENTATION_APPLICANT_TRANSLATED,
    @CCD(label = "Written Reps Respondent Translated")
    WRITTEN_REPRESENTATION_RESPONDENT_TRANSLATED,
    @CCD(label = "Request More Info Applicant Translated")
    REQUEST_MORE_INFORMATION_APPLICANT_TRANSLATED,
    @CCD(label = "Request More Info Respondent Translated")
    REQUEST_MORE_INFORMATION_RESPONDENT_TRANSLATED,
    JUDGES_DIRECTIONS_APPLICANT_TRANSLATED,
    JUDGES_DIRECTIONS_RESPONDENT_TRANSLATED,
    UPLOADED_DOCUMENT_APPLICANT,
    UPLOADED_DOCUMENT_RESPONDENT,
    FREE_FORM_ORDER,
    POST_ORDER_COVER_LETTER_LIP;
}
