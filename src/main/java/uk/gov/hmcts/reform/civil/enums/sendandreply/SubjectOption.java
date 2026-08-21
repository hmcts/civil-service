package uk.gov.hmcts.reform.civil.enums.sendandreply;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SendMessageSubjectOptionsList", generate = true)
@Getter
@RequiredArgsConstructor
public enum SubjectOption {
    @CCD(label = "A hearing")
    HEARING("A hearing"),
    @CCD(label = "Review submitted documents")
    REVIEW_DOCUMENTS("Review submitted documents"),
    @CCD(label = "An application")
    APPLICATION("An application"),
    @CCD(label = "Other")
    OTHER("Other");

    private final String label;
}

