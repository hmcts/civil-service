package uk.gov.hmcts.reform.civil.enums.caseprogression;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ConfirmListingTickBox {

    @CCD(
            label = "By ticking this box, I confirm I have listed the required hearing and have added a case note with the details of the hearing."
    )
    CONFIRM_LISTING,
}
