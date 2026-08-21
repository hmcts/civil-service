package uk.gov.hmcts.reform.civil.ga.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum OrderMadeOnTypes {

    @CCD(label = "Order on court's own initiative")
    COURTS_INITIATIVE("Order on court's own initiative"),
    @CCD(label = "Order without notice")
    WITHOUT_NOTICE("Order without notice"),
    @CCD(label = "None")
    NONE("None");

    private final String displayedValue;
}
