package uk.gov.hmcts.reform.civil.model.welshenhancements;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum PreTranslationDocumentType {
    @CCD(label = "Interlocutory Judgment")
    INTERLOCUTORY_JUDGMENT,
    @CCD(label = "Decision on reconsideration request")
    DECISION_MADE_ON_APPLICATIONS,
    @CCD(label = "Notice Of Discontinuance Document")
    NOTICE_OF_DISCONTINUANCE,
    @CCD(label = "Manual Determination Document")
    MANUAL_DETERMINATION_DOCUMENT,
    @CCD(label = "Defendant sealed claim form")
    DEFENDANT_SEALED_CLAIM_FORM_FOR_LIP_VS_LR,
    @CCD(label = "Hearing Notice")
    HEARING_NOTICE,
    @CCD(label = "Claimant Dq")
    LIP_CLAIMANT_DQ;
}
