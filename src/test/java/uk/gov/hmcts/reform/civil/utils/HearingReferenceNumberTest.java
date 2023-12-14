package uk.gov.hmcts.reform.civil.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static uk.gov.hmcts.reform.civil.utils.HearingReferenceNumber.generateHearingReference;

public class HearingReferenceNumberTest {

    @Test
    void shouldGenerateHearingReferenceNumber() {
        String result = generateHearingReference();

        Assertions.assertThat(result.equals("000HN001"));
    }
}
