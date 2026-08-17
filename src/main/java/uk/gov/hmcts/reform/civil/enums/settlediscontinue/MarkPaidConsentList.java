package uk.gov.hmcts.reform.civil.enums.settlediscontinue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum MarkPaidConsentList {
    @CCD(label = "Yes")
    YES
}
