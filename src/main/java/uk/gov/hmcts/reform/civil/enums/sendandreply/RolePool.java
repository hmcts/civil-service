package uk.gov.hmcts.reform.civil.enums.sendandreply;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum RolePool {
    @CCD(label = "Court Staff")
    ADMIN("Court staff"),
    @CCD(label = "Judge")
    JUDICIAL("Judge"),
    @CCD(label = "Judge")
    JUDICIAL_DISTRICT("District Judge"),
    @CCD(label = "Judge")
    JUDICIAL_CIRCUIT("Circuit Judge"),
    @CCD(label = "Legal Advisor")
    LEGAL_OPERATIONS("Legal advisor"),
    @CCD(label = "Welsh language unit")
    WLU_ADMIN("Welsh language unit");

    private final String label;
}

