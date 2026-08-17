package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum FinalOrderSelection {

    @CCD(label = "Assisted order")
    ASSISTED_ORDER,
    @CCD(label = "Free form order")
    FREE_FORM_ORDER,
    DOWNLOAD_ORDER_TEMPLATE,
}
