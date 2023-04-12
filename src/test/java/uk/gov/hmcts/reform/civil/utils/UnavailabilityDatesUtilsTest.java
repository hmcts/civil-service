package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.hearing.UnavailabilityType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class UnavailabilityDatesUtilsTest {

    @Test
    public void shouldReturnSingleUnavailabilityDateWhenProvidedForRespondent1(){
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent1DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, true);
        assertThat(builder.build().getRespondent1().getUnavailableDates().get(0).getUnavailableToDate().equals(LocalDate.now().plusDays(3)));
        assertThat(builder.build().getRespondent1().getUnavailableDates().get(0).getUnavailabilityType().equals(
            UnavailabilityType.ALL_DAY));

    }

    @Test
    public void shouldReturnEmptyWhenDefendantResponseIsFalse(){
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent1DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, false);
        assertThat(builder.build().getRespondent1().getUnavailableDates()==null);
    }

    public void shouldReturnDateRangesWhenProvidedForApplicant1(){
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpExpertUnavailabilityDates(builder, true);
        assertThat(builder.build().getRespondent1().getUnavailableDates().get(0).getUnavailableFromDate().equals(LocalDate.now().plusDays(2)));
    }
}
