package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class UnavailabilityDatesUtilsTest {

    private final LocalDate issueDate = now();
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

    @Nested
    class ListingTabFields {
        List<Element<UnavailableDate>> existingDates = Stream.of(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2022, 5, 2))
                .build()
        ).map(ElementUtils::element).collect(Collectors.toList());

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForRespondent1() {
            List<UnavailableDate> expectedDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(DEFENDANT_RESPONSE_EVENT)
                    .build()
            ).collect(Collectors.toList());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .respondent1(PartyBuilder.builder()
                                 .soleTrader().build().toBuilder()
                                 .partyID("res-1-party-id")
                                 .unavailableDates(new ArrayList<>(existingDates))
                                 .build())
                .respondent1ResponseDate(issueDate.atStartOfDay())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQHearing(Hearing.builder()
                                                             .unavailableDatesRequired(YES)
                                                             .build())
                                   .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            assertThat(unwrapElements(builder.build().getRespondent1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getRespondent1UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForRespondent2() {
            List<UnavailableDate> expectedDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(DEFENDANT_RESPONSE_EVENT)
                    .build()
            ).collect(Collectors.toList());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .respondent2(PartyBuilder.builder()
                                 .soleTrader().build().toBuilder()
                                 .partyID("res-2-party-id")
                                 .unavailableDates(new ArrayList<>(existingDates))
                                 .build())
                .respondent2ResponseDate(issueDate.atStartOfDay())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQHearing(Hearing.builder()
                                                             .unavailableDatesRequired(YES)
                                                             .build())
                                   .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            assertThat(unwrapElements(builder.build().getRespondent2().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getRespondent2UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForApplicant1() {
            List<UnavailableDate> expectedDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(CLAIMANT_INTENTION_EVENT)
                    .build()
            ).collect(Collectors.toList());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build().toBuilder()
                                .partyID("someid")
                                .unavailableDates(new ArrayList<>(existingDates))
                                .build())
                .applicant1ResponseDate(issueDate.atStartOfDay())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDatesRequired(YES)
                                                           .build())
                                  .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForApplicant2() {
            List<UnavailableDate> expectedDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(CLAIMANT_INTENTION_EVENT)
                    .build()
            ).collect(Collectors.toList());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .multiPartyClaimTwoApplicants()
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build().toBuilder()
                                .partyID("app-2-party-id")
                                .unavailableDates(new ArrayList<>(existingDates))
                                .build())
                .applicant2(PartyBuilder.builder()
                                .soleTrader().build().toBuilder()
                                .partyID("app-2-party-id")
                                .unavailableDates(new ArrayList<>(existingDates))
                                .build())
                .applicant1ResponseDate(issueDate.atStartOfDay())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDatesRequired(YES)
                                                           .build())
                                  .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant2().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant2UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForDJ() {
            List<UnavailableDate> expectedDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(DJ_EVENT)
                    .build()
            ).collect(Collectors.toList());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build().toBuilder()
                                .partyID("someid")
                                .unavailableDates(new ArrayList<>(existingDates))
                                .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyAllDatesFromAllParties_1v2DS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .respondent1(PartyBuilder.builder()
                                 .soleTrader().build().toBuilder()
                                 .partyID("res-1-party-id")
                                 .unavailableDates(new ArrayList<>(existingDates))
                                 .build())
                .respondent1ResponseDate(issueDate.atStartOfDay())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQHearing(Hearing.builder()
                                                             .unavailableDatesRequired(YES)
                                                             .build())
                                   .build())
                .respondent2(PartyBuilder.builder()
                                 .soleTrader().build().toBuilder()
                                 .partyID("res-2-party-id")
                                 .unavailableDates(new ArrayList<>(existingDates))
                                 .build())
                .respondent2ResponseDate(issueDate.atStartOfDay())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQHearing(Hearing.builder()
                                                             .unavailableDatesRequired(YES)
                                                             .build())
                                   .build())
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build().toBuilder()
                                .partyID("someid")
                                .unavailableDates(new ArrayList<>(existingDates))
                                .build())
                .applicant1ResponseDate(issueDate.atStartOfDay())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDatesRequired(YES)
                                                           .build())
                                  .build())
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            List<UnavailableDate> expectedDatesDefendant = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(DEFENDANT_RESPONSE_EVENT)
                    .build()
            ).collect(Collectors.toList());

            List<UnavailableDate> expectedDatesApplicant = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.of(2022, 5, 2))
                    .dateAdded(issueDate)
                    .eventAdded(CLAIMANT_INTENTION_EVENT)
                    .build()
            ).collect(Collectors.toList());

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDatesApplicant);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDatesApplicant);
            assertThat(unwrapElements(builder.build().getRespondent1().getUnavailableDates())).isEqualTo(expectedDatesDefendant);
            assertThat(unwrapElements(builder.build().getRespondent1UnavailableDatesForTab())).isEqualTo(expectedDatesDefendant);
            assertThat(unwrapElements(builder.build().getRespondent2().getUnavailableDates())).isEqualTo(expectedDatesDefendant);
            assertThat(unwrapElements(builder.build().getRespondent2UnavailableDatesForTab())).isEqualTo(expectedDatesDefendant);
        }
    }
}
