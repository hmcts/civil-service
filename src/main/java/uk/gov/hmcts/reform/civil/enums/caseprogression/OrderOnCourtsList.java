package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum OrderOnCourtsList {
    @CCD(label = "Order on court's own initiative")
    ORDER_ON_COURT_INITIATIVE,
    @CCD(label = "Order without notice")
    ORDER_WITHOUT_NOTICE,
    @CCD(label = "Not applicable")
    NOT_APPLICABLE
}
