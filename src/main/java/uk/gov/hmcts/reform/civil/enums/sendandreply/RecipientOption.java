package uk.gov.hmcts.reform.civil.enums.sendandreply;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SendMessageRecipientOptionsList", generate = true)
@Getter
@RequiredArgsConstructor
public enum RecipientOption {
    @CCD(label = "Court staff")
    COURT_STAFF("Court staff"),
    @CCD(label = "District judge")
    DISTRICT_JUDGE("Judge"),
    @CCD(label = "Circuit judge")
    CIRCUIT_JUDGE("Judge"),
    @CCD(label = "Legal advisor")
    LEGAL_ADVISOR("Legal advisor"),
    @CCD(label = "Welsh language unit")
    WELSH_LANGUAGE_UNIT("Welsh language unit");

    private final String label;
}
