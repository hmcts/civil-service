package uk.gov.hmcts.reform.unspec.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("HideUtilityClassConstructor")
class ClaimValueTest {

    public static final BigDecimal HIGHER_VALUE = BigDecimal.valueOf(1000);
    public static final BigDecimal LOWER_VALUE = BigDecimal.valueOf(10);

    @Nested
    class ValidClaimValue {

        @Test
        void shouldReturnTrue_whenHigherValueIsSmallerThanLower() {
            ClaimValue claimValue = ClaimValue.builder()
                .higherValue(LOWER_VALUE)
                .lowerValue(HIGHER_VALUE)
                .build();

            assertTrue(claimValue.hasLargerLowerValue());
        }
    }

    @Nested
    class InvalidClaimValues {

        @Test
        void shouldReturnFalse_whenHigherValueIsLargerThanLower() {
            ClaimValue claimValue = ClaimValue.builder()
                .higherValue(HIGHER_VALUE)
                .lowerValue(LOWER_VALUE)
                .build();

            assertFalse(claimValue.hasLargerLowerValue());
        }

        @Test
        void shouldReturnFalse_whenHigherValueAndLowerValueAreEqual() {
            ClaimValue claimValue = ClaimValue.builder()
                .higherValue(HIGHER_VALUE)
                .lowerValue(HIGHER_VALUE)
                .build();

            assertFalse(claimValue.hasLargerLowerValue());
        }

        @Test
        void shouldReturnFalse_whenHigherValueIsPresentWithNoLowerValue() {
            ClaimValue claimValue = ClaimValue.builder()
                .higherValue(HIGHER_VALUE)
                .build();

            assertFalse(claimValue.hasLargerLowerValue());
        }

        @Test
        void shouldReturnFalse_whenLowerValueIsPresentWithNoHigherValue() {
            ClaimValue claimValue = ClaimValue.builder()
                .lowerValue(HIGHER_VALUE)
                .build();

            assertFalse(claimValue.hasLargerLowerValue());
        }

        @Test
        void shouldReturnFalse_whenBothValuesAreEmpty() {
            ClaimValue claimValue = ClaimValue.builder().build();

            assertFalse(claimValue.hasLargerLowerValue());
        }
    }
}
