package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HWFFeesSummaryType", generate = true)
@Getter
@RequiredArgsConstructor
public enum NoRemissionDetailsSummary {
    @CCD(label = "Does not qualify for Help with Fees assistance")
    NOT_QUALIFY_FEE_ASSISTANCE("Does not qualify for Help with Fees assistance", "Nid yw’n gymwys i gael"
        + " Help i Dalu Ffioedd"),
    @CCD(label = "Incorrect evidence supplied")
    INCORRECT_EVIDENCE("Incorrect evidence supplied", "Tystiolaeth anghywir wedi’i darparu"),
    @CCD(label = "Insufficient evidence supplied")
    INSUFFICIENT_EVIDENCE("Insufficient evidence supplied", "Tystiolaeth annigonol wedi’i darparu"),
    @CCD(label = "Income / outgoings calculation determines Help with Fees requirement not met")
    FEES_REQUIREMENT_NOT_MET(
        "Income/outgoings calculation determines Help with Fees requirement not met",
        "Mae’r cyfrifiad incwm/treuliau yn dangos nad yw’r gofyniad Help i Dalu Ffioedd"
            + " wedi ei fodloni"
    );

    private final String label;
    private final String labelWelsh;


}
