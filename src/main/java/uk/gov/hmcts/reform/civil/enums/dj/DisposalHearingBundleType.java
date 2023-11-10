package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum DisposalHearingBundleType {
    DOCUMENTS("an indexed bundle of documents, with each page clearly numbered"),
    ELECTRONIC("an electronic bundle of digital documents"),
    SUMMARY("a case summary containing no more than 500 words");

    private final String label;

    DisposalHearingBundleType(String value) {
        this.label = value;
    }

}
