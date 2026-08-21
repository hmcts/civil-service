package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseType {
    @CCD(label = "Reject all of the claim")
    FULL_DEFENCE("rejects all of the claim"),
    @CCD(label = "Admit all of the claim")
    FULL_ADMISSION("admits all of the claim"),
    @CCD(label = "Admit part of the claim")
    PART_ADMISSION("admits part of the claim"),
    @CCD(label = "Reject all of the claim and wants to counterclaim")
    COUNTER_CLAIM("rejects all of the claim and wants to counterclaim");

    private final String displayedValue;
}
