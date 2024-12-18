package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SetAsideApplicantTypeForRPA {
    PROPER_OFFICER("PROPER OFFICER"),
    PARTY_AGAINST("PARTY AGAINST");

    private final String value;
}
