package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;

@ExtendWith(SpringExtension.class)
class HearingFeeUtilsTest {

    private static final DateTimeFormatter DATE_FORMAT
        = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK);

    @Mock
    private HearingFeesService hearingFeesService;

    @ParameterizedTest
    @CsvSource({
        // current date,hearing date,expected
        "2022-10-27,2022-11-04,2022-11-03",   // based on bug report: on the boundary of exactly 7 days
        "2022-10-01,2022-11-14,2022-10-17",   // hearing date more than 36 days away -> expect in 28 straight days time
        "2022-10-01,2022-10-14,2022-10-08",   // hearing date less than 36 days away -> expect in 7 straight days
        "2022-10-01,2022-10-10,2022-10-08"    // should never happen. If it does the deadline is the hearing day
    })
    void shouldApplyAppropriateDate_whenHearingDateIsSetToSpecificValues(
        String strCurrentDate, String strHearingDate, String strExpectedHearingDueDate) {
        // Given

        LocalDate currentDate = LocalDate.parse(strCurrentDate, DATE_FORMAT);
        LocalDate hearingDate = LocalDate.parse(strHearingDate, DATE_FORMAT);
        LocalDate expectedHearingDueDate = LocalDate.parse(strExpectedHearingDueDate, DATE_FORMAT);

        // When
        LocalDate actualHearingDueDate = calculateHearingDueDate(currentDate, hearingDate);

        // Then
        assertThat(actualHearingDueDate).isEqualTo(expectedHearingDueDate);

    }

    @ParameterizedTest
    @CsvSource({
        // track, expected fee
        "SMALL_CLAIM,34600",
        "FAST_CLAIM,54500",
        "MULTI_CLAIM,117500",
    })
    void shouldCalculateAndApplyFee_whenClaimTrackIsSet(String track, String expectedFee) {
        AllocatedTrack allocatedTrack = getAllocatedTrack(track);
        BigDecimal feeInPence = new BigDecimal(expectedFee);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .build().toBuilder()
            .allocatedTrack(allocatedTrack)
            .build();

        Fee expected = Fee.builder()
            .calculatedAmountInPence(feeInPence)
            .code("FEE0225").version("7")
            .build();

        when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(expected);
        when(hearingFeesService.getFeeForHearingFastTrackClaims(any())).thenReturn(expected);
        when(hearingFeesService.getFeeForHearingMultiClaims(any())).thenReturn(expected);

        assertThat(calculateAndApplyFee(hearingFeesService, caseData, allocatedTrack.name()))
            .isEqualTo(expected);
    }

    private AllocatedTrack getAllocatedTrack(String allocatedTrack) {
        return switch (allocatedTrack) {
            case "SMALL_CLAIM" -> AllocatedTrack.SMALL_CLAIM;
            case "FAST_CLAIM" -> AllocatedTrack.FAST_CLAIM;
            case "MULTI_CLAIM" -> AllocatedTrack.MULTI_CLAIM;
            default -> null;
        };
    }
}
