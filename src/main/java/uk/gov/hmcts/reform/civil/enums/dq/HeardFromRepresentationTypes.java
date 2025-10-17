package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HeardFromRepresentationTypes {

    CLAIMANT_AND_DEFENDANT("Claimant and Defendant"),
    OTHER_REPRESENTATION("Other representation");

    private final String displayedValue;
}
