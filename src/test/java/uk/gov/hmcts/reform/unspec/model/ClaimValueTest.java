package uk.gov.hmcts.reform.unspec.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimValueTest {

    public static final int HIGHER_VALUE = 1000;
    public static final int LOWER_VALUE = 10;

    @Test
    void shouldReturnTrueWhenHigherValueIsSmallerThanLower() {
        ClaimValue claimValue = ClaimValue.builder()
            .higherValue(LOWER_VALUE)
            .lowerValue(HIGHER_VALUE)
            .build();

        assertTrue(claimValue.hasLargerLowerValue());
    }

    @Test
    void shouldReturnFalseWhenHigherValueIsLargerThanLower() {
        ClaimValue claimValue = ClaimValue.builder()
            .higherValue(HIGHER_VALUE)
            .lowerValue(LOWER_VALUE)
            .build();

        assertFalse(claimValue.hasLargerLowerValue());
    }

    @Test
    void shouldReturnFalseWhenHigherValueAndLowerValueAreEqual() {
        ClaimValue claimValue = ClaimValue.builder()
            .higherValue(HIGHER_VALUE)
            .lowerValue(HIGHER_VALUE)
            .build();

        assertFalse(claimValue.hasLargerLowerValue());
    }

    @Test
    void shouldReturnFalseWhenHigherValueIsPresentWithNoLowerValue() {
        ClaimValue claimValue = ClaimValue.builder()
            .higherValue(HIGHER_VALUE)
            .build();

        assertFalse(claimValue.hasLargerLowerValue());
    }

    @Test
    void shouldReturnFalseWhenLowerValueIsPresentWithNoHigherValue() {
        ClaimValue claimValue = ClaimValue.builder()
            .lowerValue(HIGHER_VALUE)
            .build();

        assertFalse(claimValue.hasLargerLowerValue());
    }

    @Test
    void shouldReturnFalseWhenBothValuesAreEmpty() {
        ClaimValue claimValue = ClaimValue.builder().build();

        assertFalse(claimValue.hasLargerLowerValue());
    }
}
