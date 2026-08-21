package uk.gov.hmcts.reform.civil.enums.settlediscontinue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ConfirmOrderGivesPermission {
    @CCD(label = "Yes - generate a Notice of Discontinuance")
    YES("Yes"),
    @CCD(label = "No - the claimant will be notified and requested to resubmit")
    NO("No");

    private final String selectedValue;
}
