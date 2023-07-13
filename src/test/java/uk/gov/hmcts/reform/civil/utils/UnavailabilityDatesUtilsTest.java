package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class UnavailabilityDatesUtilsTest {

    @Test
    public void shouldReturnSingleUnavailabilityDateWhenProvidedForRespondent1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent1DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder);
        UnavailableDate expected = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(SINGLE_DATE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getRespondent1().getUnavailableDates()).get(0);
        assertEquals(expected.getDate(), result.getDate());
        assertEquals(expected.getUnavailableDateType(), result.getUnavailableDateType());
    }

    @Test
    public void shouldReturnEmptyWhenDefendantResponseNoUnavailableDates() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder);
        assertThat(builder.build().getRespondent1().getUnavailableDates() == null).isTrue();
    }

    @Test
    public void shouldReturnDateRangesWhenProvidedForRespondent1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder);
        UnavailableDate expected = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(SINGLE_DATE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getRespondent1().getUnavailableDates()).get(0);
        assertEquals(result.getFromDate(), expected.getFromDate());
    }

    @Test
    public void shouldReturnDatesForBothRespondents1v2SameSolSingleResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondentResponseIsSame(YES)
            .multiPartyClaimOneDefendantSolicitor()
            .respondent1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder);
        List<UnavailableDate> expected = List.of(UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(DATE_RANGE)
            .build());

        List<UnavailableDate> expectedRespondent1Dates = unwrapElements(builder.build().getRespondent1().getUnavailableDates());
        List<UnavailableDate> expectedRespondent2Dates = unwrapElements(builder.build().getRespondent2().getUnavailableDates());
        assertThat(expectedRespondent1Dates).isEqualTo(expected);
        assertThat(expectedRespondent2Dates).isEqualTo(expected);
    }

    @Test
    public void shouldReturnDatesForBothRespondents1v2SameSolDivergentResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimOneDefendantSolicitor()
            .respondent2DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder);

        List<Element<UnavailableDate>> expected = wrapElements(
            UnavailableDate.builder()
                .date(LocalDate.of(2023, 8, 20))
                .unavailableDateType(SINGLE_DATE)
                .build(),
            UnavailableDate.builder()
                .fromDate(LocalDate.of(2023, 8, 20))
                .toDate(LocalDate.of(2023, 8, 22))
                .unavailableDateType(DATE_RANGE)
                .build()
        );

        assertThat(builder.build().getRespondent1().getUnavailableDates()).isNull();
        assertThat(builder.build().getRespondent2().getUnavailableDates()).isEqualTo(expected);
    }

    @Test
    public void shouldReturnDateRangesWhenProvidedForApplicant1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder);
        UnavailableDate expected = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getApplicant1().getUnavailableDates()).get(0);
        assertEquals(result.getFromDate(), expected.getFromDate());
    }

    @Test
    public void shouldReturnSingleUnavailabilityDateWhenProvidedForApplicant1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1DQWithUnavailableDate()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder);
        UnavailableDate expected = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(SINGLE_DATE)
            .build();
        UnavailableDate result = unwrapElements(builder.build().getApplicant1().getUnavailableDates()).get(0);
        assertEquals(expected.getDate(), result.getDate());
        assertEquals(expected.getUnavailableDateType(), result.getUnavailableDateType());
    }

    @Test
    public void shouldReturnDatesForBothApplicants2v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
            .applicant1DQWithUnavailableDateRange()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder);
        List<Element<UnavailableDate>> expected = wrapElements(UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(DATE_RANGE)
            .build());
        List<Element<UnavailableDate>> expectedApplicant1 = builder.build().getApplicant1().getUnavailableDates();
        List<Element<UnavailableDate>> expectedApplicant2 = builder.build().getApplicant2().getUnavailableDates();

        assertThat(expectedApplicant1).isEqualTo(expected);
        assertThat(expectedApplicant2).isEqualTo(expected);
    }

    @Test
    public void shouldReturnDatesForApplicant2DivergentResponse2v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .applicant2DQWithUnavailableDates()
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder);
        List<UnavailableDate> expected = List.of(
            UnavailableDate.builder()
                .date(LocalDate.of(2023, 8, 20))
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .build(),
            UnavailableDate.builder()
                .fromDate(LocalDate.of(2023, 8, 20))
                .toDate(LocalDate.of(2023, 8, 22))
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .build());

        List<UnavailableDate> expectedApplicant1 = unwrapElements(builder.build().getApplicant1().getUnavailableDates());
        List<UnavailableDate> expectedApplicant2 = unwrapElements(builder.build().getApplicant2().getUnavailableDates());
        assertThat(expectedApplicant1).isEqualTo(emptyList());
        assertThat(expectedApplicant2).isEqualTo(expected);
    }

    @Test
    public void shouldUnavailabilityDateWhenProvidedForApplicantDJ() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .atStateClaimantRequestsDJWithUnavailableDates()
            .build();

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(builder);

        UnavailableDate expectedSingleDate = UnavailableDate.builder()
            .unavailableDateType(SINGLE_DATE)
            .date(LocalDate.of(2023, 8, 20))
            .build();

        UnavailableDate expectedDateRange = UnavailableDate.builder()
            .unavailableDateType(DATE_RANGE)
            .fromDate(LocalDate.of(2023, 8, 20))
            .toDate(LocalDate.of(2023, 8, 22))
            .build();

        List<Element<UnavailableDate>> expected = wrapElements(List.of(expectedSingleDate, expectedDateRange));

        assertThat(builder.build().getApplicant1().getUnavailableDates()).isEqualTo(expected);
    }

    @Test
    public void shouldReturnUnavailabilityDateWhenProvidedForApplicantDJ2v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimTwoApplicants()
            .atStateClaimantRequestsDJWithUnavailableDates()
            .build();

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(builder);

        UnavailableDate expectedSingleDate = UnavailableDate.builder()
            .unavailableDateType(SINGLE_DATE)
            .date(LocalDate.of(2023, 8, 20))
            .build();

        UnavailableDate expectedDateRange = UnavailableDate.builder()
            .unavailableDateType(DATE_RANGE)
            .fromDate(LocalDate.of(2023, 8, 20))
            .toDate(LocalDate.of(2023, 8, 22))
            .build();

        List<Element<UnavailableDate>> expected = wrapElements(List.of(expectedSingleDate, expectedDateRange));

        assertThat(builder.build().getApplicant1().getUnavailableDates()).isEqualTo(expected);
    }
}
