package uk.gov.hmcts.reform.civil.enums.sendandreply;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubjectOption {
    HEARING("A hearing"),
    REVIEW_DOCUMENTS("Review submitted documents"),
    APPLICATION("An application"),
    OTHER("Other");

    private final String label;
}

