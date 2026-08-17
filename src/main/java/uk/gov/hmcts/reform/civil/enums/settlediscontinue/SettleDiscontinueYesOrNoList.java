package uk.gov.hmcts.reform.civil.enums.settlediscontinue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum SettleDiscontinueYesOrNoList {
    @CCD(label = "Yes")
    YES("Yes"),
    @CCD(label = "No")
    NO("No");

    private final String displayedValue;
}
