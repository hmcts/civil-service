package uk.gov.hmcts.reform.civil.enums.sendandreply;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RolePool {
    ADMIN("Court staff"),
    JUDICIAL("Judge"),
    LEGAL_OPERATIONS("Legal advisor");

    private final String label;
}

