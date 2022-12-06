package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum FastTrackTrialBundleType {
    DOCUMENTS("an indexed bundle of documents, with each page clearly numbered"),
    ELECTRONIC("an electronic bundle of digital documents"),
    SUMMARY("a case summary containing no more than 500 words");

    private final String label;

    FastTrackTrialBundleType(String value) {
        this.label = value;
    }
}
