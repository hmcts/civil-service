package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonetaryConversionsTest {

    @Test
    void shouldThrowNullPointer_whenGivenNullAmount() {
        assertThrows(NullPointerException.class, () ->
            MonetaryConversions.penniesToPounds(null));
    }

    @Test
    void shouldConvertToZeroPounds_whenZeroPennies() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(BigDecimal.ZERO);
        assertThat(converted).isEqualByComparingTo("0");
    }

    @Test
    void shouldConvertToZeroPennies_whenZeroPounds() {
        BigInteger converted = MonetaryConversions.poundsToPennies(BigDecimal.ZERO);
        assertThat(converted).isEqualTo(new BigInteger("0"));
    }

    @Test
    void shouldConvertToOneHundredthOfPound_whenOnePenny() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("1"));
        assertThat(converted).isEqualByComparingTo("0.01");
    }

    @Test
    void shouldConvertToOnePenny_whenOneHundredthOfPound() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("0.01"));
        assertThat(converted).isEqualTo(new BigInteger("1"));
    }

    @Test
    void shouldConvertToOneTenthOfPound_whenTenPennies() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("10"));
        assertThat(converted).isEqualByComparingTo("0.10");
    }

    @Test
    void shouldConvertToTenPennies_whenOneTenthOfPound() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("0.10"));
        assertThat(converted).isEqualTo(new BigInteger("10"));
    }

    @Test
    void shouldConvertToOnePound_whenHundredPennies() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("100"));
        assertThat(converted).isEqualByComparingTo("1.00");
    }

    @Test
    void shouldConvertToHundredPennies_whenOnePound() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("1.00"));
        assertThat(converted).isEqualTo(new BigInteger("100"));
    }

    @Test
    void shouldConvertToTwentyFivePounds_whenTwoAndHalfThousandPennies() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("2500"));
        assertThat(converted).isEqualByComparingTo("25.00");
    }

    @Test
    void shouldConvertToTwoAndHalfThousandPennies_whenTwentyFivePounds() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("25.00"));
        assertThat(converted).isEqualTo(new BigInteger("2500"));
    }
}
