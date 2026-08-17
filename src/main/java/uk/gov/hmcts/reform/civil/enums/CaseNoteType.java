package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CaseNoteType {
    @CCD(label = "Note Only")
    NOTE_ONLY,
    @CCD(label = "Document with a note")
    DOCUMENT_AND_NOTE,
    @CCD(label = "Document only")
    DOCUMENT_ONLY
}

