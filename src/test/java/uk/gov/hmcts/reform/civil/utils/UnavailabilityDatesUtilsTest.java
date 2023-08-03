package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class UnavailabilityDatesUtilsTest {

    @Nested
    class UpdateContactDetailsNotEnabled {
        @Test
        public void shouldReturnSingleUnavailabilityDateWhenProvidedForRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondent1DQWithUnavailableDates()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder, false);
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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder, false);
            assertThat(builder.build().getRespondent1().getUnavailableDates() == null).isTrue();
        }

        @Test
        public void shouldReturnDateRangesWhenProvidedForRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1DQWithUnavailableDateRange()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder, false);
            UnavailableDate expected = UnavailableDate.builder()
                .fromDate(LocalDate.now().plusDays(1))
                .toDate(LocalDate.now().plusDays(2))
                .unavailableDateType(SINGLE_DATE)
                .build();
            UnavailableDate result = unwrapElements(builder.build().getRespondent1().getUnavailableDates()).get(0);
            assertEquals(result.getFromDate(), expected.getFromDate());
        }

        @Test
        public void shouldReturnDateRangesWhenProvidedForApplicant1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1DQWithUnavailableDateRange()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder, false);
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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder, false);
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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder, false);
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
        public void shouldUnavailabilityDateWhenProvidedForApplicantDJ() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(builder, false);

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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(builder, false);

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

    @Nested
    class UpdateContactDetailsEnabled {

        private static final String DEFENDANT_RESPONSE_EVENT = "Defendant Response Event";
        private static final String CLAIMANT_INTENTION_EVENT = "Claimant Intention Event";
        private static final String DJ_EVENT = "Request DJ Event";

        @Test
        public void shouldReturnSingleUnavailabilityDateWhenProvidedForRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondent1DQWithUnavailableDates()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder, true);
            UnavailableDate expected = UnavailableDate.builder()
                .date(LocalDate.now().plusDays(1))
                .unavailableDateType(SINGLE_DATE)
                .dateAdded(caseData.getRespondent1ResponseDate().toLocalDate())
                .eventAdded(DEFENDANT_RESPONSE_EVENT)
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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder, true);
            assertThat(builder.build().getRespondent1().getUnavailableDates() == null).isTrue();
        }

        @Test
        public void shouldReturnDateRangesWhenProvidedForRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1DQWithUnavailableDateRange()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(builder, true);
            UnavailableDate expected = UnavailableDate.builder()
                .fromDate(LocalDate.now().plusDays(1))
                .toDate(LocalDate.now().plusDays(2))
                .unavailableDateType(SINGLE_DATE)
                .dateAdded(caseData.getRespondent1ResponseDate().toLocalDate())
                .eventAdded(DEFENDANT_RESPONSE_EVENT)
                .build();
            UnavailableDate result = unwrapElements(builder.build().getRespondent1().getUnavailableDates()).get(0);
            assertEquals(result.getFromDate(), expected.getFromDate());
        }

        @Test
        public void shouldReturnDateRangesWhenProvidedForApplicant1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1DQWithUnavailableDateRange()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder, true);
            UnavailableDate expected = UnavailableDate.builder()
                .fromDate(LocalDate.now().plusDays(1))
                .toDate(LocalDate.now().plusDays(2))
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .dateAdded(caseData.getApplicant1ResponseDate().toLocalDate())
                .eventAdded(CLAIMANT_INTENTION_EVENT)
                .build();
            UnavailableDate result = unwrapElements(builder.build().getApplicant1().getUnavailableDates()).get(0);
            assertEquals(result.getFromDate(), expected.getFromDate());
        }

        @Test
        public void shouldReturnSingleUnavailabilityDateWhenProvidedForApplicant1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1DQWithUnavailableDate()
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder, true);
            UnavailableDate expected = UnavailableDate.builder()
                .date(LocalDate.now().plusDays(1))
                .unavailableDateType(SINGLE_DATE)
                .dateAdded(caseData.getApplicant1ResponseDate().toLocalDate())
                .eventAdded(CLAIMANT_INTENTION_EVENT)
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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder, true);
            List<Element<UnavailableDate>> expected = wrapElements(UnavailableDate.builder()
                                                                       .fromDate(LocalDate.now().plusDays(1))
                                                                       .toDate(LocalDate.now().plusDays(2))
                                                                       .unavailableDateType(DATE_RANGE)
                                                                       .dateAdded(caseData.getApplicant1ResponseDate().toLocalDate())
                                                                       .eventAdded(CLAIMANT_INTENTION_EVENT)
                                                                       .build());
            List<Element<UnavailableDate>> expectedApplicant1 = builder.build().getApplicant1().getUnavailableDates();
            List<Element<UnavailableDate>> expectedApplicant2 = builder.build().getApplicant2().getUnavailableDates();

            assertThat(expectedApplicant1).isEqualTo(expected);
            assertThat(expectedApplicant2).isEqualTo(expected);
        }

        @Test
        public void shouldUnavailabilityDateWhenProvidedForApplicantDJ() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(builder, true);

            UnavailableDate expectedSingleDate = UnavailableDate.builder()
                .unavailableDateType(SINGLE_DATE)
                .date(LocalDate.of(2023, 8, 20))
                .dateAdded(caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate())
                .eventAdded(DJ_EVENT)
                .build();

            UnavailableDate expectedDateRange = UnavailableDate.builder()
                .unavailableDateType(DATE_RANGE)
                .fromDate(LocalDate.of(2023, 8, 20))
                .toDate(LocalDate.of(2023, 8, 22))
                .dateAdded(caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate())
                .eventAdded(DJ_EVENT)
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
            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(builder, true);

            UnavailableDate expectedSingleDate = UnavailableDate.builder()
                .unavailableDateType(SINGLE_DATE)
                .date(LocalDate.of(2023, 8, 20))
                .dateAdded(caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate())
                .eventAdded(DJ_EVENT)
                .build();

            UnavailableDate expectedDateRange = UnavailableDate.builder()
                .unavailableDateType(DATE_RANGE)
                .fromDate(LocalDate.of(2023, 8, 20))
                .toDate(LocalDate.of(2023, 8, 22))
                .dateAdded(caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate())
                .eventAdded(DJ_EVENT)
                .build();

            List<Element<UnavailableDate>> expected = wrapElements(List.of(expectedSingleDate, expectedDateRange));

            assertThat(builder.build().getApplicant1().getUnavailableDates()).isEqualTo(expected);
        }
    }
}
