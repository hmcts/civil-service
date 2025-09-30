package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderMadeOnTypes {

    COURTS_INITIATIVE("Order on court's own initiative"),
    WITHOUT_NOTICE("Order without notice"),
    NONE("None");

    private final String displayedValue;
}
