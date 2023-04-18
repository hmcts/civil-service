package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

public class UnavailabilityDatesUtilsTest {

    @Test
    public void shouldReturnSingleUnavailabilityDateWhenProvidedForRespondent1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent1DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, true);
        UnavailableDate expected = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getRespondent1().getUnavailableDates()).get(0);
        assertThat(expected.getDate().isEqual(result.getDate())).isTrue();
        assertThat(expected.getUnavailableDateType().equals(result.getUnavailableDateType())).isTrue();
    }

    @Test
    public void shouldReturnEmptyWhenDefendantResponseIsFalse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent1DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, false);
        assertThat(builder.build().getRespondent1().getUnavailableDates() == null).isTrue();
    }

    @Test
    public void shouldReturnDateRangesWhenProvidedForRespondent1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, true);
        UnavailableDate expected = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getRespondent1().getUnavailableDates()).get(0);
        assertThat(result.getFromDate().isEqual(expected.getFromDate())).isTrue();
    }

    @Test
    public void shouldReturnDateRangesWhenProvidedForApplicant1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, false);
        UnavailableDate expected = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getApplicant1().getUnavailableDates()).get(0);
        assertThat(result.getFromDate().isEqual(expected.getFromDate())).isTrue();
    }

    @Test
    public void shouldReturnSingleUnavailabilityDateWhenProvidedForApplicant1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1DQWithUnavailableDate()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, false);
        UnavailableDate expected = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getApplicant1().getUnavailableDates()).get(0);
        assertThat(expected.getDate().isEqual(result.getDate())).isTrue();
        assertThat(expected.getUnavailableDateType().equals(result.getUnavailableDateType())).isTrue();
    }

}
