package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum FastTrackTrialBundleType {
    @CCD(
            label = "An indexed electronic bundle of documents for trial, with each page clearly numbered including a case summary limited to 500 words"
    )
    DOCUMENTS("an indexed bundle of documents, with each page clearly numbered"),
    ELECTRONIC("an electronic bundle of digital documents"),
    SUMMARY("a case summary containing no more than 500 words");

    private final String label;

    FastTrackTrialBundleType(String value) {
        this.label = value;
    }
}
