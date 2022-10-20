package uk.gov.hmcts.reform.civil.utils;

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

    @Test
    void shouldReturnFee1_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(0)).isEqualTo(new BigDecimal(0));
    }

    @Test
    void shouldReturnFee2_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(15000)).isEqualTo(new BigDecimal(2700));
    }

    @Test
    void shouldReturnFee3_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(35000)).isEqualTo(new BigDecimal(5900));
    }

    @Test
    void shouldReturnFee4_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(90000)).isEqualTo(new BigDecimal(8500));
    }

    @Test
    void shouldReturnFee5_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(140000)).isEqualTo(new BigDecimal(12300));
    }

    @Test
    void shouldReturnFee6_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(290000)).isEqualTo(new BigDecimal(18100));
    }

    @Test
    void shouldReturnFee7_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(500000)).isEqualTo(new BigDecimal(34600));
    }

    @Test
    void shouldReturnFormattedFee_whenGivenMidClaimFee() {
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(new BigDecimal(34600)).build())).isEqualTo("£346");
    }

    @Test
    void shouldReturnFormattedFee_whenGivenBigClaimFee() {
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(new BigDecimal(132000)).build())).isEqualTo("£1,320");
    }

    @Test
    void shouldReturnFormattedFee_whenGivenSmallClaimFee() {
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(new BigDecimal(5000)).build())).isEqualTo("£50");
    }

    @Test
    void shouldReturnNull_when0ClaimFee() {
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(new BigDecimal(0)).build())).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"34600;£346", "132000;£1,320", "5000;£50"}, delimiter=';')
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
    void shouldReturnNull_whenNotAllowedTime(String input) {
        assertThat(HearingUtils.getHearingTimeFormatted(input)).isNull();
    }

    @Test
    void shouldReturnNull_whenGivenEmptyTime() {
        assertThat(HearingUtils.getHearingTimeFormatted("")).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"0000;00:00", "0500;05:00", "1200;12:00", "2300;23:00", "1230;12:30", "2318;23:18"}, delimiter=';')
    void shouldReturnTimedFormatted_whenGivenAnyTime(String input, String expectedOutput) {
        assertThat(HearingUtils.getHearingTimeFormatted(input)).isEqualTo(expectedOutput);
    }
}
