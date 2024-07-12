package uk.gov.hmcts.reform.civil.enums.settlediscontinue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscontinuanceTypeList {
    FULL_DISCONTINUANCE("Full discontinuance"),
    PART_DISCONTINUANCE("Part discontinuance");

    private final String type;
}
