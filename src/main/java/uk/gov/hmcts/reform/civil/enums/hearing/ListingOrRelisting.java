package uk.gov.hmcts.reform.civil.enums.hearing;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ListingOrRelisting {

    @CCD(label = "Listing")
    LISTING,
    @CCD(label = "Relisting")
    RELISTING

}
