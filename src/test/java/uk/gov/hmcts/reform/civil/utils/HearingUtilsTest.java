package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HearingUtilsTest {

    @Test
    void shouldThrowNullException_whenGivenNullDate() {
        assertThrows(IllegalArgumentException.class, () ->
            HearingUtils.addBusinessDays(null, 0, null));
    }

    @Test
    void shouldReturnLocalDate_whenGivenAnyDate() {
        Set<LocalDate> holidaySet = new HashSet<>();
        holidaySet.add(LocalDate.now().plusDays(5));
        LocalDate days = HearingUtils.addBusinessDays(LocalDate.now(), 10, holidaySet);
        assertThat(days).isNotNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"0;0", "15000;2700", "35000;5900", "90000;8500", "140000;12300",
        "290000;18100", "500000;34600"}, delimiter = ';')
    @DisplayName("HearingUtils.getFastTrackFee should return the appropriate fast-track fee" +
        " based on the claim fee amount.")
    void shouldReturnFee_whenGivenAnyClaimFee(int intClaimFee, int expectedFastTrackFee) {
        assertThat(HearingUtils.getFastTrackFee(intClaimFee)).isEqualTo(new BigDecimal(expectedFastTrackFee));
    }

    @Test
    @DisplayName("HearingUtils.formatHearingFee should return <null> when the hearing fee is zero.")
    void shouldReturnNull_when0ClaimFee() {
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(new BigDecimal(0)).build())).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"34600;£346", "132000;£1,320", "5000;£50"}, delimiter = ';')
    @DisplayName("HearingUtils.formatHearingFee should format an amount in pence into a pound value." +
        " Fractional values can be discarded.")
    void shouldReturnFormattedFee_whenGivenAnyClaimFee(int amount, String expectedOutput) {
        BigDecimal feeAmount = new BigDecimal(amount);
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(feeAmount).build())).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @EnumSource(HearingDuration.class)
    void shouldReturnHearingDuration_whenGivenAnyHearingDuration(HearingDuration hearingDuration) {
        assertThat(HearingUtils.formatHearingDuration(hearingDuration)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"000", "50000", "08:00", "8:00", "12:00", "23:00"})
    @DisplayName("HearingUtils.getHearingTimeFormatted should not allow invalid values. Valid values" +
        " are composed of 4 numerical digits only.")
    void shouldReturnNull_whenNotAllowedTime(String input) {
        assertThat(HearingUtils.getHearingTimeFormatted(input)).isNull();
    }

    @Test
    @DisplayName("HearingUtils.getHearingTimeFormatted should return <null> when an empty string is passed.")
    void shouldReturnNull_whenGivenEmptyTime() {
        assertThat(HearingUtils.getHearingTimeFormatted("")).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"0000;00:00", "0500;05:00", "1200;12:00", "2300;23:00",
        "1230;12:30", "2318;23:18"}, delimiter = ';')
    @DisplayName("HearingUtils.getHearingTimeFormatted should put a ':' between a 4-digit value" +
        " as they are considered to be hours.")
    void shouldReturnTimedFormatted_whenGivenAnyTime(String input, String expectedOutput) {
        assertThat(HearingUtils.getHearingTimeFormatted(input)).isEqualTo(expectedOutput);
    }
}
