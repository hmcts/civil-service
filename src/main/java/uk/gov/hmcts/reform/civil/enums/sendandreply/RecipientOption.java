package uk.gov.hmcts.reform.civil.enums.sendandreply;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecipientOption {
    COURT_STAFF("Court staff"),
    DISTRICT_JUDGE("Judge"),
    CIRCUIT_JUDGE("Judge"),
    LEGAL_ADVISOR("Legal advisor");

    private final String label;
}
