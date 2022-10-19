package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

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
}
