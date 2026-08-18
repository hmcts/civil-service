package uk.gov.hmcts.reform.civil.model.scanneddocument;

import java.util.Arrays;
import java.util.List;

public enum ScannedDocumentType {
    CHERISHED,
    OTHER,
    FORM,
    LETTER,
    COVERSHEET,
    PAPER_RESPONSE_FULL_ADMIT,
    PAPER_RESPONSE_PART_ADMIT,
    PAPER_RESPONSE_STATES_PAID,
    PAPER_RESPONSE_MORE_TIME,
    PAPER_RESPONSE_DISPUTES_ALL,
    PAPER_RESPONSE_COUNTER_CLAIM;

    private final List<String> values;

    ScannedDocumentType(String... values) {
        this.values = Arrays.asList(values);
    }

    public List<String> getValues() {
        return values;
    }

    public static ScannedDocumentType fromValue(String value) {
        return Arrays.stream(values())
            .filter(v -> v.getValues().contains(value) || v.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown Scanned Document Type: " + value));
    }
}
