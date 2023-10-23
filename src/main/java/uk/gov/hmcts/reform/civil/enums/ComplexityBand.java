package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplexityBand {
    BAND_1("Band 1"),
    BAND_2("Band 2"),
    BAND_3("Band 3"),
    BAND_4("Band 4");

    private final String label;
}
