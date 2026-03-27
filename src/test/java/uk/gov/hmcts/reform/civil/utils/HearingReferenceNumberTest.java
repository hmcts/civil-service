package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.HearingReferenceNumber.generateHearingReference;

public class HearingReferenceNumberTest {

    @Test
    void shouldGenerateHearingReferenceNumber() {
        String result = generateHearingReference();

        assertThat(result).matches("\\d{3}HN\\d{3}");
    }
}
