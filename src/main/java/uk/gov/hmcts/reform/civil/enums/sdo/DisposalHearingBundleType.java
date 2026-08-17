package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum DisposalHearingBundleType {
    @CCD(label = "an indexed bundle of documents, with each page clearly numbered")
    DOCUMENTS("an indexed bundle of documents, with each page clearly numbered"),
    @CCD(label = "an electronic bundle of digital documents")
    ELECTRONIC("an electronic bundle of digital documents"),
    @CCD(label = "a case summary containing no more than 500 words")
    SUMMARY("a case summary containing no more than 500 words");

    private final String label;

    DisposalHearingBundleType(String value) {
        this.label = value;
    }
}
