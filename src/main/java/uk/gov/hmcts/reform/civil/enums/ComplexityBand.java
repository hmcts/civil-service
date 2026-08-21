package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ComplexityBand {
    @CCD(label = "Band 1: road traffic accident without personal injury; debt claims")
    BAND_1("Band 1"),
    @CCD(
            label = "Band 2: road traffic accident with personal injury covered by protocol; personal injury; package travel claims"
    )
    BAND_2("Band 2"),
    @CCD(
            label = "Band 3: road traffic accident with personal injury but not covered by protocol; employer liability (accident); public liability (personal injury); housing disrepair; other money claims"
    )
    BAND_3("Band 3"),
    @CCD(
            label = "Band 4: employer liability (disease, but not noise induced hearing loss); complex housing disrepair; property/building disputes; professional negligence; complex claims"
    )
    BAND_4("Band 4");

    private final String label;
}
