package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionToAppealTypes {

    GRANTED("granted"),
    REFUSED("not granted"),
    CIRCUIT_COURT_JUDGE("Circuit Court Judge"),
    HIGH_COURT_JUDGE("High Court Judge");

    private final String displayedValue;
}
