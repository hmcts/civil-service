package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator.generateAccessCode;

public class AccessCodeGeneratorTest {

    @Test
    void shouldReturnValueWithSameLengthRequired() {
        String result = generateAccessCode();
        assertNotNull(result);
        assertEquals(result.length(), 12);
    }
}
