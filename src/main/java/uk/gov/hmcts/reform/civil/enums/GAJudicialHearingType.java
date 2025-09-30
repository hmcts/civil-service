package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAJudicialHearingType {

    IN_PERSON("held in person"),
    VIDEO("held via video"),
    TELEPHONE("held via telephone"),
    PAPER_HEARING("decided on the papers. The parties are not to attend");

    private final String displayedValue;
}
