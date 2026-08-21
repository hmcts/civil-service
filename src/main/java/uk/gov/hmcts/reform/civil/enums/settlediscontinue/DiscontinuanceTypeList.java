package uk.gov.hmcts.reform.civil.enums.settlediscontinue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum DiscontinuanceTypeList {
    @CCD(label = "Full discontinuance")
    FULL_DISCONTINUANCE("Full discontinuance"),
    @CCD(label = "Part discontinuance")
    PART_DISCONTINUANCE("Part discontinuance");

    private final String type;
}
