package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

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
        assertThat(days).isEqualTo(LocalDate.now().plusDays(17));
    }

    @Test
    void shouldReturnFee1_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(0)).isEqualTo("£0");
    }

    @Test
    void shouldReturnFee2_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(15000)).isEqualTo("£27");
    }

    @Test
    void shouldReturnFee3_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(35000)).isEqualTo("£59");
    }

    @Test
    void shouldReturnFee4_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(90000)).isEqualTo("£85");
    }

    @Test
    void shouldReturnFee5_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(140000)).isEqualTo("£123");
    }

    @Test
    void shouldReturnFee6_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(290000)).isEqualTo("£181");
    }

    @Test
    void shouldReturnFee7_whenGivenAnyClaimFee() {
        assertThat(HearingUtils.getFastTrackFee(500000)).isEqualTo("£346");
    }
}
